/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.wrapper;

import rife.bld.BuildExecutor;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static rife.bld.dependencies.Dependency.CLASSIFIER_JAVADOC;
import static rife.bld.dependencies.Dependency.CLASSIFIER_SOURCES;

/**
 * Resolves, downloads and purges the bld extension dependencies.
 * <p>
 * This is used by the bld wrapper and should not be called directly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.8
 */
public class WrapperExtensionResolver {
    private final File hashFile_;
    private final String fingerPrintHash_;
    private final File destinationDirectory_;
    private final List<Repository> repositories_ = new ArrayList<>();
    private final DependencySet dependencies_ = new DependencySet();
    private final boolean downloadSources_;
    private final boolean downloadJavadoc_;

    private boolean headerPrinted_ = false;

    public WrapperExtensionResolver(File currentDir, File hashFile, File destinationDirectory,
                                    Collection<String> repositories, Collection<String> extensions,
                                    boolean downloadSources, boolean downloadJavadoc) {
        var properties = BuildExecutor.setupProperties(currentDir);
        Repository.resolveMavenLocal(properties);

        hashFile_ = hashFile;
        destinationDirectory_ = destinationDirectory;
        for (var repository : repositories) {
            if (repository.equalsIgnoreCase("MAVEN_LOCAL")) {
                repositories_.add(Repository.MAVEN_LOCAL);
            } else if (repository.equalsIgnoreCase("MAVEN_CENTRAL")) {
                repositories_.add(Repository.MAVEN_CENTRAL);
            } else if (repository.equalsIgnoreCase("SONATYPE_RELEASES")) {
                repositories_.add(Repository.SONATYPE_RELEASES);
            } else if (repository.equalsIgnoreCase("SONATYPE_SNAPSHOTS")) {
                repositories_.add(Repository.SONATYPE_SNAPSHOTS);
            } else if (repository.equalsIgnoreCase("APACHE")) {
                repositories_.add(Repository.APACHE);
            } else {
                repositories_.add(Repository.resolveRepository(properties, repository));
            }
        }
        dependencies_.addAll(extensions.stream().map(Dependency::parse).toList());
        downloadSources_ = downloadSources;
        downloadJavadoc_ = downloadJavadoc;
        fingerPrintHash_ = createHash(repositories_.stream().map(Objects::toString).toList(), extensions, downloadSources, downloadJavadoc);
    }

    private String createHash(Collection<String> repositories, Collection<String> extensions, boolean downloadSources, boolean downloadJavadoc) {
        try {
            var fingerprint = String.join("\n", repositories) + "\n" + String.join("\n", extensions) + "\n" + downloadSources + "\n" + downloadJavadoc;
            var digest = MessageDigest.getInstance("SHA-1");
            digest.update(fingerprint.getBytes(StandardCharsets.UTF_8));
            return StringUtils.encodeHexLower(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateExtensions() {
        // verify and update the fingerprint hash file,
        // don't update the extensions if the hash is identical
        if (validateHash()) {
            return;
        }

        // collect and download the extensions dependencies
        var filenames = transferExtensionDependencies();

        // purge the files that are not part of the latest extensions anymore
        purgeExtensionDependencies(filenames);

        writeHash();

        if (headerPrinted_) {
            System.out.println();
        }
    }

    private boolean validateHash() {
        try {
            if (hashFile_.exists()) {
                var hash = FileUtils.readString(hashFile_);
                if (hash.equals(fingerPrintHash_)) {
                    return true;
                }
                hashFile_.delete();
            }
            return false;
        } catch (FileUtilsErrorException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeHash() {
        try {
            FileUtils.writeString(fingerPrintHash_, hashFile_);
        } catch (FileUtilsErrorException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> transferExtensionDependencies() {
        var filenames = new HashSet<String>();
        var dependencies = new DependencySet();
        for (var d : dependencies_) {
            if (d != null) {
                dependencies.addAll(new DependencyResolver(repositories_, d).getAllDependencies(Scope.compile, Scope.runtime));
            }
        }
        if (!dependencies.isEmpty()) {
            ensurePrintedHeader();

            dependencies.removeIf(dependency -> dependency.baseDependency().equals(new Dependency("com.uwyn.rife2", "rife2")));

            var additional_classifiers = new String[0];
            if (downloadSources_ || downloadJavadoc_) {
                var classifiers = new ArrayList<String>();
                if (downloadSources_) classifiers.add(CLASSIFIER_SOURCES);
                if (downloadJavadoc_) classifiers.add(CLASSIFIER_JAVADOC);

                additional_classifiers = classifiers.toArray(new String[0]);
            }
            dependencies.transferIntoDirectory(repositories_, destinationDirectory_, additional_classifiers);

            for (var dependency : dependencies) {
                addTransferLocations(filenames, dependency);
                if (downloadSources_) {
                    addTransferLocations(filenames, dependency.withClassifier(CLASSIFIER_SOURCES));
                }
                if (downloadJavadoc_) {
                    addTransferLocations(filenames, dependency.withClassifier(CLASSIFIER_JAVADOC));
                }
            }
        }

        return filenames;
    }

    private void addTransferLocations(HashSet<String> filenames, Dependency dependency) {
        for (var location : new DependencyResolver(repositories_, dependency).getTransferLocations()) {
            filenames.add(location.substring(location.lastIndexOf("/") + 1));
        }
    }

    private void purgeExtensionDependencies(Set<String> filenames) {
        for (var file : destinationDirectory_.listFiles()) {
            if (file.getName().startsWith(Wrapper.WRAPPER_PREFIX)) {
                continue;
            }
            if (!filenames.contains(file.getName())) {
                ensurePrintedHeader();
                System.out.println("Deleting : " + file.getName());
                file.delete();
            }
        }
    }

    private void ensurePrintedHeader() {
        if (!headerPrinted_) {
            System.out.println("Updating bld extensions...");
        }
        headerPrinted_ = true;
    }
}
