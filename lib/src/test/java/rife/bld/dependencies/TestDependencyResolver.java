/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import org.junit.jupiter.api.Disabled;
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
        assertEquals(17, dependencies.size());
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
            org.slf4j:jul-to-slf4j:2.0.6
            jakarta.annotation:jakarta.annotation-api:2.1.1
            org.springframework:spring-core:6.0.6
            org.springframework:spring-jcl:6.0.6
            org.yaml:snakeyaml:1.33""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesMaven() {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(31, dependencies.size());
        assertEquals("""
            org.apache.maven:maven-model:3.9.0
            org.apache.maven:maven-settings:3.9.0
            org.apache.maven:maven-settings-builder:3.9.0
            org.codehaus.plexus:plexus-sec-dispatcher:2.0
            org.codehaus.plexus:plexus-cipher:2.0
            org.apache.maven:maven-builder-support:3.9.0
            org.apache.maven:maven-repository-metadata:3.9.0
            org.apache.maven:maven-artifact:3.9.0
            org.apache.maven:maven-plugin-api:3.9.0
            org.apache.maven:maven-model-builder:3.9.0
            org.apache.maven:maven-resolver-provider:3.9.0
            org.apache.maven.resolver:maven-resolver-impl:1.9.4
            org.apache.maven.resolver:maven-resolver-named-locks:1.9.4
            org.apache.maven.resolver:maven-resolver-api:1.9.4
            org.apache.maven.resolver:maven-resolver-spi:1.9.4
            org.apache.maven.resolver:maven-resolver-util:1.9.4
            org.apache.maven.shared:maven-shared-utils:3.3.4
            org.eclipse.sisu:org.eclipse.sisu.plexus:0.3.5
            javax.annotation:javax.annotation-api:1.2
            org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5
            com.google.inject:guice:5.1.0
            aopalliance:aopalliance:1.0
            com.google.guava:guava:30.1-jre
            com.google.guava:failureaccess:1.0.1
            javax.inject:javax.inject:1
            org.codehaus.plexus:plexus-utils:3.4.2
            org.codehaus.plexus:plexus-classworlds:2.6.0
            org.codehaus.plexus:plexus-interpolation:1.26
            org.codehaus.plexus:plexus-component-annotations:2.1.0
            org.apache.commons:commons-lang3:3.8.1
            org.slf4j:slf4j-api:1.7.36""", StringUtils.join(dependencies, "\n"));
    }

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
            assertEquals(17, files.size());
            Collections.sort(files);
            assertEquals("""
                jakarta.annotation-api-2.1.1.jar
                jul-to-slf4j-2.0.6.jar
                log4j-api-2.19.0.jar
                log4j-to-slf4j-2.19.0.jar
                logback-classic-1.4.5.jar
                logback-core-1.4.5.jar
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

    @Test
    void testDownloadDependencyMaven()
    throws Exception {
        var resolver = new DependencyResolver(MAVEN_CENTRAL, new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            for (var dep : resolver.getTransitiveDependencies(compile)) {
                new DependencyResolver(MAVEN_CENTRAL, dep).downloadIntoFolder(tmp);
            }
            var files = FileUtils.getFileList(tmp);
            assertEquals(31, files.size());
            Collections.sort(files);
            assertEquals("""
                aopalliance-1.0.jar
                commons-lang3-3.8.1.jar
                failureaccess-1.0.1.jar
                guava-30.1-jre.jar
                guice-5.1.0.jar
                javax.annotation-api-1.2.jar
                javax.inject-1.jar
                maven-artifact-3.9.0.jar
                maven-builder-support-3.9.0.jar
                maven-model-3.9.0.jar
                maven-model-builder-3.9.0.jar
                maven-plugin-api-3.9.0.jar
                maven-repository-metadata-3.9.0.jar
                maven-resolver-api-1.9.4.jar
                maven-resolver-impl-1.9.4.jar
                maven-resolver-named-locks-1.9.4.jar
                maven-resolver-provider-3.9.0.jar
                maven-resolver-spi-1.9.4.jar
                maven-resolver-util-1.9.4.jar
                maven-settings-3.9.0.jar
                maven-settings-builder-3.9.0.jar
                maven-shared-utils-3.3.4.jar
                org.eclipse.sisu.inject-0.3.5.jar
                org.eclipse.sisu.plexus-0.3.5.jar
                plexus-cipher-2.0.jar
                plexus-classworlds-2.6.0.jar
                plexus-component-annotations-2.1.0.jar
                plexus-interpolation-1.26.jar
                plexus-sec-dispatcher-2.0.jar
                plexus-utils-3.4.2.jar
                slf4j-api-1.7.36.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
