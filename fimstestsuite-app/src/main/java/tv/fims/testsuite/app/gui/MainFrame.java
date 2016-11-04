package tv.fims.testsuite.app.gui;

import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import tv.fims.testsuite.app.AppController;

public class MainFrame extends JFrame
{
    private final AppController myController;
    private final ActionListener myActionListener;
    private final JTabbedPane myTabbedPane;
    private final MonitorFrame myMontitorFrame;

    public MainFrame(String title, AppController controller) throws HeadlessException
    {
        super(title);

        myController = controller;
        myController.loadProperties();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        myActionListener = new ActionListenerImpl();

        setJMenuBar(createMenuBar());

        myTabbedPane = new JTabbedPane();
        myTabbedPane.addTab("Proxy", new ProxyPanel(myController));
        myTabbedPane.addTab("Logging", new LoggingPanel(myController));
        myTabbedPane.addTab("Validation", new ValidationPanel(myController));
        add(myTabbedPane);

        myMontitorFrame = new MonitorFrame(this, "Monitor", myController);

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
        menubar.add(createWindowMenu());

        return menubar;
    }

    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("File");

        JMenuItem item = new JMenuItem("Replay binary log file...");
        item.setActionCommand("Replay");
        item.addActionListener(myActionListener);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Exit");
        item.setActionCommand("Exit");
        item.addActionListener(myActionListener);
        menu.add(item);

        return menu;
    }

    private JMenu createWindowMenu()
    {
        JMenu menu = new JMenu("Window");

        JMenuItem item = new JMenuItem("Monitor");
        item.setActionCommand("Monitor");
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
                case "Replay":
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary Log files (*.binlog)", "binlog");
                    chooser.setFileFilter(filter);
                    chooser.setSelectedFile(myController.getLoggingModule().getBinaryLogFile());
                    int returnVal = chooser.showOpenDialog(MainFrame.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        if (myController.getReplayModule().replay(chooser.getSelectedFile())) {
                            JOptionPane.showMessageDialog(MainFrame.this, "Sucessfully replayed file '" + chooser.getSelectedFile() + "'.");
                        } else {
                            JOptionPane.showMessageDialog(MainFrame.this, "An error occurred when replaying file '" + chooser.getSelectedFile() + "'.");
                        }
                    }
                    break;
                case "Monitor":
                    myMontitorFrame.setVisible(true);
                    break;
                case "Exit":
                    dispose();
                    break;
            }
        }
    }
}
