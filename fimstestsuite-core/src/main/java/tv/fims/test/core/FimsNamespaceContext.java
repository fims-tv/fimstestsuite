package tv.fims.test.core;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class FimsNamespaceContext implements NamespaceContext
{
    @Override
    public String getNamespaceURI(String prefix)
    {
        switch (prefix) {
            case "bms":
                return "http://base.fims.tv";
            case "cms":
                return "http://capturemedia.fims.tv";
            case "desc":
                return "http://description.fims.tv";
            case "tim":
                return "http://baseTime.fims.tv";
            case "tms":
                return "http://transfermedia.fims.tv";
            case "tfms":
                return "http://transformmedia.fims.tv";
            case "rps":
                return "http://repository.fims.tv";
            case "mqas":
                return "http://mediaqa.fims.tv";
            case "qar":
                return "http://mediaqar.fims.tv";
            case "qat":
                return "http://mediaqat.fims.tv";
        }

        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI)
    {
        switch (namespaceURI) {
            case "http://base.fims.tv":
                return "bms";
            case "http://capturemedia.fims.tv":
                return "cms";
            case "http://description.fims.tv":
                return "desc";
            case "http://baseTime.fims.tv":
                return "tim";
            case "http://transfermedia.fims.tv":
                return "tms";
            case "http://transformmedia.fims.tv":
                return "tfms";
            case "http://repository.fims.tv":
                return "rps";
            case "http://mediaqa.fims.tv":
                return "mqas";
            case "http://mediaqar.fims.tv":
                return "qar";
            case "http://mediaqat.fims.tv":
                return "qat";
        }
        return "";
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
