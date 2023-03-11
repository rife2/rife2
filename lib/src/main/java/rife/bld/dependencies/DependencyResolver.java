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
import java.util.*;

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

    public DependencySet getDependencies(Scope... scopes) {
        var pom_dependencies = getMavenPom().getDependencies(scopes);
        var result = new DependencySet();
        for (var dependency : pom_dependencies) {
            result.add(convertPomDependency(dependency));
        }
        return result;
    }

    private Dependency convertPomDependency(PomDependency pomDependency) {
        return new Dependency(
            pomDependency.groupId(),
            pomDependency.artifactId(),
            VersionNumber.parse(pomDependency.version()),
            pomDependency.classifier(),
            pomDependency.type());
    }

    public DependencySet getTransitiveDependencies(Scope... scopes) {
        var result = new DependencySet();
        var pom_dependencies = new ArrayList<>(getMavenPom().getDependencies(scopes));
        var exclusions = new Stack<Set<PomExclusion>>();
        getTransitiveDependencies(result, pom_dependencies, exclusions, scopes);
        return result;
    }

    private void getTransitiveDependencies(DependencySet result, ArrayList<PomDependency> pomDependencies, Stack<Set<PomExclusion>> exclusions, Scope... scopes) {
        var next_dependencies = getMavenPom().getDependencies(scopes);

        pomDependencies.forEach(next_dependencies::remove);

        var next_it = next_dependencies.iterator();
        NextIterator:
        while (next_it.hasNext()) {
            var next_dependency = next_it.next();
            for (var exclusionset : exclusions) {
                if (exclusionset != null) {
                    for (var exclusion : exclusionset) {
                        if ((exclusion.groupId().equals("*") && exclusion.artifactId().equals("*")) ||
                            (exclusion.groupId().equals("*") && exclusion.artifactId().equals(next_dependency.artifactId())) ||
                            (exclusion.groupId().equals(next_dependency.groupId()) && exclusion.artifactId().equals("*")) ||
                            (exclusion.groupId().equals(next_dependency.groupId()) && exclusion.artifactId().equals(next_dependency.artifactId()))) {
                            next_it.remove();
                            continue NextIterator;
                        }
                    }
                }
            }
        }
        pomDependencies.addAll(next_dependencies);

        while (!pomDependencies.isEmpty()) {
            var it = pomDependencies.iterator();
            var pom_dependency = it.next();
            it.remove();

            var dependency = convertPomDependency(pom_dependency);
            if (!result.contains(dependency)) {
                result.add(dependency);

                exclusions.push(pom_dependency.exclusions());
                new DependencyResolver(repository_, dependency).getTransitiveDependencies(result, pomDependencies, exclusions, scopes);
                exclusions.pop();
            }
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

    Xml2MavenMetadata getMavenMetadata() {
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

    Xml2MavenPom getMavenPom() {
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

    public String getDownloadUrl(VersionNumber version) {
        var result = new StringBuilder(artifactUrl_);
        result.append(version).append("/").append(dependency_.artifactId()).append("-").append(version);
        if (!dependency_.classifier().isEmpty()) {
            result.append("-").append(dependency_.classifier());
        }
        var type = dependency_.type();
        if (type == null) {
            type = "jar";
        }
        result.append(".").append(type);
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
            System.out.println("Downloading: " + download_url);
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
}
