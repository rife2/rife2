/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import rife.config.RifeConfig;
import rife.engine.exceptions.EngineException;
import rife.engine.exceptions.InvalidFormException;
import rife.servlet.MultipartRequest;
import rife.tools.ArrayUtils;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A request that your application passes to a site through a
 * {@link SiteRunner}, in the same process and without a servlet container.
 * <p>You provide the method, the headers and the body, while the path and
 * the query string are given to {@link SiteRunner#handle(String, SiteRequest)}.
 * RIFE2 will then parse the parameters from the query string and from
 * {@code application/x-www-form-urlencoded} and {@code multipart/form-data}
 * bodies. The cookies are taken from the {@code Cookie} header and the
 * locales from the {@code Accept-Language} header.
 * <p>A request can only be handled once.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SiteRunner
 * @since 1.11.0
 */
public class SiteRequest implements Request {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final Map<String, String[]> parameters_ = new LinkedHashMap<>();
    private Map<String, UploadedFile[]> files_;
    private final SiteHeaders headers_ = new SiteHeaders();
    private RequestMethod method_ = RequestMethod.GET;
    private String body_ = null;
    private byte[] bodyAsBytes_ = null;
    private InputStream bodyAsStream_ = null;
    private boolean textBody_ = false;
    private Map<String, Object> attributes_;
    private String characterEncoding_;
    private List<Locale> locales_;
    private String protocol_ = "HTTP/1.1";
    private String remoteAddr_ = "127.0.0.1";
    private String remoteUser_;
    private String remoteHost_ = "localhost";
    private Boolean secure_ = null;

    private volatile SiteRunner runner_;
    private volatile SiteResponse response_;
    private String requestPath_ = "/";
    private volatile boolean cancelled_ = false;
    private SiteSession session_;
    private String requestedSessionId_;
    private boolean requestedSessionIdFromCookie_ = false;

    /**
     * Creates a new {@code GET} request.
     *
     * @since 1.11.0
     */
    public SiteRequest() {
    }

    /**
     * Creates a new request with a particular method.
     *
     * @param method the method of the request
     * @since 1.11.0
     */
    public SiteRequest(RequestMethod method) {
        setMethod(method);
    }

    void attach(SiteRunner runner) {
        // a request that's handled again starts out without being cancelled
        if (response_ != null) {
            cancelled_ = false;
        }
        runner_ = runner;
    }

    SiteRunner getRunner() {
        return runner_;
    }

    void prepare(SiteResponse response, String path) {
        response_ = response;
        requestPath_ = path;
    }

    void parse(String queryString, String urlSessionId) {
        parseParameters(queryString);
        determineRequestedSessionId(urlSessionId);
    }

    /**
     * Creates the response of this request. Extending classes can override
     * this to provide their own type of response.
     *
     * @return a new response
     * @since 1.11.0
     */
    protected SiteResponse createResponse() {
        return new SiteResponse(this);
    }

    /**
     * Parses the parameters of this request from the query string and from
     * the body.
     * <p>The values of the query string come first. Urlencoded bodies are
     * read for {@code POST}, {@code PUT} and {@code PATCH}, while multipart
     * bodies are read for any method. A request with a malformed
     * escape, or with a form beyond the limits of the runner, is answered
     * with {@code 400}.
     *
     * @param queryString the raw query string, or {@code null} when there's
     *                    none
     * @since 1.11.0
     */
    protected void parseParameters(String queryString) {
        var encoding = RifeConfig.engine().getRequestEncoding();
        addFormParameters(parameters_, queryString, encoding, encoding);

        var content_type = getContentType();
        if (MultipartRequest.isValidContentType(content_type)) {
            try (var body = bodyInputStream()) {
                var multipart_request = new MultipartRequest(content_type, body);
                for (var entry : multipart_request.getParameterMap().entrySet()) {
                    var values = parameters_.get(entry.getKey());
                    parameters_.put(entry.getKey(), null == values ? entry.getValue() : ArrayUtils.join(values, entry.getValue()));
                }
                files_ = multipart_request.getFileMap();
            } catch (IOException e) {
                throw new EngineException(e);
            }
            return;
        }

        if ((method_ == RequestMethod.POST || method_ == RequestMethod.PUT || method_ == RequestMethod.PATCH) &&
            content_type != null &&
            content_type.toLowerCase(Locale.ENGLISH).startsWith(FORM_CONTENT_TYPE)) {
            var body = readFormBody();
            if (body.length > 0) {
                var form = new LinkedHashMap<String, String[]>();
                addFormParameters(form, new String(body, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1, encoding);
                var runner = runner_;
                var max_parameters = null == runner ? -1 : runner.getMaxFormParameters();
                // like Jetty, the values of one name count once
                if (max_parameters >= 0 && form.size() > max_parameters) {
                    throw new InvalidFormException("the form has more than " + max_parameters + " parameters");
                }
                for (var entry : form.entrySet()) {
                    var values = parameters_.get(entry.getKey());
                    parameters_.put(entry.getKey(), null == values ? entry.getValue() : ArrayUtils.join(values, entry.getValue()));
                }
            }
        }
    }

    // reads at most one byte more than the limit, so that a large body isn't kept
    private byte[] readFormBody() {
        var runner = runner_;
        var max_size = null == runner ? -1 : runner.getMaxFormContentSize();
        if (max_size < 0 || bodyAsBytes_ != null || null == bodyAsStream_) {
            var body = getBodyAsBytes();
            if (max_size >= 0 && body.length > max_size) {
                throw new InvalidFormException("the form is larger than " + max_size + " bytes");
            }
            return body;
        }

        try (var stream = bodyAsStream_) {
            bodyAsStream_ = null;
            var body = stream.readNBytes((int) Math.min((long) max_size + 1, Integer.MAX_VALUE));
            if (body.length > max_size) {
                bodyAsBytes_ = new byte[0];
                throw new InvalidFormException("the form is larger than " + max_size + " bytes");
            }
            bodyAsBytes_ = body;
            return body;
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    // closes a body that was provided as a stream and that wasn't read
    void closeBody() {
        var stream = bodyAsStream_;
        if (stream != null) {
            bodyAsStream_ = null;
            try {
                stream.close();
            } catch (IOException e) {
                // the stream isn't used anymore, so there's nothing left to do
            }
        }
    }

    // the source charset is the one that turned the bytes of the form into
    // characters, so that its original bytes can be obtained again
    static void addFormParameters(Map<String, String[]> parameters, String form, Charset source, Charset encoding) {
        if (null == form || form.isEmpty()) {
            return;
        }

        for (var pair : StringUtils.split(form, "&")) {
            if (pair.isEmpty()) {
                continue;
            }

            var separator = pair.indexOf('=');
            var name = decodeFormComponent(-1 == separator ? pair : pair.substring(0, separator), source, encoding);
            var value = -1 == separator ? "" : decodeFormComponent(pair.substring(separator + 1), source, encoding);
            if (name.isEmpty()) {
                continue;
            }

            var values = parameters.get(name);
            if (null == values) {
                values = new String[]{value};
            } else {
                values = ArrayUtils.join(values, value);
            }
            parameters.put(name, values);
        }
    }

    // like Jetty, a malformed escape makes the whole form invalid
    static String decodeFormComponent(String component, Charset source, Charset encoding) {
        var raw = component.getBytes(source);
        var bytes = new ByteArrayOutputStream(raw.length);
        for (var i = 0; i < raw.length; i++) {
            var b = raw[i];
            if ('+' == b) {
                bytes.write(' ');
            } else if ('%' == b) {
                if (i + 2 >= raw.length ||
                    Character.digit(raw[i + 1], 16) == -1 || Character.digit(raw[i + 2], 16) == -1) {
                    throw new InvalidFormException("the form has a malformed escape");
                }
                bytes.write((Character.digit(raw[i + 1], 16) << 4) + Character.digit(raw[i + 2], 16));
                i += 2;
            } else {
                bytes.write(b);
            }
        }
        return bytes.toString(encoding);
    }

    private InputStream bodyInputStream() {
        if (bodyAsStream_ != null) {
            var stream = bodyAsStream_;
            bodyAsStream_ = null;
            bodyAsBytes_ = new byte[0];
            return stream;
        }
        return new ByteArrayInputStream(getBodyAsBytes());
    }

    private void determineRequestedSessionId(String urlSessionId) {
        requestedSessionId_ = null;
        requestedSessionIdFromCookie_ = false;

        var runner = runner_;
        if (null == runner) {
            return;
        }

        var cookie_name = runner.getSessionCookieName();
        var cookies = getCookies();
        if (cookies != null) {
            for (var cookie : cookies) {
                if (cookie_name.equalsIgnoreCase(cookie.getName())) {
                    // with several session cookies, pick the first known
                    // session or the last cookie
                    if (requestedSessionId_ != null &&
                        runner.findSession(requestedSessionId_) != null) {
                        break;
                    }

                    requestedSessionId_ = cookie.getValue();
                    requestedSessionIdFromCookie_ = true;
                }
            }
        }

        if (null == requestedSessionId_ &&
            runner.isSessionUrlParameters()) {
            requestedSessionId_ = urlSessionId;
        }

        if (requestedSessionId_ != null) {
            var session = runner.findSession(requestedSessionId_);
            if (session != null) {
                session.access();
            }
        }
    }

    SiteResponse getResponse() {
        return response_;
    }

    String getRequestPath() {
        return requestPath_;
    }

    boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie_;
    }

    /**
     * Cancels this request, for instance because the web view stopped
     * loading it. This method can be called from any thread.
     * <p>No new call to the listener of the response will start, anything
     * that the element still writes will fail like it does on a closed
     * connection, and a detached event stream will be closed. The listener
     * is told with {@link SiteResponseListener#cancelled()} once no call of
     * it is under way anymore.
     *
     * @see SiteResponseListener
     * @since 1.11.0
     */
    public void cancel() {
        cancelled_ = true;
        var response = response_;
        if (response != null) {
            response.cancel();
        }
    }

    /**
     * Indicates whether this request has been cancelled.
     *
     * @return {@code true} if the request was cancelled; or
     * <p>{@code false} otherwise
     * @see #cancel()
     * @since 1.11.0
     */
    public boolean isCancelled() {
        return cancelled_;
    }

    public RequestMethod getMethod() {
        return method_;
    }

    /**
     * Sets the method of this request.
     * <p>The method defaults to {@link RequestMethod#GET}.
     *
     * @param method the method that will be used by this request
     * @see #getMethod
     * @see #method(RequestMethod)
     * @since 1.11.0
     */
    public void setMethod(RequestMethod method) {
        if (null == method) throw new IllegalArgumentException("method can't be null");

        method_ = method;
    }

    /**
     * Sets the method of this request.
     *
     * @param method the method that will be used by this request
     * @return this {@code SiteRequest} instance
     * @see #getMethod
     * @see #setMethod(RequestMethod)
     * @since 1.11.0
     */
    public SiteRequest method(RequestMethod method) {
        setMethod(method);

        return this;
    }

    public Map<String, String[]> getParameters() {
        return parameters_;
    }

    /**
     * Sets the uploaded files of this request, for extending classes that
     * provide them themselves.
     *
     * @param files the uploaded files by parameter name, or {@code null}
     *              when there are none
     * @since 1.11.0
     */
    protected void setUploadedFiles(Map<String, UploadedFile[]> files) {
        files_ = files;
    }

    /**
     * Sets this request's body as a string, which will be converted to bytes
     * with the character encoding of this request when the body is read.
     *
     * @param body the body as a string
     * @see #body(String)
     * @see #getBody
     * @since 1.11.0
     */
    public void setBody(String body) {
        body_ = body;
        bodyAsStream_ = null;
        bodyAsBytes_ = null;
        textBody_ = body != null;
    }

    /**
     * Sets this request's body as a string.
     *
     * @param body the body as a string
     * @return this {@code SiteRequest} instance
     * @see #setBody(String)
     * @see #getBody
     * @since 1.11.0
     */
    public SiteRequest body(String body) {
        setBody(body);

        return this;
    }

    public String getBody() {
        if (body_ == null) {
            body_ = StringUtils.toString(getBodyAsBytes(), getCharacterEncoding());
        }

        return body_;
    }

    /**
     * Sets this request's body as bytes.
     *
     * @param bytes the body as bytes
     * @see #bodyAsBytes(byte[])
     * @see #getBodyAsBytes
     * @since 1.11.0
     */
    public void setBodyAsBytes(byte[] bytes) {
        body_ = null;
        bodyAsStream_ = null;
        bodyAsBytes_ = bytes;
        textBody_ = false;
    }

    /**
     * Sets this request's body as bytes.
     *
     * @param bytes the body as bytes
     * @return this {@code SiteRequest} instance
     * @see #setBodyAsBytes(byte[])
     * @see #getBodyAsBytes
     * @since 1.11.0
     */
    public SiteRequest bodyAsBytes(byte[] bytes) {
        setBodyAsBytes(bytes);

        return this;
    }

    /**
     * Sets the body of this request as a stream, which will be read when the
     * body is needed. A multipart body is read from the stream as it
     * arrives, so that it isn't kept in memory.
     *
     * @param stream the stream with the body
     * @see #bodyAsStream(InputStream)
     * @since 1.11.0
     */
    public void setBodyAsStream(InputStream stream) {
        body_ = null;
        bodyAsBytes_ = null;
        bodyAsStream_ = stream;
        textBody_ = false;
    }

    /**
     * Sets the body of this request as a stream.
     *
     * @param stream the stream with the body
     * @return this {@code SiteRequest} instance
     * @see #setBodyAsStream(InputStream)
     * @since 1.11.0
     */
    public SiteRequest bodyAsStream(InputStream stream) {
        setBodyAsStream(stream);

        return this;
    }

    public byte[] getBodyAsBytes() {
        if (null == bodyAsBytes_) {
            if (body_ != null) {
                bodyAsBytes_ = body_.getBytes(Charset.forName(getCharacterEncoding()));
            } else if (bodyAsStream_ != null) {
                try (var stream = bodyAsStream_) {
                    bodyAsStream_ = null;
                    bodyAsBytes_ = FileUtils.readBytes(stream);
                } catch (IOException e) {
                    throw new EngineException(e);
                }
            } else {
                return new byte[0];
            }
        }

        return bodyAsBytes_;
    }

    public Map<String, UploadedFile[]> getFiles() {
        return files_;
    }

    public boolean hasFile(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        if (null == getFiles()) {
            return false;
        }

        var uploaded_files = getFiles().get(name);
        if (null == uploaded_files ||
            0 == uploaded_files.length) {
            return false;
        }

        for (var uploaded_file : uploaded_files) {
            if (uploaded_file != null &&
                uploaded_file.getName() != null) {
                return true;
            }
        }

        return false;
    }

    public UploadedFile getFile(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        if (null == getFiles()) {
            return null;
        }

        var files = getFiles().get(name);
        if (null == files ||
            0 == files.length) {
            return null;
        }

        return files[0];
    }

    public UploadedFile[] getFiles(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        if (null == getFiles()) {
            return null;
        }

        return getFiles().get(name);
    }

    public String getServerRootUrl(int port) {
        if (port <= -1) {
            var runner = runner_;
            port = null == runner ? -1 : runner.getServerPort();
        }
        return SiteRunner.formatRootUrl(getScheme(), getServerName(), port);
    }

    public boolean hasCookie(String name) {
        return getCookie(name) != null;
    }

    public Cookie getCookie(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        var cookies = getCookies();
        if (null == cookies) {
            return null;
        }

        for (var cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }

        return null;
    }

    public Cookie[] getCookies() {
        var cookies = new ArrayList<Cookie>();
        for (var header : headers_.getHeaders(HEADER_COOKIE)) {
            for (var pair : StringUtils.split(header, ";")) {
                var separator = pair.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                var name = pair.substring(0, separator).trim();
                var value = pair.substring(separator + 1).trim();
                if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                try {
                    cookies.add(new Cookie(name, value));
                } catch (IllegalArgumentException e) {
                    // a name that isn't a valid cookie name is skipped
                }
            }
        }

        if (cookies.isEmpty()) {
            return null;
        }

        return cookies.toArray(new Cookie[0]);
    }

    public Object getAttribute(String name) {
        if (null == attributes_) {
            return null;
        }

        return attributes_.get(name);
    }

    public boolean hasAttribute(String name) {
        if (null == attributes_) {
            return false;
        }

        return attributes_.containsKey(name);
    }

    public Enumeration<String> getAttributeNames() {
        if (null == attributes_) {
            return Collections.emptyEnumeration();
        }

        return Collections.enumeration(new ArrayList<>(attributes_.keySet()));
    }

    public void removeAttribute(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        if (null == attributes_) {
            return;
        }

        attributes_.remove(name);
    }

    public void setAttribute(String name, Object object) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        if (null == attributes_) {
            attributes_ = new HashMap<>();
        }

        attributes_.put(name, object);
    }

    /**
     * Retrieves the character encoding of this request.
     * <p>When none was set, this is the
     * {@linkplain RifeConfig.EngineConfig#getRequestEncoding() request encoding}
     * of the engine, which is what a deployed site uses too.
     *
     * @return the character encoding
     * @since 1.11.0
     */
    public String getCharacterEncoding() {
        if (characterEncoding_ != null) {
            return characterEncoding_;
        }

        return RifeConfig.engine().getRequestEncoding().name();
    }

    /**
     * Sets the character encoding of this request.
     *
     * @param encoding the name of the character encoding
     * @see #characterEncoding(String)
     * @since 1.11.0
     */
    public void setCharacterEncoding(String encoding) {
        if (null == encoding) throw new IllegalArgumentException("encoding can't be null");
        if (encoding.isEmpty()) throw new IllegalArgumentException("encoding can't be empty");

        characterEncoding_ = encoding;
        // what was converted with the previous encoding is converted again
        if (textBody_) {
            bodyAsBytes_ = null;
        } else {
            body_ = null;
        }
    }

    /**
     * Sets the character encoding of this request.
     *
     * @param encoding the name of the character encoding
     * @return this {@code SiteRequest} instance
     * @see #setCharacterEncoding(String)
     * @since 1.11.0
     */
    public SiteRequest characterEncoding(String encoding) {
        setCharacterEncoding(encoding);

        return this;
    }

    /**
     * Retrieves the content type of this request, which is its
     * {@code Content-Type} header.
     *
     * @return the content type; or
     * <p>{@code null} if there's none
     * @since 1.11.0
     */
    public String getContentType() {
        return headers_.getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * Sets the content type of this request.
     *
     * @param type the content type
     * @see #contentType(String)
     * @since 1.11.0
     */
    public void setContentType(String type) {
        if (null == type) throw new IllegalArgumentException("type can't be null");
        if (type.isEmpty()) throw new IllegalArgumentException("type can't be empty");

        // like with a servlet container, it's also the header
        headers_.setHeader(HEADER_CONTENT_TYPE, type);
    }

    /**
     * Sets the content type of this request.
     *
     * @param type the content type
     * @return this {@code SiteRequest} instance
     * @see #setContentType(String)
     * @since 1.11.0
     */
    public SiteRequest contentType(String type) {
        setContentType(type);

        return this;
    }

    public long getDateHeader(String name) {
        return headers_.getDateHeader(name);
    }

    public String getHeader(String name) {
        return headers_.getHeader(name);
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers_.getHeaderNames());
    }

    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(headers_.getHeaders(name));
    }

    public int getIntHeader(String name) {
        return headers_.getIntHeader(name);
    }

    /**
     * Adds a request header with the given name and value. This method allows
     * request headers to have multiple values. The header name is
     * case-insensitive.
     *
     * @param name  the name of the header to set
     * @param value the additional header value
     * @return this {@code SiteRequest} instance
     * @since 1.11.0
     */
    public SiteRequest addHeader(String name, String value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == value) throw new IllegalArgumentException("value can't be null");

        headers_.addHeader(name, value);

        return this;
    }

    /**
     * Adds a request header with the given name and date-value. The date is
     * specified in terms of milliseconds since the epoch. This method allows
     * request headers to have multiple values.
     *
     * @param name  the name of the header to set
     * @param value the additional date value
     * @return this {@code SiteRequest} instance
     * @since 1.11.0
     */
    public SiteRequest addDateHeader(String name, long value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        headers_.addDateHeader(name, value);

        return this;
    }

    /**
     * Adds a request header with the given name and integer value. This
     * method allows request headers to have multiple values.
     *
     * @param name  the name of the header to set
     * @param value the additional integer value
     * @return this {@code SiteRequest} instance
     * @since 1.11.0
     */
    public SiteRequest addIntHeader(String name, int value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        headers_.addIntHeader(name, value);

        return this;
    }

    /**
     * Checks whether a certain request header is present.
     *
     * @param name the name of the header to check
     * @return {@code true} if the header was present; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    public boolean containsHeader(String name) {
        return headers_.containsHeader(name);
    }

    /**
     * Sets a request header with the given name and date-value. The date is
     * specified in terms of milliseconds since the epoch. If the header had
     * already been set, the new value overwrites the previous one. The {@link
     * #containsHeader} method can be used to test for the presence of a
     * header before setting its value.
     *
     * @param name  the name of the header to set
     * @param value the assigned date value
     * @see #dateHeader(String, long)
     * @since 1.11.0
     */
    public void setDateHeader(String name, long value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        headers_.setDateHeader(name, value);
    }

    /**
     * Sets a request header with the given name and date-value.
     *
     * @param name  the name of the header to set
     * @param value the assigned date value
     * @return this {@code SiteRequest} instance
     * @see #setDateHeader(String, long)
     * @since 1.11.0
     */
    public SiteRequest dateHeader(String name, long value) {
        setDateHeader(name, value);

        return this;
    }

    /**
     * Sets a request header with the given name and value. If the header had
     * already been set, the new value overwrites the previous one. The {@link
     * #containsHeader} method can be used to test for the presence of a
     * header before setting its value.
     *
     * @param name  the name of the header to set
     * @param value the header value
     * @see #header(String, String)
     * @since 1.11.0
     */
    public void setHeader(String name, String value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == value) throw new IllegalArgumentException("value can't be null");

        headers_.setHeader(name, value);
    }

    /**
     * Sets a request header with the given name and value.
     *
     * @param name  the name of the header to set
     * @param value the header value
     * @return this {@code SiteRequest} instance
     * @see #setHeader(String, String)
     * @since 1.11.0
     */
    public SiteRequest header(String name, String value) {
        setHeader(name, value);

        return this;
    }

    /**
     * Sets a request header with the given name and integer value. If the
     * header had already been set, the new value overwrites the previous one.
     * The {@link #containsHeader} method can be used to test for the presence
     * of a header before setting its value.
     *
     * @param name  the name of the header to set
     * @param value the assigned integer value
     * @see #intHeader(String, int)
     * @since 1.11.0
     */
    public void setIntHeader(String name, int value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        headers_.setIntHeader(name, value);
    }

    /**
     * Sets a request header with the given name and integer value.
     *
     * @param name  the name of the header to set
     * @param value the assigned integer value
     * @return this {@code SiteRequest} instance
     * @see #setIntHeader(String, int)
     * @since 1.11.0
     */
    public SiteRequest intHeader(String name, int value) {
        setIntHeader(name, value);

        return this;
    }

    /**
     * Removes a request header with the given name.
     *
     * @param name the name of the header to remove
     * @since 1.11.0
     */
    public void removeHeader(String name) {
        headers_.removeHeader(name);
    }

    /**
     * Retrieves the preferred locale of this request, from the locales
     * that were added or otherwise from the {@code Accept-Language} header.
     *
     * @return the preferred locale; or
     * <p>the default locale if there's none
     * @since 1.11.0
     */
    public Locale getLocale() {
        return localeList().get(0);
    }

    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(localeList());
    }

    private List<Locale> localeList() {
        if (locales_ != null &&
            !locales_.isEmpty()) {
            return new ArrayList<>(locales_);
        }

        var result = parseAcceptLanguage(headers_.getHeaders(HEADER_ACCEPT_LANGUAGE));
        if (result.isEmpty()) {
            result.add(Locale.getDefault());
        }
        return result;
    }

    static List<Locale> parseAcceptLanguage(List<String> headers) {
        record Weighted(Locale locale, double quality, int index) {
        }

        var weighted = new ArrayList<Weighted>();
        for (var header : headers) {
            for (var part : StringUtils.split(header, ",")) {
                var segments = StringUtils.split(part, ";");
                var tag = segments.get(0).trim();
                if (tag.isEmpty() || tag.equals("*")) {
                    continue;
                }
                var quality = 1.0;
                for (var i = 1; i < segments.size(); i++) {
                    var segment = segments.get(i).trim();
                    if (segment.startsWith("q=")) {
                        try {
                            quality = Double.parseDouble(segment.substring(2));
                        } catch (NumberFormatException e) {
                            quality = 0;
                        }
                    }
                }
                if (quality > 0) {
                    weighted.add(new Weighted(Locale.forLanguageTag(tag), quality, weighted.size()));
                }
            }
        }

        weighted.sort(Comparator.comparingDouble(Weighted::quality).reversed().thenComparingInt(Weighted::index));
        var result = new ArrayList<Locale>();
        for (var w : weighted) {
            result.add(w.locale());
        }
        return result;
    }

    /**
     * Adds a {@link Locale} to this request, which then takes precedence over
     * the {@code Accept-Language} header.
     *
     * @param locale the locale to add
     * @see #locale(Locale)
     * @since 1.11.0
     */
    public void addLocale(Locale locale) {
        if (null == locale) {
            return;
        }

        if (null == locales_) {
            locales_ = new ArrayList<>();
        }

        locales_.add(locale);
    }

    /**
     * Adds a {@link Locale} to this request.
     *
     * @param locale the locale to add
     * @return this {@code SiteRequest} instance
     * @see #addLocale(Locale)
     * @since 1.11.0
     */
    public SiteRequest locale(Locale locale) {
        addLocale(locale);

        return this;
    }

    public String getProtocol() {
        return protocol_;
    }

    /**
     * Sets the protocol of this request.
     * <p>The default protocol is {@code "HTTP/1.1"}.
     *
     * @param protocol the protocol to set
     * @see #protocol(String)
     * @since 1.11.0
     */
    public void setProtocol(String protocol) {
        if (null == protocol) throw new IllegalArgumentException("protocol can't be null");
        if (protocol.isEmpty()) throw new IllegalArgumentException("protocol can't be empty");

        protocol_ = protocol;
    }

    /**
     * Sets the protocol of this request.
     *
     * @param protocol the protocol to set
     * @return this {@code SiteRequest} instance
     * @see #setProtocol(String)
     * @since 1.11.0
     */
    public SiteRequest protocol(String protocol) {
        setProtocol(protocol);

        return this;
    }

    public String getRemoteAddr() {
        return remoteAddr_;
    }

    /**
     * Sets the remote address of this request.
     * <p>The default remote address is {@code "127.0.0.1"}.
     *
     * @param remoteAddr the remote address to set
     * @see #remoteAddr(String)
     * @since 1.11.0
     */
    public void setRemoteAddr(String remoteAddr) {
        if (null == remoteAddr) throw new IllegalArgumentException("remoteAddr can't be null");
        if (remoteAddr.isEmpty()) throw new IllegalArgumentException("remoteAddr can't be empty");

        remoteAddr_ = remoteAddr;
    }

    /**
     * Sets the remote address of this request.
     *
     * @param remoteAddr the remote address to set
     * @return this {@code SiteRequest} instance
     * @see #setRemoteAddr(String)
     * @since 1.11.0
     */
    public SiteRequest remoteAddr(String remoteAddr) {
        setRemoteAddr(remoteAddr);

        return this;
    }

    public String getRemoteUser() {
        return remoteUser_;
    }

    /**
     * Sets the remote user of this request.
     * <p>The default remote user is {@code null}.
     *
     * @param remoteUser the remote user to set
     * @see #remoteUser(String)
     * @since 1.11.0
     */
    public void setRemoteUser(String remoteUser) {
        if (null == remoteUser) throw new IllegalArgumentException("remoteUser can't be null");
        if (remoteUser.isEmpty()) throw new IllegalArgumentException("remoteUser can't be empty");

        remoteUser_ = remoteUser;
    }

    /**
     * Sets the remote user of this request.
     *
     * @param remoteUser the remote user to set
     * @return this {@code SiteRequest} instance
     * @see #setRemoteUser(String)
     * @since 1.11.0
     */
    public SiteRequest remoteUser(String remoteUser) {
        setRemoteUser(remoteUser);

        return this;
    }

    public String getRemoteHost() {
        return remoteHost_;
    }

    /**
     * Sets the remote host of this request.
     * <p>The default remote host is {@code "localhost"}.
     *
     * @param remoteHost the remote host to set
     * @see #remoteHost(String)
     * @since 1.11.0
     */
    public void setRemoteHost(String remoteHost) {
        if (null == remoteHost) throw new IllegalArgumentException("remoteHost can't be null");
        if (remoteHost.isEmpty()) throw new IllegalArgumentException("remoteHost can't be empty");

        remoteHost_ = remoteHost;
    }

    /**
     * Sets the remote host of this request.
     *
     * @param remoteHost the remote host to set
     * @return this {@code SiteRequest} instance
     * @see #setRemoteHost(String)
     * @since 1.11.0
     */
    public SiteRequest remoteHost(String remoteHost) {
        setRemoteHost(remoteHost);

        return this;
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        return null;
    }

    public HttpSession getSession(boolean create) {
        if (session_ != null && session_.isValid()) {
            return session_;
        }

        session_ = null;

        var runner = runner_;
        if (null == runner) {
            return null;
        }

        if (requestedSessionId_ != null) {
            session_ = runner.findSession(requestedSessionId_);
        }

        if (null == session_ && create) {
            var response = response_;
            // like Jetty, since the cookie of a new session can't be sent anymore
            if (response != null && response.isCommittedToListener()) {
                throw new IllegalStateException("the response has already been committed");
            }
            session_ = runner.createSession();
            if (response != null) {
                response.addCookie(runner.createSessionCookie(this, session_.getId()));
            }
        }

        return session_;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public int getServerPort() {
        var runner = runner_;
        var port = null == runner ? -1 : runner.getServerPort();
        if (port > -1) {
            return port;
        }

        var scheme = getScheme();
        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        }
        return -1;
    }

    public String getScheme() {
        var runner = runner_;
        return null == runner ? SiteRunner.DEFAULT_SCHEME : runner.getScheme();
    }

    public String getServerName() {
        var runner = runner_;
        return null == runner ? SiteRunner.DEFAULT_SERVER_NAME : runner.getServerName();
    }

    public String getContextPath() {
        var runner = runner_;
        return null == runner ? "" : runner.getContextPath();
    }

    /**
     * Indicates whether this request is secure. By default, this is the
     * case when its scheme is {@code https} or when the site runner was
     * configured to be secure.
     *
     * @return {@code true} if the request is secure; or
     * <p>{@code false} otherwise
     * @see #setSecure(boolean)
     * @since 1.11.0
     */
    public boolean isSecure() {
        if (secure_ != null) {
            return secure_;
        }

        var runner = runner_;
        return (runner != null && runner.isSecure()) || "https".equalsIgnoreCase(getScheme());
    }

    /**
     * Sets whether this request is secure.
     * <p>By default, a request is secure when its scheme is {@code https} or
     * when the site runner was configured to be secure.
     *
     * @param secure {@code true} if this request is secure; or
     *               <p>{@code false} otherwise
     * @see #secure(boolean)
     * @since 1.11.0
     */
    public void setSecure(boolean secure) {
        secure_ = secure;
    }

    /**
     * Sets whether this request is secure.
     *
     * @param secure {@code true} if this request is secure; or
     *               <p>{@code false} otherwise
     * @return this {@code SiteRequest} instance
     * @see #setSecure(boolean)
     * @since 1.11.0
     */
    public SiteRequest secure(boolean secure) {
        setSecure(secure);

        return this;
    }

    /**
     * Retrieves the underlying servlet request, which doesn't exist when a
     * site is run by a {@link SiteRunner}.
     *
     * @return {@code null}
     * @since 1.11.0
     */
    public HttpServletRequest getHttpServletRequest() {
        return null;
    }
}
