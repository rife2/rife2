/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Test;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;
import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Scope.compile;

public class TestDependencyResolver {
    @Test
    void testInstantiation() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertNotNull(resolver);
    }

    @Test
    void testNotFound() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.org.unknown", "voidthing"));
        assertFalse(resolver.exists());
    }

    @Test
    void testCheckExistence() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceVersion() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceMissingVersion() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 3, 9)));
        assertFalse(resolver.exists());
    }

    @Test
    void testListVersions() {
        var resolver1 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var versions1 = resolver1.listVersions();
        assertNotNull(versions1);
        assertFalse(versions1.isEmpty());
        assertFalse(versions1.contains(VersionNumber.UNKNOWN));
        assertTrue(versions1.contains(new VersionNumber(1, 0, 0)));
        assertTrue(versions1.contains(new VersionNumber(1, 2, 1)));

        var resolver2 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server"));
        var versions2 = resolver2.listVersions();
        assertNotNull(versions2);
        assertFalse(versions2.isEmpty());
        assertFalse(versions2.contains(VersionNumber.UNKNOWN));
        assertTrue(versions2.contains(new VersionNumber(9, 4, 51, "v20230217")));
        assertTrue(versions2.contains(new VersionNumber(11, 0, 14)));
    }

    @Test
    void testGetLatestVersion() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.latestVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }

    @Test
    void testGetReleaseVersion() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.releaseVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }

    @Test
    void testGetCompileDependencies() {
        var resolver1 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies1 = resolver1.getDependencies(compile);
        assertNotNull(dependencies1);
        assertEquals(0, dependencies1.size());

        var resolver2 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var dependencies2 = resolver2.getDependencies(compile);
        assertNotNull(dependencies2);
        assertEquals(5, dependencies2.size());
        assertEquals("""
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api
            org.eclipse.jetty:jetty-http
            org.eclipse.jetty:jetty-io
            org.eclipse.jetty:jetty-jmx
            org.slf4j:slf4j-api""", StringUtils.join(dependencies2, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependencies() {
        var resolver1 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies1 = resolver1.getTransitiveDependencies(compile);
        assertNotNull(dependencies1);
        assertEquals(0, dependencies1.size());

        var resolver2 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var dependencies2 = resolver2.getTransitiveDependencies(compile);
        assertNotNull(dependencies2);
        assertEquals(6, dependencies2.size());
        assertEquals("""
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api
            org.eclipse.jetty:jetty-http
            org.eclipse.jetty:jetty-io
            org.eclipse.jetty:jetty-jmx
            org.slf4j:slf4j-api
            org.eclipse.jetty:jetty-util""", StringUtils.join(dependencies2, "\n"));

        var resolver3 = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var dependencies3 = resolver3.getTransitiveDependencies(compile);
        assertNotNull(dependencies3);
        assertEquals(24, dependencies3.size());
        assertEquals("""
            org.springframework.boot:spring-boot:3.0.4
            org.springframework.boot:spring-boot-autoconfigure:3.0.4
            org.springframework.boot:spring-boot-starter-logging:3.0.4
            jakarta.annotation:jakarta.annotation-api:2.1.1
            org.springframework:spring-core:6.0.6
            org.yaml:snakeyaml:1.33
            org.springframework:spring-context:6.0.6
            ch.qos.logback:logback-classic:1.4.5
            org.apache.logging.log4j:log4j-to-slf4j:2.19.0
            org.slf4j:jul-to-slf4j:2.0.6
            org.springframework:spring-jcl:6.0.6
            org.springframework:spring-aop:6.0.6
            org.springframework:spring-beans:6.0.6
            org.springframework:spring-expression:6.0.6
            ch.qos.logback:logback-core
            org.slf4j:slf4j-api
            jakarta.mail:jakarta.mail-api
            jakarta.activation:jakarta.activation-api
            org.codehaus.janino:janino
            org.apache.logging.log4j:log4j-api
            org.osgi:org.osgi.core
            org.codehaus.janino:commons-compiler
            org.fusesource.jansi:jansi
            jakarta.servlet:jakarta.servlet-api""", StringUtils.join(dependencies3, "\n"));
    }
}
