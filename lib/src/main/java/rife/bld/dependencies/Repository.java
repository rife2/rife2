/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.nio.file.Path;

/**
 * Contains the information required to locate a Maven-compatible repository.
 *
 * @param location the base location of the repository
 * @param username the username to access the repository
 * @param password the password to access the repository
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public record Repository(String location, String username, String password) {
    public static final Repository MAVEN_LOCAL = new Repository(Path.of(System.getProperty("user.home"), ".m2", "repository").toUri().toString());
    public static final Repository MAVEN_CENTRAL = new Repository("https://repo1.maven.org/maven2/");
    public static final Repository SONATYPE_RELEASES = new Repository("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/");
    public static final Repository SONATYPE_SNAPSHOTS = new Repository("https://s01.oss.sonatype.org/content/repositories/snapshots/");
    public static final Repository APACHE = new Repository("https://repo.maven.apache.org/maven2/");

    /**
     * Creates a new repository with only a location.
     *
     * @param location the location to create the repository for
     * @since 1.5
     */
    public Repository(String location) {
        this(location, null, null);
    }

    /**
     * Indicates whether this repository is local.
     *
     * @return {@code true} when this repository is local; or
     * {@code false} otherwise
     * @since 1.5.10
     */
    public boolean isLocal() {
        return location.startsWith("/") || location.startsWith("file:");
    }

    /**
     * Creates a new repository instance of the same location, but with
     * different credentials.
     *
     * @param username the username to access the repository
     * @param password the password to access the repository
     * @return the new repository
     * @since 1.5.10
     */
    public Repository withCredentials(String username, String password) {
        return new Repository(location(), username, password);
    }

    /**
     * Constructs the location for a dependency if it would be located in this repository.
     *
     * @param dependency the dependency to create the location for
     * @return the constructed location
     * @since 1.5.10
     */
    public String getArtifactLocation(Dependency dependency) {
        return getArtifactLocation(dependency.groupId(), dependency.artifactId());
    }

    /**
     * Constructs the location for a dependency if it would be located in this repository.
     *
     * @param groupId    the groupId dependency to create the location for
     * @param artifactId the artifactId dependency to create the location for
     * @return the constructed location
     * @since 1.5.10
     */
    public String getArtifactLocation(String groupId, String artifactId) {
        var group_path = groupId.replace(".", "/");
        var result = new StringBuilder();
        if (isLocal()) {
            if (location.startsWith("file://")) {
                result.append(location.substring("file://".length()));
            } else {
                result.append(location);
            }
        } else {
            result.append(location);
        }
        if (!location.endsWith("/")) {
            result.append("/");
        }
        return result.append(group_path).append("/").append(artifactId).append("/").toString();
    }

    /**
     * Returns the appropriate metadata name.
     *
     * @return the metadata name for this repository.
     * @since 1.5.10
     */
    public String getMetadataName() {
        if (isLocal()) {
            return "maven-metadata-local.xml";
        } else {
            return "maven-metadata.xml";
        }
    }
}