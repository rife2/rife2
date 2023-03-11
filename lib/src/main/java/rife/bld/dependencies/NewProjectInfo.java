/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.Version;
import rife.bld.DependencyScopes;

import static rife.bld.Project.*;
import static rife.bld.dependencies.Scope.*;

public final class NewProjectInfo {
    public static final DependencyScopes DEPENDENCIES = new DependencyScopes();

    static {
        DEPENDENCIES.scope(compile)
            .include(dependency("com.uwyn.rife2", "rife2", Version.getVersionNumber()));
        DEPENDENCIES.scope(test)
            .include(dependency("org.jsoup", "jsoup", version(1,15,4)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,9,2)));
        DEPENDENCIES.scope(standalone)
            .include(dependency("org.eclipse.jetty", "jetty-server", version(11,0,14)))
            .include(dependency("org.eclipse.jetty", "jetty-servlet", version(11,0,14)))
            .include(dependency("org.slf4j", "slf4j-simple", version(2,0,5)));
    }

    private NewProjectInfo() {
        // no-op
    }
}