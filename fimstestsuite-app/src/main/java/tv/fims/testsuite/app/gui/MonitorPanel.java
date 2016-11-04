package tv.fims.testsuite.app.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import org.apache.http.HttpRequest;
import tv.fims.testsuite.app.AppController;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.validation.ValidationModule;
import tv.fims.testsuite.modules.validation.ValidationResult;

public class MonitorPanel extends JPanel
{
    private final ValidationModule myModule;

    private final JPanel myContentPanel;
    private final Component myContentGlue;

    private final List<RequestResponsePanel> myRequestResponsePanels;
    private final ReentrantLock myRequestResponsePanelsLock;

    private final Icon myWarningIcon;
    private boolean myClean;


    public MonitorPanel(AppController controller)
    {
        super(new BorderLayout());

        myModule = controller.getValidationModule();
        myModule.addListener(new EventListenerImpl());
        myModule.addListener(new ValidationListenerImpl());

        myRequestResponsePanels = new ArrayList<>();
        myRequestResponsePanelsLock = new ReentrantLock();

        myContentPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(myContentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        myContentGlue = Box.createGlue();

        Icon icon = UIManager.getIcon("OptionPane.warningIcon");

        BufferedImage bi = new BufferedImage(icon.getIconWidth() / 2, icon.getIconHeight() / 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.scale(0.5, 0.5);
        icon.paintIcon(null, g, 0, 5);
        g.dispose();

        myWarningIcon = new ImageIcon(bi);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void reload()
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                List<RequestResponsePanel> requestResponsePanels = new ArrayList<>();

                if (myClean) {
                    myContentPanel.removeAll();
                    myClean = false;
                }

                myRequestResponsePanelsLock.lock();
                try {
                    requestResponsePanels.addAll(myRequestResponsePanels);
                    myRequestResponsePanels.clear();
                } finally {
                    myRequestResponsePanelsLock.unlock();
                }

                myContentPanel.remove(myContentGlue);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                for (RequestResponsePanel requestResponsePanel : requestResponsePanels) {
                    myContentPanel.add(requestResponsePanel, gbc);
                }

                gbc.weighty = 1;
                myContentPanel.add(myContentGlue, gbc);

                revalidate();
                repaint();
            }
        });
    }

    private class EventListenerImpl implements Module.EventListener
    {
        @Override
        public void onEvent(Module.Event event)
        {
            switch (event) {
                case EnabledUpdate:
                    if (myModule.isEnabled()) {
                        myClean = true;
                        reload();
                    }
            }
        }
    }

    private class ValidationListenerImpl implements ValidationModule.ValidationListener
    {
        @Override
        public void onValidation(ValidationResult request, ValidationResult response)
        {
            RequestResponsePanel rrp = new RequestResponsePanel(request, response);

            myRequestResponsePanelsLock.lock();
            try {
                myRequestResponsePanels.add(rrp);
            } finally {
                myRequestResponsePanelsLock.unlock();
            }

            reload();
        }
    }

    private class RequestResponsePanel extends JPanel
    {
        private final JButton myRequestButton;
        private final JTextPane myRequestErrorPane;
        private final JTextPane myRequestTextPane;
        private boolean myRequestVisible;
        private GridBagConstraints myRequestErrorGridBagConstraints;
        private GridBagConstraints myRequestTextGridBagConstraints;

        private final JButton myResponseButton;
        private final JTextPane myResponseErrorPane;
        private final JTextPane myResponseTextPane;
        private boolean myResponseVisible;
        private GridBagConstraints myResponseErrorGridBagConstraints;
        private GridBagConstraints myResponseTextGridBagConstraints;

        private ActionListener myActionListener;

        private JButton createButton(ValidationResult result)
        {
            JButton button = new JButton();

            boolean isCallback = result.getHttpMessageWrapper().isCallback();
            boolean isRequest = result.getHttpMessageWrapper().getMessage() instanceof HttpRequest;

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(result.getHttpMessageWrapper().getTimestamp()));

            StringBuilder sb = new StringBuilder();
            if ((!isCallback && isRequest) || (isCallback && !isRequest)) {
                sb.append(timestamp);
                if (!result.getRootLocalName().isEmpty()) {
                    sb.append(" - ");
                    sb.append(result.getRootLocalName());
                }
            } else {
                if (!result.getRootLocalName().isEmpty()) {
                    sb.append(result.getRootLocalName());
                    sb.append(" - ");
                }
                sb.append(timestamp);
            }
            button.setText(sb.toString());
            button.setToolTipText(result.getRootNamespaceURI());

            if (result.getStatus() == ValidationResult.Status.ERROR) {
                button.setIcon(myWarningIcon);
            }

            return button;
        }

        public RequestResponsePanel(ValidationResult request, ValidationResult response)
        {
            super(new GridBagLayout());

            myActionListener = new ActionListenerImpl();

            boolean isCallback = request.getHttpMessageWrapper().isCallback();

            myRequestButton = createButton(request);
            myRequestButton.setActionCommand("Request");
            myRequestButton.addActionListener(myActionListener);

            myRequestErrorPane = new JTextPane();
            myRequestErrorPane.setText(request.getMessage());
            myRequestErrorPane.setEditable(false);
            myRequestErrorPane.setBackground(java.awt.Color.getHSBColor(4, 30, 96));
            myRequestErrorPane.setForeground(java.awt.Color.red);

            myRequestTextPane = new JTextPane();
            myRequestTextPane.setText(request.getHttpMessageWrapper().toString());
            myRequestTextPane.setEditable(false);

            myResponseErrorPane = new JTextPane();
            myResponseErrorPane.setText(response.getMessage());
            myResponseErrorPane.setEditable(false);
            myResponseErrorPane.setBackground(java.awt.Color.getHSBColor(4, 30, 96));
            myResponseErrorPane.setForeground(java.awt.Color.red);

            myResponseButton = createButton(response);
            myResponseButton.setActionCommand("Response");
            myResponseButton.addActionListener(myActionListener);

            myResponseTextPane = new JTextPane();
            myResponseTextPane.setText(response.getHttpMessageWrapper().toString());
            myResponseTextPane.setEditable(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.anchor = isCallback ? GridBagConstraints.EAST : GridBagConstraints.WEST;
            add(myRequestButton, gbc);

            gbc.gridy = 3;
            gbc.anchor = isCallback ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            add(myResponseButton, gbc);

            myRequestErrorGridBagConstraints = new GridBagConstraints();
            myRequestErrorGridBagConstraints.gridx = 0;
            myRequestErrorGridBagConstraints.gridy = 1;
            myRequestErrorGridBagConstraints.weightx = 1;
            myRequestErrorGridBagConstraints.anchor = isCallback ? GridBagConstraints.EAST : GridBagConstraints.WEST;

            myRequestTextGridBagConstraints = new GridBagConstraints();
            myRequestTextGridBagConstraints.gridx = 0;
            myRequestTextGridBagConstraints.gridy = 2;
            myRequestTextGridBagConstraints.weightx = 1;
            myRequestTextGridBagConstraints.anchor = isCallback ? GridBagConstraints.EAST : GridBagConstraints.WEST;

            myResponseErrorGridBagConstraints = new GridBagConstraints();
            myResponseErrorGridBagConstraints.gridx = 0;
            myResponseErrorGridBagConstraints.gridy = 5;
            myResponseErrorGridBagConstraints.weightx = 1;
            myResponseErrorGridBagConstraints.anchor = isCallback ? GridBagConstraints.WEST : GridBagConstraints.EAST;

            myResponseTextGridBagConstraints = new GridBagConstraints();
            myResponseTextGridBagConstraints.gridx = 0;
            myResponseTextGridBagConstraints.gridy = 6;
            myResponseTextGridBagConstraints.weightx = 1;
            myResponseTextGridBagConstraints.anchor = isCallback ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        }

        private class ActionListenerImpl implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getActionCommand() != null) {
                    switch (e.getActionCommand()) {
                        case "Request":
                            if (myRequestVisible) {
                                RequestResponsePanel.this.remove(myRequestErrorPane);
                                RequestResponsePanel.this.remove(myRequestTextPane);
                                myRequestVisible = false;
                            } else {
                                if (!myRequestErrorPane.getText().isEmpty()) {
                                    RequestResponsePanel.this.add(myRequestErrorPane, myRequestErrorGridBagConstraints);
                                }
                                RequestResponsePanel.this.add(myRequestTextPane, myRequestTextGridBagConstraints);
                                myRequestVisible = true;
                            }
                            RequestResponsePanel.this.revalidate();
                            RequestResponsePanel.this.repaint();
                            break;
                        case "Response":
                            if (myResponseVisible) {
                                    RequestResponsePanel.this.remove(myResponseErrorPane);
                                RequestResponsePanel.this.remove(myResponseTextPane);
                                myResponseVisible = false;
                            } else {
                                if (!myResponseErrorPane.getText().isEmpty()) {
                                    RequestResponsePanel.this.add(myResponseErrorPane, myResponseErrorGridBagConstraints);
                                }
                                RequestResponsePanel.this.add(myResponseTextPane, myResponseTextGridBagConstraints);
                                myResponseVisible = true;
                            }
                            RequestResponsePanel.this.revalidate();
                            RequestResponsePanel.this.repaint();
                            break;
                    }
                }
            }
        }
    }
}
