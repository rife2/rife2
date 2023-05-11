/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.tools.FileUtils;

/**
 * Singleton class that provides access to the current RIFE2 version as a string.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Version {
    private final String version_;

    Version() {
        version_ = FileUtils.versionFromResource("RIFE_VERSION");
    }

    private String getVersionString() {
        return version_;
    }

    public static String getVersion() {
        return VersionSingleton.INSTANCE.getVersionString();
    }
}

