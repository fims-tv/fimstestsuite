package tv.fims.test.app.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import tv.fims.test.app.AppController;

public class MainPanel extends JPanel
{
    private final AppController myController;
    private final ActionListener myActionListener;

    private final JTextField myLocalPortField;
    private final JTextField myRemoteAddressField;
    private final JTextField myRemotePortField;
    private final JTextField myLogFileField;
    private final JButton myLogFileButton;

    private final JButton myStartButton;
    private final JButton myStopButton;

    public MainPanel(AppController controller)
    {
        super(new GridBagLayout());

        myController = controller;
        myActionListener = new ActionListenerImpl();

        myController.addListener(new EventListenerImpl());

        JLabel lblLocalPort = new JLabel("Local Port:");
        myLocalPortField = new JTextField();
        myLocalPortField.setPreferredSize(new Dimension(300, 26));
        myLocalPortField.addFocusListener(new FocusListenerImpl(myLocalPortField));

        JLabel lblRemoteAddress = new JLabel("Remote Address:");
        myRemoteAddressField = new JTextField();
        myRemoteAddressField.setPreferredSize(new Dimension(300, 26));
        myRemoteAddressField.addFocusListener(new FocusListenerImpl(myRemoteAddressField));

        JLabel lblRemotePort = new JLabel("Remote Port:");
        myRemotePortField = new JTextField();
        myRemotePortField.setPreferredSize(new Dimension(300, 26));
        myRemotePortField.addFocusListener(new FocusListenerImpl(myRemotePortField));

        JLabel lblLogFile = new JLabel("Log file:");
        myLogFileField = new JTextField();
        myLogFileField.setPreferredSize(new Dimension(300, 26));
        myLogFileField.addFocusListener(new FocusListenerImpl(myLogFileField));
        myLogFileField.setEditable(false);
        myLogFileButton = new JButton("...");
        myLogFileButton.setActionCommand("SelectFile");
        myLogFileButton.addActionListener(myActionListener);

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
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 2;
        c3.anchor = GridBagConstraints.WEST;
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c23 = new GridBagConstraints();
        c23.gridx = 1;
        c23.gridwidth = 2;
        c23.anchor = GridBagConstraints.WEST;
        c23.weightx = 1;
        c23.fill = GridBagConstraints.HORIZONTAL;
        c23.insets = new Insets(5, 5, 5, 5);

        add(lblLocalPort, c1);
        add(myLocalPortField, c23);

        add(lblRemoteAddress, c1);
        add(myRemoteAddressField, c23);

        add(lblRemotePort, c1);
        add(myRemotePortField, c23);

        add(lblLogFile, c1);
        add(myLogFileField, c2);
        add(myLogFileButton, c3);

        add(myStartButton, c1);
        add(myStopButton, c23);

        reload();
    }

    private void reload()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myLocalPortField.setText(String.valueOf(myController.getLocalPort()));
                myRemoteAddressField.setText(myController.getRemoteAddress());
                myRemotePortField.setText(String.valueOf(myController.getRemotePort()));
                myLogFileField.setText(String.valueOf(myController.getLogFile()));

                boolean isDisconnected = !myController.isLogging();

                myLocalPortField.setEditable(isDisconnected);
                myRemoteAddressField.setEditable(isDisconnected);
                myRemotePortField.setEditable(isDisconnected);
                myLogFileButton.setEnabled(isDisconnected);

                myStartButton.setEnabled(isDisconnected);
                myStopButton.setEnabled(!isDisconnected);
            }
        });
    }

    private class FocusListenerImpl implements FocusListener
    {
        private final JComponent myComponent;

        public FocusListenerImpl(JComponent component)
        {
            myComponent = component;
        }

        @Override
        public void focusGained(FocusEvent e)
        {
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            try {
                if (myComponent.equals(myLocalPortField)) {
                    int portNumber = Integer.parseInt(myLocalPortField.getText());
                    myController.setLocalPort(portNumber);
                } else if (myComponent.equals(myRemoteAddressField)) {
                    myController.setRemoteAddress(myRemoteAddressField.getText());
                } else if (myComponent.equals(myRemotePortField)) {
                    int portNumber = Integer.parseInt(myRemotePortField.getText());
                    myController.setRemotePort(portNumber);
                } else if (myComponent.equals(myRemoteAddressField)) {
                    myController.setLogFile(new File(myLogFileField.getText()));
                }
            } catch (NumberFormatException ex) {
                reload();
            }
        }
    }

    private class EventListenerImpl implements AppController.EventListener
    {
        @Override
        public void onEvent(AppController.Event event)
        {
            switch (event) {
                case ConfigurationUpdate:
                case ConnectionUpdate:
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
            switch (e.getActionCommand()) {
                case "SelectFile":
                    JFileChooser chooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Log files (*.log)", "log");
                    chooser.setFileFilter(filter);
                    chooser.setSelectedFile(new File(myLogFileField.getText()));
                    int returnVal = chooser.showSaveDialog((Window) SwingUtilities.getRoot(MainPanel.this));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        myLogFileField.setText(String.valueOf(chooser.getSelectedFile()));
                    }
                    break;
                case "Start":
                    myController.startLogging();
                    break;
                case "Stop":
                    myController.stopLogging();
                    break;
            }
        }
    }

}
