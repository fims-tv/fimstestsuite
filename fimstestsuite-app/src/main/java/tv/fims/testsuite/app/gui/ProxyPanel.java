package tv.fims.testsuite.app.gui;

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
import tv.fims.testsuite.app.AppController;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.message.proxy.ProxyModule;

public class ProxyPanel extends JPanel
{
    private final ProxyModule myModule;
    private final ActionListener myActionListener;

    private final JTextField myFimsServiceIpAddressField;
    private final JTextField myFimsServicePortNumberField;
    private final JTextField myTestSuiteIpAddressField;
    private final JTextField myTestSuitePortNumberField;

    private final JButton myConnectButton;
    private final JButton myDisconnectButton;

    public ProxyPanel(AppController controller)
    {
        super(new GridBagLayout());

        myModule = controller.getProxyModule();

        myModule.addListener(new EventListenerImpl());

        myActionListener = new ActionListenerImpl();

        JLabel lblRemoteAddress = new JLabel("FIMS Service IP Address:");
        myFimsServiceIpAddressField = new JTextField();
        myFimsServiceIpAddressField.setPreferredSize(new Dimension(300, 26));
        myFimsServiceIpAddressField.addFocusListener(new FocusListenerImpl(myFimsServiceIpAddressField));

        JLabel lblRemotePort = new JLabel("FIMS Service Port Number:");
        myFimsServicePortNumberField = new JTextField();
        myFimsServicePortNumberField.setPreferredSize(new Dimension(300, 26));
        myFimsServicePortNumberField.addFocusListener(new FocusListenerImpl(myFimsServicePortNumberField));

        JLabel lblLocalAddress = new JLabel("Test Suite IP Address:");
        myTestSuiteIpAddressField = new JTextField();
        myTestSuiteIpAddressField.setPreferredSize(new Dimension(300, 26));
        myTestSuiteIpAddressField.addFocusListener(new FocusListenerImpl(myTestSuiteIpAddressField));

        JLabel lblLocalPort = new JLabel("Test Suite Port Number:");
        myTestSuitePortNumberField = new JTextField();
        myTestSuitePortNumberField.setPreferredSize(new Dimension(300, 26));
        myTestSuitePortNumberField.addFocusListener(new FocusListenerImpl(myTestSuitePortNumberField));

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

        add(lblRemoteAddress, c1);
        add(myFimsServiceIpAddressField, c2);

        add(lblRemotePort, c1);
        add(myFimsServicePortNumberField, c2);

        add(lblLocalAddress, c1);
        add(myTestSuiteIpAddressField, c2);

        add(lblLocalPort, c1);
        add(myTestSuitePortNumberField, c2);

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
                myTestSuiteIpAddressField.setText(myModule.getLocalAddress());
                myTestSuitePortNumberField.setText(String.valueOf(myModule.getLocalPort()));
                myFimsServiceIpAddressField.setText(myModule.getRemoteAddress());
                myFimsServicePortNumberField.setText(String.valueOf(myModule.getRemotePort()));

                boolean isDisconnected = !myModule.isEnabled();

                myTestSuiteIpAddressField.setEditable(isDisconnected);
                myTestSuitePortNumberField.setEditable(isDisconnected);
                myFimsServiceIpAddressField.setEditable(isDisconnected);
                myFimsServicePortNumberField.setEditable(isDisconnected);

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
                if (myComponent.equals(myTestSuiteIpAddressField)) {
                    myModule.setLocalAddress(myTestSuiteIpAddressField.getText());
                } else if (myComponent.equals(myTestSuitePortNumberField)) {
                    int portNumber = Integer.parseInt(myTestSuitePortNumberField.getText());
                    myModule.setLocalPort(portNumber);
                } else if (myComponent.equals(myFimsServiceIpAddressField)) {
                    myModule.setRemoteAddress(myFimsServiceIpAddressField.getText());
                } else if (myComponent.equals(myFimsServicePortNumberField)) {
                    int portNumber = Integer.parseInt(myFimsServicePortNumberField.getText());
                    myModule.setRemotePort(portNumber);
                }
            } catch (NumberFormatException ex) {
                reload();
            }
        }
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
            switch (e.getActionCommand()) {
                case "Connect":
                    myModule.enable();
                    break;
                case "Disconnect":
                    myModule.disable();
                    break;
            }
        }
    }

}
