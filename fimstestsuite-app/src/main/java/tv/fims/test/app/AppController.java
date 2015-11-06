package tv.fims.test.app;

import java.io.File;

public interface AppController
{
    void setLocalPort(int port);
    int getLocalPort();

    void setRemoteAddress(String address);
    String getRemoteAddress();

    void setRemotePort(int port);
    int getRemotePort();

    void setLogFile(File file);
    File getLogFile();

    void startLogging();
    void stopLogging();
    boolean isLogging();

    void loadProperties();
    void saveProperties();

    public void addListener(EventListener listener);
    public void removeListener(EventListener listener);

    public static enum Event
    {
        ConfigurationUpdate,
        ConnectionUpdate
    }

    public interface EventListener
    {
        void onEvent(Event event);
    }
}
