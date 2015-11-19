package tv.fims.test.app.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import tv.fims.test.app.AppController;

public class ConnectionPanel extends JPanel
{
    private final AppController myController;
    private final ActionListener myActionListener;

    private final JTextField myLocalAddressField;
    private final JTextField myLocalPortField;
    private final JTextField myRemoteAddressField;
    private final JTextField myRemotePortField;

    private final JButton myConnectButton;
    private final JButton myDisconnectButton;

    public ConnectionPanel(AppController controller)
    {
        super(new GridBagLayout());

        myController = controller;
        myController.addListener(new EventListenerImpl());

        myActionListener = new ActionListenerImpl();

        JLabel lblLocalAddress = new JLabel("Local Address:");
        myLocalAddressField = new JTextField();
        myLocalAddressField.setPreferredSize(new Dimension(300, 26));
        myLocalAddressField.addFocusListener(new FocusListenerImpl(myLocalAddressField));

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

        myConnectButton = new JButton("Connect");
        myConnectButton.setActionCommand("Connect");
        myConnectButton.addActionListener(myActionListener);

        myDisconnectButton = new JButton("Disconnect");
        myDisconnectButton.setActionCommand("Disconnect");
        myDisconnectButton.addActionListener(myActionListener);

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.anchor = GridBagConstraints.WEST;
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridwidth = 3;
        c2.anchor = GridBagConstraints.WEST;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);

        add(lblLocalAddress, c1);
        add(myLocalAddressField, c2);

        add(lblLocalPort, c1);
        add(myLocalPortField, c2);

        add(lblRemoteAddress, c1);
        add(myRemoteAddressField, c2);

        add(lblRemotePort, c1);
        add(myRemotePortField, c2);

        add(myConnectButton, c1);
        add(myDisconnectButton, c2);

        reload();
    }

    private void reload()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                myLocalAddressField.setText(myController.getLocalAddress());
                myLocalPortField.setText(String.valueOf(myController.getLocalPort()));
                myRemoteAddressField.setText(myController.getRemoteAddress());
                myRemotePortField.setText(String.valueOf(myController.getRemotePort()));

                boolean isDisconnected = !myController.isConnected();

                myLocalAddressField.setEditable(isDisconnected);
                myLocalPortField.setEditable(isDisconnected);
                myRemoteAddressField.setEditable(isDisconnected);
                myRemotePortField.setEditable(isDisconnected);

                myConnectButton.setEnabled(isDisconnected);
                myDisconnectButton.setEnabled(!isDisconnected);
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
                if (myComponent.equals(myLocalAddressField)) {
                    myController.setLocalAddress(myLocalAddressField.getText());
                } else if (myComponent.equals(myLocalPortField)) {
                    int portNumber = Integer.parseInt(myLocalPortField.getText());
                    myController.setLocalPort(portNumber);
                } else if (myComponent.equals(myRemoteAddressField)) {
                    myController.setRemoteAddress(myRemoteAddressField.getText());
                } else if (myComponent.equals(myRemotePortField)) {
                    int portNumber = Integer.parseInt(myRemotePortField.getText());
                    myController.setRemotePort(portNumber);
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
                case "Connect":
                    myController.connect();
                    break;
                case "Disconnect":
                    myController.disconnect();
                    break;
            }
        }
    }

}
