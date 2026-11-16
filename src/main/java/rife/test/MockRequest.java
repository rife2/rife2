/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import rife.config.RifeConfig;
import rife.engine.Request;
import rife.engine.RequestMethod;
import rife.engine.SiteRequest;
import rife.engine.SiteResponse;
import rife.engine.UploadedFile;
import rife.engine.exceptions.MultipartFileTooBigException;
import rife.engine.exceptions.MultipartInvalidUploadDirectoryException;
import rife.engine.exceptions.MultipartRequestException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Provides a {@link Request} implementation that is suitable for testing a
 * web application outside a servlet container.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockRequest extends SiteRequest {
    private File uploadDirectory_;

    private MockConversation mockConversation_;

    /**
     * Creates a new {@code GET} request that isn't secure unless
     * {@link #setSecure(boolean)} is used.
     *
     * @since 1.0
     */
    public MockRequest() {
        setSecure(false);
    }

    public void init() {
        String parameter_name = null;
        String[] parameter_values = null;
        for (var entry : getParameters().entrySet()) {
            parameter_name = entry.getKey();
            parameter_values = entry.getValue();

            getParameters().put(parameter_name, parameter_values);
        }
    }

    void setMockConversation(MockConversation conversation) {
        mockConversation_ = conversation;
    }

    @Override
    protected SiteResponse createResponse() {
        if (null == mockConversation_) throw new IllegalStateException("a MockRequest can only be used with a MockConversation");

        return new MockResponse(mockConversation_, this);
    }

    // the body isn't parsed, and the parameters of the query string are only
    // added when the request doesn't have them yet
    @Override
    protected void parseParameters(String queryString) {
        if (null == queryString) {
            return;
        }

        var parameters = MockConversation.extractQueryParameters(queryString);
        if (parameters != null) {
            for (var entry : parameters.entrySet()) {
                if (!hasParameter(entry.getKey())) {
                    setParameter(entry.getKey(), entry.getValue());
                }
            }
        }
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
        return getParameters().containsKey(name);
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

        getParameters().put(name, values);
        if (getFiles() != null) {
            getFiles().remove(name);
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

        getParameters().put(name, strings);
        if (getFiles() != null) {
            getFiles().remove(name);
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

    /**
     * Sets the body of this request as a stream.
     *
     * @param stream the stream with the body
     * @return this {@code MockRequest} instance
     * @see #setBodyAsStream
     * @since 1.11.0
     */
    public MockRequest bodyAsStream(InputStream stream) {
        setBodyAsStream(stream);

        return this;
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

        if (null == getFiles()) {
            setUploadedFiles(new LinkedHashMap<>());
        }

        var uploaded_files = new UploadedFile[files.length];
        for (var i = 0; i < files.length; i++) {
            var file_name = files[i].getFileName();
            if (file_name != null && file_name.isEmpty()) {
                // empty filename, probably an "empty" file param
                file_name = null;
            }
            var uploaded_file = new UploadedFile(file_name, files[i].getContentType());

            try {
                var tmp_file = File.createTempFile("upl", ".tmp", uploadDirectory_);
                var output_stream = Files.newOutputStream(tmp_file.toPath());
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

        getFiles().put(name, uploaded_files);
        getParameters().remove(name);
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
        server_root.append(getScheme())
            .append("://")
            .append(getServerName());
        if (port <= -1) {
            port = getServerPort();
        }
        if (port != 80) {
            server_root.append(':')
                .append(port);
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
        super.addHeader(name, value);

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
        super.addDateHeader(name, value);

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
        super.addIntHeader(name, value);

        return this;
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
     * Marks this request as coming from htmx by setting the
     * {@code HX-Request} header that htmx sends along with every request.
     * <p>This makes it easy to test the htmx side of an element that serves
     * both a full page and a fragment, so that a single test can check both:
     * <pre>var full     = m.doRequest("/books");
     * var fragment = m.doRequest("/books", new MockRequest().htmx());</pre>
     * <p>You can add the other {@code HX-*} request headers by chaining
     * {@link #header}, for instance
     * {@code new MockRequest().htmx().header("HX-Target", "list")}.
     *
     * @return this {@code MockRequest} instance, so that method calls can
     * be chained
     * @see #header
     * @see rife.engine.Context#isHxRequest()
     * @since 1.10
     */
    public MockRequest htmx() {
        return header("HX-Request", "true");
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

}
