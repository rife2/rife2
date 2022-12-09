/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.http.Cookie;

/**
 * Helper class for building a <code>Cookie</code>.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Cookie
 * @since 1.0
 */
public class CookieBuilder {
    private final Cookie cookie_;

    /**
     * Starts building a new <code>Cookie</code>.
     *
     * @param name  the name of the <code>Cookie</code>
     * @param value the value of the <code>Cookie</code>
     */
    public CookieBuilder(String name, String value) {
        this.cookie_ = new Cookie(name, value);
    }

    /**
     * Returns the <code>Cookie</code>
     *
     * @return the <code>Cookie</code> instance that being built
     * @since 1.0
     */
    public Cookie cookie() {
        return cookie_;
    }

    /**
     * See {@link Cookie#setComment}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder comment(String purpose) {
        cookie_.setComment(purpose);
        return this;
    }

    /**
     * See {@link Cookie#setDomain}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder domain(String domain) {
        cookie_.setDomain(domain);
        return this;
    }

    /**
     * See {@link Cookie#setMaxAge}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder maxAge(int expiry) {
        cookie_.setMaxAge(expiry);
        return this;
    }

    /**
     * See {@link Cookie#setPath}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder path(String uri) {
        cookie_.setPath(uri);
        return this;
    }

    /**
     * See {@link Cookie#setSecure}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder secure(boolean flag) {
        cookie_.setSecure(flag);
        return this;
    }

    /**
     * See {@link Cookie#setValue}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder value(String newValue) {
        cookie_.setValue(newValue);
        return this;
    }

    /**
     * See {@link Cookie#setVersion}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder version(int v) {
        cookie_.setVersion(v);
        return this;
    }

    /**
     * See {@link Cookie#setHttpOnly}
     *
     * @return the <code>CookieBuilder</code> instance
     * @since 1.0
     */
    public CookieBuilder httpOnly(boolean isHttpOnly) {
        cookie_.setHttpOnly(isHttpOnly);
        return this;
    }
}
