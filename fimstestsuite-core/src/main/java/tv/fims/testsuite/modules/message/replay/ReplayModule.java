package tv.fims.testsuite.modules.message.replay;

import java.io.File;
import tv.fims.testsuite.modules.message.MessageModule;

public interface ReplayModule extends MessageModule
{
    boolean replay(File file);
}
