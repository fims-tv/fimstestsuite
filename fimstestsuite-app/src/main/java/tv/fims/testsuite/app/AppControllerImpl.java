package tv.fims.testsuite.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.Module.EventListener;
import tv.fims.testsuite.modules.logging.LoggingModule;
import tv.fims.testsuite.modules.logging.LoggingModuleImpl;
import tv.fims.testsuite.modules.message.proxy.ProxyModule;
import tv.fims.testsuite.modules.message.proxy.ProxyModuleImpl;
import tv.fims.testsuite.modules.message.replay.ReplayModule;
import tv.fims.testsuite.modules.message.replay.ReplayModuleImpl;

public class AppControllerImpl implements AppController
{
    private final ProxyModule myProxyModule;
    private final LoggingModule myLoggingModule;
    private final ReplayModule myReplayModule;

    private boolean isLoadingProperties;

    public AppControllerImpl()
    {
        EventListener listener = new EventListenerImpl();

        myLoggingModule = new LoggingModuleImpl();
        myLoggingModule.addListener(listener);

        myProxyModule = new ProxyModuleImpl();
        myProxyModule.addListener(listener);
        myProxyModule.addListener(myLoggingModule);

        myReplayModule = new ReplayModuleImpl();
        myReplayModule.addListener(myLoggingModule);
    }

    @Override
    public ProxyModule getProxyModule()
    {
        return myProxyModule;
    }

    @Override
    public LoggingModule getLoggingModule()
    {
        return myLoggingModule;
    }

    @Override
    public ReplayModule getReplayModule()
    {
        return myReplayModule;
    }

    @Override
    public void loadProperties()
    {
        try (InputStream is = new FileInputStream("application.properties")) {
            isLoadingProperties = true;

            Properties props = new Properties();
            props.load(is);

            try {
                myProxyModule.setLocalAddress(props.getProperty("local.address", myProxyModule.getLocalAddress()));
            } catch (Exception ex) {
            }

            try {
                myProxyModule.setLocalPort(Integer.valueOf(props.getProperty("local.port", String.valueOf(myProxyModule.getLocalPort()))));
            } catch (Exception ex) {
            }

            try {
                myProxyModule.setRemoteAddress(props.getProperty("remote.address", myProxyModule.getRemoteAddress()));
            } catch (Exception ex) {
            }

            try {
                myProxyModule.setRemotePort(Integer.valueOf(props.getProperty("remote.port", String.valueOf(myProxyModule.getRemotePort()))));
            } catch (Exception ex) {
            }

            try {
                myLoggingModule.setBinaryLogEnabled(Boolean.valueOf(props.getProperty("log.binary.enabled", String.valueOf(myLoggingModule.isBinaryLogEnabled()))));
            } catch (Exception ex) {
            }

            try {
                myLoggingModule.setBinaryLogFile(new File(props.getProperty("log.binary.file", String.valueOf(myLoggingModule.getBinaryLogFile()))));
            } catch (Exception ex) {
            }

            try {
                myLoggingModule.setRegularLogEnabled(Boolean.valueOf(props.getProperty("log.regular.enabled", String.valueOf(myLoggingModule.isRegularLogEnabled()))));
            } catch (Exception ex) {
            }

            try {
                myLoggingModule.setRegularLogFile(new File(props.getProperty("log.regular.file", String.valueOf(myLoggingModule.getRegularLogFile()))));
            } catch (Exception ex) {
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            isLoadingProperties = false;
        }
    }

    @Override
    public void saveProperties()
    {
        if (!isLoadingProperties) {
            try (OutputStream os = new FileOutputStream("application.properties")) {
                Properties props = new Properties();
                props.setProperty("local.address", myProxyModule.getLocalAddress());
                props.setProperty("local.port", String.valueOf(myProxyModule.getLocalPort()));
                props.setProperty("remote.address", myProxyModule.getRemoteAddress());
                props.setProperty("remote.port", String.valueOf(myProxyModule.getRemotePort()));
                props.setProperty("log.binary.enabled", String.valueOf(myLoggingModule.isBinaryLogEnabled()));
                props.setProperty("log.binary.file", String.valueOf(myLoggingModule.getBinaryLogFile()));
                props.setProperty("log.regular.enabled", String.valueOf(myLoggingModule.isRegularLogEnabled()));
                props.setProperty("log.regular.file", String.valueOf(myLoggingModule.getRegularLogFile()));
                props.store(os, "");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AppControllerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class EventListenerImpl implements Module.EventListener
    {
        @Override
        public void onEvent(Module.Event event)
        {
            switch (event) {
                case ConfigurationUpdate:
                    saveProperties();
                    break;
            }
        }
    }
}
