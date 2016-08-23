package tv.fims.testsuite.modules.message.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tv.fims.testsuite.modules.utils.FimsNamespaceContext;
import tv.fims.testsuite.modules.message.HttpMessageWrapper;
import tv.fims.testsuite.modules.utils.Utils;

public class Connection extends Thread
{
    private final ConnectionBuilder myConnectionBuilder;
    private final Socket myClientSocket;
    private final Socket myServerSocket;

    public Connection(ConnectionBuilder connectionBuilder, Socket clientSocket, Socket serverSocket)
    {
        super("Connection:" + clientSocket.getRemoteSocketAddress().toString() + " <-> " + serverSocket.getRemoteSocketAddress());
        myConnectionBuilder = connectionBuilder;
        myClientSocket = clientSocket;
        myServerSocket = serverSocket;
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

        request = processMessage(request);

        return new HttpMessageWrapper(request, System.currentTimeMillis(), myConnectionBuilder.isCallback());
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

        response = processMessage(response);

        return new HttpMessageWrapper(response, System.currentTimeMillis(), myConnectionBuilder.isCallback());
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

                myConnectionBuilder.getModule().process(request, response);
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

    private <T extends HttpMessage> T processMessage(T message)
    {
        if (message instanceof HttpRequest) {
            // setting Host header so we can correctly forward it to the service
            if (myConnectionBuilder.getRemotePort() == 80) {
                message.setHeader("Host", myConnectionBuilder.getRemoteAddress());
            } else {
                message.setHeader("Host", myConnectionBuilder.getRemoteAddress() + ":" + myConnectionBuilder.getRemotePort());
            }
        }

        HttpEntity entity = null;
        HttpEntity newEntity = null;

        if (message instanceof HttpEntityEnclosingRequest) {
            entity = ((HttpEntityEnclosingRequest) message).getEntity();
        } else if (message instanceof HttpResponse) {
            entity = ((HttpResponse) message).getEntity();
        }

        // redirecting reply to and fault to nodes if present
        if (entity != null) {
            try (InputStream is = entity.getContent()) {
                ContentType contentType = null;
                for (Header header : message.getHeaders("Content-Type")) {
                    try {
                        contentType = ContentType.parse(header.getValue());
                    } catch (ParseException | UnsupportedCharsetException ex) {
                    }
                }

                if (contentType != null) {
                    Charset charset = contentType.getCharset();
                    if (charset == null) {
                        charset = Charset.forName("UTF-8");
                    }
                    String mimeType = contentType.getMimeType();

                    if (mimeType.contains("json") || mimeType.contains("xml")) {
                        String content = IOUtils.toString(is, charset.name());

                        if (mimeType.contains("json")) {
                            // TODO convert content to XML
                            // content = Utils.convertJSONtoXML(content);
                        }

                        Document xmlDocument = Utils.parseXML(content);
                        if (xmlDocument != null) {
                            XPath xpath = XPathFactory.newInstance().newXPath();
                            xpath.setNamespaceContext(new FimsNamespaceContext());

                            for (String expression : new String[]{"//bms:replyTo", "//bms:faultTo"}) {
                                NodeList nodes = (NodeList) xpath.evaluate(expression, xmlDocument.getDocumentElement(), XPathConstants.NODESET);
                                for (int i = 0; i < nodes.getLength(); i++) {
                                    Node node = nodes.item(i);

                                    try {
                                        URL url = new URL(node.getTextContent());

                                        if ((message instanceof HttpRequest) && !myConnectionBuilder.isCallback()) {
                                            int port = url.getPort();
                                            if (port < 0) {
                                                switch (url.getProtocol()) {
                                                    case "http":
                                                        port = 80;
                                                        break;
                                                    case "https":
                                                        port = 443;
                                                        break;
                                                    default:
                                                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, "Not able to determine callback port number for URL ''{0}''", url);
                                                        break;
                                                }
                                            }

                                            ConnectionBuilder cb = myConnectionBuilder.getModule().connect(myConnectionBuilder.getLocalAddress(), 0, url.getHost(), port, url);
                                            URL newURL = new URL(url.getProtocol(), cb.getLocalAddress(), cb.getPort(), url.getFile());
                                            node.setTextContent(String.valueOf(newURL));
                                            myConnectionBuilder.getModule().putCallback(newURL, url);
                                        } else {
                                            URL newURL = myConnectionBuilder.getModule().getCallback(url);
                                            if (newURL != null) {
                                                node.setTextContent(String.valueOf(newURL));
                                            }
                                        }
                                    } catch (MalformedURLException ex) {
                                    }
                                }
                            }

                            content = Utils.writeXML(xmlDocument, false, content.startsWith("<?xml"));

                            if (mimeType.contains("json")) {
                                // TODO convert content to JSON
                                // content = Utils.convertXMLtoJSON(content);
                            }

                            if (content != null) {
                                byte[] data = content.getBytes(charset);
                                if (message.containsHeader("Content-Length")) {
                                    message.setHeader("Content-Length", String.valueOf(data.length));
                                }
                                newEntity = new ByteArrayEntity(data);
                            }
                        }
                    }
                }
            } catch (IOException | UnsupportedOperationException | XPathExpressionException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (newEntity != null) {
                if (message instanceof HttpEntityEnclosingRequest) {
                    ((HttpEntityEnclosingRequest) message).setEntity(newEntity);
                } else if (message instanceof HttpResponse) {
                    ((HttpResponse) message).setEntity(newEntity);
                }
            }
        }

        return message;
    }
}
