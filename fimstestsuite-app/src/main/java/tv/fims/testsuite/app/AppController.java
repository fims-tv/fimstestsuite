package tv.fims.testsuite.app;

import tv.fims.testsuite.modules.logging.LoggingModule;
import tv.fims.testsuite.modules.message.proxy.ProxyModule;
import tv.fims.testsuite.modules.message.replay.ReplayModule;

public interface AppController
{
    // Modules
    ProxyModule getProxyModule();
    ReplayModule getReplayModule();
    LoggingModule getLoggingModule();

    // Other
    void loadProperties();
    void saveProperties();
}
