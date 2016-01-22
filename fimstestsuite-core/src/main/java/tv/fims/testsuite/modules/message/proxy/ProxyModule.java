package tv.fims.testsuite.modules.message.proxy;

import tv.fims.testsuite.modules.message.MessageModule;
import tv.fims.testsuite.modules.message.HttpMessageWrapper;
import tv.fims.testsuite.modules.Module;

public interface ProxyModule extends MessageModule
{
    void setLocalAddress(String address);
    String getLocalAddress();
    void setLocalPort(int port);
    int getLocalPort();

    void setRemoteAddress(String address);
    String getRemoteAddress();
    void setRemotePort(int port);
    int getRemotePort();
}
