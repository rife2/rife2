/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.Version;
import rife.bld.Project;
import rife.bld.dependencies.*;
import rife.bld.dependencies.exceptions.DependencyException;
import rife.bld.operations.exceptions.OperationOptionException;
import rife.bld.operations.exceptions.UploadException;
import rife.bld.publish.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static rife.bld.publish.MetadataBuilder.SNAPSHOT_TIMESTAMP_FORMATTER;
import static rife.tools.HttpUtils.*;
import static rife.tools.StringUtils.encodeHexLower;

/**
 * Published artifacts to a Maven repository.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.7
 */
public class PublishOperation extends AbstractOperation<PublishOperation> {
    private final HttpClient client_ = HttpClient.newHttpClient();

    private ZonedDateTime moment_ = null;
    private Repository repository_;
    private final DependencyScopes dependencies_ = new DependencyScopes();
    private PublishInfo info_ = new PublishInfo();
    private final List<PublishArtifact> artifacts_ = new ArrayList<>();

    /**
     * Performs the publish operation.
     *
     * @since 1.5.7
     */
    public void execute() {
        if (repository() == null) {
            throw new OperationOptionException("ERROR: the publication repository should be specified");
        }

        var moment = moment_;
        if (moment == null) {
            moment = ZonedDateTime.now();
        }

        var actual_version = info().version();

        // treat a snapshot version differently
        if (info().version().isSnapshot()) {
            actual_version = executeUploadSnapshotMetadata(moment);
        }

        executeUploadArtifacts(actual_version);
        executeUploadPom(actual_version);
        executeUploadMetadata(moment);
        if (!silent()) {
            System.out.println("Publishing finished successfully.");
        }
    }

    /**
     * Part of the {@link #execute} operation, uploads snapshot metadata if this
     * is a snapshot version.
     *
     * @param moment the timestamp at which the operation started executing
     * @return the adapted version number with the snapshot timestamp and build number
     * @since 1.5.8
     */
    public VersionNumber executeUploadSnapshotMetadata(ZonedDateTime moment) {
        VersionNumber actual_version;
        var snapshot_timestamp = SNAPSHOT_TIMESTAMP_FORMATTER.format(moment.withZoneSameInstant(ZoneId.of("UTC")));

        // determine with build number to use
        var snapshot_build_number = 1;
        try {
            var resolver = new DependencyResolver(List.of(repository()), new Dependency(info().groupId(), info().artifactId(), info().version()));
            var snapshot_meta = resolver.getSnapshotMavenMetadata();
            snapshot_build_number = snapshot_meta.getSnapshotBuildNumber() + 1;
        } catch (DependencyException e) {
            // start the build number from the beginning
            System.out.println("Unable to retrieve previous snapshot metadata, using first build number.");
            System.out.println("This is expected for a first publication or for publication to a staging repository.");
        }

        // adapt the actual version that's use by the artifacts
        var snapshot_qualifier = snapshot_timestamp + "-" + snapshot_build_number;
        actual_version = info().version().withQualifier(snapshot_qualifier);

        // include version information about each artifact in this snapshot
        var metadata = new MetadataBuilder();
        for (var artifact : artifacts()) {
            metadata.snapshotVersions().add(new SnapshotVersion(artifact.classifier(), artifact.type(), actual_version.toString(), moment));
        }
        metadata.snapshotVersions().add(new SnapshotVersion(null, "pom", actual_version.toString(), moment));

        // upload snapshot metadata
        executeUploadStringArtifact(
            metadata
                .info(info())
                .updated(moment)
                .snapshot(moment, snapshot_build_number)
                .build(),
            info().version() + "/maven-metadata.xml");
        return actual_version;
    }

    /**
     * Part of the {@link #execute} operation, uploads all the artifacts
     * in this publish operation.
     *
     * @param actualVersion the version that was potentially adapted if this is a snapshot
     * @since 1.5.8
     */
    public void executeUploadArtifacts(VersionNumber actualVersion) {
        // upload artifacts
        for (var artifact : artifacts()) {
            var artifact_name = new StringBuilder(info().artifactId()).append("-").append(actualVersion);
            if (!artifact.classifier().isEmpty()) {
                artifact_name.append("-").append(artifact.classifier());
            }
            var type = artifact.type();
            if (type == null) {
                type = "jar";
            }
            artifact_name.append(".").append(type);

            executeUploadFileArtifact(artifact.file(), info().version() + "/" + artifact_name);
        }
    }

    /**
     * Part of the {@link #execute} operation, uploads the Maven POM.
     *
     * @param actualVersion the version that was potentially adapted if this is a snapshot
     * @since 1.5.8
     */
    public void executeUploadPom(VersionNumber actualVersion) {
        // generate and upload pom
        executeUploadStringArtifact(
            new PomBuilder().info(info()).dependencies(dependencies()).build(),
            info().version() + "/" + info().artifactId() + "-" + actualVersion + ".pom");
    }

