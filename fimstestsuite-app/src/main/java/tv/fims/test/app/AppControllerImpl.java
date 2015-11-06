package tv.fims.test.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;
import tv.fims.test.core.Engine;

public class AppControllerImpl implements AppController
{
    private final ReentrantLock myListenersLock;
    private final List<EventListener> myListeners;

    private final Engine myEngine;

    private int myLocalPort;
    private String myRemoteAddress;
    private int myRemotePort;
    private File myLogFile;

    private boolean myConnected;

    public AppControllerImpl()
    {
        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();

        myEngine = new Engine();

        myLocalPort = 5000;
        myRemoteAddress = "www.fims.tv";
        myRemotePort = 80;
        myLogFile = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "fimstest.log");
    }

    @Override
    public void setLocalPort(int port)
    {
        if (myLocalPort != port) {
            if (port > 0 && port < 65536) {
                myLocalPort = port;
                saveProperties();
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public int getLocalPort()
    {
        return myLocalPort;
    }

    @Override
    public void setRemoteAddress(String address)
    {
        if (!myRemoteAddress.equals(address)) {
            if (address != null) {
                myRemoteAddress = address;
                saveProperties();
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public String getRemoteAddress()
    {
        return myRemoteAddress;
    }

    @Override
    public void setRemotePort(int port)
    {
        if (myRemotePort != port) {
            if (port > 0 && port < 65536) {
                myRemotePort = port;
                saveProperties();
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public int getRemotePort()
    {
        return myRemotePort;
    }

    @Override
    public void setLogFile(File file)
    {
        if (!myLogFile.equals(file)) {
            if (file != null) {
                myLogFile = file;
                saveProperties();
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public File getLogFile()
    {
        return myLogFile;
    }


    @Override
    public void startLogging()
    {
        myConnected = myEngine.connect(myLocalPort, myRemoteAddress, myRemotePort);
        if (!myEngine.startLogging(myLogFile)) {
            myEngine.disconnect();
            myConnected = false;
        }
        notifyEvent(Event.ConnectionUpdate);
    }

    @Override
    public void stopLogging()
    {
        myEngine.stopLogging();
        myEngine.disconnect();
        myConnected = false;
        notifyEvent(Event.ConnectionUpdate);
    }

    @Override
    public boolean isLogging()
    {
        return myConnected;
    }

    @Override
    public void loadProperties()
    {
        try (InputStream is = new FileInputStream("application.properties")) {
            Properties props = new Properties();
            props.load(is);

            try {
                int port = Integer.valueOf(props.getProperty("local.port", "5000"));
                if (port > 0 && port < 65536) {
                    myLocalPort = port;
                }
            } catch (Exception ex) {
            }

            try {
                myRemoteAddress = props.getProperty("remote.address", "www.fims.tv");
            } catch (Exception ex) {
            }

            try {
                int port = Integer.valueOf(props.getProperty("remote.port", "80"));
                if (port > 0 && port < 65536) {
                    myRemotePort = port;
                }
            } catch (Exception ex) {
            }

            try {
                myLogFile = new File(props.getProperty("log.file", String.valueOf(myLogFile)));
            } catch (Exception ex) {
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveProperties()
    {
        try (OutputStream os = new FileOutputStream("application.properties")) {
            Properties props = new Properties();
            props.setProperty("local.port", String.valueOf(myLocalPort));
            props.setProperty("remote.address", myRemoteAddress);
            props.setProperty("remote.port", String.valueOf(myRemotePort));
            props.setProperty("log.file", String.valueOf(myLogFile));
            props.store(os, "");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addListener(EventListener listener)
    {
        try {
            myListenersLock.lock();
            myListeners.remove(listener);
            myListeners.add(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public void removeListener(EventListener listener)
    {
        try {
            myListenersLock.lock();
            myListeners.remove(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    private void notifyEvent(Event event)
    {
        try {
            myListenersLock.lock();
            for (EventListener listener : myListeners) {
                listener.onEvent(event);
            }
        } finally {
            myListenersLock.unlock();
        }
    }

}
