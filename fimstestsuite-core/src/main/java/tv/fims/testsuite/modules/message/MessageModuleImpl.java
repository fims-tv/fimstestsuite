package tv.fims.testsuite.modules.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import tv.fims.testsuite.modules.ModuleImpl;

public abstract class MessageModuleImpl extends ModuleImpl implements MessageModule
{
    private final ReentrantLock myListenersLock;
    private final List<MessageListener> myListeners;

    public MessageModuleImpl()
    {
        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();
    }

    @Override
    public void addListener(MessageListener listener)
    {
        myListenersLock.lock();
        try {
            myListeners.remove(listener);
            myListeners.add(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public void removeListener(MessageListener listener)
    {
        myListenersLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    public synchronized void process(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        myListenersLock.lock();
        try {
            for (MessageListener listener : myListeners) {
                listener.onMessage(request, response);
            }
        } finally {
            myListenersLock.unlock();
        }
    }
}
