/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import rife.tools.StringUtils;

/**
 * This interface contains all the methods that the web engine needs to be
 * able to correctly handle incoming requests.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface Request {
    /**
     * See {@link HttpServletRequest#getMethod()}.
     *
     * @since 1.0
     */
    RequestMethod getMethod();

    /**
     * Retrieves the parameters that were sent in this request.
     *
     * @return a {@code Map} with all the parameter names and values
     * @since 1.0
     */
    Map<String, String[]> getParameters();

    /**
     * Retrieves the body of this request as a string.
     *
     * @return the string of the request body
     * @see #getBodyAsBytes()
     * @since 1.0
     */
    String getBody();

    /**
     * Retrieves the body of this request as a byte array.
     *
     * @return the byte array of the request body
     * @see #getBody()
     * @since 1.0
     */
    byte[] getBodyAsBytes();

    /**
     * Retrieves the files that were uploaded in this request.
     *
     * @return a {@code Map} with all the uploaded files
     * @see #hasFile(String)
     * @see #getFile(String)
     * @see #getFiles(String)
     * @since 1.0
     */
    Map<String, UploadedFile[]> getFiles();

    /**
     * Checks if a particular file has been uploaded in this request.
     *
     * @param name the name of the file
     * @return {@code true} if the file was uploaded; or
     * <p>{@code false} otherwise
     * @see #getFiles()
     * @see #getFile(String)
     * @see #getFiles(String)
     * @since 1.0
     */
    boolean hasFile(String name);

    /**
     * Retrieves an uploaded file.
     *
     * @param name the name of the file
     * @return the uploaded file; or
     * <p>{@code null} if no file was uploaded
     * @see #getFiles()
     * @see #hasFile(String)
     * @see #getFiles(String)
     * @since 1.0
     */
    UploadedFile getFile(String name);

    /**
     * Retrieves all files that have been uploaded for a particular name.
     *
     * @param name the name of the file
     * @return the uploaded files; or
     * <p>{@code null} if no files were uploaded for that name
     * @see #getFiles()
     * @see #hasFile(String)
     * @see #getFile(String)
     * @since 1.0
     */
    UploadedFile[] getFiles(String name);

    /**
     * Returns the root URL of the server that is running this web
     * applications.
     * <p>This includes the protocol, the server name and the server port, for
     * example: {@code http://www.somehost.com:8080}.
     *
     * @return the server's root url
     * @since 1.0
     */
    String getServerRootUrl(int port);

    /**
     * Checks whether a cookie is present.
     *
     * @param name the name of the cookie
     * @return {@code true} if the cookie was present; or
     * <p>{@code false} otherwise
     * @see #getCookie(String)
     * @see #getCookies()
     * @since 1.0
     */
    boolean hasCookie(String name);

    /**
     * Retrieves a cookie.
     *
     * @param name the name of the cookie.
     * @return the instance of the cookie; or
     * <p>{@code null} if no such cookie is present
     * @see #hasCookie(String)
     * @see #getCookies()
     * @since 1.0
     */
    Cookie getCookie(String name);

    /**
     * See {@link HttpServletRequest#getCookies()}.
     *
     * @since 1.0
     */
    Cookie[] getCookies();

    /**
     * See {@link HttpServletRequest#getAttribute(String)}.
     *
     * @since 1.0
     */
    Object getAttribute(String name);

    /**
     * Checks if a request attribute exists.
     *
     * @param name a {@code String} specifying the name of the attribute
     *             <p>{@code false} otherwise
     * @since 1.0
     */
    boolean hasAttribute(String name);

    /**
     * See {@link HttpServletRequest#getAttributeNames()}.
     *
     * @since 1.0
     */
    Enumeration<String> getAttributeNames();

    /**
     * See {@link HttpServletRequest#removeAttribute(String)}.
     *
     * @since 1.0
     */
    void removeAttribute(String name);

    /**
     * See {@link HttpServletRequest#setAttribute(String, Object)}.
     *
     * @since 1.0
     */
    void setAttribute(String name, Object object);

    /**
     * See {@link HttpServletRequest#getCharacterEncoding()}.
     *
     * @since 1.0
     */
    String getCharacterEncoding();

    /**
     * See {@link HttpServletRequest#getContentType()}.
     *
     * @since 1.0
     */
    String getContentType();

    /**
     * See {@link HttpServletRequest#getDateHeader(String)}.
     *
     * @since 1.0
     */
    long getDateHeader(String name);

    /**
     * See {@link HttpServletRequest#getHeader(String)}.
     *
     * @since 1.0
     */
    String getHeader(String name);

    /**
     * See {@link HttpServletRequest#getHeaderNames()}.
     *
     * @since 1.0
     */
    Enumeration<String> getHeaderNames();

    /**
     * See {@link HttpServletRequest#getHeaders(String)}.
     *
     * @since 1.0
     */
    Enumeration<String> getHeaders(String name);

    /**
     * See {@link HttpServletRequest#getIntHeader(String)}.
     *
     * @since 1.0
     */
    int getIntHeader(String name);

    /**
     * See {@link HttpServletRequest#getLocale()}.
     *
     * @since 1.0
     */
    Locale getLocale();

    /**
     * See {@link HttpServletRequest#getLocales()}.
     *
     * @since 1.0
     */
    Enumeration<Locale> getLocales();

    /**
     * See {@link HttpServletRequest#getProtocol()}.
     *
     * @since 1.0
     */
    String getProtocol();

    /**
     * See {@link HttpServletRequest#getRemoteAddr()}.
     *
     * @since 1.0
     */
    String getRemoteAddr();

    /**
     * See {@link HttpServletRequest#getRemoteUser()}.
     *
     * @since 1.0
     */
    String getRemoteUser();

    /**
     * See {@link HttpServletRequest#getRemoteHost()}.
     *
     * @since 1.0
     */
    String getRemoteHost();

    /**
     * See {@link HttpServletRequest#getRequestDispatcher(String)}.
     *
     * @since 1.0
     */
    RequestDispatcher getRequestDispatcher(String url);

    /**
     * See {@link HttpServletRequest#getSession()}.
     *
     * @since 1.0
     */
    HttpSession getSession();

    /**
     * See {@link HttpServletRequest#getSession(boolean)}.
     *
     * @since 1.0
     */
    HttpSession getSession(boolean create);

    /**
     * See {@link HttpServletRequest#getServerPort()}.
     *
     * @since 1.0
     */
    int getServerPort();

    /**
     * See {@link HttpServletRequest#getScheme()}.
     *
     * @since 1.0
     */
    String getScheme();

    /**
     * See {@link HttpServletRequest#getServerName()}.
     *
     * @since 1.0
     */
    String getServerName();

    /**
     * See {@link HttpServletRequest#getContextPath()}.
     *
     * @since 1.0
     */
    String getContextPath();

    /**
     * See {@link HttpServletRequest#isSecure()}.
     *
     * @since 1.0
     */
    boolean isSecure();

    /**
     * Retrieves the underlying {@link HttpServletRequest}.
     *
     * @return the underlying {@code HttpServletRequest} instance; or
     * <p>{@code null} if this request isn't backed by
     * {@code HttpServletRequest}
     * @since 1.0
     */
    HttpServletRequest getHttpServletRequest();
}
