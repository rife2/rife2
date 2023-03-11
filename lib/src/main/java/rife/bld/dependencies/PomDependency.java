/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import java.util.Objects;
import java.util.Set;

record PomDependency(String groupId, String artifactId, String version, String classifier, String type, String scope, String optional, Set<PomExclusion> exclusions) {
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomDependency that = (PomDependency) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(classifier, that.classifier) && Objects.equals(type, that.type);
    }

    public int hashCode() {
        return Objects.hash(groupId, artifactId, classifier, type);
    }
}
