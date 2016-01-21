package tv.fims.testsuite.modules.message.replay;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import tv.fims.testsuite.modules.message.HttpMessageWrapper;
import tv.fims.testsuite.modules.message.MessageModuleImpl;

public class ReplayModuleImpl extends MessageModuleImpl implements ReplayModule
{
    @Override
    public void enable()
    {
    }

    @Override
    public void disable()
    {
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public synchronized boolean replay(File file)
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
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ReplayModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
