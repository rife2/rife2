/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.Collection;

/**
 * Stores the cookies of a {@link SiteCookieJar} somewhere that lasts, so
 * that they survive a restart of your application.
 * <p>Only the cookies with an expiry are stored, in the same way that a
 * browser forgets its session cookies when it's closed. These can be
 * credentials, a remembered login for instance, so they belong in storage
 * that your application protects.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
public interface SiteCookieStore {
    /**
     * Provides the cookies that were stored before. The jar will drop the
     * ones that have expired.
     *
     * @return the stored cookies; or
     * <p>{@code null} if there are none
     * @since 1.11.0
     */
    Collection<SiteCookie> load();

    /**
     * Stores all the cookies that should last. This method will be called
     * each time they change.
     *
     * @param cookies the cookies to store, which replace what was stored
     *                before
     * @since 1.11.0
     */
    void save(Collection<SiteCookie> cookies);
}
