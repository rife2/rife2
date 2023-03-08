/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.dependencies;

public record Dependency(String groupId, String artifactId, VersionNumber version) {
    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public Dependency(String groupId, String artifactId, VersionNumber version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = (version == null ? VersionNumber.UNKNOWN : version);
    }

    public String toString() {
        if (version.equals(VersionNumber.UNKNOWN)) {
            return groupId + ":" + artifactId;
        }
        return groupId + ":" + artifactId + ":" + version;
    }
}
