/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

/**
 * Contains the information required to locate a Maven-compatible repository.
 *
 * @param url the base URL of the repository
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public record Repository(String url) {
    public static final Repository MAVEN_CENTRAL = new Repository("https://repo1.maven.org/maven2/");
    public static final Repository SONATYPE_SNAPSHOTS = new Repository("https://s01.oss.sonatype.org/content/repositories/snapshots/");

    /**
     * Constructs the artifact URL for a dependency if it would be located in this repository.
     *
     * @param dependency the dependency to create the URL for
     * @return the constructed artifact URL
     * @since 1.5
     */
    public String getArtifactUrl(Dependency dependency) {
        var group_path = dependency.groupId().replace(".", "/");
        var result = new StringBuilder(url);
        if (!url.endsWith("/")) {
            result.append("/");
        }
        return result.append(group_path).append("/").append(dependency.artifactId()).append("/").toString();
    }
}