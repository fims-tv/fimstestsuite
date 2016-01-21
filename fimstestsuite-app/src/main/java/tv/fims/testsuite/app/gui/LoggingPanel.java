package tv.fims.testsuite.app.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import tv.fims.testsuite.app.AppController;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.logging.LoggingModule;

public class LoggingPanel extends JPanel
{
    private final LoggingModule myModule;
    private final ActionListener myActionListener;

    private final JCheckBox myBinaryLogCheckBox;
    private final JTextField myBinaryLogFileField;
    private final JButton myBinaryLogFileButton;

    private final JCheckBox myRegularLogCheckBox;
    private final JTextField myRegularLogFileField;
    private final JButton myRegularLogFileButton;

    private final JButton myStartButton;
    private final JButton myStopButton;

    public LoggingPanel(AppController controller)
    {
        super(new GridBagLayout());

        myActionListener = new ActionListenerImpl();

        myModule = controller.getLoggingModule();

        myModule.addListener(new EventListenerImpl());

        JLabel lblBinaryLogFile = new JLabel("Binary log file:");
        myBinaryLogCheckBox = new JCheckBox();
        myBinaryLogCheckBox.setActionCommand("EnableBinaryLogFile");
        myBinaryLogCheckBox.addActionListener(myActionListener);
        myBinaryLogFileField = new JTextField();
        myBinaryLogFileField.setPreferredSize(new Dimension(300, 26));
        myBinaryLogFileField.setEditable(false);
        myBinaryLogFileButton = new JButton("...");
        myBinaryLogFileButton.setActionCommand("SelectBinaryLogFile");
        myBinaryLogFileButton.addActionListener(myActionListener);

        JLabel lblLogFile = new JLabel("Regular log file:");
        myRegularLogCheckBox = new JCheckBox();
        myRegularLogCheckBox.setActionCommand("EnableRegularLogFile");
        myRegularLogCheckBox.addActionListener(myActionListener);
        myRegularLogFileField = new JTextField();
        myRegularLogFileField.setPreferredSize(new Dimension(300, 26));
        myRegularLogFileField.setEditable(false);
        myRegularLogFileButton = new JButton("...");
        myRegularLogFileButton.setActionCommand("SelectRegularLogFile");
        myRegularLogFileButton.addActionListener(myActionListener);

        myStartButton = new JButton("Start");
        myStartButton.setActionCommand("Start");
        myStartButton.addActionListener(myActionListener);

        myStopButton = new JButton("Stop");
        myStopButton.setActionCommand("Stop");
        myStopButton.addActionListener(myActionListener);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.anchor = GridBagConstraints.WEST;
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.anchor = GridBagConstraints.WEST;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 2;
        c3.anchor = GridBagConstraints.WEST;
        c3.weightx = 1;
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridx = 3;
        c4.anchor = GridBagConstraints.WEST;
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c234 = new GridBagConstraints();
        c234.gridx = 1;
        c234.gridwidth = 3;
        c234.anchor = GridBagConstraints.WEST;
        c234.weightx = 1;
        c234.fill = GridBagConstraints.HORIZONTAL;
        c234.insets = new Insets(5, 5, 5, 5);

        add(lblBinaryLogFile, c1);
        add(myBinaryLogCheckBox, c2);
        add(myBinaryLogFileField, c3);
        add(myBinaryLogFileButton, c4);

        add(lblLogFile, c1);
        add(myRegularLogCheckBox, c2);
        add(myRegularLogFileField, c3);
        add(myRegularLogFileButton, c4);

        add(myStartButton, c1);
        add(myStopButton, c234);

        reload();
    }

    private void reload()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myBinaryLogCheckBox.setSelected(myModule.isBinaryLogEnabled());
                myRegularLogCheckBox.setSelected(myModule.isRegularLogEnabled());
                myBinaryLogFileField.setText(String.valueOf(myModule.getBinaryLogFile()));
                myRegularLogFileField.setText(String.valueOf(myModule.getRegularLogFile()));

                boolean isLogging = myModule.isEnabled();

                myBinaryLogCheckBox.setEnabled(!isLogging);
                myBinaryLogFileButton.setEnabled(!isLogging);

                myRegularLogCheckBox.setEnabled(!isLogging);
                myRegularLogFileButton.setEnabled(!isLogging);

                myStartButton.setEnabled(!isLogging);
                myStopButton.setEnabled(isLogging);
            }
        });
    }

    private class EventListenerImpl implements Module.EventListener
    {
        @Override
        public void onEvent(Module.Event event)
        {
            switch (event) {
                case ConfigurationUpdate:
                case EnabledUpdate:
                    reload();
                    break;
            }
        }
    }

    private class ActionListenerImpl implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser chooser;
            FileNameExtensionFilter filter;
            int returnVal;

            switch (e.getActionCommand()) {
                case "SelectBinaryLogFile":
                    chooser = new JFileChooser();
                    filter = new FileNameExtensionFilter("Binary Log files (*.binlog)", "binlog");
                    chooser.setFileFilter(filter);
                    chooser.setSelectedFile(new File(myBinaryLogFileField.getText()));
                    returnVal = chooser.showSaveDialog((Window) SwingUtilities.getRoot(LoggingPanel.this));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        myModule.setBinaryLogFile(chooser.getSelectedFile());
                    }
                    break;
                case "SelectRegularLogFile":
                    chooser = new JFileChooser();
                    filter = new FileNameExtensionFilter("Log files (*.log)", "log");
                    chooser.setFileFilter(filter);
                    chooser.setSelectedFile(new File(myRegularLogFileField.getText()));
                    returnVal = chooser.showSaveDialog((Window) SwingUtilities.getRoot(LoggingPanel.this));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                         myModule.setRegularLogFile(chooser.getSelectedFile());
                    }
                    break;
                case "EnableBinaryLogFile":
                    myModule.setBinaryLogEnabled(myBinaryLogCheckBox.isSelected());
                    break;
                case "EnableRegularLogFile":
                    myModule.setRegularLogEnabled(myRegularLogCheckBox.isSelected());
                    break;
                case "Start":
                    myModule.enable();
                    break;
                case "Stop":
                    myModule.disable();
                    break;
            }
        }
    }
}
