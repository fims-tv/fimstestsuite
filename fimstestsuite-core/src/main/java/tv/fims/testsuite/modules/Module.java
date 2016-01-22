package tv.fims.testsuite.modules;

public interface Module
{
    void enable();
    void disable();
    boolean isEnabled();

    public void addListener(EventListener listener);
    public void removeListener(EventListener listener);

    public static enum Event
    {
        ConfigurationUpdate,
        EnabledUpdate,
    }

    public interface EventListener
    {
        void onEvent(Event event);
    }
}
