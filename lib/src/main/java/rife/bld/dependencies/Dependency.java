/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;

/**
 * Contains the information required to describe an artifact dependency in the build system.
 *
 * @param groupId the dependency group identifier
 * @param artifactId the dependency artifact identifier
 * @param version the dependency version
 * @param classifier the dependency classier
 * @param type the dependency type
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public record Dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type, ExclusionSet exclusions) {
    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null, null, null);
    }

    public Dependency(String groupId, String artifactId, VersionNumber version) {
        this(groupId, artifactId, version, null, null);
    }

    public Dependency(String groupId, String artifactId, VersionNumber version, String classifier) {
        this(groupId, artifactId, version, classifier, null);
    }

    public Dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type) {
        this(groupId, artifactId, version, classifier, type, null);
    }

    public Dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type, ExclusionSet exclusions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = (version == null ? VersionNumber.UNKNOWN : version);
        this.classifier = (classifier == null ? "" : classifier);
        this.type = (type == null ? "jar" : type);
        this.exclusions = (exclusions == null ? new ExclusionSet() : exclusions);
    }

    /**
     * Returns the base dependency of this dependency, replacing the version number
     * with an unknown version number.
     *
     * @return this dependency's base dependency
     * @since 1.5
     */
    public Dependency baseDependency() {
        return new Dependency(groupId, artifactId, VersionNumber.UNKNOWN, classifier, type);
    }

    /**
     * Adds an exclusion to this dependency.
     *
     * @param groupId the exclusion group identifier, use {@code "*"} to exclude all groupIds
     * @param artifactId the exclusion artifact identifier, use {@code "*"} to exclude all artifactIds
     * @return this dependency instance
     * @since 1.5
     */
    public Dependency exclude(String groupId, String artifactId) {
        exclusions.add(new DependencyExclusion(groupId, artifactId));
        return this;
    }

    public String toString() {
        var result = new StringBuilder(groupId).append(":").append(artifactId);
        if (!version.equals(VersionNumber.UNKNOWN)) {
            result.append(":").append(version);
        }
        if (!classifier.isEmpty()) {
            result.append(":").append(classifier);
        }
        if (!type.isEmpty() && !type.equals("jar")) {
            result.append("@").append(type);
        }
        return result.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (Dependency) o;
        return groupId.equals(that.groupId) &&
               artifactId.equals(that.artifactId) &&
               classifier.equals(that.classifier) &&
               type.equals(that.type);
    }

    public int hashCode() {
        return Objects.hash(groupId, artifactId, classifier, type);
    }
}
