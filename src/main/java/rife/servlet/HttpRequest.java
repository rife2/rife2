/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import rife.config.RifeConfig;
import rife.engine.Request;
import rife.engine.RequestMethod;
import rife.engine.SiteRunner;
import rife.engine.UploadedFile;
import rife.engine.exceptions.EngineException;
import rife.engine.exceptions.InvalidFormException;
import rife.tools.ArrayUtils;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HttpRequest implements Request {
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final HttpServletRequest request_;

    private Map<String, String[]> parameters_ = null;
    private Map<String, UploadedFile[]> files_ = null;
    private String body_ = null;
    private byte[] bodyAsBytes_ = null;
    private boolean formBodyPending_ = false;

    public HttpRequest(HttpServletRequest request)
    throws EngineException {
        assert request != null;
        request_ = request;
    }

    public void init() {
        if (MultipartRequest.isValidContentType(request_.getContentType())) {
            var multipart_request = new MultipartRequest(request_);
            parameters_ = multipart_request.getParameterMap();
            files_ = multipart_request.getFileMap();
        } else {
            parameters_ = new LinkedHashMap<>();

            try {
                request_.setCharacterEncoding(RifeConfig.engine().getRequestEncoding().toString());
            } catch (UnsupportedEncodingException e) {
                // should never happen
            }

            var parameter_names = request_.getParameterNames();
            String parameter_name = null;
            String[] parameter_values = null;
            while (parameter_names.hasMoreElements()) {
                parameter_name = parameter_names.nextElement();
                parameter_values = request_.getParameterValues(parameter_name);

                parameters_.put(parameter_name, parameter_values);
            }

            // servlet containers don't parse the forms of PATCH requests,
            // while htmx sends PATCH values in the body too; it's read once
            // a route needs the parameters, so that the body of a request
            // that's passed on stays untouched
            formBodyPending_ = RequestMethod.PATCH.name().equals(request_.getMethod()) &&
                isFormContentType(request_.getContentType());
        }
    }

    private static boolean isFormContentType(String type) {
        return type != null &&
            type.toLowerCase(Locale.ENGLISH).startsWith(FORM_CONTENT_TYPE);
    }

    // reads at most one byte more than the limit, so that a large body isn't kept
    private void addFormBody() {
        if (null == bodyAsBytes_) {
            try {
                bodyAsBytes_ = request_.getInputStream().readNBytes(SiteRunner.DEFAULT_MAX_FORM_CONTENT_SIZE + 1);
            } catch (IOException e) {
                throw new EngineException(e);
            }
        }
        if (bodyAsBytes_.length > SiteRunner.DEFAULT_MAX_FORM_CONTENT_SIZE) {
            bodyAsBytes_ = new byte[0];
            throw new InvalidFormException("the form is larger than " + SiteRunner.DEFAULT_MAX_FORM_CONTENT_SIZE + " bytes");
        }

        var encoding = RifeConfig.engine().getRequestEncoding();
        var form = new LinkedHashMap<String, String[]>();
        for (var pair : StringUtils.split(new String(bodyAsBytes_, StandardCharsets.ISO_8859_1), "&")) {
            if (pair.isEmpty()) {
                continue;
            }

            var separator = pair.indexOf('=');
            var name = decodeFormComponent(-1 == separator ? pair : pair.substring(0, separator), encoding);
            var value = -1 == separator ? "" : decodeFormComponent(pair.substring(separator + 1), encoding);
            if (name.isEmpty()) {
                continue;
            }

            var values = form.get(name);
            form.put(name, null == values ? new String[]{value} : ArrayUtils.join(values, value));
        }
        if (form.size() > SiteRunner.DEFAULT_MAX_FORM_PARAMETERS) {
            throw new InvalidFormException("the form has more than " + SiteRunner.DEFAULT_MAX_FORM_PARAMETERS + " parameters");
        }

        for (var entry : form.entrySet()) {
            var values = parameters_.get(entry.getKey());
            parameters_.put(entry.getKey(), null == values ? entry.getValue() : ArrayUtils.join(values, entry.getValue()));
        }
    }

    // like Jetty, a malformed escape makes the whole form invalid
    private static String decodeFormComponent(String component, Charset encoding) {
        var raw = component.getBytes(StandardCharsets.ISO_8859_1);
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

    @Override
    public RequestMethod getMethod() {
        return RequestMethod.valueOf(request_.getMethod());
    }

    @Override
    public Map<String, String[]> getParameters() {
        if (formBodyPending_) {
            formBodyPending_ = false;
            addFormBody();
        }
        return parameters_;
    }

    @Override
    public String getBody() {
        if (body_== null) {
            body_ = StringUtils.toString(getBodyAsBytes(), request_.getCharacterEncoding());
        }

        return body_;
    }

    @Override
    public byte[] getBodyAsBytes() {
        if (bodyAsBytes_ == null) {
            try {
                bodyAsBytes_ = FileUtils.readBytes(request_.getInputStream());
            } catch (IOException e) {
                throw new EngineException(e);
            }
        }

        return bodyAsBytes_;
    }

    @Override
    public Map<String, UploadedFile[]> getFiles() {
        return files_;
    }

    @Override
    public boolean hasFile(String name) {
        assert name != null;
        assert !name.isEmpty();

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

    @Override
    public UploadedFile getFile(String name) {
        assert name != null;
        assert !name.isEmpty();

        if (null == getFiles()) {
            return null;
        }

        var files = getFiles().get(name);
        if (null == files) {
            return null;
        }

        return files[0];
    }

    @Override
    public UploadedFile[] getFiles(String name) {
        assert name != null;
        assert !name.isEmpty();

        if (null == getFiles()) {
            return null;
        }

        return getFiles().get(name);
    }

    @Override
    public boolean hasCookie(String name) {
        assert name != null;
        assert !name.isEmpty();

        var cookies = request_.getCookies();

        if (null == cookies) {
            return false;
        }

        for (var cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Cookie getCookie(String name) {
        assert name != null;
        assert !name.isEmpty();

        var cookies = request_.getCookies();

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

    // simply wrapped methods
    @Override
    public Object getAttribute(String name) {
        return request_.getAttribute(name);
    }

    @Override
    public Cookie[] getCookies() {
        return request_.getCookies();
    }

    @Override
    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return request_.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return request_.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return request_.getContentType();
    }

    @Override
    public long getDateHeader(String name) {
        return request_.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return request_.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request_.getHeaderNames();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return request_.getHeaders(name);
    }

    @Override
    public int getIntHeader(String name) {
        return request_.getIntHeader(name);
    }

    @Override
    public Locale getLocale() {
        return request_.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return request_.getLocales();
    }

    @Override
    public String getProtocol() {
        return request_.getProtocol();
    }

    @Override
    public String getRemoteAddr() {
        return request_.getRemoteAddr();
    }

    @Override
    public String getRemoteUser() {
        return request_.getRemoteUser();
    }

    @Override
    public String getRemoteHost() {
        return request_.getRemoteHost();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String url) {
        return request_.getRequestDispatcher(url);
    }

    @Override
    public HttpSession getSession() {
        return request_.getSession();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return request_.getSession(create);
    }

    @Override
    public int getServerPort() {
        return request_.getServerPort();
    }

    @Override
    public String getScheme() {
        var proxy_scheme = proxyRootScheme();
        if (proxy_scheme != null) {
            return proxy_scheme;
        }

        return request_.getScheme();
    }

    // behind a reverse proxy the container only sees the proxied request, the
    // configured proxy root URL is what browsers actually use
    private static String proxyRootScheme() {
        var proxy_root_url = RifeConfig.engine().getProxyRootUrl();
        if (proxy_root_url == null) {
            return null;
        }

        var separator = proxy_root_url.indexOf("://");
        if (separator <= 0) {
            return null;
        }

        return proxy_root_url.substring(0, separator).toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getServerName() {
        return request_.getServerName();
    }

    @Override
    public String getContextPath() {
        return request_.getContextPath();
    }

    @Override
    public boolean isSecure() {
        var proxy_scheme = proxyRootScheme();
        if (proxy_scheme != null) {
            return proxy_scheme.equals("https");
        }

        return request_.isSecure();
    }

    @Override
    public void removeAttribute(String name) {
        request_.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object object) {
        request_.setAttribute(name, object);
    }

    @Override
    public String getServerRootUrl(int port) {
        if (RifeConfig.engine().getProxyRootUrl() != null) {
            return RifeConfig.engine().getProxyRootUrl();
        }

        var server_root = new StringBuilder();
        server_root.append(getScheme())
            .append("://")
            .append(getServerName());
        if (port <= -1) {
            port = getServerPort();
        }
        if (port != 80 && port != 443) {
            server_root.append(':')
                .append(port);
        }
        return server_root.toString();
    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        return request_;
    }
}
