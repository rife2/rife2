/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.servlet;

import rife.engine.AbstractResponse;
import rife.engine.Request;
import rife.engine.exceptions.EngineException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class HttpResponse extends AbstractResponse {
    private HttpServletResponse response_ = null;

    public HttpResponse(Request request, HttpServletResponse response) {
        super(request);

        response_ = response;
    }

    @Override
    protected void _setContentType(String contentType) {
        response_.setContentType(contentType);
    }

    @Override
    protected OutputStream _getOutputStream() throws IOException {
        return response_.getOutputStream();
    }

    @Override
    public void addCookie(Cookie cookie) {
        response_.addCookie(cookie);
    }

    @Override
    public void addHeader(String name, String value) {
        response_.addHeader(name, value);
    }

    @Override
    public void addDateHeader(String name, long date) {
        response_.addDateHeader(name, date);
    }

    @Override
    public void addIntHeader(String name, int integer) {
        response_.addIntHeader(name, integer);
    }

    @Override
    public boolean containsHeader(String name) {
        return response_.containsHeader(name);
    }

    @Override
    public void sendError(int statusCode) throws EngineException {
        try {
            response_.sendError(statusCode);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void sendError(int statusCode, String message) throws EngineException {
        try {
            response_.sendError(statusCode, message);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void sendRedirect(String location) throws EngineException {
        try {
            response_.sendRedirect(location);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void setDateHeader(String name, long date) {
        response_.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        response_.setHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        response_.setIntHeader(name, value);
    }

    @Override
    public void setStatus(int statusCode) {
        response_.setStatus(statusCode);
    }

    @Override
    public String encodeURL(String url) {
        return response_.encodeURL(url);
    }

    @Override
    public void setLocale(Locale locale) {
        response_.setLocale(locale);
    }

    @Override
    public Locale getLocale() {
        return response_.getLocale();
    }

    @Override
    public String getCharacterEncoding() {
        return response_.getCharacterEncoding();
    }

    @Override
    public void setContentLength(int length) {
        response_.setContentLength(length);
    }

    @Override
    public PrintWriter getWriter()
        throws IOException {
        return response_.getWriter();
    }

    @Override
    public HttpServletResponse getHttpServletResponse() {
        return response_;
    }
}
