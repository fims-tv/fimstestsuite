package tv.fims.testsuite.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ModuleImpl implements Module
{
    private final ReentrantLock myListenersLock;
    private final List<EventListener> myListeners;

    protected boolean myEnabled;

    public ModuleImpl()
    {
        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();
    }

    @Override
    public void addListener(EventListener listener)
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
    public void removeListener(EventListener listener)
    {
        myListenersLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    protected void notifyEvent(Event e)
    {
        myListenersLock.lock();
        try {
            for (EventListener listener : myListeners) {
                listener.onEvent(e);
            }
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public boolean isEnabled()
    {
        return myEnabled;
    }
}
