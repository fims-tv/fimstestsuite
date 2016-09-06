package tv.fims.testsuite.modules.message.proxy;

import tv.fims.testsuite.modules.message.MessageModuleImpl;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyModuleImpl extends MessageModuleImpl implements ProxyModule
{
    private String myLocalAddress;
    private int myLocalPort;
    private String myRemoteAddress;
    private int myRemotePort;

    private final Map<URL, ConnectionBuilder> myConnectionBuilders;
    private final List<Connection> myConnections;
    private final Map<URL, URL> myCallbackMap;

    public ProxyModuleImpl()
    {
        myLocalAddress = "127.0.0.1";
        myLocalPort = 5000;
        myRemoteAddress = "www.fims.tv";
        myRemotePort = 80;

        myConnectionBuilders = new HashMap<>();
        myConnections = new ArrayList<>();
        myCallbackMap = new HashMap<>();
    }

    @Override
    public void setLocalAddress(String address)
    {
        if (!myLocalAddress.equals(address)) {
            if (address != null) {
                myLocalAddress = address;
            }
            notifyEvent(Event.ConfigurationUpdate);
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
    public void enable()
    {
        myEnabled = connect(myLocalAddress, myLocalPort, myRemoteAddress, myRemotePort, null) != null;
        notifyEvent(Event.EnabledUpdate);
    }

    @Override
    public void disable()
    {
        disconnect();
        myEnabled = false;
        notifyEvent(Event.EnabledUpdate);
    }

    synchronized ConnectionBuilder connect(String localAddress, int localPort, String remoteAddress, int remotePort, URL callbackAddress)
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
            Logger.getLogger(ProxyModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    synchronized void disconnect()
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

    synchronized void putCallback(URL replacement, URL original)
    {
        myCallbackMap.put(replacement, original);
    }

    synchronized URL getCallback(URL url)
    {
        return myCallbackMap.get(url);
    }
}
