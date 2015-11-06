package tv.fims.test.app;

import java.awt.EventQueue;
import javax.swing.JFrame;
import tv.fims.test.app.gui.MainFrame;

class FimsTestSuite
{
    private static JFrame theFrame;

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                theFrame = new MainFrame("FIMS Test Suite", new AppControllerImpl());
                theFrame.setVisible(true);
            }
        });
    }
}
