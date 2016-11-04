package tv.fims.testsuite.modules.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tv.fims.testsuite.modules.ModuleImpl;
import tv.fims.testsuite.modules.message.HttpMessageWrapper;
import tv.fims.testsuite.modules.utils.FimsNamespaceContext;
import tv.fims.testsuite.modules.utils.XMLUtils;

public class ValidationModuleImpl extends ModuleImpl implements ValidationModule
{
    private final ReentrantLock myListenersLock;
    private final List<ValidationListener> myListeners;

    private File myXMLSchemaFile;
    private Validator myXMLSchemaValidator;

    public ValidationModuleImpl()
    {
        myListenersLock = new ReentrantLock();
        myListeners = new ArrayList<>();

        File dir = FileSystemView.getFileSystemView().getDefaultDirectory();
        myXMLSchemaFile = new File(dir, "xmlschema.xsd");
    }

    @Override
    public void setXMLSchemaFile(File file)
    {
        if (!myXMLSchemaFile.equals(file)) {
            if (file != null) {
                myXMLSchemaFile = file;
            }
            notifyEvent(Event.ConfigurationUpdate);
        }
    }

    @Override
    public File getXMLSchemaFile()
    {
        return myXMLSchemaFile;
    }

    @Override
    public void addListener(ValidationListener listener)
    {
        myListenersLock.lock();
        try {
            myListeners.remove(listener);
            myListeners.add(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public void removeListener(ValidationListener listener)
    {
        myListenersLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            myListenersLock.unlock();
        }
    }

    protected void notifyValidation(ValidationResult request, ValidationResult response)
    {
        myListenersLock.lock();
        try {
            for (ValidationListener listener : myListeners) {
                listener.onValidation(request, response);
            }
        } finally {
            myListenersLock.unlock();
        }
    }

    @Override
    public void enable()
    {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(myXMLSchemaFile.toURI().toURL());
            myXMLSchemaValidator = schema.newValidator();
            myEnabled = true;
        } catch (IOException | SAXException ex) {
            Logger.getLogger(ValidationModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        notifyEvent(Event.EnabledUpdate);
    }

    @Override
    public void disable()
    {
        myEnabled = false;
        notifyEvent(Event.EnabledUpdate);
    }

    @Override
    public void onMessage(HttpMessageWrapper request, HttpMessageWrapper response)
    {
        if (myEnabled) {
            ValidationResult requestValidation = validateMessage(request);
            ValidationResult responseValidation = validateMessage(response);

            notifyValidation(requestValidation, responseValidation);
        }
    }

    private ValidationResult validateMessage(HttpMessageWrapper wrapper)
    {
        Document doc = getHttpMessageContent(wrapper.getMessage());

        if (doc == null) {
            return new ValidationResult(wrapper, "", "", ValidationResult.Status.OK);
        }

        String documentRootLocalName = doc.getDocumentElement().getLocalName();
        String documentRootNamespaceURI = doc.getDocumentElement().getNamespaceURI();

        String msg;
        try {
            DOMSource source = new DOMSource(doc);
            myXMLSchemaValidator.validate(source);
            return new ValidationResult(wrapper, documentRootNamespaceURI, documentRootLocalName, ValidationResult.Status.OK);
        } catch (SAXException | IOException ex) {
            msg = ex.toString();
        }
        return new ValidationResult(wrapper, documentRootNamespaceURI, documentRootLocalName, ValidationResult.Status.ERROR, msg);
    }

    private Document getHttpMessageContent(HttpMessage message)
    {
        HttpEntity entity = null;

        if (message instanceof HttpEntityEnclosingRequest) {
            entity = ((HttpEntityEnclosingRequest) message).getEntity();
        } else if (message instanceof HttpResponse) {
            entity = ((HttpResponse) message).getEntity();
        }

        Document doc = null;

        // redirecting reply to and fault to nodes if present
        if (entity != null) {
            try (InputStream is = entity.getContent()) {
                ContentType contentType = null;
                for (Header header : message.getHeaders("Content-Type")) {
                    try {
                        contentType = ContentType.parse(header.getValue());
                    } catch (ParseException | UnsupportedCharsetException ex) {
                    }
                }

                if (contentType != null) {
                    Charset charset = contentType.getCharset();
                    if (charset == null) {
                        charset = Charset.forName("UTF-8");
                    }
                    String mimeType = contentType.getMimeType();

                    if (mimeType.contains("xml")) {
                        String content = IOUtils.toString(is, charset.name());

                        Document xmlDocument = XMLUtils.parseXML(content);
                        if (xmlDocument != null) {
                            XPath xpath = XPathFactory.newInstance().newXPath();
                            xpath.setNamespaceContext(new FimsNamespaceContext());

                            NodeList nodes = (NodeList) xpath.evaluate("/S:Envelope/S:Body/S:Fault/detail/*", xmlDocument.getDocumentElement(), XPathConstants.NODESET);

                            if (nodes.getLength() == 0) {
                                nodes = (NodeList) xpath.evaluate("/S:Envelope/S:Body/*", xmlDocument.getDocumentElement(), XPathConstants.NODESET);
                            }

                            doc = XMLUtils.newEmptyDocument();

                            if (nodes.getLength() > 0) {
                                Node node = nodes.item(0);
                                Node newNode = doc.importNode(node, true);
                                doc.appendChild(newNode);
                            }
                        }
                    }
                }
            } catch (IOException | UnsupportedOperationException | XPathExpressionException ex) {
                Logger.getLogger(ValidationModuleImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return doc;
    }

}
