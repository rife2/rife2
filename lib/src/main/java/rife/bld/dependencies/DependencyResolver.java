/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.bld.DependencySet;
import rife.bld.dependencies.exceptions.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.util.ArrayList;
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

    public VersionNumber resolveVersion() {
        var version = dependency_.version();
        if (version.equals(VersionNumber.UNKNOWN)) {
            return latestVersion();
        }
        return version;
    }

    public DependencySet getDependencies(Scope scope) {
        return getMavenPom().getDependencies(scope);
    }

    public DependencySet getTransitiveDependencies(Scope scope) {
        var result = new DependencySet();

        var dependencies = new ArrayList<>(getDependencies(scope));
        while (!dependencies.isEmpty()) {
            var it = dependencies.iterator();
            var dependency = it.next();
            it.remove();

            // TODO : support exclusions
            if (!result.contains(dependency)) {
                result.add(dependency);

                var next_dependencies = new DependencyResolver(repository_, dependency).getDependencies(scope);
                dependencies.forEach(next_dependencies::remove);
                dependencies.addAll(0, next_dependencies);
            }
        }

        return result;
    }

    public String getDownloadUrl(VersionNumber version) {
        var result = new StringBuilder(artifactUrl_);
        result.append(version).append("/").append(dependency_.artifactId()).append("-").append(version);
        if (!dependency_.classifier().isEmpty()) {
            result.append("-").append(dependency_.classifier());
        }
        result.append(".").append(dependency_.type());
        return result.toString();
    }

    public void downloadIntoFolder(File file)
    throws DependencyDownloadException {
        if (file == null) throw new IllegalArgumentException("file can't be null");
        if (!file.exists()) throw new IllegalArgumentException("file '" + file + "' doesn't exit");
        if (!file.canWrite()) throw new IllegalArgumentException("file '" + file + "' can't be written to");
        if (!file.isDirectory()) throw new IllegalArgumentException("file '" + file + "' is not a directory");

        var download_url = getDownloadUrl(resolveVersion());
        var download_filename = download_url.substring(download_url.lastIndexOf("/") + 1);
        var download_file = new File(file, download_filename);
        try {
            var url = new URL(download_url);
            var readableByteChannel = Channels.newChannel(url.openStream());
            try (var fileOutputStream = new FileOutputStream(download_file)) {
                var fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
        } catch (IOException e) {
            throw new DependencyDownloadException(dependency_, download_url, download_file, e);
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

    public String getMetadataUrl() {
        return artifactUrl_ + MAVEN_METADATA_XML;
    }

    public Xml2MavenMetadata getMavenMetadata() {
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

    public String getPomUrl() {
        var version = resolveVersion();
        return artifactUrl_ + version + "/" + dependency_.artifactId() + "-" + version + ".pom";
    }

    public Xml2MavenPom getMavenPom() {
        String pom;
        var url = getPomUrl();
        try {
            var content = FileUtils.readString(new URL(url));
            if (content == null) {
                throw new ArtifactNotFoundException(dependency_, url);
            }

            pom = content;
        } catch (IOException | FileUtilsErrorException e) {
            throw new ArtifactRetrievalErrorException(dependency_, url, e);
        }

        var xml = new Xml2MavenPom(repository_);
        if (!xml.processXml(pom)) {
            throw new DependencyXmlParsingErrorException(dependency_, url, xml.getErrors());
        }

        return xml;
    }
}
