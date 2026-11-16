/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import rife.engine.exceptions.EngineException;
import rife.tools.ExceptionUtils;
import rife.tools.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * The response of a site to a {@link SiteRequest}.
 * <p>Without a listener, the content is kept in memory, and you can get it
 * with {@link #getBytes()} and {@link #getText()} once the request has been
 * handled. With a {@link SiteResponseListener}, the content is given to the
 * listener while it's being written, so that it isn't kept.
 * <p>{@link #getHttpHeaders()} provides the headers as they would go over
 * HTTP, with a {@code Set-Cookie} header for each cookie.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SiteRunner
 * @since 1.11.0
 */
public class SiteResponse extends AbstractResponse {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LANGUAGE = "Content-Language";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_SET_COOKIE = "Set-Cookie";

    private final SiteRequest request_;
    private final SiteHeaders headers_ = new SiteHeaders();
    private final Map<String, Cookie> cookies_ = new LinkedHashMap<>();
    private final ByteArrayOutputStream buffer_ = new ByteArrayOutputStream();
    private final BodyStream bodyStream_ = new BodyStream();
    private final AtomicBoolean committed_ = new AtomicBoolean(false);
    private final AtomicBoolean ended_ = new AtomicBoolean(false);
    private final AtomicBoolean finished_ = new AtomicBoolean(false);
    private final AtomicBoolean listenerEnded_ = new AtomicBoolean(false);
    private final AtomicInteger listenerCalls_ = new AtomicInteger(0);
    private final AtomicLong written_ = new AtomicLong(0);

    // only used by the thread that runs the element
    private String mimeType_;
    private String characterEncoding_;
    private String reason_;
    private Locale locale_;
    private PrintWriter writer_;

    private volatile int status_ = 200;
    private volatile SiteResponseListener listener_ = null;
    private volatile boolean handled_ = true;
    private volatile boolean cancelled_ = false;

    /**
     * Creates the response of a request.
     *
     * @param request the request of the response
     * @since 1.11.0
     */
    protected SiteResponse(SiteRequest request) {
        super(request);

        request_ = request;
    }

    void setListener(SiteResponseListener listener) {
        listener_ = listener;
    }

    SiteResponseListener getListener() {
        return listener_;
    }

    /**
     * Indicates whether the request was handled by the site or by its static
     * files.
     * <p>When no route handled the request and no static file was found, the
     * status is {@code 404} and this is {@code false}, so that your
     * application can still answer the request in its own way. When an
     * element sends a {@code 404} itself, this remains {@code true}.
     *
     * @return {@code true} if the request was handled; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    public boolean isHandled() {
        return handled_;
    }

    void markUnhandled() {
        handled_ = false;
        clearBuffer();
        setStatus(404);
    }

    void commit() {
        if (!committed_.compareAndSet(false, true)) {
            return;
        }

        var listener = listener_;
        if (null == listener || !startListenerCall()) {
            return;
        }

        var failed = false;
        try {
            listener.committed(this);
        } catch (RuntimeException e) {
            failed = true;
        } finally {
            endListenerCall();
        }
        if (failed) {
            cancel();
            return;
        }
        // the listener received the cookies with the headers, unless it
        // cancelled the request right away
        if (!cancelled_) {
            storeCookies();
        }
    }

    // a call only starts when the request isn't cancelled, and the one that
    // ends last after a cancel gives the listener its final call
    private boolean startListenerCall() {
        listenerCalls_.incrementAndGet();
        if (cancelled_) {
            endListenerCall();
            return false;
        }
        return true;
    }

    private void endListenerCall() {
        if (0 == listenerCalls_.decrementAndGet() && cancelled_) {
            notifyCancelled();
        }
    }

    private void notifyCancelled() {
        var listener = listener_;
        if (listener != null && listenerEnded_.compareAndSet(false, true)) {
            try {
                listener.cancelled();
            } catch (RuntimeException e) {
                // a failure in the application's listener shouldn't affect the site
            }
        }
    }

    boolean isCommittedToListener() {
        return listener_ != null && committed_.get();
    }

    // a failure in the application's cookie store shouldn't fail the response
    private void storeCookies() {
        var runner = request_.getRunner();
        var jar = null == runner ? null : runner.getCookieJar();
        if (jar != null) {
            try {
                jar.store(request_, getNewCookies());
            } catch (RuntimeException e) {
                Logger.getLogger("rife.engine").warning("Error while storing cookies\n" + ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    // ends a response that isn't detached once its request was handled
    void finish() {
        if (writer_ != null) {
            writer_.flush();
        }
        end();
    }

    private void end() {
        // a detached stream can be closed by its element and by the runner
        if (!ended_.compareAndSet(false, true)) {
            return;
        }

        close();
        commit();
        if (null == listener_ && !cancelled_) {
            // without a listener, the cookies are only final now
            storeCookies();
        }
        complete(null);
    }

    void fail(Throwable exception) {
        complete(exception);
    }

    private void complete(Throwable exception) {
        if (!finished_.compareAndSet(false, true)) {
            return;
        }

        var runner = request_.getRunner();
        if (runner != null) {
            runner.detachedResponseEnded(this);
        }

        var listener = listener_;
        if (null == listener || !startListenerCall()) {
            return;
        }

        try {
            if (listenerEnded_.compareAndSet(false, true)) {
                if (exception != null) {
                    listener.failed(exception);
                } else {
                    listener.completed();
                }
            }
        } catch (RuntimeException e) {
            // a failure in the application's listener shouldn't affect the site
        } finally {
            endListenerCall();
        }
    }

    /**
     * Indicates whether this response has ended, either normally, with a
     * failure, or because its request was cancelled.
     *
     * @return {@code true} if the response has ended; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    public boolean isFinished() {
        return finished_.get();
    }

    /**
     * Indicates whether this response is a detached event stream, which
     * stays open after its element returned.
     *
     * @return {@code true} if the response is a detached event stream; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    @Override
    public boolean isDetached() {
        return super.isDetached();
    }

    // doesn't wait for an event that's still being written, since the thread
    // that cancels can be the one that's blocked in that listener call
    void cancel() {
        cancelled_ = true;
        finished_.set(true);
        var connection = detachedConnection();
        if (connection != null) {
            connection.abandon();
        }
        var runner = request_.getRunner();
        if (runner != null) {
            runner.detachedResponseEnded(this);
        }
        if (0 == listenerCalls_.get()) {
            notifyCancelled();
        }
    }

    boolean isCancelled() {
        return cancelled_;
    }

    @Override
    void detachedConnectionClosed() {
        try {
            end();
        } catch (RuntimeException e) {
            // the response still ends when the connection is gone
            complete(null);
        }
    }

    /**
     * Retrieves an array of all the bytes that have been written to this
     * response.
     * <p>For a {@code HEAD} request this is empty, since a servlet container
     * doesn't send the content of the response. It's also empty when the
     * response is given to a listener, since the content isn't kept then.
     *
     * @return an array of bytes with the response content
     * @see #getText
     * @since 1.11.0
     */
    public byte[] getBytes() {
        if (request_.getMethod() == RequestMethod.HEAD) {
            return new byte[0];
        }
        return buffer_.toByteArray();
    }

    /**
     * Retrieves the content of this response as text, with the character
     * encoding of the response.
     *
     * @return the response content as text
     * @see #getBytes
     * @since 1.11.0
     */
    public String getText() {
        var charset = characterEncoding_;
        if (null == charset) {
            charset = StringUtils.ENCODING_ISO_8859_1;
        }

        return new String(getBytes(), Charset.forName(charset));
    }

    /**
     * Retrieves the content type of the response, as it's set in the
     * {@code Content-Type} header.
     *
     * @return the content type; or
     * <p>{@code null} if it wasn't set
     * @since 1.11.0
     */
    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    protected void _setContentType(String contentType) {
        if (null == contentType) {
            mimeType_ = null;
            headers_.removeHeader(HEADER_CONTENT_TYPE);
            return;
        }

        var params = contentType.indexOf(';');
        if (params > 0) {
            mimeType_ = contentType.substring(0, params).trim();

            var charset = contentType.indexOf("charset=", params);
            if (charset >= 0) {
                charset += 8;
                var end = charset;
                while (end < contentType.length() && contentType.charAt(end) != ';' && contentType.charAt(end) != ' ') {
                    end++;
                }
                characterEncoding_ = unquote(contentType.substring(charset, end));
            } else if (characterEncoding_ != null) {
                contentType += ";charset=" + characterEncoding_;
            }
        } else {
            mimeType_ = contentType;
            if (characterEncoding_ != null) {
                contentType += ";charset=" + characterEncoding_;
            }
        }

        setHeader(HEADER_CONTENT_TYPE, contentType);
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public String getCharacterEncoding() {
        return characterEncoding_;
    }

    public void setContentLength(int length) {
        setIntHeader(HEADER_CONTENT_LENGTH, length);
    }

    /**
     * Retrieves the content length that was set.
     *
     * @return the content length; or
     * <p>{@code -1} if it wasn't set
     * @since 1.11.0
     */
    public int getContentLength() {
        return getIntHeader(HEADER_CONTENT_LENGTH);
    }

    public void sendRedirect(String location) {
        clearBuffer();

        setStatus(302);
        setHeader(HEADER_LOCATION, location);
    }

    protected OutputStream _getOutputStream() {
        return bodyStream_;
    }

    // the stream that static files are written to, without compression
    OutputStream rawOutputStream() {
        markOutputStarted();
        return bodyStream_;
    }

    public void addCookie(Cookie cookie) {
        if (null == cookie) {
            return;
        }

        var runner = request_.getRunner();
        if (null == runner || runner.isValidatingCookies()) {
            validateCookie(cookie);
        }

        synchronized (cookies_) {
            cookies_.put(cookieId(cookie), cookie);
        }
    }

    private static String cookieId(Cookie cookie) {
        return (null == cookie.getDomain() ? "" : cookie.getDomain()) + "\n" +
               (null == cookie.getPath() || cookie.getPath().isEmpty() ? "/" : cookie.getPath()) + "\n" +
               cookie.getName();
    }

    // rejects a cookie that can't be sent in a Set-Cookie header without
    // changing its meaning, like Jetty does
    private static void validateCookie(Cookie cookie) {
        if (!isValidCookieValue(cookie.getValue())) {
            throw new IllegalArgumentException("the value of cookie '" + cookie.getName() + "' has characters that can't be sent");
        }
        for (var attribute : cookie.getAttributes().entrySet()) {
            if (!isValidAttributeValue(attribute.getValue())) {
                throw new IllegalArgumentException("the " + attribute.getKey() + " attribute of cookie '" + cookie.getName() + "' has characters that can't be sent");
            }
        }
    }

    // the cookie-octet characters of RFC 6265, optionally in double quotes
    static boolean isValidCookieValue(String value) {
        if (null == value) {
            return true;
        }
        var start = 0;
        var end = value.length();
        if (end >= 2 && value.charAt(0) == '"' && value.charAt(end - 1) == '"') {
            start = 1;
            end -= 1;
        }
        for (var i = start; i < end; i++) {
            var c = value.charAt(i);
            if (c < 0x21 || c > 0x7e || c == '"' || c == ',' || c == ';' || c == '\\') {
                return false;
            }
        }
        return true;
    }

    static boolean isValidAttributeValue(String value) {
        if (null == value) {
            return true;
        }
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            if (c < 0x20 || c == 0x7f || c == ';') {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the cookies that were added to this response. A later cookie
     * with the same name, domain and path replaces an earlier one.
     *
     * @return the cookies of the response
     * @since 1.11.0
     */
    public List<Cookie> getNewCookies() {
        synchronized (cookies_) {
            return new ArrayList<>(cookies_.values());
        }
    }

    /**
     * Retrieves the headers of this response as they would go over HTTP,
     * with a {@code Set-Cookie} header for each cookie. The header names
     * are case-insensitive.
     * <p>When there's no listener, the {@code Content-Length} header is also
     * provided once the response is finished.
     *
     * @return the names of the headers with their values
     * @since 1.11.0
     */
    public Map<String, List<String>> getHttpHeaders() {
        var result = new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(headers_.getAll());
        for (var cookie : getNewCookies()) {
            result.computeIfAbsent(HEADER_SET_COOKIE, k -> new ArrayList<>()).add(formatSetCookie(cookie));
        }
        var status = status_;
        if (null == listener_ && isFinished() && !cancelled_ &&
            status >= 200 && status != 204 && status != 304 &&
            !result.containsKey(HEADER_CONTENT_LENGTH)) {
            result.put(HEADER_CONTENT_LENGTH, List.of(String.valueOf(written_.get())));
        }
        return result;
    }

    /**
     * Retrieves the headers of this response with one value for each name,
     * which is what the responses of web views on iOS and Android take. The
     * values of a header are joined with a comma, like HTTP allows, and the
     * header names are case-insensitive.
     * <p>The {@code Set-Cookie} headers are left out, since they can't be
     * joined that way. A web view app keeps its cookies with a
     * {@link SiteCookieJar} instead.
     *
     * @return the names of the headers with their value
     * @see #getHttpHeaders()
     * @since 1.11.0
     */
    public Map<String, String> getSingleValueHttpHeaders() {
        var result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (var header : getHttpHeaders().entrySet()) {
            if (!HEADER_SET_COOKIE.equalsIgnoreCase(header.getKey())) {
                result.put(header.getKey(), String.join(", ", header.getValue()));
            }
        }
        return result;
    }

    static String formatSetCookie(Cookie cookie) {
        var result = new StringBuilder();
        result.append(cookie.getName()).append('=');
        if (cookie.getValue() != null) {
            result.append(cookie.getValue());
        }

        for (var attribute : cookie.getAttributes().entrySet()) {
            var name = attribute.getKey();
            var value = attribute.getValue();
            if (name.equalsIgnoreCase("Secure") || name.equalsIgnoreCase("HttpOnly")) {
                if (Boolean.parseBoolean(value)) {
                    result.append("; ").append(name);
                }
            } else if (name.equalsIgnoreCase("Comment")) {
                // obsolete, browsers ignore it
            } else if (name.equalsIgnoreCase("Max-Age")) {
                var max_age = Integer.parseInt(value);
                result.append("; Max-Age=").append(max_age);
                result.append("; Expires=").append(SiteHeaders.formatDate(
                    max_age == 0 ? 0 : System.currentTimeMillis() + max_age * 1000L));
            } else if (null == value || value.isEmpty()) {
                result.append("; ").append(name);
            } else {
                result.append("; ").append(name).append('=').append(value);
            }
        }

        return result.toString();
    }

    /**
     * Returns the value of the specified response header as a long value that
     * represents a Date object. Use this method with headers that contain
     * dates.
     * <p>The date is returned as the number of milliseconds since January 1,
     * 1970 GMT. The header name is case-insensitive.
     * <p>If the response did not have a header of the specified name, this
     * method returns {@code -1}. If the header can't be converted to a
     * date, the method throws an {@code IllegalArgumentException}.
     *
     * @param name the name of the header
     * @return a {@code long} value representing the date specified in
     * the header expressed as the number of milliseconds since January 1,
     * 1970 GMT; or
     * <p>{@code -1} if the named header was not included with the
     * response
     * @throws java.lang.IllegalArgumentException if the header value can't
     *                                            be converted to a date
     * @since 1.11.0
     */
    public long getDateHeader(String name) {
        return headers_.getDateHeader(name);
    }

    /**
     * Returns the value of the specified response header as a
     * {@code String}. If the response did not include a header of the
     * specified name, this method returns {@code null}. The header name
     * is case-insensitive. You can use this method with any response header.
     *
     * @param name the name of the header
     * @return a {@code String} containing the value of the response
     * header; or
     * <p>{@code null} if the response does not have a header of that
     * name
     * @since 1.11.0
     */
    public String getHeader(String name) {
        return headers_.getHeader(name);
    }

    /**
     * Retrieves the names of the headers that were set, without the
     * cookies.
     *
     * @return the header names
     * @see #getHttpHeaders()
     * @since 1.11.0
     */
    public Collection<String> getHeaderNames() {
        return headers_.getHeaderNames();
    }

    /**
     * Returns all the values of the specified response header as a
     * {@code Collection} of {@code String} objects.
     * <p>If the response did not include any headers of the specified name,
     * this method returns an empty {@code Collection}. The header name
     * is case-insensitive.
     *
     * @param name the name of the header
     * @return a {@code Collection} containing the values of the response
     * header; or
     * <p>if the response does not have any headers of that name return an
     * empty {@code Collection}
     * @since 1.11.0
     */
    public Collection<String> getHeaders(String name) {
        return headers_.getHeaders(name);
    }

    /**
     * Returns the value of the specified response header as an
     * {@code int}. If the response does not have a header of the
     * specified name, this method returns {@code -1}.
     * <p>The header name is case-insensitive.
     *
     * @param name the name of the header
     * @return an {@code integer} expressing the value of the response
     * header; or
     * <p>{@code -1} if the response doesn't have a header of this name
     * @throws IllegalArgumentException if the header value can't be
     *                                  converted to an {@code int}
     * @since 1.11.0
     */
    public int getIntHeader(String name) {
        return headers_.getIntHeader(name);
    }

    public void addHeader(String name, String value) {
        if (null == value) {
            return;
        }
        headers_.addHeader(name, value);
    }

    public void addDateHeader(String name, long date) {
        headers_.addDateHeader(name, date);
    }

    public void addIntHeader(String name, int integer) {
        headers_.addIntHeader(name, integer);
    }

    public boolean containsHeader(String name) {
        return headers_.containsHeader(name);
    }

    public void setDateHeader(String name, long date) {
        headers_.setDateHeader(name, date);
    }

    public void setHeader(String name, String value) {
        if (null == value) {
            headers_.removeHeader(name);
            return;
        }
        headers_.setHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        headers_.setIntHeader(name, value);
    }

    /**
     * Removes a response header with the given name.
     *
     * @param name the name of the header to remove
     * @since 1.11.0
     */
    public void removeHeader(String name) {
        headers_.removeHeader(name);
    }

    /**
     * Returns the status code of this response.
     *
     * @return an {@code integer} expressing the status code of this
     * response
     * @since 1.11.0
     */
    public int getStatus() {
        return status_;
    }

    /**
     * Returns the error reason of this response, as it was given with
     * {@link #sendError(int, String)}.
     *
     * @return a {@code String} expressing the reason of this response error; or
     * <p>{@code null} if there was none
     * @since 1.11.0
     */
    public String getReason() {
        return reason_;
    }

    public void setStatus(int statusCode) {
        status_ = statusCode;
    }

    public void sendError(int statusCode)
    throws EngineException {
        sendError(statusCode, null);
    }

    public void sendError(int statusCode, String message)
    throws EngineException {
        status_ = statusCode;
        reason_ = message;
    }

    public String encodeURL(String url) {
        var runner = request_.getRunner();
        if (null == runner || !runner.isSessionUrlParameters()) {
            return url;
        }

        // the session is identified by its cookie
        if (request_.isRequestedSessionIdFromCookie()) {
            return url;
        }

        var session = request_.getSession(false);
        if (null == session || null == url) {
            return url;
        }

        var id = session.getId();
        if (null == id) {
            return url;
        }

        var prefix = ";" + SiteRunner.SESSION_URL_PARAMETER + "=";

        var existing = url.indexOf(prefix);
        if (existing != -1) {
            var suffix = url.indexOf('?', existing);
            if (suffix < 0) {
                suffix = url.indexOf('#', existing);
            }

            if (suffix <= existing) {
                return url.substring(0, existing + prefix.length()) + id;
            }

            return url.substring(0, existing + prefix.length()) + id + url.substring(suffix);
        }

        var suffix = url.indexOf('?');
        if (suffix < 0) {
            suffix = url.indexOf('#');
        }

        if (suffix < 0) {
            return url + prefix + id;
        }

        return url.substring(0, suffix) + prefix + id + url.substring(suffix);
    }

    public void setLocale(Locale locale) {
        if (null == locale) {
            return;
        }

        locale_ = locale;
        setHeader(HEADER_CONTENT_LANGUAGE, locale.toLanguageTag());
    }

    public Locale getLocale() {
        if (null == locale_) {
            return Locale.getDefault();
        }

        return locale_;
    }

    public PrintWriter getWriter()
    throws IOException {
        markOutputStarted();

        if (null == writer_) {
            var encoding = getCharacterEncoding();
            if (null == encoding) {
                encoding = StringUtils.ENCODING_ISO_8859_1;
            }

            setCharacterEncoding(encoding);

            writer_ = new PrintWriter(new OutputStreamWriter(bodyStream_, encoding));
        }

        return writer_;
    }

    private void setCharacterEncoding(String encoding) {
        characterEncoding_ = encoding;
        if (mimeType_ != null) {
            setHeader(HEADER_CONTENT_TYPE, mimeType_ + ";charset=" + characterEncoding_);
        }
    }

    /**
     * Retrieves the underlying servlet response, which doesn't exist when a
     * site is run by a {@link SiteRunner}.
     *
     * @return {@code null}
     * @since 1.11.0
     */
    public HttpServletResponse getHttpServletResponse() {
        return null;
    }

    // commits on the first write or flush, and collects the content for the
    // listener until it's flushed or full, like a servlet container does
    private class BodyStream extends OutputStream {
        private static final int LISTENER_BUFFER_SIZE = 32 * 1024;

        private byte[] pending_ = null;
        private int pendingLength_ = 0;

        @Override
        public void write(int b)
        throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length)
        throws IOException {
            if (cancelled_) {
                throw new IOException("the request was cancelled");
            }

            commit();
            if (cancelled_) {
                throw new IOException("the request was cancelled");
            }

            written_.addAndGet(length);
            if (0 == length ||
                request_.getMethod() == RequestMethod.HEAD) {
                return;
            }

            var listener = listener_;
            if (null == listener) {
                buffer_.write(bytes, offset, length);
                return;
            }

            if (pendingLength_ + length > LISTENER_BUFFER_SIZE) {
                sendPending(listener);
            }
            if (length >= LISTENER_BUFFER_SIZE) {
                send(listener, bytes, offset, length);
                return;
            }

            if (null == pending_) {
                pending_ = new byte[LISTENER_BUFFER_SIZE];
            }
            System.arraycopy(bytes, offset, pending_, pendingLength_, length);
            pendingLength_ += length;
            if (pendingLength_ == LISTENER_BUFFER_SIZE) {
                sendPending(listener);
            }
        }

        @Override
        public synchronized void flush()
        throws IOException {
            if (cancelled_) {
                throw new IOException("the request was cancelled");
            }

            commit();

            var listener = listener_;
            if (listener != null) {
                sendPending(listener);
            }
        }

        private void sendPending(SiteResponseListener listener)
        throws IOException {
            if (0 == pendingLength_) {
                return;
            }

            var length = pendingLength_;
            pendingLength_ = 0;
            send(listener, pending_, 0, length);
        }

        private void send(SiteResponseListener listener, byte[] bytes, int offset, int length)
        throws IOException {
            if (!startListenerCall()) {
                throw new IOException("the request was cancelled");
            }
            RuntimeException failure = null;
            try {
                listener.content(bytes, offset, length);
            } catch (RuntimeException e) {
                failure = e;
            } finally {
                endListenerCall();
            }
            if (failure != null) {
                cancel();
                throw new IOException(failure);
            }
        }
    }
}
