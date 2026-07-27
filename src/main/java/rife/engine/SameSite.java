/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * The {@code SameSite} policies of a cookie, which determine whether a
 * browser will send the cookie along with the requests that originate from
 * another site.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see CookieBuilder#sameSite
 * @since 1.10
 */
public enum SameSite {
    /**
     * The cookie is only sent with same-site requests, so that it's never
     * sent when another site links to yours or submits to it.
     *
     * @since 1.10
     */
    STRICT("Strict"),

    /**
     * The cookie is sent with same-site requests and when a regular link
     * from another site is followed, while it isn't sent with cross-site
     * form submissions or with other unsafe cross-site requests.
     *
     * @since 1.10
     */
    LAX("Lax"),

    /**
     * The cookie is sent with every request, including the cross-site ones,
     * which browsers will only accept when the cookie is secure as well.
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
     * <p>This method will return the textual form of this policy, which is
     * what ends up in the {@code SameSite} attribute of the cookie.
     *
     * @return the cookie attribute value of this policy
     * @see CookieBuilder#sameSite
     * @since 1.10
     */
    public String attributeValue() {
        return attributeValue_;
    }
}