    /**
     * Part of the {@link #execute} operation, uploads the artifact metadata.
     *
     * @param moment the timestamp at which the operation started executing
     * @since 1.5.8
     */
    public void executeUploadMetadata(ZonedDateTime moment) {
        var current_versions = new ArrayList<VersionNumber>();
        var resolver = new DependencyResolver(List.of(repository()), new Dependency(info().groupId(), info().artifactId(), info().version()));
        try {
            current_versions.addAll(resolver.getMavenMetadata().getVersions());
        } catch (DependencyException e) {
            // no existing versions could be found
            System.out.println("Unable to retrieve previous artifact metadata, proceeding with empty version list.");
            System.out.println("This is expected for a first publication or for publication to a staging repository.");
        }

        // upload metadata
        executeUploadStringArtifact(
            new MetadataBuilder()
                .info(info())
                .updated(moment)
                .otherVersions(current_versions)
                .build(),
            "maven-metadata.xml");
    }

    /**
     * Part of the {@link #execute} operation, uploads a single artifact with
     * hashes and a potential signature.
     *
     * @param content the content of the file that needs to be uploaded
     * @param path    the path of the artifact within the artifact folder
     * @since 1.5.8
     */
    public void executeUploadStringArtifact(String content, String path)
    throws UploadException {
        try {
            executeUploadFile(BodyPublishers.ofString(content), path);
            executeUploadFile(BodyPublishers.ofString(generateHash(content, "MD5")), path + ".md5");
            executeUploadFile(BodyPublishers.ofString(generateHash(content, "SHA-1")), path + ".sha1");
            executeUploadFile(BodyPublishers.ofString(generateHash(content, "SHA-256")), path + ".sha256");
            executeUploadFile(BodyPublishers.ofString(generateHash(content, "SHA-512")), path + ".sha512");

            if (info().signKey() != null) {
                var tmp_file = File.createTempFile(path, "gpg");
                FileUtils.writeString(content, tmp_file);
                try {
                    executeUploadFile(BodyPublishers.ofString(executeSignFile(tmp_file)), path + ".asc");
                } finally {
                    tmp_file.delete();
                }
            }
        } catch (NoSuchAlgorithmException | IOException | FileUtilsErrorException e) {
            throw new UploadException(path, e);
        }
    }

