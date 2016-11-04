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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import tv.fims.testsuite.app.AppController;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.validation.ValidationModule;

public class ValidationPanel extends JPanel
{
    private final ValidationModule myModule;
    private final ActionListener myActionListener;

    private final JTextField myBinaryLogFileField;
    private final JButton myXMLSchemaFileButton;

    private final JButton myStartButton;
    private final JButton myStopButton;

    public ValidationPanel(AppController controller)
    {
        super(new GridBagLayout());

        myActionListener = new ActionListenerImpl();

        myModule = controller.getValidationModule();

        myModule.addListener(new EventListenerImpl());

        JLabel lblXmlSchemaFile = new JLabel("XML Schema file:");
        myBinaryLogFileField = new JTextField();
        myBinaryLogFileField.setPreferredSize(new Dimension(300, 26));
        myBinaryLogFileField.setEditable(false);
        myXMLSchemaFileButton = new JButton("...");
        myXMLSchemaFileButton.setActionCommand("SelectXMLSchemaFile");
        myXMLSchemaFileButton.addActionListener(myActionListener);

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

        GridBagConstraints c23 = new GridBagConstraints();
        c23.gridx = 1;
        c23.gridwidth = 2;
        c23.anchor = GridBagConstraints.WEST;
        c23.weightx = 1;
        c23.fill = GridBagConstraints.HORIZONTAL;
        c23.insets = new Insets(5, 5, 5, 5);

        add(lblXmlSchemaFile, c1);
        add(myBinaryLogFileField, c2);
        add(myXMLSchemaFileButton, c3);

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
                myBinaryLogFileField.setText(String.valueOf(myModule.getXMLSchemaFile()));

                boolean isEnabled = myModule.isEnabled();

                myXMLSchemaFileButton.setEnabled(!isEnabled);

                myStartButton.setEnabled(!isEnabled);
                myStopButton.setEnabled(isEnabled);
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
                case "SelectXMLSchemaFile":
                    chooser = new JFileChooser();
                    filter = new FileNameExtensionFilter("XML Schemas (*.xsd)", "xsd");
                    chooser.setFileFilter(filter);
                    chooser.setSelectedFile(new File(myBinaryLogFileField.getText()));
                    returnVal = chooser.showOpenDialog((Window) SwingUtilities.getRoot(ValidationPanel.this));
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        myModule.setXMLSchemaFile(chooser.getSelectedFile());
                    }
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
