/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import java.io.*;
import java.util.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import rife.config.RifeConfig;
import rife.engine.Request;
import rife.engine.RequestMethod;
import rife.engine.UploadedFile;
import rife.engine.exceptions.MultipartFileTooBigException;
import rife.engine.exceptions.MultipartInvalidUploadDirectoryException;
import rife.engine.exceptions.MultipartRequestException;
import rife.tools.StringUtils;

/**
 * Provides a {@link Request} implementation that is suitable for testing a
 * web application outside a servlet container.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockRequest implements Request {
    private static final String
        SESSIONID_NOT_CHECKED = "not checked",
        SESSIONID_URL = "url",
        SESSIONID_COOKIE = "cookie",
        SESSIONID_NONE = "none";

    private final Map<String, String[]> parameters_ = new LinkedHashMap<>();
    private final MockHeaders headers_ = new MockHeaders();
    private RequestMethod requestMethod_ = RequestMethod.GET;
    private Map<String, UploadedFile[]> files_;
    private String body_ = null;
    private byte[] bodyAsBytes_ = null;
    private Map<String, Object> attributes_;
    private String characterEncoding_;
    private String contentType_;
    private List<Locale> locales_;
    private File uploadDirectory_;

    private MockConversation mockConversation_;
    private MockResponse mockResponse_;
    private MockSession session_;
    private String requestedSessionId_;
    private String sessionIdState_ = SESSIONID_NOT_CHECKED;

    private String protocol_ = "HTTP/1.1";
    private String remoteAddr_ = "127.0.0.1";
    private String remoteUser_;
    private String remoteHost_ = "localhost";
    private boolean secure_ = false;

    public void init() {
        String parameter_name = null;
        String[] parameter_values = null;
        for (var entry : parameters_.entrySet()) {
            parameter_name = entry.getKey();
            parameter_values = entry.getValue();

            parameters_.put(parameter_name, parameter_values);
        }
    }

    void setMockConversation(MockConversation conversation) {
        mockConversation_ = conversation;
    }

    void setMockResponse(MockResponse response) {
        mockResponse_ = response;
    }

    public RequestMethod getMethod() {
        return requestMethod_;
    }

    /**
     * Sets the method of this request.
     * <p>The method defaults to {@link RequestMethod#GET}.
     *
     * @param method the method that will be used by this request
     * @see #getMethod
     * @see #method
     * @since 1.0
     */
    public void setMethod(RequestMethod method) {
        if (null == method) throw new IllegalArgumentException("method can't be null");

        requestMethod_ = method;
    }

    /**
     * Sets the method of this request.
     *
     * @param method the method that will be used by this request
     * @return this {@code MockRequest} instance
     * @see #getMethod
     * @see #setMethod
     * @since 1.0
     */
    public MockRequest method(RequestMethod method) {
        setMethod(method);

        return this;
    }

    /**
     * Checks whether a named parameter is present in this request.
     *
     * @param name the name of the parameter to check
     * @return {@code true} if the parameter is present; or
     * <p>{@code false} otherwise
     * @see #getParameters
     * @see #setParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public boolean hasParameter(String name) {
        return parameters_.containsKey(name);
    }

    /**
     * Retrieves all the parameters of this request.
     *
     * @return a {@code Map} of the parameters with the names as the keys
     * and their value arrays as the values
     * @see #hasParameter
     * @see #setParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public Map<String, String[]> getParameters() {
        return parameters_;
    }

    /**
     * Sets a map of parameters in this request.
     *
     * @param parameters a {@code Map} of the parameters that will be set
     *                   with the names as the keys and their value arrays as the values
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public void setParameters(Map<String, String[]> parameters) {
        if (null == parameters) {
            return;
        }

        for (var parameter : parameters.entrySet()) {
            setParameter(parameter.getKey(), parameter.getValue());
        }
    }

    /**
     * Sets a map of parameters in this request.
     *
     * @param parameters a {@code Map} of the parameters that will be set
     *                   with the names as the keys and their value arrays as the values
     * @return this {@code MockRequest} instance
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public MockRequest parameters(Map<String, String[]> parameters) {
        setParameters(parameters);

        return this;
    }

    /**
     * Sets a parameter in this request.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameters
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public void setParameter(String name, String... values) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == values) throw new IllegalArgumentException("values can't be null");

        parameters_.put(name, values);
        if (files_ != null) {
            files_.remove(name);
        }
    }

    /**
     * Sets a parameter in this request.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameters
     * @see #parameter(String, String...)
     * @see #parameter(String, Object...)
     * @since 1.0
     */
    public void setParameter(String name, Object... values) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == values) throw new IllegalArgumentException("values can't be null");

        final var strings = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            strings[i] = String.valueOf(values[i]);
        }

        parameters_.put(name, strings);
        if (files_ != null) {
            files_.remove(name);
        }
    }

    /**
     * Sets a parameter in this request.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @return this {@code MockRequest} instance
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @since 1.0
     */
    public MockRequest parameter(String name, String... values) {
        setParameter(name, values);

        return this;
    }

    /**
     * Sets a parameter in this request.
     *
     * @param name   the name of the parameter
     * @param values the value array of the parameter
     * @return this {@code MockRequest} instance
     * @see #hasParameter
     * @see #getParameters
     * @see #setParameters
     * @see #setParameter(String, String...)
     * @see #setParameter(String, Object...)
     * @since 1.0
     */
    public MockRequest parameter(String name, Object... values) {
        setParameter(name, values);

        return this;
    }

    /**
     * Sets this request's body
     *
     * @param body the body as a string
     * @see #body
     * @see #getBody
     * @see #bodyAsBytes
     * @see #getBodyAsBytes
     * @since 1.0
     */
    public void setBody(String body) {
        body_ = body;
        try {
            bodyAsBytes_ = body.getBytes(getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets this request's body
     *
     * @param body the body as a string
     * @return this {@code MockRequest} instance
     * @see #setBody
     * @see #getBody
     * @see #bodyAsBytes
     * @see #getBodyAsBytes
     * @since 1.0
     */
    public MockRequest body(String body) {
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
     * @see #setBody
     * @see #body
     * @see #getBody
     * @see #bodyAsBytes
     * @see #getBodyAsBytes
     * @since 1.0
     */
    public void setBodyAsBytes(byte[] bytes) {
        body_ = null;
        bodyAsBytes_ = bytes;
    }

    /**
     * Sets this request's body as bytes.
     *
     * @param bytes the body as bytes
     * @return this {@code MockRequest} instance
     * @see #setBody
     * @see #body
     * @see #getBody
     * @see #setBodyAsBytes
     * @see #getBodyAsBytes
     * @since 1.0
     */
    public MockRequest bodyAsBytes(byte[] bytes) {
        setBodyAsBytes(bytes);

        return this;
    }

    public byte[] getBodyAsBytes() {
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

        if (!getFiles().containsKey(name)) {
            return false;
        }

        var uploaded_files = getFiles().get(name);

        if (0 == uploaded_files.length) {
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
        if (null == files) {
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

    private void checkUploadDirectory() {
        uploadDirectory_ = new File(RifeConfig.engine().getFileUploadPath());
        uploadDirectory_.mkdirs();

        if (!uploadDirectory_.exists() ||
            !uploadDirectory_.isDirectory() ||
            !uploadDirectory_.canWrite()) {
            throw new MultipartInvalidUploadDirectoryException(uploadDirectory_);
        }
    }

    /**
     * Sets a file in this request.
     *
     * @param name the parameter name of the file
     * @param file the file specification that will be uploaded
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFiles(Map)
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public void setFile(String name, MockFileUpload file) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == file) throw new IllegalArgumentException("file can't be null");

        setFiles(name, new MockFileUpload[]{file});
    }

    /**
     * Sets a map of files in this request.
     *
     * @param files a {@code Map} of the files that will be set with the
     *              names as the keys and their file upload specifications as the values
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFile(String, MockFileUpload)
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public void setFiles(Map<String, MockFileUpload[]> files) {
        if (null == files ||
            files.isEmpty()) {
            return;
        }

        for (var file : files.entrySet()) {
            setFiles(file.getKey(), file.getValue());
        }
    }

    /**
     * Sets files in this request.
     *
     * @param name  the parameter name of the file
     * @param files the file specifications that will be uploaded
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFile(String, MockFileUpload)
     * @see #setFiles(Map)
     * @since 1.0
     */
    public void setFiles(String name, MockFileUpload[] files) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");
        if (null == files) throw new IllegalArgumentException("files can't be null");

        checkUploadDirectory();

        if (null == files_) {
            files_ = new LinkedHashMap<>();
        }

        var uploaded_files = new UploadedFile[files.length];
        for (var i = 0; i < files.length; i++) {
            var uploaded_file = new UploadedFile(files[i].getFileName(), files[i].getContentType());

            try {
                var tmp_file = File.createTempFile("upl", ".tmp", uploadDirectory_);
                var output_stream = new FileOutputStream(tmp_file);
                var output = new BufferedOutputStream(output_stream, 8 * 1024); // 8K

                var input_stream = files[i].getInputStream();
                long downloaded_size = 0;

                var buffer = new byte[1024];
                var return_value = -1;

                return_value = input_stream.read(buffer);
                while (-1 != return_value) {
                    output.write(buffer, 0, return_value);

                    // increase size count
                    if (output != null &&
                        RifeConfig.engine().getFileUploadSizeCheck()) {
                        downloaded_size += return_value;

                        if (downloaded_size > RifeConfig.engine().getFileUploadSizeLimit()) {
                            uploaded_file.setSizeExceeded(true);
                            output.close();
                            output = null;
                            tmp_file.delete();
                            tmp_file = null;
                            if (RifeConfig.engine().getFileUploadSizeException()) {
                                throw new MultipartFileTooBigException(name, RifeConfig.engine().getFileUploadSizeLimit());
                            }
                        }
                    }

                    return_value = input_stream.read(buffer);
                }

                if (output != null) {
                    output.flush();
                    output.close();
                    output_stream.close();
                }

                if (tmp_file != null) {
                    uploaded_file.setTempFile(tmp_file);
                }

                uploaded_files[i] = uploaded_file;
            } catch (IOException e) {
                throw new MultipartRequestException(e);
            }
        }

        files_.put(name, uploaded_files);
        parameters_.remove(name);
    }

    /**
     * Sets a file in this request.
     *
     * @param name the parameter name of the file
     * @param file the file specification that will be uploaded
     * @return this {@code MockRequest} instance
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFile(String, MockFileUpload)
     * @see #setFiles(Map)
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public MockRequest file(String name, MockFileUpload file) {
        setFile(name, file);

        return this;
    }

    /**
     * Sets a map of files in this request.
     *
     * @param files a {@code Map} of the files that will be set with the
     *              names as the keys and their file upload specifications as the values
     * @return this {@code MockRequest} instance
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFile(String, MockFileUpload)
     * @see #setFiles(Map)
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public MockRequest files(Map<String, MockFileUpload[]> files) {
        setFiles(files);

        return this;
    }

    /**
     * Sets files in this request.
     *
     * @param name  the parameter name of the file
     * @param files the file specifications that will be uploaded
     * @return this {@code MockRequest} instance
     * @see #hasFile
     * @see #getFile
     * @see #getFiles
     * @see #setFile(String, MockFileUpload)
     * @see #setFiles(Map)
     * @see #setFiles(String, MockFileUpload[])
     * @since 1.0
     */
    public MockRequest files(String name, MockFileUpload[] files) {
        setFiles(name, files);

        return this;
    }

    public String getServerRootUrl(int port) {
        var server_root = new StringBuilder();
        server_root.append(getScheme());
        server_root.append("://");
        server_root.append(getServerName());
        if (port <= -1) {
            port = getServerPort();
        }
        if (port != 80) {
            server_root.append(":");
            server_root.append(port);
        }
        return server_root.toString();
    }

    public boolean hasCookie(String name) {
        return mockConversation_.hasCookie(name);
    }

    public Cookie getCookie(String name) {
        return mockConversation_.getCookie(name);
    }

    public Cookie[] getCookies() {
        return mockConversation_.getCookies();
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
            return Collections.enumeration(new ArrayList<>());
        }

        return Collections.enumeration(attributes_.keySet());
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

    public String getCharacterEncoding() {
        return characterEncoding_;
    }

    /**
     * Set the character encoding of this request.
     *
     * @param encoding the name of the character encoding
     * @since 1.0
     */
    public void setCharacterEncoding(String encoding) {
        if (null == encoding) throw new IllegalArgumentException("encoding can't be null");
        if (encoding.isEmpty()) throw new IllegalArgumentException("encoding can't be empty");

        characterEncoding_ = encoding;
    }

    /**
     * Set the character encoding of this request.
     *
     * @param encoding the name of the character encoding
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest characterEncoding(String encoding) {
        setCharacterEncoding(encoding);

        return this;
    }

    public String getContentType() {
        return contentType_;
    }

    /**
     * Set the content type of this request.
     *
     * @param type the content type
     * @since 1.0
     */
    public void setContentType(String type) {
        if (null == type) throw new IllegalArgumentException("type can't be null");
        if (type.isEmpty()) throw new IllegalArgumentException("type can't be empty");

        contentType_ = type;
    }

    /**
     * Set the content type of this request.
     *
     * @param type the content type
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest contentType(String type) {
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
     * request headers to have multiple values.
     *
     * @param name  the name of the header to set
     * @param value the additional header value
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest addHeader(String name, String value) {
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
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest addDateHeader(String name, long value) {
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
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest addIntHeader(String name, int value) {
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
     * @since 1.0
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
     * @since 1.0
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
     * @see #setDateHeader
     * @since 1.0
     */
    public MockRequest dateHeader(String name, long value) {
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
     * @since 1.0
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
     * @see #setDateHeader
     * @since 1.0
     */
    public MockRequest header(String name, String value) {
        setHeader(name, value);

        return this;
    }

    /**
     * Sets a request header with the given name and integer value. If the
     * header had already been set, the new value overwrites the previous one.
     * The containsHeader method can be used to test for the presence of a
     * header before setting its value.
     *
     * @param name  the name of the header to set
     * @param value the assigned integer value
     * @since 1.0
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
     * @see #setDateHeader
     * @since 1.0
     */
    public MockRequest intHeader(String name, int value) {
        setIntHeader(name, value);

        return this;
    }

    /**
     * Removes a request header with the given name.
     *
     * @param name the name of the header to remove
     * @since 1.0
     */
    public void removeHeader(String name) {
        headers_.removeHeader(name);
    }

    public Locale getLocale() {
        if (null == locales_ ||
            locales_.isEmpty()) {
            return Locale.getDefault();
        }

        return locales_.get(0);
    }

    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(Objects.requireNonNullElseGet(locales_, () -> new ArrayList() {{
            add(Locale.getDefault());
        }}));

    }

    /**
     * Adds a {@link Locale} to this request.
     *
     * @param locale the locale to add
     * @since 1.0
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
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest locale(Locale locale) {
        addLocale(locale);

        return this;
    }

    public String getProtocol() {
        return protocol_;
    }

    /**
     * Set the protocol of this request.
     * <p>The default protocol is {@code "HTTP/1.1"}.
     *
     * @param protocol the protocol to set
     * @since 1.0
     */
    public void setProtocol(String protocol) {
        if (null == protocol) throw new IllegalArgumentException("protocol can't be null");
        if (protocol.isEmpty()) throw new IllegalArgumentException("protocol can't be empty");

        protocol_ = protocol;
    }

    /**
     * Set the protocol of this request.
     *
     * @param protocol the protocol to set
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest protocol(String protocol) {
        setProtocol(protocol);

        return this;
    }

    public String getRemoteAddr() {
        return remoteAddr_;
    }

    /**
     * Set the remote address of this request.
     * <p>The default remote address is "{@code 127.0.0.1"}.
     *
     * @param remoteAddr the remote address to set
     * @since 1.0
     */
    public void setRemoteAddr(String remoteAddr) {
        if (null == remoteAddr) throw new IllegalArgumentException("remoteAddr can't be null");
        if (remoteAddr.isEmpty()) throw new IllegalArgumentException("remoteAddr can't be empty");

        remoteAddr_ = remoteAddr;
    }

    /**
     * Set the remote address of this request.
     *
     * @param remoteAddr the remote address to set
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest remoteAddr(String remoteAddr) {
        setRemoteAddr(remoteAddr);

        return this;
    }

    public String getRemoteUser() {
        return remoteUser_;
    }

    /**
     * Set the remote user of this request.
     * <p>The default remote user is {@code null}.
     *
     * @param remoteUser the remote user to set
     * @since 1.0
     */
    public void setRemoteUser(String remoteUser) {
        if (null == remoteUser) throw new IllegalArgumentException("remoteUser can't be null");
        if (remoteUser.isEmpty()) throw new IllegalArgumentException("remoteUser can't be empty");

        remoteUser_ = remoteUser;
    }

    /**
     * Set the remote user of this request.
     *
     * @param remoteUser the remote user to set
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest remoteUser(String remoteUser) {
        setRemoteUser(remoteUser);

        return this;
    }

    public String getRemoteHost() {
        return remoteHost_;
    }

    /**
     * Set the remote host of this request.
     * <p>The default remote host is "{@code localhost}".
     *
     * @param remoteHost the remote host to set
     * @since 1.0
     */
    public void setRemoteHost(String remoteHost) {
        if (null == remoteHost) throw new IllegalArgumentException("remoteHost can't be null");
        if (remoteHost.isEmpty()) throw new IllegalArgumentException("remoteHost can't be empty");

        remoteHost_ = remoteHost;
    }

    /**
     * Set the remote host of this request.
     *
     * @param remoteHost the remote host to set
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest remoteHost(String remoteHost) {
        setRemoteHost(remoteHost);

        return this;
    }

    public String getScheme() {
        return mockConversation_.getScheme();
    }

    public String getServerName() {
        return mockConversation_.getServerName();
    }

    public int getServerPort() {
        return mockConversation_.getServerPort();
    }

    public String getContextPath() {
        return mockConversation_.getContextPath();
    }

    public boolean isSecure() {
        return secure_;
    }

    /**
     * Set whether this request is secure.
     * <p>A request is not secure by default.
     *
     * @param secure {@code true} if this request is secure; or
     *               <p>{@code false} otherwise
     * @since 1.0
     */
    public void setSecure(boolean secure) {
        secure_ = secure;
    }

    /**
     * Set whether this request is secure.
     *
     * @param secure {@code true} if this request is secure; or
     *               <p>{@code false} otherwise
     * @return this {@code MockRequest} instance
     * @since 1.0
     */
    public MockRequest secure(boolean secure) {
        setSecure(secure);

        return this;
    }

    public HttpSession getSession(boolean create) {
        if (session_ != null && session_.isValid()) {
            return session_;
        }

        session_ = null;

        var id = getRequestedSessionId();

        if (id != null) {
            session_ = mockConversation_.getSession(id);
            if (null == session_ && !create) {
                return null;
            }
        }

        if (session_ == null && create) {
            session_ = newSession();
        }

        return session_;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    void setRequestedSessionId(String pathParams) {
        requestedSessionId_ = null;

        // try cookies first
        var cookies = getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (MockConversation.SESSION_ID_COOKIE.equalsIgnoreCase(cookie.getName())) {
                    if (requestedSessionId_ != null) {
                        // Multiple jsessionid cookies. Probably due to
                        // multiple paths and/or domains. Pick the first
                        // known session or the last defined cookie.
                        if (mockConversation_.getSession(requestedSessionId_) != null) {
                            break;
                        }
                    }

                    requestedSessionId_ = cookie.getValue();
                    sessionIdState_ = SESSIONID_COOKIE;
                }
            }
        }

        // check if there is an url encoded session param.
        if (pathParams != null && pathParams.startsWith(MockConversation.SESSION_ID_URL)) {
            var id = pathParams.substring(MockConversation.SESSION_ID_URL.length() + 1);

            if (null == requestedSessionId_) {
                requestedSessionId_ = id;
                sessionIdState_ = SESSIONID_URL;
            }
        }

        if (null == requestedSessionId_) {
            sessionIdState_ = SESSIONID_NONE;
        }
    }

    String getRequestedSessionId() {
        return requestedSessionId_;
    }

    MockSession newSession() {
        var session = mockConversation_.newHttpSession();
        var cookie = new Cookie(MockConversation.SESSION_ID_COOKIE, session.getId());
        cookie.setPath("/");
        cookie.setMaxAge(-1);

        mockResponse_.addCookie(cookie);

        return session;
    }

    boolean isRequestedSessionIdFromCookie() {
        return SESSIONID_COOKIE.equals(sessionIdState_);
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        return null;
    }

    public HttpServletRequest getHttpServletRequest() {
        return null;
    }
}
