/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import jakarta.servlet.http.Cookie;

import java.time.Instant;

/**
 * Wraps a {@code Cookie} by tracking the creation time and allowing max-age
 * expiration to be evaluated.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MockCookie extends Cookie {
    private final Instant creationTime_;

    /**
     * Creates a new cookie with the provided name and value
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @see Cookie#Cookie(String, String)
     * @since 1.0
     */
    public MockCookie(String name, String value) {
        super(name, value);
        setPath("/");
        creationTime_ = Instant.now();
    }

    /**
     * Creates a new cookie with the same values as a standard {@code Cookie}.
     * @param cookie the cookie that will provide all the values
     * @see Cookie
     * @since 1.0
     */
    public MockCookie(Cookie cookie) {
        this(cookie.getName(), cookie.getValue());
        if (cookie.getDomain() != null) setDomain(cookie.getDomain());
        setMaxAge(cookie.getMaxAge());
        setPath(cookie.getPath());
        setSecure(cookie.getSecure());
        setHttpOnly(cookie.isHttpOnly());
    }

    /**
     * Indicates whether the cookie is expired or not.
     *
     * @return {@code true} when the cookie is expired;
     * {@code false} otherwise
     * @see Cookie#setMaxAge(int)
     * @since 1.0
     */
    boolean isExpired() {
        if (getMaxAge() < 0) {
            return false;
        }
        if (getMaxAge() == 0) {
            return true;
        }
        return creationTime_.plusSeconds(getMaxAge()).isBefore(Instant.now());
    }
}
