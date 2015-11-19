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

    private String myLocalAddress;
    private int myLocalPort;
    private String myRemoteAddress;
    private int myRemotePort;
    private boolean myConnected;

    private boolean myBinaryLogEnabled;
    private File myBinaryLogFile;
    private boolean myRegularLogEnabled;
    private File myRegularLogFile;
    private boolean myLogging;

    public AppControllerImpl()
    {
        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();

        myEngine = new Engine();

        myLocalAddress = "127.0.0.1";
        myLocalPort = 5000;
        myRemoteAddress = "www.fims.tv";
        myRemotePort = 80;

        File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        myBinaryLogFile = new File(dir, "fimstest.binlog");
        myRegularLogFile = new File(dir, "fimstest.log");
    }

    @Override
    public void setLocalAddress(String address)
    {
        if (!myLocalAddress.equals(address)) {
            if (address != null) {
                myLocalAddress = address;
                saveProperties();
            }
            notifyEvent(Event.ConnectionUpdate);
        }
    }

    @Override
    public String getLocalAddress()
    {
        return myLocalAddress;
    }

    @Override
    public void setLocalPort(int port)
    {
        if (myLocalPort != port) {
            if (port > 0 && port < 65536) {
                myLocalPort = port;
                saveProperties();
            }
            notifyEvent(Event.ConnectionUpdate);
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
            notifyEvent(Event.ConnectionUpdate);
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
            notifyEvent(Event.ConnectionUpdate);
        }
    }

    @Override
    public int getRemotePort()
    {
        return myRemotePort;
    }

    @Override
    public void connect()
    {
        myConnected = myEngine.connect(myLocalAddress, myLocalPort, myRemoteAddress, myRemotePort, null) != null;
        notifyEvent(Event.ConnectionUpdate);
    }

    @Override
    public void disconnect()
    {
        myEngine.disconnect();
        myConnected = false;
        notifyEvent(Event.ConnectionUpdate);
    }

    @Override
    public boolean isConnected()
    {
        return myConnected;
    }

    @Override
    public void setBinaryLogEnabled(boolean enabled)
    {
        if (myBinaryLogEnabled != enabled) {
            myBinaryLogEnabled = enabled;
            saveProperties();
            notifyEvent(Event.LoggingUpdate);
        }
    }

    @Override
    public boolean isBinaryLogEnabled()
    {
        return myBinaryLogEnabled;
    }

    @Override
    public void setBinaryLogFile(File file)
    {
        if (!myBinaryLogFile.equals(file)) {
            if (file != null) {
                myBinaryLogFile = file;
                saveProperties();
            }
            notifyEvent(Event.LoggingUpdate);
        }
    }

    @Override
    public File getBinaryLogFile()
    {
        return myBinaryLogFile;
    }

    @Override
    public void setRegularLogEnabled(boolean enabled)
    {
        if (myRegularLogEnabled != enabled) {
            myRegularLogEnabled = enabled;
            saveProperties();
            notifyEvent(Event.LoggingUpdate);
        }
    }

    @Override
    public boolean isRegularLogEnabled()
    {
        return myRegularLogEnabled;
    }

    @Override
    public void setRegularLogFile(File file)
    {
        if (!myRegularLogFile.equals(file)) {
            if (file != null) {
                myRegularLogFile = file;
                saveProperties();
            }
            notifyEvent(Event.LoggingUpdate);
        }
    }

    @Override
    public File getRegularLogFile()
    {
        return myRegularLogFile;
    }

    @Override
    public void startLogging()
    {
        File binLogFile = myBinaryLogEnabled ? myBinaryLogFile : null;
        File logFile = myRegularLogEnabled ? myRegularLogFile : null;
        myLogging = myEngine.startLogging(binLogFile, logFile);
        notifyEvent(Event.LoggingUpdate);
    }

    @Override
    public void stopLogging()
    {
        myEngine.stopLogging();
        myLogging = false;
        notifyEvent(Event.LoggingUpdate);
    }

    @Override
    public boolean isLogging()
    {
        return myLogging;
    }

    @Override
    public void loadProperties()
    {
        try (InputStream is = new FileInputStream("application.properties")) {
            Properties props = new Properties();
            props.load(is);

            try {
                myLocalAddress = props.getProperty("local.address", "127.0.0.1");
            } catch (Exception ex) {
            }

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
                myBinaryLogEnabled = Boolean.valueOf(props.getProperty("log.binary.enabled", "false"));
            } catch (Exception ex) {
            }

            try {
                myBinaryLogFile = new File(props.getProperty("log.binary.file", String.valueOf(myBinaryLogFile)));
            } catch (Exception ex) {
            }

            try {
                myRegularLogEnabled = Boolean.valueOf(props.getProperty("log.regular.enabled", "false"));
            } catch (Exception ex) {
            }

            try {
                myRegularLogFile = new File(props.getProperty("log.regular.file", String.valueOf(myRegularLogFile)));
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
            props.setProperty("local.address", myLocalAddress);
            props.setProperty("local.port", String.valueOf(myLocalPort));
            props.setProperty("remote.address", myRemoteAddress);
            props.setProperty("remote.port", String.valueOf(myRemotePort));
            props.setProperty("log.binary.enabled", String.valueOf(myBinaryLogEnabled));
            props.setProperty("log.binary.file", String.valueOf(myBinaryLogFile));
            props.setProperty("log.regular.enabled", String.valueOf(myRegularLogEnabled));
            props.setProperty("log.regular.file", String.valueOf(myRegularLogFile));
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
