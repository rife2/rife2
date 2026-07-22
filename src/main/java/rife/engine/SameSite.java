/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * The {@code SameSite} policies of a cookie, which determine whether a
 * browser sends the cookie along with requests that originate from another
 * site.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CookieBuilder#sameSite
 * @since 1.10
 */
public enum SameSite {
    /**
     * The cookie is only sent with same-site requests, it's never sent when
     * another site links to or submits to yours.
     *
     * @since 1.10
     */
    STRICT("Strict"),

    /**
     * The cookie is sent with same-site requests and when following a
     * regular link from another site, but not with cross-site form
     * submissions or other unsafe cross-site requests.
     *
     * @since 1.10
     */
    LAX("Lax"),

    /**
     * The cookie is sent with every request, also with cross-site ones,
     * which browsers only accept for cookies that are also secure.
     *
     * @since 1.10
     */
    NONE("None");

    private final String attributeValue_;

    SameSite(String attributeValue) {
        attributeValue_ = attributeValue;
    }

    /**
     * Retrieves the value of the {@code SameSite} cookie attribute.
     *
     * @return the cookie attribute value of this policy
     * @since 1.10
     */
    public String attributeValue() {
        return attributeValue_;
    }
}
