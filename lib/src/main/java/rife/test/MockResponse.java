/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import rife.engine.*;

import java.io.*;
import java.util.*;

import rife.engine.exceptions.EngineException;
import rife.template.Template;
import rife.tools.ExceptionUtils;
import rife.tools.StringUtils;

import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Provides a {@link Response} implementation that is suitable for testing a
 * web application outside a servlet container.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockResponse extends AbstractResponse {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LANGUAGE = "Content-Language";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_LOCATION = "Location";

    private static final int SC_200_OK = 200;
    private static final int SC_302_MOVED_TEMPORARILY = 302;

    private static final Pattern STRIP_XHTML_XMLNS = Pattern.compile("html\\s+xmlns=\"[^\"]*\"");

    private final MockConversation mockConversation_;
    private final MockHeaders headers_ = new MockHeaders();
    private final HashMap<String, Cookie> newCookies_ = new HashMap<>();
    private final ByteArrayOutputStream mockOutputStream_ = new ByteArrayOutputStream();
    private String contentType_;
    private String characterEncoding_;
    private int status_ = SC_200_OK;
    private String reason_;
    private Locale locale_;
    private PrintWriter mockWriter_;
    private Template template_;

    MockResponse(MockConversation conversation, Request request) {
        super(request);

        mockConversation_ = conversation;
    }

    MockConversation getMockConversation() {
        return mockConversation_;
    }

    /**
     * Retrieves an array of all the bytes that have been written to this
     * response.
     *
     * @return an array of bytes with the response content
     * @see #getText
     * @see #getTemplate
     * @see #getParsedHtml
     * @since 1.0
     */
    public byte[] getBytes() {
        return mockOutputStream_.toByteArray();
    }

    /**
     * Retrieves the content of this response as text.
     *
     * @return the response content as text
     * @see #getBytes
     * @see #getTemplate
     * @see #getParsedHtml
     * @since 1.0
     */
    public String getText() {
        var charset = characterEncoding_;
        if (null == charset) {
            charset = StringUtils.ENCODING_ISO_8859_1;
        }
        try {
            return new String(getBytes(), charset);
        } catch (UnsupportedEncodingException e) {
            return ExceptionUtils.getExceptionStackTrace(e);
        }
    }

    /**
     * Retrieves the template instance that was printed to the response.
     *
     * @return the template instance that was printed to the response; or
     * <p><code>null</code> of no template was printed to the response
     * @see #getBytes
     * @see #getText
     * @see #getParsedHtml
     * @since 1.0
     */
    public Template getTemplate() {
        return template_;
    }

    /**
     * Retrieves the content of this response as parsed HTML.
     *
     * @return the response content as parsed HTML
     * @see #getBytes
     * @see #getText
     * @see #getTemplate
     * @since 1.0
     */
    public ParsedHtml getParsedHtml() {
        return ParsedHtml.parse(this);
    }

    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a list of DOM nodes.
     * <p>More information about XPath can be found in the <a
     * href="http://www.w3.org/TR/xpath">original specification</a> or in this
     * <a href="http://zvon.org/xxl/XMLTutorial/General/book.html">tutorial</a>.
     *
     * @return the result as a <code>NodeList</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public NodeList xpathNodeSet(String expression)
    throws XPathExpressionException {
        return (NodeList) xpath(expression, XPathConstants.NODESET);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a DOM node.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public Node xpathNode(String expression)
    throws XPathExpressionException {
        return (Node) xpath(expression, XPathConstants.NODE);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a string.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathBoolean(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public String xpathString(String expression)
    throws XPathExpressionException {
        return (String) xpath(expression, XPathConstants.STRING);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a boolean.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathNumber(String)
     * @since 1.0
     */
    public Boolean xpathBoolean(String expression)
    throws XPathExpressionException {
        return (Boolean) xpath(expression, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate an XPath expression in the context of the response text and
     * return the result as a number.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String)
     * @see #xpathNode(String)
     * @see #xpathString(String)
     * @see #xpathBoolean(String)
     * @since 1.0
     */
    public Double xpathNumber(String expression)
    throws XPathExpressionException {
        return (Double) xpath(expression, XPathConstants.NUMBER);
    }

    private Object xpath(String expression, QName returnType)
    throws XPathExpressionException {
        var matcher = STRIP_XHTML_XMLNS.matcher(getText());
        var text = matcher.replaceAll("html xmlns=\"\"");
        Reader reader = new StringReader(text);

        var input_source = new InputSource(reader);
        var xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, input_source, returnType);
    }


    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a list of DOM nodes.
     * <p>More information about XPath can be found in the <a
     * href="http://www.w3.org/TR/xpath">original specification</a> or in this
     * <a href="http://zvon.org/xxl/XMLTutorial/General/book.html">tutorial</a>.
     *
     * @return the result as a <code>NodeList</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public NodeList xpathNodeSet(String expression, Object context)
    throws XPathExpressionException {
        return (NodeList) xpath(expression, context, XPathConstants.NODESET);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a DOM node.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public Node xpathNode(String expression, Object context)
    throws XPathExpressionException {
        return (Node) xpath(expression, context, XPathConstants.NODE);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a string.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathBoolean(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public String xpathString(String expression, Object context)
    throws XPathExpressionException {
        return (String) xpath(expression, context, XPathConstants.STRING);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a boolean.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathNumber(String, Object)
     * @since 1.0
     */
    public Boolean xpathBoolean(String expression, Object context)
    throws XPathExpressionException {
        return (Boolean) xpath(expression, context, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate an XPath expression in the provided context object and
     * return the result as a number.
     *
     * @return the result as a <code>Node</code>
     * @throws XPathExpressionException if expression cannot be evaluated.
     * @see #xpathNodeSet(String, Object)
     * @see #xpathNode(String, Object)
     * @see #xpathString(String, Object)
     * @see #xpathBoolean(String, Object)
     * @since 1.0
     */
    public Double xpathNumber(String expression, Object context)
    throws XPathExpressionException {
        return (Double) xpath(expression, context, XPathConstants.NUMBER);
    }

    private Object xpath(String expression, Object context, QName returnType)
    throws XPathExpressionException {
        var xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, context, returnType);
    }

    public void print(Template template)
    throws EngineException {
        template_ = template;

        super.print(template);
    }

    protected void _setContentType(String contentType) {
        if (contentType == null) {
            contentType_ = null;
            if (headers_ != null) {
                headers_.removeHeader(HEADER_CONTENT_TYPE);
            }
        } else {
            // Look for encoding in contentType
            var i0 = contentType.indexOf(';');

            if (i0 > 0) {
                // Strip params off mimetype
                contentType_ = contentType.substring(0, i0).trim();

                // Look for charset
                var i1 = contentType.indexOf("charset=", i0);
                if (i1 >= 0) {
                    i1 += 8;
                    var i2 = contentType.indexOf(' ', i1);
                    characterEncoding_ = (0 < i2)
                        ? contentType.substring(i1, i2)
                        : contentType.substring(i1);
                    characterEncoding_ = QuotedStringTokenizer.unquote(characterEncoding_);
                } else // No encoding in the params.
                {
                    if (characterEncoding_ != null) {
                        // Add any previously set encoding.
                        contentType += ";charset=" +
                            QuotedStringTokenizer.quote(characterEncoding_, ";= ");
                    }
                }
            } else // No encoding and no other params
            {
                contentType_ = contentType;
                // Add any previously set encoding.
                if (characterEncoding_ != null) {
                    contentType += ";charset=" + QuotedStringTokenizer.quote(characterEncoding_, ";= ");
                }
            }

            setHeader(HEADER_CONTENT_TYPE, contentType);
        }

        setHeader(HEADER_CONTENT_TYPE, contentType);
    }

    public String getCharacterEncoding() {
        return characterEncoding_;
    }

    public void setContentLength(int length) {
        setIntHeader(HEADER_CONTENT_LENGTH, length);
    }

    public int getContentLength() {
        return getIntHeader(HEADER_CONTENT_LENGTH);
    }

    public void sendRedirect(String location) {
        clearBuffer();

        // TODO : correctly handle absolute and relative locations
        setStatus(SC_302_MOVED_TEMPORARILY);
        setHeader(HEADER_LOCATION, location);

        // complete
    }

    protected OutputStream _getOutputStream()
    throws IOException {
        return mockOutputStream_;
    }

    public void addCookie(Cookie cookie) {
        newCookies_.put(MockConversation.buildCookieId(cookie), cookie);
        mockConversation_.addCookie(cookie);
    }

    /**
     * Retrieves the list of cookies that have been added in this response.
     *
     * @return the list of added cookies; or
     * <p>an empty list if no cookies have been added
     * @since 1.0
     */
    public List<String> getNewCookieNames() {
        var names = new ArrayList<String>();
        for (var cookie : newCookies_.values()) {
            if (!names.contains(cookie.getName())) {
                names.add(cookie.getName());
            }
        }

        return names;
    }

    /**
     * Returns the value of the specified response header as a long value that
     * represents a Date object. Use this method with headers that contain
     * dates.
     * <p>The date is returned as the number of milliseconds since January 1,
     * 1970 GMT. The header name is case insensitive.
     * <p>If the response did not have a header of the specified name, this
     * method returns <code>-1</code>. If the header can't be converted to a
     * date, the method throws an <code>IllegalArgumentException</code>.
     *
     * @param name the name of the header
     * @return a <code>long</code> value representing the date specified in
     * the header expressed as the number of milliseconds since January 1,
     * 1970 GMT; or
     * <p><code>-1</code> if the named header was not included with the
     * response
     * @throws java.lang.IllegalArgumentException if the header value can't
     *                                            be converted to a date
     * @since 1.0
     */
    public long getDateHeader(String name) {
        return headers_.getDateHeader(name);
    }

    /**
     * Returns the value of the specified response header as a
     * <code>String</code>. If the response did not include a header of the
     * specified name, this method returns <code>null</code>. The header name
     * is case-insensitive. You can use this method with any response header.
     *
     * @param name the name of the header
     * @return a <code>String</code> containing the value of the response
     * header; or
     * <p><code>null</code> if the response does not have a header of that
     * name
     * @since 1.0
     */
    public String getHeader(String name) {
        return headers_.getHeader(name);
    }

    /**
     * Returns the value of the specified response header as a
     * <code>String</code>. If the response did not include a header of the
     * specified name, this method returns <code>null</code>. The header name
     * is case insensitive. You can use this method with any response header.
     *
     * @return a <code>Collection</code> of all the header names sent with
     * this response; or
     * <p>if the response has no headers, an empty <code>Collection</code>
     * @since 1.0
     */
    public Collection getHeaderNames() {
        return headers_.getHeaderNames();
    }

    /**
     * Returns all the values of the specified response header as an
     * <code>Collection</code> of <code>String</code> objects.
     * <p>If the response did not include any headers of the specified name,
     * this method returns an empty <code>Collection</code>. The header name
     * is case insensitive. You can use this method with any response header.
     *
     * @param name the name of the header
     * @return a <code>Collection</code> containing the values of the response
     * header; or
     * <p>if the response does not have any headers of that name return an
     * empty <code>Collection</code>
     * @since 1.0
     */
    public Collection getHeaders(String name) {
        return headers_.getHeaders(name);
    }

    /**
     * Returns the value of the specified response header as an
     * <code>int</code>. If the response does not have a header of the
     * specified name, this method returns <code>-1</code>. If the header
     * cannot be converted to an <code>integer</code>, this method throws a
     * <code>NumberFormatException</code>.
     * <p>The header name is case-insensitive.
     *
     * @param name the name of the header
     * @return an <code>integer</code> expressing the value of the response
     * header; or
     * <p><code>-1</code> if the response doesn't have a header of this name
     * @throws java.lang.NumberFormatException if the header value can't be
     *                                         converted to an <code>int</code>
     * @since 1.0
     */
    public int getIntHeader(String name) {
        return headers_.getIntHeader(name);
    }

    public void addHeader(String name, String value) {
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
        headers_.setHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        headers_.setIntHeader(name, value);
    }

    /**
     * Removes a response header with the given name.
     *
     * @param name the name of the header to remove
     * @since 1.0
     */
    public void removeHeader(String name) {
        headers_.removeHeader(name);
    }

    /**
     * Returns the status code of this response.
     *
     * @return an <code>integer</code> expressing the status code of this
     * response
     * @since 1.0
     */
    public int getStatus() {
        return status_;
    }

    /**
     * Returns the error reason of this response.
     *
     * @return an <code>String</code> expressing the reason of this response error
     * @since 1.0
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
        var request = (MockRequest) getRequest();

        // should not encode if cookies in evidence
        if (null == request ||
            request.isRequestedSessionIdFromCookie()) {
            return url;
        }

        // get session
        var session = getRequest().getSession(false);

        // no session or no url
        if (null == session || null == url) {
            return url;
        }

        // invalid session
        var id = session.getId();
        if (null == id) {
            return url;
        }

        // Already encoded
        var prefix = url.indexOf(MockConversation.SESSION_URL_PREFIX);
        if (prefix != -1) {
            var suffix = url.indexOf("?", prefix);
            if (suffix < 0) {
                suffix = url.indexOf("#", prefix);
            }

            if (suffix <= prefix) {
                return url.substring(0, prefix + MockConversation.SESSION_URL_PREFIX.length()) + id;
            }

            return url.substring(0, prefix + MockConversation.SESSION_URL_PREFIX.length()) + id + url.substring(suffix);
        }

        // edit the session
        var suffix = url.indexOf('?');
        if (suffix < 0) {
            suffix = url.indexOf('#');
        }

        if (suffix < 0) {
            return url + MockConversation.SESSION_URL_PREFIX + id;
        }

        return url.substring(0, suffix) + MockConversation.SESSION_URL_PREFIX + id + url.substring(suffix);
    }

    public void setLocale(Locale locale) {
        if (null == locale) {
            return;
        }

        locale_ = locale;
        setHeader(HEADER_CONTENT_LANGUAGE, locale.toString().replace('_', '-'));
    }

    public Locale getLocale() {
        if (null == locale_) {
            return Locale.getDefault();
        }

        return locale_;
    }

    public PrintWriter getWriter()
    throws IOException {
        mockOutputStream_.flush();

        /* if there is no writer yet */
        if (mockWriter_ == null) {
            /* get encoding from Content-Type header */
            var encoding = getCharacterEncoding();

            if (encoding == null) {
                encoding = StringUtils.ENCODING_ISO_8859_1;
            }

            setCharacterEncoding(encoding);

            /* construct Writer using correct encoding */
            mockWriter_ = new PrintWriter(new OutputStreamWriter(mockOutputStream_, encoding));
        }

        return mockWriter_;
    }

    private void setCharacterEncoding(String encoding) {
        if (null == encoding) {
            // Clear any encoding.
            if (characterEncoding_ != null) {
                characterEncoding_ = null;
                setHeader(HEADER_CONTENT_TYPE, contentType_);
            }
        } else {
            // No, so just add this one to the mimetype
            characterEncoding_ = encoding;
            if (contentType_ != null) {
                setHeader(HEADER_CONTENT_TYPE,
                    contentType_ + ";charset=" +
                        QuotedStringTokenizer.quote(characterEncoding_, ";= "));
            }
        }
    }

    public HttpServletResponse getHttpServletResponse() {
        return null;
    }

// ========================================================================
// Copyright 1999-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

    /* ------------------------------------------------------------ */

    /**
     * StringTokenizer with Quoting support. This class is a copy of the
     * java.util.StringTokenizer API and the behaviour is the same, except
     * that single and doulbe quoted string values are recognized. Delimiters
     * within quotes are not considered delimiters. Quotes can be escaped with
     * '\'.
     *
     * @author Greg Wilkins (gregw)
     * @see java.util.StringTokenizer
     */
    static class QuotedStringTokenizer
        extends StringTokenizer {
        private static final String __delim = "\t\n\r";
        private String _string;
        private String _delim = __delim;
        private boolean _returnQuotes = false;
        private boolean _returnTokens = false;
        private StringBuilder _token;
        private boolean _hasToken = false;
        private int _i = 0;
        private int _lastStart = 0;

        /* ------------------------------------------------------------ */
        public QuotedStringTokenizer(String str,
                                     String delim,
                                     boolean returnTokens,
                                     boolean returnQuotes) {
            super("");
            _string = str;
            if (delim != null)
                _delim = delim;
            _returnTokens = returnTokens;
            _returnQuotes = returnQuotes;

            if (_delim.indexOf('\'') >= 0 ||
                _delim.indexOf('"') >= 0)
                throw new Error("Can't use quotes as delimiters: " + _delim);

            _token = new StringBuilder(_string.length() > 1024 ? 512 : _string.length() / 2);
        }

        /* ------------------------------------------------------------ */
        public QuotedStringTokenizer(String str,
                                     String delim,
                                     boolean returnTokens) {
            this(str, delim, returnTokens, false);
        }

        /* ------------------------------------------------------------ */
        public QuotedStringTokenizer(String str,
                                     String delim) {
            this(str, delim, false, false);
        }

        /* ------------------------------------------------------------ */
        public QuotedStringTokenizer(String str) {
            this(str, null, false, false);
        }

        /* ------------------------------------------------------------ */
        public boolean hasMoreTokens() {
            // Already found a token
            if (_hasToken)
                return true;

            _lastStart = _i;

            var state = 0;
            var escape = false;
            while (_i < _string.length()) {
                var c = _string.charAt(_i++);

                switch (state) {
                    case 0: // Start
                        if (_delim.indexOf(c) >= 0) {
                            if (_returnTokens) {
                                _token.append(c);
                                return _hasToken = true;
                            }
                        } else if (c == '\'') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 2;
                        } else if (c == '\"') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 3;
                        } else {
                            _token.append(c);
                            _hasToken = true;
                            state = 1;
                        }
                        continue;

                    case 1: // Token
                        _hasToken = true;
                        if (_delim.indexOf(c) >= 0) {
                            if (_returnTokens)
                                _i--;
                            return _hasToken;
                        } else if (c == '\'') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 2;
                        } else if (c == '\"') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 3;
                        } else
                            _token.append(c);
                        continue;


                    case 2: // Single Quote
                        _hasToken = true;
                        if (escape) {
                            escape = false;
                            _token.append(c);
                        } else if (c == '\'') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 1;
                        } else if (c == '\\') {
                            if (_returnQuotes)
                                _token.append(c);
                            escape = true;
                        } else
                            _token.append(c);
                        continue;


                    case 3: // Double Quote
                        _hasToken = true;
                        if (escape) {
                            escape = false;
                            _token.append(c);
                        } else if (c == '\"') {
                            if (_returnQuotes)
                                _token.append(c);
                            state = 1;
                        } else if (c == '\\') {
                            if (_returnQuotes)
                                _token.append(c);
                            escape = true;
                        } else
                            _token.append(c);
                        continue;
                }
            }

            return _hasToken;
        }

        /* ------------------------------------------------------------ */
        public String nextToken()
        throws NoSuchElementException {
            if (!hasMoreTokens() || _token == null)
                throw new NoSuchElementException();
            var t = _token.toString();
            _token.setLength(0);
            _hasToken = false;
            return t;
        }

        /* ------------------------------------------------------------ */
        public String nextToken(String delim)
        throws NoSuchElementException {
            _delim = delim;
            _i = _lastStart;
            _token.setLength(0);
            _hasToken = false;
            return nextToken();
        }

        /* ------------------------------------------------------------ */
        public boolean hasMoreElements() {
            return hasMoreTokens();
        }

        /* ------------------------------------------------------------ */
        public Object nextElement()
        throws NoSuchElementException {
            return nextToken();
        }

        /* ------------------------------------------------------------ */

        /**
         * Not implemented.
         */
        public int countTokens() {
            return -1;
        }


        /* ------------------------------------------------------------ */

        /**
         * Quote a string. The string is quoted only if quoting is required
         * due to embeded delimiters, quote characters or the empty string.
         *
         * @param s The string to quote.
         * @return quoted string
         */
        public static String quote(String s, String delim) {
            if (s == null)
                return null;
            if (s.length() == 0)
                return "\"\"";


            for (var i = 0; i < s.length(); i++) {
                var c = s.charAt(i);
                if (c == '"' ||
                    c == '\\' ||
                    c == '\'' ||
                    delim.indexOf(c) >= 0) {
                    var b = new StringBuilder(s.length() + 8);
                    quote(b, s);
                    return b.toString();
                }
            }

            return s;
        }

        /* ------------------------------------------------------------ */

        /**
         * Quote a string into a StringBuilder.
         *
         * @param buf The StringBuilder
         * @param s   The String to quote.
         */
        public static void quote(StringBuilder buf, String s) {
            buf.append('"');
            for (var i = 0; i < s.length(); i++) {
                var c = s.charAt(i);
                if (c == '"') {
                    buf.append("\\\"");
                    continue;
                }
                if (c == '\\') {
                    buf.append("\\\\");
                    continue;
                }
                buf.append(c);
                continue;
            }
            buf.append('"');
        }

        /* ------------------------------------------------------------ */

        /**
         * Unquote a string.
         *
         * @param s The string to unquote.
         * @return quoted string
         */
        public static String unquote(String s) {
            if (s == null)
                return null;
            if (s.length() < 2)
                return s;

            var first = s.charAt(0);
            var last = s.charAt(s.length() - 1);
            if (first != last || (first != '"' && first != '\''))
                return s;

            var b = new StringBuilder(s.length() - 2);
            var quote = false;
            for (var i = 1; i < s.length() - 1; i++) {
                var c = s.charAt(i);

                if (c == '\\' && !quote) {
                    quote = true;
                    continue;
                }
                quote = false;
                b.append(c);
            }

            return b.toString();
        }
    }
}
