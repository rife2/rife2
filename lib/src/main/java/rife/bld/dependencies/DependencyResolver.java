/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.bld.DependencySet;
import rife.bld.dependencies.exceptions.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class DependencyResolver {
    public static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    private final Repository repository_;
    private final Dependency dependency_;
    private final String artifactUrl_;

    private Xml2MavenMetadata metadata_ = null;

    public DependencyResolver(Repository repository, Dependency dependency) {
        repository_ = repository;
        dependency_ = dependency;
        artifactUrl_ = repository_.getArtifactUrl(dependency);
    }

    public DependencySet getDependencies(Scope scope) {
        var version = dependency_.version();
        if (version.equals(VersionNumber.UNKNOWN)) {
            version = latestVersion();
        }
        return getMavenPom(version).getDependencies(scope);
    }

    public DependencySet getTransitiveDependencies(Scope scope) {
        var result = new DependencySet();

        var dependencies = getDependencies(scope);
        while (!dependencies.isEmpty()) {
            var it = dependencies.iterator();
            var dependency = it.next();
            it.remove();

            if (!result.contains(dependency)) {
                result.add(dependency);

                dependencies.addAll(new DependencyResolver(repository_, dependency).getDependencies(scope));
            }
        }

        return result;
    }

    public boolean exists() {
        try {
            if (getMavenMetadata() == null) {
                return false;
            }
            if (!dependency_.version().equals(VersionNumber.UNKNOWN)) {
                return getMavenMetadata().getVersions().contains(dependency_.version());
            }
            return true;
        } catch (ArtifactNotFoundException | ArtifactRetrievalErrorException e) {
            return false;
        }
    }

    public List<VersionNumber> listVersions() {
        return getMavenMetadata().getVersions();
    }

    public VersionNumber latestVersion() {
        return getMavenMetadata().getLatest();
    }

    public VersionNumber releaseVersion() {
        return getMavenMetadata().getRelease();
    }

    private String getMetadataUrl() {
        return artifactUrl_ + MAVEN_METADATA_XML;
    }

    private Xml2MavenMetadata getMavenMetadata() {
        if (metadata_ == null) {
            String metadata;
            var url = getMetadataUrl();
            try {
                var content = FileUtils.readString(new URL(url));
                if (content == null) {
                    throw new ArtifactNotFoundException(dependency_, url);
                }

                metadata = content;
            } catch (IOException | FileUtilsErrorException e) {
                throw new ArtifactRetrievalErrorException(dependency_, url, e);
            }

            var xml = new Xml2MavenMetadata();
            if (!xml.processXml(metadata)) {
                throw new DependencyXmlParsingErrorException(dependency_, url, xml.getErrors());
            }

            metadata_ = xml;
        }

        return metadata_;
    }

    private String getPomUrl(VersionNumber version) {
        return artifactUrl_ + version + "/" + dependency_.artifactId() + "-" + version + ".pom";
    }

    private Xml2MavenPom getMavenPom(VersionNumber version) {
        String pom;
        var url = getPomUrl(version);
        try {
            var content = FileUtils.readString(new URL(url));
            if (content == null) {
                throw new ArtifactNotFoundException(dependency_, url);
            }

            pom = content;
        } catch (IOException | FileUtilsErrorException e) {
            throw new ArtifactRetrievalErrorException(dependency_, url, e);
        }

        var xml = new Xml2MavenPom();
        if (!xml.processXml(pom)) {
            throw new DependencyXmlParsingErrorException(dependency_, url, xml.getErrors());
        }

        return xml;
    }
}
