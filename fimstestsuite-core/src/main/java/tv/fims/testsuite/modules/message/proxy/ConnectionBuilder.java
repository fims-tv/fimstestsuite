package tv.fims.testsuite.modules.message.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;

public class ConnectionBuilder extends Thread
{
    private final ProxyModuleImpl myModule;
    private final String myLocalAddress;
    private final int myLocalPort;
    private final String myRemoteAddress;
    private final int myRemotePort;
    private final boolean myCallback;
    private final ServerSocket myServerSocket;

    public ConnectionBuilder(ProxyModuleImpl module, String localAddress, int localPort, String remoteAddress, int remotePort, boolean isCallback) throws IOException
    {
        super("ConnectionBuilder:" + localPort + " -> " + remoteAddress + ":" + remotePort);
        myModule = module;
        myLocalAddress = localAddress;
        myLocalPort = localPort;
        myRemoteAddress = remoteAddress;
        myRemotePort = remotePort;
        myCallback = isCallback;
        myServerSocket = new ServerSocket(myLocalPort);
    }

    public ProxyModuleImpl getModule()
    {
        return myModule;
    }

    public String getLocalAddress()
    {
        return myLocalAddress;
    }

    public int getLocalPort()
    {
        return myLocalPort;
    }

    public String getRemoteAddress()
    {
        return myRemoteAddress;
    }

    public int getRemotePort()
    {
        return myRemotePort;
    }

    public boolean isCallback()
    {
        return myCallback;
    }

    public int getPort()
    {
        return myServerSocket.getLocalPort();
    }

    @Override
    public void run()
    {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = null;
                Socket serverSocket = null;
                boolean doClose = true;

                try {
                    clientSocket = myServerSocket.accept();
                    try {
                        serverSocket = SocketFactory.getDefault().createSocket(myRemoteAddress, myRemotePort);
                        Connection connection = new Connection(this, clientSocket, serverSocket);
                        connection.start();
                        doClose = false;
                    } catch (IOException ex) {
                    }
                } finally {
                    if (doClose) {
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    }
                }
            }
        } catch (IOException ex) {
        } finally {
            try {
                myServerSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void interrupt()
    {
        try {
            myServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.interrupt();
    }
}
