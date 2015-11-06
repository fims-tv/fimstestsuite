package tv.fims.test.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class Engine
{
    private final List<ConnectionBuilder> myConnectionBuilders;
    private final List<Connection> myConnections;

    private BufferedOutputStream myLogger;

    public Engine()
    {
        myConnectionBuilders = new ArrayList<>();
        myConnections = new ArrayList<>();
    }

    public synchronized boolean connect(int localPort, String remoteAddress, int remotePort)
    {
        try {
            ConnectionBuilder cb = new ConnectionBuilder(this, localPort, remoteAddress, remotePort, false);
            cb.start();
            myConnectionBuilders.add(cb);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public synchronized void disconnect()
    {
        for (Connection connection : myConnections) {
            connection.interrupt();
        }
        myConnections.clear();

        for (ConnectionBuilder connectionBuilder : myConnectionBuilders) {
            connectionBuilder.interrupt();
        }
        myConnectionBuilders.clear();
    }

    public synchronized boolean startLogging(File file)
    {
        if (myLogger == null) {
            try {
                myLogger = new BufferedOutputStream(new FileOutputStream(file, true));
                return true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public synchronized void stopLogging()
    {
        if (myLogger != null) {
            try {
                myLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myLogger = null;
            }
        }
    }

    public synchronized void process(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        System.out.println(request);
        System.out.println(response);

        if (myLogger != null) {
            write(request, response);
        }
    }

    private void write(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        try {
            request.writeTo(myLogger);
            response.writeTo(myLogger);
            myLogger.flush();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void replay(File file)
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
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
