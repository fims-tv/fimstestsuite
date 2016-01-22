package tv.fims.testsuite.modules.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;
import tv.fims.testsuite.modules.ModuleImpl;
import tv.fims.testsuite.modules.message.HttpMessageWrapper;

public class LoggingModuleImpl extends ModuleImpl implements LoggingModule
{
    private boolean myBinaryLogEnabled;
    private File myBinaryLogFile;
    private boolean myRegularLogEnabled;
    private File myRegularLogFile;

    private BufferedOutputStream myBinaryLogger;
    private BufferedOutputStream myRegularLogger;

    public LoggingModuleImpl()
    {
        File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        myBinaryLogFile = new File(dir, "fimstest.binlog");
        myRegularLogFile = new File(dir, "fimstest.log");
    }

    @Override
    public void setBinaryLogEnabled(boolean enabled)
    {
        if (myBinaryLogEnabled != enabled) {
            myBinaryLogEnabled = enabled;
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public boolean isBinaryLogEnabled()
    {
        return myBinaryLogEnabled;
    }

    @Override
    public void setBinaryLogFile(File file)
    {
        if (!myBinaryLogFile.equals(file)) {
            if (file != null) {
                myBinaryLogFile = file;
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public File getBinaryLogFile()
    {
        return myBinaryLogFile;
    }

    @Override
    public void setRegularLogEnabled(boolean enabled)
    {
        if (myRegularLogEnabled != enabled) {
            myRegularLogEnabled = enabled;
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public boolean isRegularLogEnabled()
    {
        return myRegularLogEnabled;
    }

    @Override
    public void setRegularLogFile(File file)
    {
        if (!myRegularLogFile.equals(file)) {
            if (file != null) {
                myRegularLogFile = file;
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public File getRegularLogFile()
    {
        return myRegularLogFile;
    }

    @Override
    public void enable()
    {
        File binLogFile = myBinaryLogEnabled ? myBinaryLogFile : null;
        File logFile = myRegularLogEnabled ? myRegularLogFile : null;
        myEnabled = startLogging(binLogFile, logFile);
        notifyEvent(Event.EnabledUpdate);
    }

    @Override
    public void disable()
    {
        stopLogging();
        myEnabled = false;
        notifyEvent(Event.EnabledUpdate);
    }

    @Override
    public void onMessage(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        if (myBinaryLogger != null) {
            writeBinaryLog(request, response);
        }
        if (myRegularLogger != null) {
            writeRegularLog(request, response);
        }
    }

    private void writeBinaryLog(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        try {
            request.writeTo(myBinaryLogger);
            response.writeTo(myBinaryLogger);
            myBinaryLogger.flush();
        } catch (IOException ex) {
            Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeRegularLog(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        try {
            myRegularLogger.write(request.toString().getBytes("UTF-8"));
            myRegularLogger.write(response.toString().getBytes("UTF-8"));
            myRegularLogger.flush();
        } catch (IOException ex) {
            Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean startLogging(File binaryLogFile, File regularLogFile)
    {
        stopLogging();

        if (binaryLogFile != null) {
            try {
                myBinaryLogger = new BufferedOutputStream(new FileOutputStream(binaryLogFile, true));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
                stopLogging();
                return false;
            }
        }

        if (regularLogFile != null) {
            try {
                myRegularLogger = new BufferedOutputStream(new FileOutputStream(regularLogFile, true));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
                stopLogging();
                return false;
            }
        }

        return myBinaryLogger != null || myRegularLogger != null;
    }

    public synchronized void stopLogging()
    {
        if (myBinaryLogger != null) {
            try {
                myBinaryLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myBinaryLogger = null;
            }
        }
        if (myRegularLogger != null) {
            try {
                myRegularLogger.close();
            } catch (IOException ex) {
                Logger.getLogger(LoggingModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                myRegularLogger = null;
            }
        }
    }

}
