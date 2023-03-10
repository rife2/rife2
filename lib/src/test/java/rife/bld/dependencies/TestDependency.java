/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Test;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;
import static rife.bld.dependencies.Scope.compile;

public class TestDependency {
    @Test
    void testInstantiation() {
        var dependency1 = new Dependency("com.uwyn.rife2", "rife2");
        assertNotNull(dependency1);
        assertEquals("com.uwyn.rife2", dependency1.groupId());
        assertEquals("rife2", dependency1.artifactId());
        assertEquals(VersionNumber.UNKNOWN, dependency1.version());
        assertEquals("", dependency1.classifier());
        assertEquals("jar", dependency1.type());

        var dependency2 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0));
        assertNotNull(dependency2);
        assertEquals("com.uwyn.rife2", dependency2.groupId());
        assertEquals("rife2", dependency2.artifactId());
        assertEquals(new VersionNumber(1, 4, 0), dependency2.version());
        assertEquals("", dependency2.classifier());
        assertEquals("jar", dependency2.type());

        var dependency3 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0), "agent");
        assertNotNull(dependency3);
        assertEquals("com.uwyn.rife2", dependency3.groupId());
        assertEquals("rife2", dependency3.artifactId());
        assertEquals(new VersionNumber(1, 4, 0), dependency3.version());
        assertEquals("agent", dependency3.classifier());
        assertEquals("jar", dependency3.type());

        var dependency4 = new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0), "standalone", "zip");
        assertNotNull(dependency4);
        assertEquals("com.uwyn.rife2", dependency4.groupId());
        assertEquals("rife2", dependency4.artifactId());
        assertEquals(new VersionNumber(1, 4, 0), dependency4.version());
        assertEquals("standalone", dependency4.classifier());
        assertEquals("zip", dependency4.type());
    }

   @Test
    void testToString() {
       assertEquals("com.uwyn.rife2:rife2", new Dependency("com.uwyn.rife2", "rife2").toString());
       assertEquals("com.uwyn.rife2:rife2:1.4.0", new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)).toString());
       assertEquals("com.uwyn.rife2:rife2:1.4.0:agent", new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0), "agent").toString());
       assertEquals("com.uwyn.rife2:rife2:1.4.0:standalone@zip", new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0), "standalone", "zip").toString());
    }
}
