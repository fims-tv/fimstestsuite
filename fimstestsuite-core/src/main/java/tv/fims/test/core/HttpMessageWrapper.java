package tv.fims.test.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;

public class HttpMessageWrapper
{
    private static final byte[] theHttpMessageHeader = {'F', 'I', 'M', 'S', 'H', 'T', 'T', 'P', 'M', 'E', 'S', 'S', 'A', 'G', 'E'};

    public enum Type
    {
        HttpRequest(1),
        HttpEntityEnclosingRequest(2),
        HttpResponse(3);

        private static final Map<Integer, Type> map;

        static {
            map = new HashMap<>();
            for (Type type : Type.values()) {
                map.put(type.myValue, type);
            }
        }

        private final int myValue;

        private Type(int value)
        {
            myValue = value;
        }

        public int getValue()
        {
            return myValue;
        }

        public static Type valueOf(int value)
        {
            return map.get(value);
        }
    }

    private final HttpMessage myMessage;
    private final long myTimestamp;
    private final boolean myCallback;

    public HttpMessageWrapper(HttpMessage message, long timestamp, boolean callback)
    {
        this.myMessage = message;
        this.myTimestamp = timestamp;
        this.myCallback = callback;
    }

    public HttpMessage getMessage()
    {
        return myMessage;
    }

    public long getTimestamp()
    {
        return myTimestamp;
    }

    public boolean isCallback()
    {
        return myCallback;
    }

    public int writeTo(OutputStream os) throws IOException
    {
        Type type;

        if (myMessage instanceof HttpEntityEnclosingRequest) {
            type = Type.HttpEntityEnclosingRequest;
        } else if (myMessage instanceof HttpRequest) {
            type = Type.HttpRequest;
        } else if (myMessage instanceof HttpResponse) {
            type = Type.HttpResponse;
        } else {
            return 0;
        }

        DataOutputStream dos = new DataOutputStream(os);

        int callbackFlag = myCallback ? 0x80 : 0x00;

        dos.write(theHttpMessageHeader);
        dos.write(callbackFlag | type.getValue());
        dos.writeLong(myTimestamp);

        HttpEntity entity = null;

        if (myMessage instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) myMessage;
            RequestLine requestLine = request.getRequestLine();
            dos.writeUTF(requestLine.getMethod());
            dos.writeUTF(requestLine.getUri());
            dos.writeUTF(requestLine.getProtocolVersion().getProtocol());
            dos.write(requestLine.getProtocolVersion().getMajor());
            dos.write(requestLine.getProtocolVersion().getMinor());

            if (myMessage instanceof HttpEntityEnclosingRequest) {
                entity = ((HttpEntityEnclosingRequest) myMessage).getEntity();
            }
        } else {
            HttpResponse response = (HttpResponse) myMessage;
            StatusLine statusLine = response.getStatusLine();
            dos.writeUTF(statusLine.getProtocolVersion().getProtocol());
            dos.write(statusLine.getProtocolVersion().getMajor());
            dos.write(statusLine.getProtocolVersion().getMinor());
            dos.writeInt(statusLine.getStatusCode());
            dos.writeUTF(statusLine.getReasonPhrase());

            entity = response.getEntity();
        }

        Header[] headers = myMessage.getAllHeaders();

        dos.write(headers.length);

        for (Header header : headers) {
            dos.writeUTF(header.getName());
            dos.writeUTF(header.getValue());
        }

        if (entity != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            dos.writeInt(baos.size());
            baos.writeTo(dos);
        }

