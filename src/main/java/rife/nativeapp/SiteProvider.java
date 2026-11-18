/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import rife.engine.Site;
import rife.ioc.HierarchicalProperties;

/**
 * Makes the site of an app from what the app told it, for a site that needs
 * that before it exists, like the directory of its database. An app names
 * its provider in {@code META-INF/services/rife.nativeapp.SiteProvider};
 * a site that doesn't need one is named in
 * {@code META-INF/services/rife.engine.Site} instead.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.11.0
 */
@FunctionalInterface
public interface SiteProvider {
    /**
     * Makes the site.
     *
     * @param properties what the app told the site, like {@link NativeApp#DATA_PROPERTY}
     * @return the site
     * @since 1.11.0
     */
    Site site(HierarchicalProperties properties);
}
