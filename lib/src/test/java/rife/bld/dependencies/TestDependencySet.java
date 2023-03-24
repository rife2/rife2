/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Test;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDependencySet {
    @Test
    void testInstantiation() {
        var set = new DependencySet();
        assertNotNull(set);
        assertTrue(set.isEmpty());
    }

    @Test
    void testPopulation() {
        var set = new DependencySet();

        var dep1 = new Dependency("com.uwyn.rife2", "rife2");
        var dep2 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 3, 0));
        var dep3 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0));
        var dep4 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 3, 1));

        set.add(dep1);
        assertEquals(VersionNumber.UNKNOWN, set.get(dep1).version());
        set.add(dep2);
        assertEquals(dep2.version(), set.get(dep1).version());
        set.add(dep3);
        assertEquals(dep3.version(), set.get(dep2).version());
        set.add(dep4);
        assertEquals(dep3.version(), set.get(dep2).version());
    }

    @Test
    void testAddAll() {
        var set1 = new DependencySet()
            .include(new Dependency("org.eclipse.jetty", "jetty-server", VersionNumber.parse("11.0.14")))
            .include(new Dependency("org.eclipse.jetty.toolchain", "jetty-jakarta-servlet-api", VersionNumber.parse("5.0.2")))
            .include(new Dependency("org.eclipse.jetty", "jetty-http", VersionNumber.parse("11.0.14")))
            .include(new Dependency("org.eclipse.jetty", "jetty-io", VersionNumber.parse("11.0.14")))
            .include(new Dependency("org.eclipse.jetty", "jetty-util", VersionNumber.parse("11.0.14")))
            .include(new Dependency("org.slf4j", "slf4j-api", VersionNumber.parse("2.0.5")));

        var set2 = new DependencySet()
            .include(new Dependency("org.slf4j", "slf4j-simple", VersionNumber.parse("2.0.6")))
            .include(new Dependency("org.slf4j", "slf4j-api", VersionNumber.parse("2.0.6")));

        var set_union1 = new DependencySet(set1);
        set_union1.addAll(set2);
        assertEquals("""
            org.eclipse.jetty:jetty-server:11.0.14
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
            org.eclipse.jetty:jetty-http:11.0.14
            org.eclipse.jetty:jetty-io:11.0.14
            org.eclipse.jetty:jetty-util:11.0.14
            org.slf4j:slf4j-simple:2.0.6
            org.slf4j:slf4j-api:2.0.6""", StringUtils.join(set_union1, "\n"));

        var set_union2 = new DependencySet(set2);
        set_union2.addAll(set1);
        assertEquals("""
            org.slf4j:slf4j-simple:2.0.6
            org.slf4j:slf4j-api:2.0.6
            org.eclipse.jetty:jetty-server:11.0.14
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
            org.eclipse.jetty:jetty-http:11.0.14
            org.eclipse.jetty:jetty-io:11.0.14
            org.eclipse.jetty:jetty-util:11.0.14""", StringUtils.join(set_union2, "\n"));
    }
}
