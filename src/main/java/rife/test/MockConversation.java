/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;
import rife.engine.Gate;
import rife.engine.Site;
import rife.engine.exceptions.EngineException;
import rife.ioc.HierarchicalProperties;
import rife.tools.ArrayUtils;
import rife.tools.StringUtils;

import java.util.*;

/**
 * Simulates a conversation between a web browser and a servlet container.
 * <p>Cookies will be remembered between requests and can be easily examined.
 * To check which new cookies have been set during a request, the {@link
 * MockResponse#getNewCookieNames} method can be used.
 * <p>An instance of this class is tied to a regular {@link Site} structure
 * instance.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockConversation {
    static final String SESSION_ID_COOKIE = "JSESSIONID";
    static final String SESSION_ID_URL = "jsessionid";
    static final String SESSION_URL_PREFIX = ";" + SESSION_ID_URL + "=";

    private Gate gate_ = null;

    private final HierarchicalProperties properties_;
    private final HashMap<String, MockCookie> cookies_ = new HashMap<>();
    private final HashMap<String, MockSession> sessions_ = new HashMap<>();
    private String scheme_ = "http";
    private String serverName_ = "localhost";
    private int serverPort_ = 80;
    private String contextPath_ = "";

    /**
     * Creates a new {@code MockConversation} instance for a particular
     * site.
     *
     * @param site the site structure that will be tested
     * @since 1.0
     */
    public MockConversation(Site site)
    throws EngineException {
        properties_ = new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance());

        gate_ = new Gate();
        gate_.setup(properties_, site);
    }

    /**
     * Retrieves this conversation's {@code Site}.
     *
     * @return this conversation's site
     * @since 1.6.1
     */
    public Site getSite() {
        return gate_.getSite();
    }

    /**
     * Perform a request for a particular URL.
     *
     * @param url the url that should be tested
     * @return the response of the request as a {@link MockResponse} instance;
     * or
     * <p>{@code null} if the scheme, hostname and port don't correspond
     * to the conversation setup
     * @see #doRequest(String, MockRequest)
     * @since 1.0
     */
    public MockResponse doRequest(String url)
    throws EngineException {
        return doRequest(url, new MockRequest());
    }

    /**
     * Perform a request for a particular URL and request configuration.
     * <p>The request can either be complete with the scheme and hostname, or
     * an absolute path. These two URLs are thus considered the same:
     * <pre>https://localhost/some/url?name1=value1&amp;name2=value2</pre>
     * <pre>/some/url?name1=value1&amp;name2=value2</pre>
     * <p>Note that when the complete URL form is used, it should correspond
     * to the scheme, hostname and port configuration of this conversation.
     *
     * @param url     the url that should be tested
     * @param request the request that will be used
     * @return the response of the request as a {@link MockResponse} instance;
     * or
     * <p>{@code null} if the scheme, hostname and port don't correspond
     * to the conversation setup
     * @see #doRequest(String)
     * @since 1.0
     */
    public MockResponse doRequest(String url, MockRequest request)
    throws EngineException {
        if (null == url) throw new IllegalArgumentException("url can't be null");
        if (null == request) throw new IllegalArgumentException("request can't be null");

        request.setMockConversation(this);

        // strip away the server root URL, or fail the request in case
        // the url doesn't start with the correct server root
        var server_root = request.getServerRootUrl(-1);
        if (url.contains(":/")) {
            if (!url.startsWith(server_root)) {
                return null;
            }

            url = url.substring(server_root.length());
        }

        // add the parameters in the URL to the request parameters
        var parameters = extractParameters(url);
        if (parameters != null) {
            for (var entry : parameters.entrySet()) {
                if (!request.hasParameter(entry.getKey())) {
                    request.setParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        // get the path parameters
        String path_parameters = null;
        var path_parameters_index = url.indexOf(',');
        if (path_parameters_index != -1) {
            path_parameters = url.substring(0, path_parameters_index);
        }

        // remove the query string
        var index_query = url.indexOf('?');
        if (index_query != -1) {
            url = url.substring(0, index_query);
        }

        // perform the request
        var response = new MockResponse(this, request);
        request.setMockResponse(response);
        request.setRequestedSessionId(path_parameters);
        if (!gate_.handleRequest("", url, request, response)) {
            response.setStatus(404);
        }
        return response;
    }

    /**
     * Retrieves the scheme that is used by this conversation.
     *
     * @return the scheme of this conversation
     * @see #setScheme
     * @see #scheme
     * @since 1.0
     */
    public String getScheme() {
        return scheme_;
    }

    /**
     * Sets the scheme that will be used by this conversation.
     *
     * @param scheme the scheme
     * @see #getScheme
     * @see #scheme
     * @since 1.0
     */
    public void setScheme(String scheme) {
        scheme_ = scheme;
    }

    /**
     * Sets the scheme that will be used by this conversation.
     *
     * @param scheme the scheme
     * @return this {@code MockConversation} instance
     * @see #getScheme
     * @see #setScheme
     * @since 1.0
     */
    public MockConversation scheme(String scheme) {
        setScheme(scheme);

        return this;
    }

    /**
     * Retrieves the server name that is used by this conversation.
     *
     * @return the server name of this conversation
     * @see #setServerName
     * @see #serverName
     * @since 1.0
     */
    public String getServerName() {
        return serverName_;
    }

    /**
     * Sets the server name that will be used by this conversation.
     *
     * @param serverName the server name
     * @see #getServerName
     * @see #serverName
     * @since 1.0
     */
    public void setServerName(String serverName) {
        serverName_ = serverName;
    }

    /**
     * Sets the server name that will be used by this conversation.
     *
     * @param serverName the server name
     * @return this {@code MockConversation} instance
     * @see #getServerName
     * @see #setServerName
     * @since 1.0
     */
    public MockConversation serverName(String serverName) {
        setServerName(serverName);

        return this;
    }

    /**
     * Retrieves the server port that is used by this conversation.
     *
     * @return the server port of this conversation
     * @see #setServerPort
     * @see #serverPort
     * @since 1.0
     */
    public int getServerPort() {
        return serverPort_;
    }

    /**
     * Sets the server port that will be used by this conversation.
     *
     * @param serverPort the server port
     * @see #getServerPort
     * @see #serverPort
     * @since 1.0
     */
    public void setServerPort(int serverPort) {
        serverPort_ = serverPort;
    }

    /**
     * Sets the server port that will be used by this conversation.
     *
     * @param serverPort the server port
     * @return this {@code MockConversation} instance
     * @see #getServerPort
     * @see #setServerPort
     * @since 1.0
     */
    public MockConversation serverPort(int serverPort) {
        setServerPort(serverPort);

        return this;
    }

    /**
     * Retrieves the context path that is used by this conversation.
     *
     * @return the context path of this conversation
     * @see #setContextPath
     * @see #contextPath
     * @since 1.0
     */
    public String getContextPath() {
        return contextPath_;
    }

    /**
     * Sets the context path that will be used by this conversation.
     *
     * @param contextPath the context path
     * @see #getContextPath
     * @see #contextPath
     * @since 1.0
     */
    public void setContextPath(String contextPath) {
        contextPath_ = contextPath;
    }

    /**
     * Sets the context path that will be used by this conversation.
     *
     * @param contextPath the context path
     * @return this {@code MockConversation} instance
     * @see #getContextPath
     * @see #setContextPath
     * @since 1.0
     */
    public MockConversation contextPath(String contextPath) {
        setContextPath(contextPath);

        return this;
    }

    /**
     * Returns the properties uses by this conversation.
     *
     * @return the instance of {@code HierarchicalProperties} that is used
     * by this conversation
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Checks whether a cookie is present.
     *
     * @param name the name of the cookie.
     * @return {@code true} if the cookie was present; or
     * <p>{@code false} otherwise
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public boolean hasCookie(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        for (var cookie : cookies_.values()) {
            if (cookie.getName().equals(name) && !cookie.isExpired()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves a cookie.
     *
     * @param name the name of the cookie.
     * @return the instance of the cookie; or
     * <p>{@code null} if no such cookie is present
     * @see #hasCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public Cookie getCookie(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        for (var cookie : cookies_.values()) {
            if (cookie.getName().equals(name) && !cookie.isExpired()) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * Retrieves the value of a cookie.
     *
     * @param name the name of the cookie.
     * @return the value of the cookie; or
     * <p>{@code null} if no such cookie is present
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public String getCookieValue(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        var cookie = getCookie(name);
        if (null == cookie) {
            return null;
        }

        return cookie.getValue();
    }

    /**
     * Retrieves all cookies.
     *
     * @return an array with all the cookies; or
     * <p>{@code null} if no cookies are present
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public Cookie[] getCookies() {
        if (cookies_.isEmpty()) {
            return null;
        }

        var active_cookies = new ArrayList<>(cookies_.values());
        active_cookies.removeIf(MockCookie::isExpired);

        var cookies = new MockCookie[active_cookies.size()];
        active_cookies.toArray(cookies);
        return cookies;
    }

    /**
     * Add a cookie.
     *
     * @param cookie the cookie instance that will be added
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public void addCookie(Cookie cookie) {
        if (null == cookie) {
            return;
        }

        cookies_.put(buildCookieId(cookie), new MockCookie(cookie));
    }

    /**
     * Add a cookie with only a name and a value, the other fields will be
     * empty.
     *
     * @param name  the name of the cookie
     * @param value the value of the cookie
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @since 1.0
     */
    public void addCookie(String name, String value) {
        if (null == name) throw new IllegalArgumentException("name can't be null");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty");

        addCookie(new MockCookie(name, value));
    }

    /**
     * Add a cookie.
     *
     * @param cookie the cookie instance that will be added
     * @return this {@code MockConversation} instance
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public MockConversation cookie(Cookie cookie) {
        addCookie(cookie);

        return this;
    }

    /**
     * Add a cookie with only a name and a value, the other fields will be
     * empty.
     *
     * @param name  the name of the cookie
     * @param value the value of the cookie
     * @return this {@code MockConversation} instance
     * @see #hasCookie(String)
     * @see #getCookie(String)
     * @see #getCookieValue(String)
     * @see #getCookies()
     * @see #addCookie(Cookie)
     * @see #addCookie(String, String)
     * @since 1.0
     */
    public MockConversation cookie(String name, String value) {
        addCookie(name, value);

        return this;
    }

    static String buildCookieId(Cookie cookie) {
        var cookie_id = new StringBuilder();
        if (cookie.getDomain() != null) {
            cookie_id.append(cookie.getDomain());
        }
        cookie_id.append("\n");
        if (cookie.getPath() != null) {
            cookie_id.append(cookie.getPath());
        }
        cookie_id.append("\n");
        if (cookie.getName() != null) {
            cookie_id.append(cookie.getName());
        }
        return cookie_id.toString();
    }

    static Map<String, String[]> extractParameters(String url) {
        if (null == url) {
            return null;
        }

        var index_query = url.indexOf('?');
        var index_anchor = url.indexOf('#');

        if (-1 == index_query) {
            return null;
        }

        String query = null;
        if (index_anchor != -1) {
            query = url.substring(index_query + 1, index_anchor);
        } else {
            query = url.substring(index_query + 1);
        }

        Map<String, String[]> parameters = new HashMap<>();

        var query_parts = StringUtils.split(query, "&");
        for (var query_part : query_parts) {
            var parameter = StringUtils.split(query_part, "=");
            if (2 == parameter.size()) {
                var name = StringUtils.decodeUrl(parameter.get(0));
                var value = StringUtils.decodeUrl(parameter.get(1));

                var values = parameters.get(name);
                if (null == values) {
                    values = new String[]{value};
                } else {
                    values = ArrayUtils.join(values, value);
                }

                parameters.put(name, values);
            }
        }

        return parameters;
    }

    MockSession getSession(String id) {
        return sessions_.get(id);
    }

    MockSession newHttpSession() {
        var session = new MockSession(this);
        sessions_.put(session.getId(), session);

        return session;
    }

    void removeSession(String id) {
        sessions_.remove(id);
    }
}
