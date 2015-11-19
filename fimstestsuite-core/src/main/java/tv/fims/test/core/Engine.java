package tv.fims.test.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class Engine
{
    private final Map<URL, ConnectionBuilder> myConnectionBuilders;
    private final List<Connection> myConnections;
    private final Map<URL, URL> myCallbackMap;

    private BufferedOutputStream myBinaryLogger;
    private BufferedOutputStream myRegularLogger;

    public Engine()
    {
        myConnectionBuilders = new HashMap<>();
        myConnections = new ArrayList<>();
        myCallbackMap = new HashMap<>();
    }

    public synchronized ConnectionBuilder connect(String localAddress, int localPort, String remoteAddress, int remotePort, URL callbackAddress)
    {
        try {
            ConnectionBuilder cb = myConnectionBuilders.get(callbackAddress);
            if (cb == null) {
                cb = new ConnectionBuilder(this, localAddress, localPort, remoteAddress, remotePort, callbackAddress != null);
                cb.start();
                myConnectionBuilders.put(callbackAddress, cb);
            }
            return cb;
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized void disconnect()
    {
        for (Connection connection : myConnections) {
            connection.interrupt();
        }
        myConnections.clear();

        for (ConnectionBuilder connectionBuilder : myConnectionBuilders.values()) {
            connectionBuilder.interrupt();
        }
        myConnectionBuilders.clear();
        myCallbackMap.clear();
    }

    public synchronized void putCallback(URL replacement, URL original)
    {
        myCallbackMap.put(replacement, original);
    }

    public synchronized URL getCallback(URL url)
    {
        return myCallbackMap.get(url);
    }

    public synchronized boolean startLogging(File binaryLogFile, File regularLogFile)
    {
        stopLogging();

        if (binaryLogFile != null) {
            try {
                myBinaryLogger = new BufferedOutputStream(new FileOutputStream(binaryLogFile, true));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                stopLogging();
            }
        }

        if (regularLogFile != null) {
            try {
                myRegularLogger = new BufferedOutputStream(new FileOutputStream(regularLogFile, true));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                stopLogging();
            }
        }

        return myBinaryLogger != null || myRegularLogger != null;
    }

    public synchronized void stopLogging()
    {
        if (myBinaryLogger != null) {
            try {
                myBinaryLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myBinaryLogger = null;
            }
        }
        if (myRegularLogger != null) {
            try {
                myRegularLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myRegularLogger = null;
            }
        }
    }

    public synchronized void process(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        if (myBinaryLogger != null) {
            writeBinaryLog(request, response);
        }
        if (myRegularLogger != null) {
            writeRegularLog(request, response);
        }
    }

    private void writeBinaryLog(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        try {
            request.writeTo(myBinaryLogger);
            response.writeTo(myBinaryLogger);
            myBinaryLogger.flush();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeRegularLog(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        try {
            myRegularLogger.write(request.toString().getBytes("UTF-8"));
            myRegularLogger.write(response.toString().getBytes("UTF-8"));
            myRegularLogger.flush();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized boolean replay(File file)
    {
        try (FileInputStream fis = new FileInputStream(file)) {
            HttpMessageWrapper wrapper;
            HttpMessageWrapper request = null;
            HttpMessageWrapper response = null;

            while ((wrapper = HttpMessageWrapper.readFrom(fis)) != null) {
                if (wrapper.getMessage() instanceof HttpRequest) {
                    request = wrapper;
                } else if (wrapper.getMessage() instanceof HttpResponse) {
                    response = wrapper;
                }

                if (request != null && response != null) {
                    process(request, response);
                    request = null;
                    response = null;
                }
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
