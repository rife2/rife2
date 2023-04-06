/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.dependencies.VersionNumber;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;

/**
 * Singleton class that provides access to the current RIFE2 version as a string.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Version {
    private String version_;

    Version() {
        ResourceFinderClasspath resource_finder = ResourceFinderClasspath.instance();
        try {
            version_ = resource_finder.getContent("RIFE_VERSION");
        } catch (ResourceFinderErrorException e) {
            version_ = null;
        }

        if (version_ != null) {
            version_ = version_.trim();
        }
        if (null == version_) {
            version_ = "unknown version";
        }
    }

    private String getVersionString() {
        return version_;
    }

    public static String getVersion() {
        return VersionSingleton.INSTANCE.getVersionString();
    }

    public static VersionNumber getVersionNumber() {
        return VersionNumber.parse(getVersion());
    }
}

