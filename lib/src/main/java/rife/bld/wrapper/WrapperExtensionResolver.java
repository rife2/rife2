/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.wrapper;

import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    private boolean headerPrinted_ = false;

    public WrapperExtensionResolver(File hashFile, File destinationDirectory, Collection<String> repositories, Collection<String> extensions) {
        hashFile_ = hashFile;
        destinationDirectory_ = destinationDirectory;
        repositories_.addAll(repositories.stream().map(Repository::new).toList());
        dependencies_.addAll(extensions.stream().map(Dependency::parse).toList());
        fingerPrintHash_ = createHash(repositories, extensions);
    }

    private String createHash(Collection<String> repositories, Collection<String> extensions) {
        try {
            var fingerprint = String.join("\n", repositories) + String.join("\n", extensions);
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
        var filenames = downloadExtensionDependencies();

        // purge the files that are not part of the latest extensions anymore
        purgeExtensionDependencies(filenames);

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
            FileUtils.writeString(fingerPrintHash_, hashFile_);
            return false;
        } catch (FileUtilsErrorException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> downloadExtensionDependencies() {
        var filenames = new HashSet<String>();
        var dependencies = new DependencySet();
        for (var d : dependencies_) {
            if (d != null) {
                dependencies.addAll(new DependencyResolver(repositories_, d).getAllDependencies(Scope.compile, Scope.runtime));
            }
        }
        if (!dependencies.isEmpty()) {
            ensurePrintedHeader();

            dependencies.downloadIntoDirectory(repositories_, destinationDirectory_);

            for (var dependency : dependencies) {
                for (var url : new DependencyResolver(repositories_, dependency).getDownloadUrls()) {
                    filenames.add(url.substring(url.lastIndexOf("/") + 1));
                }
            }
        }

        return filenames;
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
