package tv.fims.testsuite.modules.logging;

import java.io.File;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.message.MessageModule;

public interface LoggingModule extends Module, MessageModule.MessageListener
{
    void setBinaryLogEnabled(boolean enabled);
    boolean isBinaryLogEnabled();
    void setBinaryLogFile(File file);
    File getBinaryLogFile();

    void setRegularLogEnabled(boolean enabled);
    boolean isRegularLogEnabled();
    void setRegularLogFile(File file);
    File getRegularLogFile();
}
