/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

import rife.bld.dependencies.*;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

public class PomBuilder {
    private PublishInfo info_ = null;
    private DependencyScopes dependencies_ = new DependencyScopes();

    public PomBuilder info(PublishInfo info) {
        info_ = info;
        return this;
    }

    public PublishInfo info() {
        return info_;
    }

    public PomBuilder dependencies(DependencyScopes dependencies) {
        dependencies_ = dependencies;
        return this;
    }

    public DependencyScopes dependencies() {
        return dependencies_;
    }

    public String build() {
        var t = TemplateFactory.XML.get("bld.pom_blueprint");

        var info = info();
        if (info != null) {
            t.setBean(info);

            if (!info.licenses().isEmpty()) {
                for (var license : info.licenses()) {
                    t.setBean(license, "license-");
                    t.appendBlock("licenses", "license");
                }
                t.setBlock("licenses-tag");
            }

            if (!info.developers().isEmpty()) {
                for (var developer : info.developers()) {
                    t.setBean(developer, "developer-");
                    t.appendBlock("developers", "developer");
                }
                t.setBlock("developers-tag");
            }

            if (info.scm() != null) {
                t.setBean(info.scm(), "scm-");
                t.setBlock("scm-tag");
            }
        }

        if (dependencies() != null && !dependencies().isEmpty()) {
            addDependencies(t, Scope.compile);
            addDependencies(t, Scope.runtime);
            t.setBlock("dependencies-tag");
        }

        return StringUtils.stripBlankLines(t.getContent());
    }

    private void addDependencies(Template t, Scope scope) {
        var scoped_dependencies = dependencies().scope(scope);
        if (!scoped_dependencies.isEmpty()) {
            for (var dependency : scoped_dependencies) {
                t.setValueEncoded("dependency-groupId", dependency.groupId());
                t.setValueEncoded("dependency-artifactId", dependency.artifactId());

                t.blankValue("dependency-version");
                t.blankValue("dependency-version-tag");
                if (!dependency.version().equals(VersionNumber.UNKNOWN)) {
                    t.setValueEncoded("dependency-version", dependency.version());
                    t.setBlock("dependency-version-tag");
                }

                t.blankValue("dependency-type");
                t.blankValue("dependency-type-tag");
                if (!dependency.type().equals("jar")) {
                    t.setValueEncoded("dependency-type", dependency.type());
                    t.setBlock("dependency-type-tag");
                }

                t.blankValue("dependency-classifier");
                t.blankValue("dependency-classifier-tag");
                if (!dependency.classifier().isBlank()) {
                    t.setValueEncoded("dependency-classifier", dependency.classifier());
                    t.setBlock("dependency-classifier-tag");
                }

                t.setValueEncoded("dependency-scope", scope);
                t.blankValue("dependency-exclusions");
                t.blankValue("dependency-exclusions-tag");
                if (!dependency.exclusions().isEmpty()) {
                    for (var exclusion : dependency.exclusions()) {
                        t.setValueEncoded("exclusion-groupId", exclusion.groupId());
                        t.setValueEncoded("exclusion-artifactId", exclusion.artifactId());
                        t.appendBlock("dependency-exclusions", "dependency-exclusion");
                    }
                    t.setBlock("dependency-exclusions-tag");
                }

                t.appendBlock("dependencies", "dependency");
            }
        }
    }
}
