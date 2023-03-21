/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.dependencies;

import rife.bld.dependencies.exceptions.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.security.MessageDigest;
import java.util.*;

import static rife.tools.StringUtils.encodeHex;

/**
 * Resolves a dependency within a list of Maven-compatible repositories.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class DependencyResolver {
    public static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    private final List<Repository> repositories_;
    private final Dependency dependency_;

    private Xml2MavenMetadata metadata_ = null;
    private Xml2MavenMetadata snapshotMetadata_ = null;

    /**
     * Creates a new resolver for a particular dependency.
     * <p>
     * The repositories will be checked in the order they're listed.
     *
     * @param repositories the repositories to use for the resolution
     * @param dependency   the dependency to resolve
     * @since 1.5
     */
    public DependencyResolver(List<Repository> repositories, Dependency dependency) {
        if (repositories == null) {
            repositories = Collections.emptyList();
        }
        repositories_ = repositories;
        dependency_ = dependency;
    }

    /**
     * Checks whether the dependency exists in any of the provided repositories.
     *
     * @return {@code true} if the dependency exists; {@code false} otherwise
     * @since 1.5
     */
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


    /**
     * Resolves the dependency version in the provided repositories.
     * <p>
     * When the dependency was defined without a specific version number,
     * the latest version number will be returned if the dependency exists.
     * If the dependency couldn't be found and no specific version number
     * was provided, {@linkplain VersionNumber#UNKNOWN} will be returned.
     *
     * @return the resolved version
     * @since 1.5
     */
    public VersionNumber resolveVersion() {
        var version = dependency_.version();
        if (version.equals(VersionNumber.UNKNOWN)) {
            return latestVersion();
        }
        return version;
    }

    /**
     * Retrieves the direct dependencies of the resolved dependency for the
     * provided scopes.
     *
     * @param scopes the scopes to return the direct dependencies for
     * @return the requested direct dependencies; or an empty {@linkplain DependencySet}
     * if no direct dependencies could be found or the dependency doesn't exist in
     * the provided repositories
     * @since 1.5
     */
    public DependencySet getDirectDependencies(Scope... scopes) {
        var pom_dependencies = getMavenPom().getDependencies(scopes);
        var result = new DependencySet();
        for (var dependency : pom_dependencies) {
            result.add(convertPomDependency(dependency));
        }
        return result;
    }

    /**
     * Retrieves all the transitive dependencies of the resolved dependency for the
     * provided scopes. This includes the dependency of this resolver also.
     * <p>
     * This can be a slow and expensive operation since querying continues through
     * the complete POM hierarchy until all transitive dependencies have been found.
     *
     * @param scopes the scopes to return the transitive dependencies for
     * @return the requested transitive dependencies; or an empty {@linkplain DependencySet}
     * if no transitive dependencies could be found or the dependency doesn't exist in
     * the provided repositories
     * @since 1.5
     */
    public DependencySet getAllDependencies(Scope... scopes) {
        var result = new DependencySet();
        result.add(dependency_);

        var pom_dependencies = new ArrayList<PomDependency>();
        var exclusions = new Stack<ExclusionSet>();
        exclusions.push(dependency_.exclusions());
        getTransitiveDependencies(result, pom_dependencies, exclusions, scopes);
        exclusions.pop();
        return result;
    }

    /**
     * Retrieves all the versions of the resolved dependency.
     *
     * @return this dependency's version list; or an empty list if the dependency
     * couldn't be found in the provided repositories
     * @since 1.5
     */
    public List<VersionNumber> listVersions() {
        return getMavenMetadata().getVersions();
    }

    /**
     * Retrieves the latest version of the resolved dependency.
     *
     * @return this dependency's latest version; or {@link VersionNumber#UNKNOWN}
     * if the dependency couldn't be found in the provided repositories
     * @since 1.5
     */
    public VersionNumber latestVersion() {
        return getMavenMetadata().getLatest();
    }

    /**
     * Retrieves the release version of the resolved dependency.
     *
     * @return this dependency's release version; or {@link VersionNumber#UNKNOWN}
     * if the dependency couldn't be found in the provided repositories
     * @since 1.5
     */
    public VersionNumber releaseVersion() {
        return getMavenMetadata().getRelease();
    }

    /**
     * Downloads the artifact for the resolved dependency into the provided directory.
     * <p>
     * The destination directory must exist and be writable.
     *
     * @param directory the directory to download the dependency artifact into
     * @throws DependencyDownloadException when an error occurred during the download
     * @since 1.5
     */
    public void downloadIntoDirectory(File directory)
    throws DependencyDownloadException {
        if (directory == null) throw new IllegalArgumentException("directory can't be null");
        if (!directory.exists()) throw new IllegalArgumentException("directory '" + directory + "' doesn't exit");
        if (!directory.canWrite()) throw new IllegalArgumentException("directory '" + directory + "' can't be written to");
        if (!directory.isDirectory()) throw new IllegalArgumentException("directory '" + directory + "' is not a directory");

        boolean retrieved = false;
        var urls = getDownloadUrls();
        for (var download_url : urls) {
            var download_filename = download_url.substring(download_url.lastIndexOf("/") + 1);
            var download_file = new File(directory, download_filename);
            System.out.print("Downloading: " + download_url + " ... ");
            System.out.flush();
            try {
                if (download_file.exists() && download_file.canRead()) {
                    if (checkHash(download_url, download_file, ".sha256", "SHA-256") ||
                        checkHash(download_url, download_file, ".md5", "MD5")) {
                        retrieved = true;
                        System.out.print("exists");
                        break;
                    }
                }

                if (!retrieved) {
                    var url = new URL(download_url);
                    var readableByteChannel = Channels.newChannel(url.openStream());
                    try (var fileOutputStream = new FileOutputStream(download_file)) {
                        var fileChannel = fileOutputStream.getChannel();
                        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                        retrieved = true;
                        System.out.print("done");
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.print("not found");
            } catch (IOException e) {
                throw new DependencyDownloadException(dependency_, download_url, download_file, e);
            } finally {
                System.out.println();
            }
        }
    }

    private static boolean checkHash(String downloadUrl, File downloadFile, String extension, String algorithm) {
        try {
            var hash_url = new URL(downloadUrl + extension);
            var hash_url_sum = FileUtils.readString(hash_url);

            var digest = MessageDigest.getInstance(algorithm);
            digest.update(FileUtils.readBytes(downloadFile));
            var hash_file_sum = encodeHex(digest.digest());

            return hash_url_sum.equalsIgnoreCase(hash_file_sum);
        } catch (Exception e) {
            // no-op, the hash file couldn't be found or calculated, so it couldn't be checked
        }
        return false;
    }

    /**
     * Retrieve the repositories that are used by this dependency resolver.
     *
     * @return the dependency resolver's repositories
     * @since 1.5
     */
    public List<Repository> repositories() {
        return repositories_;
    }

    /**
     * Retrieve the dependency that is resolved.
     *
     * @return the resolved dependency
     * @since 1.5
     */
    public Dependency dependency() {
        return dependency_;
    }

    /**
     * Retrieves all the potential artifact download URLs for the dependency
     * within the provided repositories.
     *
     * @return a list of potential download URLs
     * @since 1.5
     */
    public List<String> getDownloadUrls() {
        final var version = resolveVersion();
        final VersionNumber pom_version;
        if (version.qualifier().equals("SNAPSHOT")) {
            var metadata = getSnapshotMavenMetadata();
            pom_version = metadata.getSnapshot();
        } else {
            pom_version = version;
        }

        return getArtifactUrls().stream().map(s -> {
            var result = new StringBuilder(s);
            result.append(version).append("/").append(dependency_.artifactId()).append("-").append(pom_version);
            if (!dependency_.classifier().isEmpty()) {
                result.append("-").append(dependency_.classifier());
            }
            var type = dependency_.type();
            if (type == null) {
                type = "jar";
            }
            result.append(".").append(type);
            return result.toString();
        }).toList();
    }

    private Dependency convertPomDependency(PomDependency pomDependency) {
        return new Dependency(
            pomDependency.groupId(),
            pomDependency.artifactId(),
            VersionNumber.parse(pomDependency.version()),
            pomDependency.classifier(),
            pomDependency.type());
    }

    private void getTransitiveDependencies(DependencySet result, List<PomDependency> pomDependencies, Stack<ExclusionSet> exclusions, Scope... scopes) {
        var next_dependencies = getMavenPom().getDependencies(scopes);

        pomDependencies.forEach(next_dependencies::remove);

        next_dependencies.removeIf(next_dependency ->
            exclusions.stream().anyMatch(set ->
                set != null && set.stream().anyMatch(exclusion ->
                    exclusion.matches(next_dependency))));

        pomDependencies.addAll(next_dependencies);

        while (!pomDependencies.isEmpty()) {
            var it = pomDependencies.iterator();
            var pom_dependency = it.next();
            it.remove();

            var dependency = convertPomDependency(pom_dependency);
            if (!result.contains(dependency)) {
                result.add(dependency);

                exclusions.push(pom_dependency.exclusions());
                new DependencyResolver(repositories_, dependency).getTransitiveDependencies(result, pomDependencies, exclusions, scopes);
                exclusions.pop();
            }
        }
    }

    private List<String> getArtifactUrls() {
        return repositories_.stream().map(repository -> repository.getArtifactUrl(dependency_)).toList();
    }

    private List<String> getMetadataUrls() {
        return getArtifactUrls().stream().map(s -> s + MAVEN_METADATA_XML).toList();
    }

    private Xml2MavenMetadata getMavenMetadata() {
        if (metadata_ == null) {
            var urls = getMetadataUrls();
            metadata_ = parseMavenMetadata(urls);
        }

        return metadata_;
    }

    private List<String> getSnapshotMetadataUrls() {
        var version = resolveVersion();
        return getArtifactUrls().stream().map(s -> s + version + "/" + MAVEN_METADATA_XML).toList();
    }

    private Xml2MavenMetadata getSnapshotMavenMetadata() {
        if (snapshotMetadata_ == null) {
            var urls = getSnapshotMetadataUrls();
            snapshotMetadata_ = parseMavenMetadata(urls);
        }

        return snapshotMetadata_;
    }

    private Xml2MavenMetadata parseMavenMetadata(List<String> urls) {
        String retrieved_url = null;
        String metadata = null;
        for (var url : urls) {
            try {
                var content = FileUtils.readString(new URL(url));
                if (content == null) {
                    throw new ArtifactNotFoundException(dependency_, url);
                }

                retrieved_url = url;
                metadata = content;

                break;
            } catch (IOException e) {
                throw new ArtifactRetrievalErrorException(dependency_, url, e);
            } catch (FileUtilsErrorException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    continue;
                }
                throw new ArtifactRetrievalErrorException(dependency_, url, e);
            }
        }

        if (metadata == null) {
            throw new ArtifactNotFoundException(dependency_, StringUtils.join(urls, ", "));
        }

        var xml = new Xml2MavenMetadata();
        if (!xml.processXml(metadata)) {
            throw new DependencyXmlParsingErrorException(dependency_, retrieved_url, xml.getErrors());
        }

        return xml;
    }

    private List<String> getPomUrls() {
        final var version = resolveVersion();
        final VersionNumber pom_version;
        if (version.qualifier().equals("SNAPSHOT")) {
            var metadata = getSnapshotMavenMetadata();
            pom_version = metadata.getSnapshot();
        } else {
            pom_version = version;
        }

        return getArtifactUrls().stream().map(s -> s + version + "/" + dependency_.artifactId() + "-" + pom_version + ".pom").toList();
    }

    Xml2MavenPom getMavenPom() {
        String retrieved_url = null;
        String pom = null;
        var urls = getPomUrls();
        for (var url : urls) {
            try {
                var content = FileUtils.readString(new URL(url));
                if (content == null) {
                    throw new ArtifactNotFoundException(dependency_, url);
                }

                retrieved_url = url;
                pom = content;

                break;
            } catch (IOException e) {
                throw new ArtifactRetrievalErrorException(dependency_, url, e);
            } catch (FileUtilsErrorException e) {
                if (e.getCause() instanceof FileNotFoundException) {
                    continue;
                }
                throw new ArtifactRetrievalErrorException(dependency_, url, e);
            }
        }

        if (pom == null) {
            throw new ArtifactNotFoundException(dependency_, StringUtils.join(urls, ", "));
        }

        var xml = new Xml2MavenPom(repositories_);
        if (!xml.processXml(pom)) {
            throw new DependencyXmlParsingErrorException(dependency_, retrieved_url, xml.getErrors());
        }

        return xml;
    }
}
