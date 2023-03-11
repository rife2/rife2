/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;

record PomExclusion(String groupId, String artifactId) {
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomExclusion that = (PomExclusion) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId);
    }

    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }
}
