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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.runtime;

public class TestDependencyResolver {
    @Test
    void testInstantiation() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertNotNull(resolver);
    }

    @Test
    void testNotFound() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.org.unknown", "voidthing"));
        assertFalse(resolver.exists());
    }

    @Test
    void testCheckExistence() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceVersion() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 4, 0)));
        assertTrue(resolver.exists());
    }

    @Test
    void testCheckExistenceMissingVersion() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1, 3, 9)));
        assertFalse(resolver.exists());
    }

    @Test
    void testListVersions() {
        var resolver1 = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var versions1 = resolver1.listVersions();
        assertNotNull(versions1);
        assertFalse(versions1.isEmpty());
        assertFalse(versions1.contains(VersionNumber.UNKNOWN));
        assertTrue(versions1.contains(new VersionNumber(1, 0, 0)));
        assertTrue(versions1.contains(new VersionNumber(1, 2, 1)));

        var resolver2 = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.eclipse.jetty", "jetty-server"));
        var versions2 = resolver2.listVersions();
        assertNotNull(versions2);
        assertFalse(versions2.isEmpty());
        assertFalse(versions2.contains(VersionNumber.UNKNOWN));
        assertTrue(versions2.contains(new VersionNumber(9, 4, 51, "v20230217")));
        assertTrue(versions2.contains(new VersionNumber(11, 0, 14)));
    }

    @Test
    void testGetLatestVersion() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.latestVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }

    @Test
    void testGetReleaseVersion() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var version = resolver.releaseVersion();
        assertNotNull(version);
        assertTrue(version.compareTo(new VersionNumber(1, 4)) >= 0);
    }

    @Test
    void testGetCompileDependenciesRIFE2() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileDependenciesRIFE2Snapshot() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1,4,0,"SNAPSHOT")));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileDependenciesJetty() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
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
    void testGetCompileRuntimeDependenciesJunit() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.junit.jupiter", "junit-jupiter", new VersionNumber(5, 9, 2)));
        var dependencies_compile = resolver.getDependencies(compile, runtime);
        assertNotNull(dependencies_compile);
        assertEquals(3, dependencies_compile.size());
        assertEquals("""
            org.junit.jupiter:junit-jupiter-api:5.9.2
            org.junit.jupiter:junit-jupiter-params:5.9.2
            org.junit.jupiter:junit-jupiter-engine:5.9.2""", StringUtils.join(dependencies_compile, "\n"));
    }

    @Test
    void testGetCompileDependenciesSpringBoot() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(6, dependencies.size());
        assertEquals("""
            org.springframework.boot:spring-boot:3.0.4
            org.springframework.boot:spring-boot-autoconfigure:3.0.4
            org.springframework.boot:spring-boot-starter-logging:3.0.4
            jakarta.annotation:jakarta.annotation-api:2.1.1
            org.springframework:spring-core:6.0.6
            org.yaml:snakeyaml:1.33""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileDependenciesMaven() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(26, dependencies.size());
        assertEquals("""
            org.apache.maven:maven-model:3.9.0
            org.apache.maven:maven-settings:3.9.0
            org.apache.maven:maven-settings-builder:3.9.0
            org.apache.maven:maven-builder-support:3.9.0
            org.apache.maven:maven-repository-metadata:3.9.0
            org.apache.maven:maven-artifact:3.9.0
            org.apache.maven:maven-plugin-api:3.9.0
            org.apache.maven:maven-model-builder:3.9.0
            org.apache.maven:maven-resolver-provider:3.9.0
            org.apache.maven.resolver:maven-resolver-impl:1.9.4
            org.apache.maven.resolver:maven-resolver-api:1.9.4
            org.apache.maven.resolver:maven-resolver-spi:1.9.4
            org.apache.maven.resolver:maven-resolver-util:1.9.4
            org.apache.maven.shared:maven-shared-utils:3.3.4
            org.eclipse.sisu:org.eclipse.sisu.plexus:0.3.5
            org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5
            com.google.inject:guice:5.1.0
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
    void testGetCompileDependenciesPlay() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.typesafe.play", "play_2.13", new VersionNumber(2, 8, 19)));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(25, dependencies.size());
        assertEquals("""
            org.scala-lang:scala-library:2.13.10
            com.typesafe.play:build-link:2.8.19
            com.typesafe.play:play-streams_2.13:2.8.19
            com.typesafe.play:twirl-api_2.13:1.5.1
            org.slf4j:slf4j-api:1.7.36
            org.slf4j:jul-to-slf4j:1.7.36
            org.slf4j:jcl-over-slf4j:1.7.36
            com.typesafe.akka:akka-actor_2.13:2.6.20
            com.typesafe.akka:akka-actor-typed_2.13:2.6.20
            com.typesafe.akka:akka-slf4j_2.13:2.6.20
            com.typesafe.akka:akka-serialization-jackson_2.13:2.6.20
            com.fasterxml.jackson.core:jackson-core:2.11.4
            com.fasterxml.jackson.core:jackson-annotations:2.11.4
            com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.11.4
            com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.4
            com.fasterxml.jackson.core:jackson-databind:2.11.4
            com.typesafe.play:play-json_2.13:2.8.2
            com.google.guava:guava:30.1.1-jre
            io.jsonwebtoken:jjwt:0.9.1
            jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
            jakarta.transaction:jakarta.transaction-api:1.3.3
            javax.inject:javax.inject:1
            org.scala-lang.modules:scala-java8-compat_2.13:1.0.2
            com.typesafe:ssl-config-core_2.13:0.4.3
            org.scala-lang.modules:scala-parser-combinators_2.13:1.1.2""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileDependenciesVaadin() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.vaadin", "vaadin", new VersionNumber(23, 3, 7)));
        var dependencies = resolver.getDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(9, dependencies.size());
        assertEquals("""
            com.vaadin:vaadin-core:23.3.7
            com.vaadin:vaadin-board-flow:23.3.7
            com.vaadin:vaadin-charts-flow:23.3.7
            com.vaadin:vaadin-cookie-consent-flow:23.3.7
            com.vaadin:vaadin-crud-flow:23.3.7
            com.vaadin:vaadin-grid-pro-flow:23.3.7
            com.vaadin:vaadin-map-flow:23.3.7
            com.vaadin:vaadin-rich-text-editor-flow:23.3.7
            com.vaadin:collaboration-engine:5.3.0""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesRIFE2() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileTransitiveDependenciesRIFE2Snapshot() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1,4,0,"SNAPSHOT")));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(0, dependencies.size());
    }

    @Test
    void testGetCompileTransitiveDependenciesJetty() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(5, dependencies.size());
        assertEquals("""
            org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
            org.eclipse.jetty:jetty-http:11.0.14
            org.eclipse.jetty:jetty-io:11.0.14
            org.slf4j:slf4j-api:2.0.5
            org.eclipse.jetty:jetty-util:11.0.14""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileRuntimeTransitiveDependenciesJunit() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.junit.jupiter", "junit-jupiter", new VersionNumber(5, 9, 2)));
        var dependencies_compile = resolver.getTransitiveDependencies(compile, runtime);
        assertNotNull(dependencies_compile);
        assertEquals(7, dependencies_compile.size());
        assertEquals("""
            org.junit.jupiter:junit-jupiter-api:5.9.2
            org.junit.jupiter:junit-jupiter-params:5.9.2
            org.junit.jupiter:junit-jupiter-engine:5.9.2
            org.opentest4j:opentest4j:1.2.0
            org.junit.platform:junit-platform-commons:1.9.2
            org.apiguardian:apiguardian-api:1.1.2
            org.junit.platform:junit-platform-engine:1.9.2""", StringUtils.join(dependencies_compile, "\n"));
        var dependencies_runtime = resolver.getTransitiveDependencies(runtime);
        assertNotNull(dependencies_runtime);
        assertEquals(1, dependencies_runtime.size());
        assertEquals("""
            org.junit.jupiter:junit-jupiter-engine:5.9.2""", StringUtils.join(dependencies_runtime, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesSpringBoot() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(17, dependencies.size());
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
            ch.qos.logback:logback-core:1.4.5
            org.slf4j:slf4j-api:2.0.4
            org.apache.logging.log4j:log4j-api:2.19.0""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesMaven() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(31, dependencies.size());
        assertEquals("""
            org.apache.maven:maven-model:3.9.0
            org.apache.maven:maven-settings:3.9.0
            org.apache.maven:maven-settings-builder:3.9.0
            org.apache.maven:maven-builder-support:3.9.0
            org.apache.maven:maven-repository-metadata:3.9.0
            org.apache.maven:maven-artifact:3.9.0
            org.apache.maven:maven-plugin-api:3.9.0
            org.apache.maven:maven-model-builder:3.9.0
            org.apache.maven:maven-resolver-provider:3.9.0
            org.apache.maven.resolver:maven-resolver-impl:1.9.4
            org.apache.maven.resolver:maven-resolver-api:1.9.4
            org.apache.maven.resolver:maven-resolver-spi:1.9.4
            org.apache.maven.resolver:maven-resolver-util:1.9.4
            org.apache.maven.shared:maven-shared-utils:3.3.4
            org.eclipse.sisu:org.eclipse.sisu.plexus:0.3.5
            org.eclipse.sisu:org.eclipse.sisu.inject:0.3.5
            com.google.inject:guice:5.1.0
            com.google.guava:guava:30.1-jre
            com.google.guava:failureaccess:1.0.1
            javax.inject:javax.inject:1
            org.codehaus.plexus:plexus-utils:3.4.2
            org.codehaus.plexus:plexus-classworlds:2.6.0
            org.codehaus.plexus:plexus-interpolation:1.26
            org.codehaus.plexus:plexus-component-annotations:2.1.0
            org.apache.commons:commons-lang3:3.8.1
            org.slf4j:slf4j-api:1.7.36
            org.codehaus.plexus:plexus-sec-dispatcher:2.0
            org.apache.maven.resolver:maven-resolver-named-locks:1.9.4
            javax.annotation:javax.annotation-api:1.2
            aopalliance:aopalliance:1.0
            org.codehaus.plexus:plexus-cipher:2.0""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesPlay() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.typesafe.play", "play_2.13", new VersionNumber(2, 8, 19)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(47, dependencies.size());
        assertEquals("""
            org.scala-lang:scala-library:2.13.10
            com.typesafe.play:build-link:2.8.19
            com.typesafe.play:play-streams_2.13:2.8.19
            com.typesafe.play:twirl-api_2.13:1.5.1
            org.slf4j:slf4j-api:1.7.36
            org.slf4j:jul-to-slf4j:1.7.36
            org.slf4j:jcl-over-slf4j:1.7.36
            com.typesafe.akka:akka-actor_2.13:2.6.20
            com.typesafe.akka:akka-actor-typed_2.13:2.6.20
            com.typesafe.akka:akka-slf4j_2.13:2.6.20
            com.typesafe.akka:akka-serialization-jackson_2.13:2.6.20
            com.fasterxml.jackson.core:jackson-core:2.11.4
            com.fasterxml.jackson.core:jackson-annotations:2.11.4
            com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.11.4
            com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.4
            com.fasterxml.jackson.core:jackson-databind:2.11.4
            com.typesafe.play:play-json_2.13:2.8.2
            com.google.guava:guava:30.1.1-jre
            io.jsonwebtoken:jjwt:0.9.1
            jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
            jakarta.transaction:jakarta.transaction-api:1.3.3
            javax.inject:javax.inject:1
            org.scala-lang.modules:scala-java8-compat_2.13:1.0.2
            com.typesafe:ssl-config-core_2.13:0.4.3
            org.scala-lang.modules:scala-parser-combinators_2.13:1.1.2
            com.typesafe.play:play-exceptions:2.8.19
            org.reactivestreams:reactive-streams:1.0.3
            com.typesafe.akka:akka-stream_2.13:2.6.20
            org.scala-lang.modules:scala-xml_2.13:1.2.0
            com.typesafe:config:1.4.2
            com.fasterxml.jackson.module:jackson-module-parameter-names:2.11.4
            com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.11.4
            com.fasterxml.jackson.module:jackson-module-scala_2.13:2.11.4
            org.lz4:lz4-java:1.8.0
            com.typesafe.play:play-functional_2.13:2.8.2
            org.scala-lang:scala-reflect:2.13.1
            joda-time:joda-time:2.10.5
            com.google.guava:failureaccess:1.0.1
            com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
            com.google.code.findbugs:jsr305:3.0.2
            org.checkerframework:checker-qual:3.8.0
            com.google.errorprone:error_prone_annotations:2.5.1
            com.google.j2objc:j2objc-annotations:1.3
            jakarta.activation:jakarta.activation-api:1.2.2
            com.typesafe.akka:akka-protobuf-v3_2.13:2.6.20
            com.fasterxml.jackson.module:jackson-module-paranamer:2.11.4
            com.thoughtworks.paranamer:paranamer:2.8""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testGetCompileTransitiveDependenciesVaadin() {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.vaadin", "vaadin", new VersionNumber(23, 3, 7)));
        var dependencies = resolver.getTransitiveDependencies(compile);
        assertNotNull(dependencies);
        assertEquals(87, dependencies.size());
        assertEquals("""
            com.vaadin:vaadin-core:23.3.7
            com.vaadin:vaadin-board-flow:23.3.7
            com.vaadin:vaadin-charts-flow:23.3.7
            com.vaadin:vaadin-cookie-consent-flow:23.3.7
            com.vaadin:vaadin-crud-flow:23.3.7
            com.vaadin:vaadin-grid-pro-flow:23.3.7
            com.vaadin:vaadin-map-flow:23.3.7
            com.vaadin:vaadin-rich-text-editor-flow:23.3.7
            com.vaadin:collaboration-engine:5.3.0
            com.vaadin:flow-server:23.3.4
            com.vaadin:vaadin-dev-server:23.3.4
            com.vaadin:flow-lit-template:23.3.4
            com.vaadin:flow-polymer-template:23.3.4
            com.vaadin:flow-push:23.3.4
            com.vaadin:flow-client:23.3.4
            com.vaadin:flow-html-components:23.3.4
            com.vaadin:flow-data:23.3.4
            com.vaadin:flow-dnd:23.3.4
            com.vaadin:vaadin-lumo-theme:23.3.7
            com.vaadin:vaadin-material-theme:23.3.7
            com.vaadin:vaadin-accordion-flow:23.3.7
            com.vaadin:vaadin-avatar-flow:23.3.7
            com.vaadin:vaadin-button-flow:23.3.7
            com.vaadin:vaadin-checkbox-flow:23.3.7
            com.vaadin:vaadin-combo-box-flow:23.3.7
            com.vaadin:vaadin-confirm-dialog-flow:23.3.7
            com.vaadin:vaadin-custom-field-flow:23.3.7
            com.vaadin:vaadin-date-picker-flow:23.3.7
            com.vaadin:vaadin-date-time-picker-flow:23.3.7
            com.vaadin:vaadin-details-flow:23.3.7
            com.vaadin:vaadin-time-picker-flow:23.3.7
            com.vaadin:vaadin-select-flow:23.3.7
            com.vaadin:vaadin-dialog-flow:23.3.7
            com.vaadin:vaadin-form-layout-flow:23.3.7
            com.vaadin:vaadin-field-highlighter-flow:23.3.7
            com.vaadin:vaadin-grid-flow:23.3.7
            com.vaadin:vaadin-icons-flow:23.3.7
            com.vaadin:vaadin-iron-list-flow:23.3.7
            com.vaadin:vaadin-virtual-list-flow:23.3.7
            com.vaadin:vaadin-list-box-flow:23.3.7
            com.vaadin:vaadin-login-flow:23.3.7
            com.vaadin:vaadin-messages-flow:23.3.7
            com.vaadin:vaadin-ordered-layout-flow:23.3.7
            com.vaadin:vaadin-progress-bar-flow:23.3.7
            com.vaadin:vaadin-radio-button-flow:23.3.7
            com.vaadin:vaadin-renderer-flow:23.3.7
            com.vaadin:vaadin-split-layout-flow:23.3.7
            com.vaadin:vaadin-tabs-flow:23.3.7
            com.vaadin:vaadin-text-field-flow:23.3.7
            com.vaadin:vaadin-upload-flow:23.3.7
            com.vaadin:vaadin-notification-flow:23.3.7
            com.vaadin:vaadin-app-layout-flow:23.3.7
            com.vaadin:vaadin-context-menu-flow:23.3.7
            com.vaadin:vaadin-menu-bar-flow:23.3.7
            com.fasterxml.jackson.core:jackson-databind:2.14.1
            com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1
            com.vaadin:license-checker:1.5.1
            com.vaadin.servletdetector:throw-if-servlet5:1.0.2
            org.slf4j:slf4j-api:1.7.36
            javax.annotation:javax.annotation-api:1.3.2
            com.vaadin.external.gwt:gwt-elemental:2.8.2.vaadin2
            commons-fileupload:commons-fileupload:1.4
            commons-io:commons-io:2.11.0
            com.fasterxml.jackson.core:jackson-core:2.14.1
            org.jsoup:jsoup:1.15.3
            com.helger:ph-css:6.5.0
            net.bytebuddy:byte-buddy:1.12.20
            com.vaadin.external:gentyref:1.2.0.vaadin1
            org.apache.commons:commons-compress:1.22
            org.apache.httpcomponents:httpclient:4.5.13
            commons-codec:commons-codec:1.15
            com.vaadin:open:8.5.0
            com.vaadin.external.atmosphere:atmosphere-runtime:2.7.3.slf4jvaadin4
            javax.validation:validation-api:2.0.1.Final
            org.webjars.npm:vaadin__vaadin-mobile-drag-drop:1.0.1
            org.webjars.npm:mobile-drag-drop:2.3.0-rc.2
            com.vaadin:vaadin-flow-components-base:23.3.7
            org.apache.commons:commons-lang3:3.12.0
            com.fasterxml.jackson.core:jackson-annotations
            com.github.oshi:oshi-core:6.1.6
            com.auth0:java-jwt:3.19.2
            com.helger.commons:ph-commons
            org.apache.httpcomponents:httpcore:4.4.13
            commons-logging:commons-logging:1.2
            net.java.dev.jna:jna:5.11.0
            net.java.dev.jna:jna-platform:5.11.0
            com.google.code.findbugs:jsr305:3.0.2""", StringUtils.join(dependencies, "\n"));
    }

    @Test
    void testDownloadDependency()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2"));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

            var files = FileUtils.getFileList(tmp);
            assertEquals(1, files.size());
            assertTrue(files.contains("rife2-1.4.0.jar"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencySnapshot()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.uwyn.rife2", "rife2", new VersionNumber(1,4,0,"SNAPSHOT")));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

            var files = FileUtils.getFileList(tmp);
            assertEquals(1, files.size());
            assertTrue(files.contains("rife2-1.4.0-20230303.130437-4.jar"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencyJetty()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.eclipse.jetty", "jetty-server", new VersionNumber(11, 0, 14)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);
            var files = FileUtils.getFileList(tmp);
            assertEquals(6, files.size());
            Collections.sort(files);
            assertEquals("""
                jetty-http-11.0.14.jar
                jetty-io-11.0.14.jar
                jetty-jakarta-servlet-api-5.0.2.jar
                jetty-server-11.0.14.jar
                jetty-util-11.0.14.jar
                slf4j-api-2.0.5.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependenciesJunit()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.junit.jupiter", "junit-jupiter", new VersionNumber(5, 9, 2)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile, runtime);

            var files = FileUtils.getFileList(tmp);
            assertEquals(8, files.size());
            Collections.sort(files);
            assertEquals("""
                apiguardian-api-1.1.2.jar
                junit-jupiter-5.9.2.jar
                junit-jupiter-api-5.9.2.jar
                junit-jupiter-engine-5.9.2.jar
                junit-jupiter-params-5.9.2.jar
                junit-platform-commons-1.9.2.jar
                junit-platform-engine-1.9.2.jar
                opentest4j-1.2.0.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencySpringBoot()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.springframework.boot", "spring-boot-starter", new VersionNumber(3, 0, 4)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

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
                slf4j-api-2.0.4.jar
                snakeyaml-1.33.jar
                spring-aop-6.0.6.jar
                spring-beans-6.0.6.jar
                spring-boot-3.0.4.jar
                spring-boot-autoconfigure-3.0.4.jar
                spring-boot-starter-3.0.4.jar
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
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("org.apache.maven", "maven-core", new VersionNumber(3, 9, 0)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

            var files = FileUtils.getFileList(tmp);
            assertEquals(32, files.size());
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
                maven-core-3.9.0.jar
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

    @Test
    void testDownloadDependencyPlay()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.typesafe.play", "play_2.13", new VersionNumber(2, 8, 19)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

            var files = FileUtils.getFileList(tmp);
            assertEquals(48, files.size());
            Collections.sort(files);
            assertEquals("""
                akka-actor-typed_2.13-2.6.20.jar
                akka-actor_2.13-2.6.20.jar
                akka-protobuf-v3_2.13-2.6.20.jar
                akka-serialization-jackson_2.13-2.6.20.jar
                akka-slf4j_2.13-2.6.20.jar
                akka-stream_2.13-2.6.20.jar
                build-link-2.8.19.jar
                checker-qual-3.8.0.jar
                config-1.4.2.jar
                error_prone_annotations-2.5.1.jar
                failureaccess-1.0.1.jar
                guava-30.1.1-jre.jar
                j2objc-annotations-1.3.jar
                jackson-annotations-2.11.4.jar
                jackson-core-2.11.4.jar
                jackson-databind-2.11.4.jar
                jackson-dataformat-cbor-2.11.4.jar
                jackson-datatype-jdk8-2.11.4.jar
                jackson-datatype-jsr310-2.11.4.jar
                jackson-module-parameter-names-2.11.4.jar
                jackson-module-paranamer-2.11.4.jar
                jackson-module-scala_2.13-2.11.4.jar
                jakarta.activation-api-1.2.2.jar
                jakarta.transaction-api-1.3.3.jar
                jakarta.xml.bind-api-2.3.3.jar
                javax.inject-1.jar
                jcl-over-slf4j-1.7.36.jar
                jjwt-0.9.1.jar
                joda-time-2.10.5.jar
                jsr305-3.0.2.jar
                jul-to-slf4j-1.7.36.jar
                listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar
                lz4-java-1.8.0.jar
                paranamer-2.8.jar
                play-exceptions-2.8.19.jar
                play-functional_2.13-2.8.2.jar
                play-json_2.13-2.8.2.jar
                play-streams_2.13-2.8.19.jar
                play_2.13-2.8.19.jar
                reactive-streams-1.0.3.jar
                scala-java8-compat_2.13-1.0.2.jar
                scala-library-2.13.10.jar
                scala-parser-combinators_2.13-1.1.2.jar
                scala-reflect-2.13.1.jar
                scala-xml_2.13-1.2.0.jar
                slf4j-api-1.7.36.jar
                ssl-config-core_2.13-0.4.3.jar
                twirl-api_2.13-1.5.1.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testDownloadDependencyVaadin()
    throws Exception {
        var resolver = new DependencyResolver(List.of(MAVEN_CENTRAL, SONATYPE_SNAPSHOTS), new Dependency("com.vaadin", "vaadin", new VersionNumber(23, 3, 7)));
        var tmp = Files.createTempDirectory("downloads").toFile();
        try {
            resolver.downloadTransitivelyIntoFolder(tmp, compile);

            var files = FileUtils.getFileList(tmp);
            assertEquals(88, files.size());
            Collections.sort(files);
            assertEquals("""
                atmosphere-runtime-2.7.3.slf4jvaadin4.jar
                byte-buddy-1.12.20.jar
                collaboration-engine-5.3.0.jar
                commons-codec-1.15.jar
                commons-compress-1.22.jar
                commons-fileupload-1.4.jar
                commons-io-2.11.0.jar
                commons-lang3-3.12.0.jar
                commons-logging-1.2.jar
                flow-client-23.3.4.jar
                flow-data-23.3.4.jar
                flow-dnd-23.3.4.jar
                flow-html-components-23.3.4.jar
                flow-lit-template-23.3.4.jar
                flow-polymer-template-23.3.4.jar
                flow-push-23.3.4.jar
                flow-server-23.3.4.jar
                gentyref-1.2.0.vaadin1.jar
                gwt-elemental-2.8.2.vaadin2.jar
                httpclient-4.5.13.jar
                httpcore-4.4.13.jar
                jackson-annotations-2.14.2.jar
                jackson-core-2.14.1.jar
                jackson-databind-2.14.1.jar
                jackson-datatype-jsr310-2.14.1.jar
                java-jwt-3.19.2.jar
                javax.annotation-api-1.3.2.jar
                jna-5.11.0.jar
                jna-platform-5.11.0.jar
                jsoup-1.15.3.jar
                jsr305-3.0.2.jar
                license-checker-1.5.1.jar
                mobile-drag-drop-2.3.0-rc.2.jar
                open-8.5.0.jar
                oshi-core-6.1.6.jar
                ph-commons-11.0.2.jar
                ph-css-6.5.0.jar
                slf4j-api-1.7.36.jar
                throw-if-servlet5-1.0.2.jar
                vaadin-23.3.7.jar
                vaadin-accordion-flow-23.3.7.jar
                vaadin-app-layout-flow-23.3.7.jar
                vaadin-avatar-flow-23.3.7.jar
                vaadin-board-flow-23.3.7.jar
                vaadin-button-flow-23.3.7.jar
                vaadin-charts-flow-23.3.7.jar
                vaadin-checkbox-flow-23.3.7.jar
                vaadin-combo-box-flow-23.3.7.jar
                vaadin-confirm-dialog-flow-23.3.7.jar
                vaadin-context-menu-flow-23.3.7.jar
                vaadin-cookie-consent-flow-23.3.7.jar
                vaadin-core-23.3.7.jar
                vaadin-crud-flow-23.3.7.jar
                vaadin-custom-field-flow-23.3.7.jar
                vaadin-date-picker-flow-23.3.7.jar
                vaadin-date-time-picker-flow-23.3.7.jar
                vaadin-details-flow-23.3.7.jar
                vaadin-dev-server-23.3.4.jar
                vaadin-dialog-flow-23.3.7.jar
                vaadin-field-highlighter-flow-23.3.7.jar
                vaadin-flow-components-base-23.3.7.jar
                vaadin-form-layout-flow-23.3.7.jar
                vaadin-grid-flow-23.3.7.jar
                vaadin-grid-pro-flow-23.3.7.jar
                vaadin-icons-flow-23.3.7.jar
                vaadin-iron-list-flow-23.3.7.jar
                vaadin-list-box-flow-23.3.7.jar
                vaadin-login-flow-23.3.7.jar
                vaadin-lumo-theme-23.3.7.jar
                vaadin-map-flow-23.3.7.jar
                vaadin-material-theme-23.3.7.jar
                vaadin-menu-bar-flow-23.3.7.jar
                vaadin-messages-flow-23.3.7.jar
                vaadin-notification-flow-23.3.7.jar
                vaadin-ordered-layout-flow-23.3.7.jar
                vaadin-progress-bar-flow-23.3.7.jar
                vaadin-radio-button-flow-23.3.7.jar
                vaadin-renderer-flow-23.3.7.jar
                vaadin-rich-text-editor-flow-23.3.7.jar
                vaadin-select-flow-23.3.7.jar
                vaadin-split-layout-flow-23.3.7.jar
                vaadin-tabs-flow-23.3.7.jar
                vaadin-text-field-flow-23.3.7.jar
                vaadin-time-picker-flow-23.3.7.jar
                vaadin-upload-flow-23.3.7.jar
                vaadin-virtual-list-flow-23.3.7.jar
                vaadin__vaadin-mobile-drag-drop-1.0.1.jar
                validation-api-2.0.1.Final.jar""", StringUtils.join(files, "\n"));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
