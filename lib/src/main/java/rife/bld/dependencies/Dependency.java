/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;

public record Dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type) {
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
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = (version == null ? VersionNumber.UNKNOWN : version);
        this.classifier = (classifier == null ? "" : classifier);
        this.type = (type == null ? "jar" : type);
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
               version.equals(that.version) &&
               classifier.equals(that.classifier) &&
               type.equals(that.type);
    }

    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier, type);
    }
}
