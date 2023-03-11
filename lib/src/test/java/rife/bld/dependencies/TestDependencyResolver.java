/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Test;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.nio.file.Files;
import java.util.Collections;

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
    void testGetCompileDependenciesRIFE2() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileDependenciesJetty() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(4, dependencies.size());
        assertEquals("""
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
            org.eclipse.jetty:jetty-http:11.0.14
            org.eclipse.jetty:jetty-io:11.0.14
            org.slf4j:slf4j-api:2.0.5""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesRIFE2() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileTransitiveDependenciesJetty() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(5, dependencies.size());
        assertEquals("""
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
            org.eclipse.jetty:jetty-http:11.0.14
            org.eclipse.jetty:jetty-util:11.0.14
            org.eclipse.jetty:jetty-io:11.0.14
            org.slf4j:slf4j-api:2.0.5""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesSpringBoot() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(18, dependencies.size());
        assertEquals("""
            org.springframework.boot:spring-boot:3.0.4
            org.springframework:spring-context:6.0.6
            org.springframework:spring-aop:6.0.6
            org.springframework:spring-beans:6.0.6
            org.springframework:spring-expression:6.0.6
            org.springframework.boot:spring-boot-autoconfigure:3.0.4
            org.springframework.boot:spring-boot-starter-logging:3.0.4
            ch.qos.logback:logback-classic:1.4.5
            ch.qos.logback:logback-core:1.4.5
            org.slf4j:slf4j-api:2.0.4
            org.apache.logging.log4j:log4j-to-slf4j:2.19.0
            org.apache.logging.log4j:log4j-api:2.19.0
            org.osgi:org.osgi.core:6.0.0
            org.slf4j:jul-to-slf4j:2.0.6
            jakarta.annotation:jakarta.annotation-api:2.1.1
            org.springframework:spring-core:6.0.6
            org.springframework:spring-jcl:6.0.6
            org.yaml:snakeyaml:1.33""", StringUtils.join(dependencies, "\n"));
    }

//    @Test
//    void testGetCompileTransitiveDependenciesMaven() {
//        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
//        var dependencies = resolver.getTransitiveDependencies(compile);
//        assertNotNull(dependencies);
//        assertEquals(0, dependencies.size());
//        assertEquals("""
//            """, StringUtils.join(dependencies, "\n"));
//    }

    @Test
    void testDownloadDependency()
    throws Exception {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("com.uwyn.rife2", "rife2"));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadIntoFolder(tmp);
            var files = FileUtils.getFileList(tmp);
            assertEquals(1, files.size());
            assertTrue(files.contains("rife2-1.4.0.jar"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencyJetty()
    throws Exception {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            for (var dep : resolver.getTransitiveDependencies(compile)) {
                new DependencyResolver(MAVEN_CENTRAL, dep).downloadIntoFolder(tmp);
            }
            var files = FileUtils.getFileList(tmp);
            assertEquals(5, files.size());
            Collections.sort(files);
            assertEquals("""
                jetty-http-11.0.14.jar
                jetty-io-11.0.14.jar
                jetty-jakarta-servlet-api-5.0.2.jar
                jetty-util-11.0.14.jar
                slf4j-api-2.0.5.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencySpringBoot()
    throws Exception {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            for (var dep : resolver.getTransitiveDependencies(compile)) {
                new DependencyResolver(MAVEN_CENTRAL, dep).downloadIntoFolder(tmp);
            }
            var files = FileUtils.getFileList(tmp);
            assertEquals(18, files.size());
            Collections.sort(files);
            assertEquals("""
                jakarta.annotation-api-2.1.1.jar
                jul-to-slf4j-2.0.6.jar
                log4j-api-2.19.0.jar
                log4j-to-slf4j-2.19.0.jar
                logback-classic-1.4.5.jar
                logback-core-1.4.5.jar
                org.osgi.core-6.0.0.jar
                slf4j-api-2.0.4.jar
                snakeyaml-1.33.jar
                spring-aop-6.0.6.jar
                spring-beans-6.0.6.jar
                spring-boot-3.0.4.jar
                spring-boot-autoconfigure-3.0.4.jar
                spring-boot-starter-logging-3.0.4.jar
                spring-context-6.0.6.jar
                spring-core-6.0.6.jar
                spring-expression-6.0.6.jar
                spring-jcl-6.0.6.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }

    }
}
