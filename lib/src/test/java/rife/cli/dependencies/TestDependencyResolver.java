/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies;

import org.junit.jupiter.api.Test;
import rife.cli.dependencies.exceptions.ArtifactNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDependencyResolver {
    @Test
    void testInstantiation() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertNotNull(resolver);
    }

    @Test
    void testNotFound() {
        var resolver = new DependencyResolver(new Dependency("com.org.unknown", "voidthing"));
        assertFalse(resolver.exists());
    }

    @Test
    void testCheckExistence() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2"));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceVersion() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceMissingVersion() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 3, 9)));
        assertFalse(resolver.exists());
    }

    @Test
    void testListVersions() {
        var resolver1 = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2"));
        var versions1 = resolver1.listVersions();
        assertNotNull(versions1);
        assertFalse(versions1.isEmpty());
        assertFalse(versions1.contains(VersionNumber.UNKNOWN));
        assertTrue(versions1.contains(new VersionNumber(1, 0, 0)));
        assertTrue(versions1.contains(new VersionNumber(1, 2, 1)));

        var resolver2 = new DependencyResolver(new Dependency("org.eclipse.jetty", "jetty-server"));
        var versions2 = resolver2.listVersions();
        assertNotNull(versions2);
        assertFalse(versions2.isEmpty());
        assertFalse(versions2.contains(VersionNumber.UNKNOWN));
        assertTrue(versions2.contains(new VersionNumber(9, 4, 51, "v20230217")));
        assertTrue(versions2.contains(new VersionNumber(11, 0, 14)));
    }

    @Test
    void testGetLatestVersion() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.latestVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }

    @Test
    void testGetReleaseVersion() {
        var resolver = new DependencyResolver(new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.releaseVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }
}
