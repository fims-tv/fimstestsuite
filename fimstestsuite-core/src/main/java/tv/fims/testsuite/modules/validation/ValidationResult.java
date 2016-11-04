package tv.fims.testsuite.modules.validation;

import tv.fims.testsuite.modules.message.HttpMessageWrapper;

public class ValidationResult
{
    public enum Status
    {
        OK,
        ERROR,
        UNKNOWN
    }

    private final HttpMessageWrapper myHttpMessageWrapper;
    private final String myRootNamespaceURI;
    private final String myRootLocalName;
    private final Status myStatus;
    private final String myMessage;

    public ValidationResult(HttpMessageWrapper httpMessageWrapper, String rootNamespaceURI, String rootLocalName, Status status)
    {
        this(httpMessageWrapper, rootNamespaceURI, rootLocalName, status, "");
    }

    public ValidationResult(HttpMessageWrapper httpMessageWrapper, String rootNamespaceURI, String rootLocalName, Status status, String message)
    {
        myRootNamespaceURI = rootNamespaceURI;
        myRootLocalName = rootLocalName;
        myStatus = status;
        myHttpMessageWrapper = httpMessageWrapper;
        myMessage = message;
    }

    public HttpMessageWrapper getHttpMessageWrapper()
    {
        return myHttpMessageWrapper;
    }

    public String getRootNamespaceURI()
    {
        return myRootNamespaceURI;
    }

    public String getRootLocalName()
    {
        return myRootLocalName;
    }

    public Status getStatus()
    {
        return myStatus;
    }

    public String getMessage()
    {
        return myMessage;
    }
}
