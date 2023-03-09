/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.bld.dependencies.exceptions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

public class DependencyResolver {
    public static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    private final String repository_ = "https://repo1.maven.org/maven2/";
    private final String groupPath_;
    private final String artifactUrl_;

    private Xml2MavenMetadata metadata_ = null;

    private final Dependency dependency_;

    public DependencyResolver(Dependency dependency) {
        dependency_ = dependency;
        groupPath_ = dependency_.groupId().replace(".", "/");
        artifactUrl_ = repository_ + groupPath_ + "/" + dependency_.artifactId() + "/";
    }

    public List<Dependency> getDependencies(String scope) {
        var version = dependency_.version();
        if (version.equals(VersionNumber.UNKNOWN)) {
            version = latestVersion();
        }
        return getMavenPom(version).getDependencies(scope);
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
        } catch (ArtifactNotFoundException e) {
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
            var uri = getMetadataUrl();
            try {
                var response_get = repositoryRequest(uri);
                if (response_get.statusCode() != 200) {
                    throw new ArtifactNotFoundException(dependency_, uri, response_get.statusCode());
                }

                metadata = response_get.body();
            } catch (IOException | InterruptedException e) {
                throw new ArtifactRetrievalErrorException(dependency_, uri, e);
            }

            var xml = new Xml2MavenMetadata();
            if (!xml.processXml(metadata)) {
                throw new DependencyXmlParsingErrorException(dependency_, uri, xml.getErrors());
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
        var uri = getPomUrl(version);
        try {
            var response_get = repositoryRequest(uri);
            if (response_get.statusCode() != 200) {
                throw new ArtifactNotFoundException(dependency_, uri, response_get.statusCode());
            }

            pom = response_get.body();
        } catch (IOException | InterruptedException e) {
            throw new ArtifactRetrievalErrorException(dependency_, uri, e);
        }

        var xml = new Xml2MavenPom();
        if (!xml.processXml(pom)) {
            throw new DependencyXmlParsingErrorException(dependency_, uri, xml.getErrors());
        }

        return xml;
    }

    private HttpResponse<String> repositoryRequest(String uri)
    throws IOException, InterruptedException {
        return HttpClient.newHttpClient()
            .send(HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
    }
}
