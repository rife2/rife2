/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;

public class Version {
    private String mVersion = null;

    Version() {
        ResourceFinderClasspath resource_finder = ResourceFinderClasspath.instance();
        try {
            mVersion = resource_finder.getContent("RIFE_VERSION");
        } catch (ResourceFinderErrorException e) {
            mVersion = null;
        }

        if (mVersion != null) {
            mVersion = mVersion.trim();
        }
        if (null == mVersion) {
            mVersion = "unknown version";
        }
    }

    private String getVersionString() {
        return mVersion;
    }

    public static String getVersion() {
        return VersionSingleton.INSTANCE.getVersionString();
    }
}