    private String generateHash(String content, String algorithm)
    throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance(algorithm);
        digest.update(content.getBytes(StandardCharsets.UTF_8));
        return encodeHexLower(digest.digest());
    }

    /**
     * Part of the {@link #execute} operation, uploads a single artifact with
     * hashes and a potential signature.
     *
     * @param file the file that needs to be uploaded
     * @param path the path of the artifact within the artifact folder
     * @since 1.5.8
     */
    public void executeUploadFileArtifact(File file, String path)
    throws UploadException {
        try {
            var digest_md5 = MessageDigest.getInstance("MD5");
            var digest_sha1 = MessageDigest.getInstance("SHA-1");
            var digest_sha256 = MessageDigest.getInstance("SHA-256");
            var digest_sha512 = MessageDigest.getInstance("SHA-512");

            try (var is = Files.newInputStream(file.toPath())) {
                var buffer = new byte[1024];
                var return_value = -1;
                while (-1 != (return_value = is.read(buffer))) {
                    digest_md5.update(buffer, 0, return_value);
                    digest_sha1.update(buffer, 0, return_value);
                    digest_sha256.update(buffer, 0, return_value);
                    digest_sha512.update(buffer, 0, return_value);
                }

                executeUploadFile(BodyPublishers.ofFile(file.toPath()), path);
                executeUploadFile(BodyPublishers.ofString(encodeHexLower(digest_md5.digest())), path + ".md5");
                executeUploadFile(BodyPublishers.ofString(encodeHexLower(digest_sha1.digest())), path + ".sha1");
                executeUploadFile(BodyPublishers.ofString(encodeHexLower(digest_sha256.digest())), path + ".sha256");
                executeUploadFile(BodyPublishers.ofString(encodeHexLower(digest_sha512.digest())), path + ".sha512");
                if (info().signKey() != null) {
                    executeUploadFile(BodyPublishers.ofString(executeSignFile(file)), path + ".asc");
                }
            }
        } catch (IOException | NoSuchAlgorithmException | FileUtilsErrorException e) {
            throw new UploadException(path, e);
        }
    }

    /**
     * Part of the {@link #execute} operation, generates the signature of a file.
     *
     * @param file the file whose signature will be generated
     * @since 1.5.8
     */
    public String executeSignFile(File file)
    throws IOException, FileUtilsErrorException {
        var gpg_path = info().signGpgPath();
        if (gpg_path == null) {
            gpg_path = "gpg";
        }
        var gpg_arguments = new ArrayList<>(List.of(
            gpg_path,
            "--pinentry-mode=loopback",
            "--no-tty", "--batch", "--detach-sign", "--armor", "-o-",
            "--local-user", info().signKey()));
        if (info().signPassphrase() != null) {
            gpg_arguments.addAll(List.of("--passphrase", info().signPassphrase()));
        }
        gpg_arguments.add(file.getAbsolutePath());
        var builder = new ProcessBuilder(gpg_arguments);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.PIPE);
        var process = builder.start();
        return FileUtils.readString(process.getInputStream());
    }

    /**
     * Part of the {@link #execute} operation, uploads a file.
     *
     * @param body the body of the file to upload
     * @param path the path of the file within the artifact folder
     * @since 1.5.8
     */
    public void executeUploadFile(HttpRequest.BodyPublisher body, String path)
    throws UploadException {
        var url = repository().getArtifactUrl(info().groupId(), info().artifactId()) + path;
        System.out.print("Uploading: " + url + " ... ");
        System.out.flush();
        try {
            var builder = HttpRequest.newBuilder()
                .PUT(body)
                .uri(URI.create(url))
                .header(HEADER_USER_AGENT, "bld/" + Version.getVersion() +
                                           " (" + System.getProperty("os.name") + "; " + System.getProperty("os.version") + "; " + System.getProperty("os.arch") + ") " +
                                           "RIFE2/" + Version.getVersion() +
                                           " (" + System.getProperty("java.vendor") + " " + System.getProperty("java.vm.name") + "; " + System.getProperty("java.version") + "; " + System.getProperty("java.vm.version") + ")");
            if (repository().username() != null && repository().password() != null) {
                builder.header(HEADER_AUTHORIZATION, basicAuthorizationHeader(repository().username(), repository().password()));
            }
            var request = builder.build();

            HttpResponse<String> response;
            try {
                response = client_.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                System.out.print("I/O error");
                throw new UploadException(url, e);
            } catch (InterruptedException e) {
                System.out.print("interrupted");
                throw new UploadException(url, e);
            }

            if (response.statusCode() >= 200 &&
                response.statusCode() < 300) {
                System.out.print("done");
            } else {
                System.out.print("failed");
                throw new UploadException(url, response.statusCode());
            }
        } finally {
            System.out.println();
        }
    }

    /**
     * Configures a publish operation from a {@link Project}.
     *
     * @param project the project to configure the publish operation from
     * @since 1.5.7
     */
    public PublishOperation fromProject(Project project) {
        repository(project.publishRepository());
        dependencies().include(project.dependencies());
        artifacts(List.of(new PublishArtifact(new File(project.buildDistDirectory(), project.jarFileName()), "", "jar")));
        var info = project.publishInfo();
        if (info != null) {
            info_ = info;
        }
        if (info_.groupId() == null) {
            info_.groupId(project.pkg());
        }
        if (info_.artifactId() == null) {
            info_.artifactId(project.name().toLowerCase());
        }
        if (info_.version() == null) {
            info_.version(project.version());
        }
        if (info_.name() == null) {
            info_.name(project.name());
        }
        return this;
    }

    /**
     * Provides the moment of publication.
     * <p>
     * If this is not provided, the publication will use the current data and time.
     *
     * @param moment the publication moment
     * @return this operation instance
     * @since 1.5.8
     */
    public PublishOperation moment(ZonedDateTime moment) {
        moment_ = moment;
        return this;
    }

    /**
     * Retrieves the moment of publication.
     *
     * @return the moment of publication; or
     * {@code null} if it wasn't provided
     * @since 1.5.8
     */
    public ZonedDateTime moment() {
        return moment_;
    }

    /**
     * Provides the repository to publish to
     *
     * @param repository the repository that the artifacts will be published to
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation repository(Repository repository) {
        repository_ = repository;
        return this;
    }

    /**
     * Provides scoped dependencies to reference in the publication.
     *
     * @param dependencies the dependencies that will be references in the publication
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation dependencies(DependencyScopes dependencies) {
        dependencies_.include(dependencies);
        return this;
    }

    /**
     * Provides the artifacts that will be published.
     *
     * @param artifacts the artifacts to publish
     * @return this operation instance
     * @since 1.5.7
     */
    public PublishOperation artifacts(List<PublishArtifact> artifacts) {
        artifacts_.addAll(artifacts);
        return this;
    }

    /**
     * Retrieves the repository that will be published to.
     *
     * @return the publishing repository
     * @since 1.5.7
     */
    public Repository repository() {
        return repository_;
    }

    /**
     * Retrieves the scoped dependencies to reference in the publication.
     * <p>
     * This is a modifiable structure that can be retrieved and changed.
     *
     * @return the scoped dependencies
     * @since 1.5.7
     */
    public DependencyScopes dependencies() {
        return dependencies_;
    }

    /**
     * Retrieves the publication info structure.
     * <p>
     * This is a modifiable structure that can be retrieved and changed.
     *
     * @return the publication info
     * @since 1.5.7
     */
    public PublishInfo info() {
        return info_;
    }

    /**
     * Retrieves the list of artifacts that will be published.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the list of artifacts to publish
     * @since 1.5.7
     */
    public List<PublishArtifact> artifacts() {
        return artifacts_;
    }
}
