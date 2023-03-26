/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.publish;

import rife.bld.dependencies.VersionNumber;

import java.util.ArrayList;
import java.util.List;

public class PublishInfo {
    private String groupId_ = null;
    private String artifactId_ = null;
    private VersionNumber version_ = null;
    private String name_ = null;
    private String description_ = null;
    private String url_ = null;

    private final List<PublishLicense> licenses_ = new ArrayList<>();
    private final List<PublishDeveloper> developers_ = new ArrayList<>();
    private PublishScm scm_ = null;

    public String getGroupId() {
        return groupId_;
    }

    public PublishInfo groupId(String groupId) {
        setGroupId(groupId);
        return this;
    }

    public void setGroupId(String groupId) {
        groupId_ = groupId;
    }

    public String getArtifactId() {
        return artifactId_;
    }

    public PublishInfo artifactId(String artifactId) {
        setArtifactId(artifactId);
        return this;
    }

    public void setArtifactId(String artifactId) {
        artifactId_ = artifactId;
    }

    public VersionNumber getVersion() {
        return version_;
    }

    public PublishInfo version(VersionNumber version) {
        setVersion(version);
        return this;
    }

    public void setVersion(VersionNumber version) {
        version_ = version;
    }

    public String getName() {
        return name_;
    }

    public PublishInfo name(String name) {
        setName(name);
        return this;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getDescription() {
        return description_;
    }

    public PublishInfo description(String description) {
        setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        description_ = description;
    }

    public String getUrl() {
        return url_;
    }

    public PublishInfo url(String url) {
        setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        url_ = url;
    }

    public PublishInfo developer(PublishDeveloper developer) {
        developers_.add(developer);
        return this;
    }

    public PublishInfo developers(List<PublishDeveloper> developers) {
        developers_.addAll(developers);
        return this;
    }

    public List<PublishDeveloper> developers() {
        return developers_;
    }

    public PublishInfo license(PublishLicense license) {
        licenses_.add(license);
        return this;
    }

    public PublishInfo licenses(List<PublishLicense> licenses) {
        licenses_.addAll(licenses);
        return this;
    }

    public List<PublishLicense> licenses() {
        return licenses_;
    }

    public PublishInfo scm(PublishScm scm) {
        scm_ = scm;
        return this;
    }

    public PublishScm scm() {
        return scm_;
    }
}
