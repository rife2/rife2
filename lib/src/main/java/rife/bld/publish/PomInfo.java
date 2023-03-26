/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

import java.util.ArrayList;
import java.util.List;

public class PomInfo {
    private String groupId_ = null;
    private String artifactId_ = null;
    private String version_ = null;
    private String name_ = null;
    private String description_ = null;
    private String url_ = null;

    private final List<PomLicense> licenses_ = new ArrayList<>();
    private final List<PomDeveloper> developers_ = new ArrayList<>();
    private PomScm scm_ = null;

    public String getGroupId() {
        return groupId_;
    }

    public PomInfo groupId(String groupId) {
        setGroupId(groupId);
        return this;
    }

    public void setGroupId(String groupId) {
        groupId_ = groupId;
    }

    public String getArtifactId() {
        return artifactId_;
    }

    public PomInfo artifactId(String artifactId) {
        setArtifactId(artifactId);
        return this;
    }

    public void setArtifactId(String artifactId) {
        artifactId_ = artifactId;
    }

    public String getVersion() {
        return version_;
    }

    public PomInfo version(String version) {
        setVersion(version);
        return this;
    }

    public void setVersion(String version) {
        version_ = version;
    }

    public String getName() {
        return name_;
    }

    public PomInfo name(String name) {
        setName(name);
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getDescription() {
        return description_;
    }

    public PomInfo description(String description) {
        setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        description_ = description;
    }

    public String getUrl() {
        return url_;
    }

    public PomInfo url(String url) {
        setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        url_ = url;
    }

    public PomInfo developer(PomDeveloper developer) {
        developers_.add(developer);
        return this;
    }

    public PomInfo developers(List<PomDeveloper> developers) {
        developers_.addAll(developers);
        return this;
    }

    public List<PomDeveloper> developers() {
        return developers_;
    }

    public PomInfo license(PomLicense license) {
        licenses_.add(license);
        return this;
    }

    public PomInfo licenses(List<PomLicense> licenses) {
        licenses_.addAll(licenses);
        return this;
    }

    public List<PomLicense> licenses() {
        return licenses_;
    }

    public PomInfo scm(PomScm scm) {
        scm_ = scm;
        return this;
    }

    public PomScm scm() {
        return scm_;
    }
}
