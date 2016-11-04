package tv.fims.testsuite.modules.validation;

import java.io.File;
import tv.fims.testsuite.modules.Module;
import tv.fims.testsuite.modules.message.MessageModule;

public interface ValidationModule extends Module, MessageModule.MessageListener
{
    void setXMLSchemaFile(File file);
    File getXMLSchemaFile();

    void addListener(ValidationListener listener);
    void removeListener(ValidationListener listener);

    interface ValidationListener
    {
        void onValidation(ValidationResult request, ValidationResult response);
    }
}
