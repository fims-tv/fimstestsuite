package tv.fims.test.app;

import java.io.File;

public interface AppController
{
    // Connection
    void setLocalAddress(String address);
    String getLocalAddress();
    void setLocalPort(int port);
    int getLocalPort();

    void setRemoteAddress(String address);
    String getRemoteAddress();
    void setRemotePort(int port);
    int getRemotePort();

    void connect();
    void disconnect();
    boolean isConnected();

    // Logging
    void setBinaryLogEnabled(boolean enabled);
    boolean isBinaryLogEnabled();
    void setBinaryLogFile(File file);
    File getBinaryLogFile();

    void setRegularLogEnabled(boolean enabled);
    boolean isRegularLogEnabled();
    void setRegularLogFile(File file);
    File getRegularLogFile();

    void startLogging();
    void stopLogging();
    boolean isLogging();

    // Replay
    boolean replayBinaryLogFile(File file);

    // Other
    void loadProperties();
    void saveProperties();

    public void addListener(EventListener listener);
    public void removeListener(EventListener listener);

    public static enum Event
    {
        ConnectionUpdate,
        LoggingUpdate,
    }

    public interface EventListener
    {
        void onEvent(Event event);
    }
}
