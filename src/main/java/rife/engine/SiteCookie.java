/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * A cookie as it's kept by a {@link SiteCookieJar}, with an absolute expiry
 * so that it can be stored and restored later.
 *
 * @param name     the name of the cookie
 * @param value    the value of the cookie
 * @param host     the host that set the cookie, which is the only host
 *                 that it's sent to when there's no domain
 * @param domain   the domain the cookie was set for, which includes its
 *                 subdomains, or {@code null} for only the host that set it
 * @param path     the path the cookie applies to
 * @param expires  when the cookie expires, in milliseconds since the epoch,
 *                 or {@code -1} for a session cookie, which isn't stored
 *                 and is forgotten together with its jar
 * @param secure   whether the cookie is only sent with secure requests
 * @param httpOnly whether the cookie was marked as not visible to scripts
 * @param sameSite the {@code SameSite} attribute, or {@code null} when it
 *                 wasn't set
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public record SiteCookie(String name, String value, String host, String domain, String path, long expires, boolean secure, boolean httpOnly, String sameSite) {
    /**
     * Indicates whether this is a session cookie, which isn't stored and is
     * forgotten together with its jar.
     *
     * @return {@code true} if the cookie has no expiry; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    public boolean isSessionCookie() {
        return expires < 0;
    }

    /**
     * Indicates whether this cookie has expired at a particular moment.
     *
     * @param now the moment in milliseconds since the epoch
     * @return {@code true} if the cookie has expired; or
     * <p>{@code false} otherwise
     * @since 1.11.0
     */
    public boolean isExpired(long now) {
        return expires >= 0 && expires <= now;
    }
}
