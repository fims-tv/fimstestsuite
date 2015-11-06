package tv.fims.test.app.gui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import tv.fims.test.app.AppController;

public class MainFrame extends JFrame
{
    private final AppController myController;
    private final ActionListener myActionListener;

    public MainFrame(String title, AppController controller) throws HeadlessException
    {
        super(title);

        myController = controller;
        myController.loadProperties();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        myActionListener = new ActionListenerImpl();

        setJMenuBar(createMenuBar());

        add(new MainPanel(myController));

        pack();
        setLocationRelativeTo(null);

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

    private JMenuBar createMenuBar()
    {
        JMenuBar menubar = new JMenuBar();

        menubar.add(createFileMenu());

        return menubar;
    }

    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");

        JMenuItem item = new JMenuItem("Exit");
        item.setActionCommand("Exit");
        item.addActionListener(myActionListener);
        menu.add(item);

        return menu;
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            switch (e.getActionCommand()) {
                case "Exit":
                    dispose();
                    break;
            }
        }
    }
}
