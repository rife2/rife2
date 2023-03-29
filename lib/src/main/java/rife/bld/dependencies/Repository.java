/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

/**
 * Contains the information required to locate a Maven-compatible repository.
 *
 * @param url      the base URL of the repository
 * @param username the username to access the repository
 * @param password the password to access repository
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public record Repository(String url, String username, String password) {
    public static final Repository MAVEN_CENTRAL = new Repository("https://repo1.maven.org/maven2/");
    public static final Repository SONATYPE_SNAPSHOTS = new Repository("https://s01.oss.sonatype.org/content/repositories/snapshots/");

    /**
     * Creates a new repository with only a URL.
     *
     * @param url the dependency to create the URL for
     * @since 1.5
     */
    public Repository(String url) {
        this(url, null, null);
    }

    /**
     * Constructs the URL for a dependency if it would be located in this repository.
     *
     * @param dependency the dependency to create the URL for
     * @return the constructed URL
     * @since 1.5
     */
    public String getArtifactUrl(Dependency dependency) {
        return getArtifactUrl(dependency.groupId(), dependency.artifactId());
    }

    /**
     * Constructs the URL for a dependency if it would be located in this repository.
     *
     * @param groupId    the groupId dependency to create the URL for
     * @param artifactId the artifactId dependency to create the URL for
     * @return the constructed URL
     * @since 1.5.7
     */
    public String getArtifactUrl(String groupId, String artifactId) {
        var group_path = groupId.replace(".", "/");
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }
        return result.append(group_path).append("/").append(artifactId).append("/").toString();
    }
}