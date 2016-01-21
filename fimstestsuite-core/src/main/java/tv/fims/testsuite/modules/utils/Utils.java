package tv.fims.testsuite.modules.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utils
{
    public static String formatXML(String xml)
    {
        return writeXML(parseXML(xml), true, xml.startsWith("<?xml"));
    }

    public static Document parseXML(String xml)
    {
        if (xml != null) {
            try {
                InputSource src = new InputSource(new StringReader(xml));
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                return factory.newDocumentBuilder().parse(src);
            } catch (ParserConfigurationException | SAXException | IOException ex) {
            }
        }
        return null;
    }

    public static String writeXML(Document document, boolean prettyPrint, boolean xmlDeclaration)
    {
        if (document != null) {
            try {
                DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
                LSSerializer writer = impl.createLSSerializer();

                writer.getDomConfig().setParameter("format-pretty-print", prettyPrint);
                writer.getDomConfig().setParameter("xml-declaration", xmlDeclaration);

                LSOutput lsOutput = impl.createLSOutput();
                lsOutput.setEncoding("UTF-8");
                Writer stringWriter = new StringWriter();
                lsOutput.setCharacterStream(stringWriter);
                writer.write(document.getDocumentElement(), lsOutput);

                return stringWriter.toString();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException ex) {
            }
        }
        return null;
    }
}
