package tv.fims.testsuite.app.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import javax.swing.JFrame;
import tv.fims.testsuite.app.AppController;

public class MonitorFrame extends JFrame
{
    private final AppController myController;

    public MonitorFrame(Component parent, String title, AppController controller) throws HeadlessException
    {
        super(title);

        myController = controller;

        setSize(640, 480);
        setResizable(true);

        add(new MonitorPanel(controller));

        setLocationRelativeTo(parent);

        reload();
    }

    private void reload()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
            }
        });
    }
}
