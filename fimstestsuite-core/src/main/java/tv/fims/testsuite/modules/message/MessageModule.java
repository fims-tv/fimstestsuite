package tv.fims.testsuite.modules.message;

import tv.fims.testsuite.modules.Module;

public interface MessageModule extends Module
{
    public void addListener(MessageListener listener);
    public void removeListener(MessageListener listener);

    public interface MessageListener
    {
        void onMessage(HttpMessageWrapper request, HttpMessageWrapper response);
    }
}
