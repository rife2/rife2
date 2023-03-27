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

    public String groupId() {
        return groupId_;
    }

    public PublishInfo groupId(String groupId) {
        groupId_ = groupId;
        return this;
    }

    public String artifactId() {
        return artifactId_;
    }

    public PublishInfo artifactId(String artifactId) {
        artifactId_ = artifactId;
        return this;
    }

    public VersionNumber version() {
        return version_;
    }

    public PublishInfo version(VersionNumber version) {
        version_ = version;
        return this;
    }

    public String name() {
        return name_;
    }

    public PublishInfo name(String name) {
        name_ = name;
        return this;
    }

    public String description() {
        return description_;
    }

    public PublishInfo description(String description) {
        description_ = description;
        return this;
    }

    public String url() {
        return url_;
    }

    public PublishInfo url(String url) {
        url_ = url;
        return this;
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
