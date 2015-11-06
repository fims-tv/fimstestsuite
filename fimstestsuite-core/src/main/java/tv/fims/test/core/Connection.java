package tv.fims.test.core;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.util.EntityUtils;

public class Connection extends Thread
{
    private final Engine myEngine;
    private final Socket myClientSocket;
    private final Socket myServerSocket;
    private final String myRemoteAddress;
    private final int myRemotePort;
    private final boolean myCallback;

    public Connection(Engine engine, Socket clientSocket, Socket serverSocket, String remoteAddress, int remotePort, boolean isCallback)
    {
        super("Connection:" + clientSocket.getRemoteSocketAddress().toString() + " <-> " + serverSocket.getRemoteSocketAddress());
        myEngine = engine;
        myClientSocket = clientSocket;
        myServerSocket = serverSocket;
        myRemoteAddress = remoteAddress;
        myRemotePort = remotePort;
        myCallback = isCallback;
    }

    private HttpMessageWrapper receiveRequest(DefaultBHttpServerConnection clientConnection) throws HttpException, IOException
    {
        HttpRequest request = clientConnection.receiveRequestHeader();
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest enclosingRequest = (HttpEntityEnclosingRequest) request;
            clientConnection.receiveRequestEntity(enclosingRequest);
            HttpEntity entity = enclosingRequest.getEntity();
            if (entity != null) {
                BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
                EntityUtils.consume(entity);
                enclosingRequest.setEntity(bufferedEntity);
            }
        }

        // setting Host header so we can correctly forward it to the service
        if (myRemotePort == 80) {
            request.setHeader("Host", myRemoteAddress);
        } else {
            request.setHeader("Host", myRemoteAddress + ":" + myRemotePort);
        }

        return new HttpMessageWrapper(request, System.currentTimeMillis(), myCallback);
    }

    private void sendMessage(DefaultBHttpClientConnection connection, HttpMessageWrapper wrapper) throws HttpException, IOException
    {
        HttpMessage message = wrapper.getMessage();

        if (message instanceof HttpRequest) {
            connection.sendRequestHeader((HttpRequest) message);
            if (message instanceof HttpEntityEnclosingRequest) {
                connection.sendRequestEntity((HttpEntityEnclosingRequest) message);
            }
            connection.flush();
        }
    }

    private HttpMessageWrapper receiveResponse(DefaultBHttpClientConnection serverConnection) throws HttpException, IOException
    {
        HttpResponse response = serverConnection.receiveResponseHeader();
        serverConnection.receiveResponseEntity(response);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
            EntityUtils.consume(entity);
            response.setEntity(bufferedEntity);
        }

        return new HttpMessageWrapper(response, System.currentTimeMillis(), myCallback);
    }

    private void sendMessage(DefaultBHttpServerConnection connection, HttpMessageWrapper wrapper) throws HttpException, IOException
    {
        HttpMessage message = wrapper.getMessage();

        if (message instanceof HttpResponse) {
            connection.sendResponseHeader((HttpResponse) message);
            connection.sendResponseEntity((HttpResponse) message);
            connection.flush();
        }
    }

    @Override
    public void run()
    {
        try {
            DefaultBHttpServerConnection clientConnection = new DefaultBHttpServerConnection(8092);
            clientConnection.bind(myClientSocket);

            DefaultBHttpClientConnection serverConnection = new DefaultBHttpClientConnection(8092);
            serverConnection.bind(myServerSocket);

            while (!Thread.currentThread().isInterrupted()) {
                HttpMessageWrapper request = receiveRequest(clientConnection);
                sendMessage(serverConnection, request);

                HttpMessageWrapper response = receiveResponse(serverConnection);
                sendMessage(clientConnection, response);

                myEngine.process(request, response);
            }
        } catch (HttpException | IOException ex) {
        } finally {
            Logger.getLogger(Connection.class.getName()).log(Level.INFO, "Closing {0}", getName());
            try {
                myServerSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                myClientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void interrupt()
    {
        try {
            myServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            myClientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.interrupt();
    }
}