        return dos.size();
    }

    public static HttpMessageWrapper readFrom(InputStream is) throws IOException
    {
        DataInputStream dis = new DataInputStream(is);

        byte[] buf = new byte[theHttpMessageHeader.length];

        dis.read(buf);

        if (!Arrays.equals(buf, theHttpMessageHeader)) {
            return null;
        }

        int val = dis.read();
        boolean callback = (val & 0x80) == 0x80;

        val &= 0x7F;

        Type type = Type.valueOf(val);
        if (type == null) {
            return null;
        }

        long timestamp = dis.readLong();

        HttpMessage httpMessage;
        String method, uri, protocol, reason;
        int major, minor, code;

        switch (type) {
            case HttpRequest:
            case HttpEntityEnclosingRequest:
                method = dis.readUTF();
                uri = dis.readUTF();
                protocol = dis.readUTF();
                major = dis.read();
                minor = dis.read();
                if (type == Type.HttpRequest) {
                    httpMessage = new BasicHttpRequest(method, uri, new ProtocolVersion(protocol, major, minor));
                } else {
                    httpMessage = new BasicHttpEntityEnclosingRequest(method, uri, new ProtocolVersion(protocol, major, minor));
                }
                break;
            case HttpResponse:
                protocol = dis.readUTF();
                major = dis.read();
                minor = dis.read();
                code = dis.readInt();
                reason = dis.readUTF();
                httpMessage = new BasicHttpResponse(new ProtocolVersion(protocol, major, minor), code, reason);
                break;
            default:
                return null;
        }

        Header[] headers = new Header[dis.read()];

        for (int i = 0; i < headers.length; i++) {
            String name = dis.readUTF();
            String value = dis.readUTF();
            headers[i] = new BasicHeader(name, value);
        }

        httpMessage.setHeaders(headers);

        if (type == Type.HttpEntityEnclosingRequest || type == Type.HttpResponse) {
            byte[] data = new byte[dis.readInt()];
            dis.read(data);
            HttpEntity entity = new ByteArrayEntity(data);

            if (httpMessage instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) httpMessage).setEntity(entity);
            } else {
                ((HttpResponse) httpMessage).setEntity(entity);
            }
        }

        return new HttpMessageWrapper(httpMessage, timestamp, callback);
    }

    @Override
    public String toString()
    {
        String arrowsString = (myMessage instanceof HttpRequest) ? ">>>>>>>>>> " : "<<<<<<<<<< ";
        String callbackString = myCallback ? "Callback " : "";
        String typeString = (myMessage instanceof HttpRequest) ? "HttpRequest " : "HttpResponse ";

        StringBuilder sb = new StringBuilder();
        sb.append(arrowsString);
        sb.append(callbackString);
        sb.append(typeString);
        sb.append("at ");
        sb.append(new SimpleDateFormat().format(new Date(myTimestamp)));
        sb.append(" ");
        sb.append(arrowsString);
        sb.append("\r\n");

        HttpEntity entity = null;

        if (myMessage instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) myMessage;
            sb.append(request.getRequestLine().toString()).append("\r\n");

            if (myMessage instanceof HttpEntityEnclosingRequest) {
                entity = ((HttpEntityEnclosingRequest) myMessage).getEntity();
            }
        } else if (myMessage instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) myMessage;
            sb.append(response.getStatusLine().toString()).append("\r\n");
            entity = response.getEntity();
        }

        for (Header header : myMessage.getAllHeaders()) {
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
        }

        sb.append("\r\n");

        if (entity != null) {
            boolean isText = false;
            boolean isXml = false;

            for (Header header : myMessage.getHeaders("Content-Type")) {
                String value = header.getValue().toLowerCase();
                if (value.contains("xml")) {
                    isXml = true;
                }
                if (value.contains("text")) {
                    isText = true;
                }
            }

            if (isXml || isText) {
                try (InputStream is = entity.getContent()) {
                    String text = IOUtils.toString(is, "UTF-8");
                    if (isXml) {
                        text = Utils.formatXML(text);
                    }
                    sb.append(text);
                    sb.append("\r\n");
                } catch (IOException | UnsupportedOperationException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                sb.append("NOT SHOWING BINARY CONTENT\r\n");
            }
        }

        return sb.toString();
    }
}
