/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.NamedFile;
import rife.bld.Project;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;

/**
 * Creates a jar archive of the provided sources and directories.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class JarOperation extends AbstractOperation<JarOperation> {
    private final Map<Attributes.Name, Object> manifestAttributes_ = new HashMap<>();
    private final List<File> sourceDirectories_ = new ArrayList<>();
    private final List<NamedFile> sourceFiles_ = new ArrayList<>();
    private File destinationDirectory_;
    private String destinationFileName_;
    private final List<Pattern> included_ = new ArrayList<>();
    private final List<Pattern> excluded_ = new ArrayList<>();

    private final byte[] buffer_ = new byte[1024];

    /**
     * Performs the jar operation.
     *
     * @throws IOException when an exception occurred during the jar creation process
     * @since 1.5
     */
    public void execute()
    throws IOException {
        executeCreateDestinationDirectory();
        executeCreateJarArchive();

        if (!silent()) {
            System.out.println("The jar archive was created at '" + new File(destinationDirectory(), destinationFileName()) + "'");
        }
    }

    /**
     * Part of the {@link #execute} operation, create the destination directory.
     *
     * @since 1.5
     */
    public void executeCreateDestinationDirectory() {
        destinationDirectory().mkdirs();
    }

    /**
     * Part of the {@link #execute} operation, create the jar archive.
     *
     * @since 1.5
     */
    public void executeCreateJarArchive()
    throws IOException {
        var out_file = new File(destinationDirectory(), destinationFileName());
        try (var jar = new JarOutputStream(new FileOutputStream(out_file), executeCreateManifest())) {
            for (var source_dir : sourceDirectories()) {
                for (var file_name : FileUtils.getFileList(source_dir)) {
                    var file = new File(source_dir, file_name);
                    if (StringUtils.filter(file.getAbsolutePath(), included(), excluded())) {
                        executeAddFileToJar(jar, new NamedFile(file_name, file));
                    }
                }
            }
            for (var source_file : sourceFiles()) {
                if (StringUtils.filter(source_file.file().getAbsolutePath(), included(), excluded())) {
                    executeAddFileToJar(jar, source_file);
                }
            }
            jar.flush();
        }
    }

    /**
     * Part of the {@link #execute} operation, create the manifest for the jar archive.
     *
     * @since 1.5
     */
    public Manifest executeCreateManifest() {
        var manifest = new Manifest();
        var attributes = manifest.getMainAttributes();
        // don't use putAll since Attributes does an instanceof check
        // on the map being passed in, causing it to fail if it's not
        // and instance of Attributes
        for (var entry : manifestAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }
        return manifest;
    }

    /**
     * Part of the {@link #execute} operation, add a single file to the jar archive.
     *
     * @since 1.5
     */
    private void executeAddFileToJar(JarOutputStream jar, NamedFile file)
    throws IOException {
        var entry = new JarEntry(file.name());
        entry.setTime(file.file().lastModified());
        jar.putNextEntry(entry);

        try (var in = new BufferedInputStream(new FileInputStream(file.file()))) {
            int count;
            while ((count = in.read(buffer_)) != -1) {
                jar.write(buffer_, 0, count);
            }
            jar.closeEntry();
        }
    }

    /**
     * Configures a jar operation from a {@link Project}.
     *
     * @param project the project to configure the jar operation from
     * @since 1.5
     */
    public JarOperation fromProject(Project project) {
        return manifestAttributes(Map.of(Attributes.Name.MANIFEST_VERSION, "1.0"))
            .sourceDirectories(List.of(project.buildMainDirectory(), project.srcMainResourcesDirectory()))
            .destinationDirectory(project.buildDistDirectory())
            .destinationFileName(project.jarFileName())
            .excluded(List.of(Pattern.compile("^\\Q" + project.srcMainResourcesTemplatesDirectory().getAbsolutePath() + "\\E.*")));
    }

    /**
     * Provides a map of attributes to put in the jar manifest.
     *
     * @param attributes the attributes to put in the manifest
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation manifestAttributes(Map<Attributes.Name, Object> attributes) {
        manifestAttributes_.putAll(attributes);
        return this;
    }

    /**
     * Provides the source directories that will be used for the jar archive creation.
     *
     * @param directories the source directories
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation sourceDirectories(List<File> directories) {
        sourceDirectories_.addAll(directories);
        return this;
    }

    /**
     * Provides the source files that will be used for the jar archive creation.
     *
     * @param files the source files
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation sourceFiles(List<NamedFile> files) {
        sourceFiles_.addAll(files);
        return this;
    }

    /**
     * Provides the destination directory in which the jar archive will be created.
     *
     * @param directory the jar destination directory
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    /**
     * Provides the destination file name that will be used for the jar archive creation.
     *
     * @param name the jar archive destination file name
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation destinationFileName(String name) {
        destinationFileName_ = name;
        return this;
    }

    /**
     * Provides a list of patterns that will be evaluated to determine which files
     * will be included in the jar archive.
     *
     * @param included the list of inclusion patterns
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation included(List<Pattern> included) {
        included_.addAll(included);
        return this;
    }

    /**
     * Provides a list of patterns that will be evaluated to determine which files
     * will be excluded from the jar archive.
     *
     * @param excluded the list of exclusion patterns
     * @return this operation instance
     * @since 1.5
     */
    public JarOperation excluded(List<Pattern> excluded) {
        excluded_.addAll(excluded);
        return this;
    }

    /**
     * Retrieves the map of attributes that will be put in the jar manifest.
     * <p>
     * This is a modifiable map that can be retrieved and changed.
     *
     * @return the manifest's attributes map
     * @since 1.5
     */
    public Map<Attributes.Name, Object> manifestAttributes() {
        return manifestAttributes_;
    }

    /**
     * Retrieves the list of source directories that will be used for the
     * jar archive creation.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the jar archive's source directories
     * @since 1.5
     */
    public List<File> sourceDirectories() {
        return sourceDirectories_;
    }

    /**
     * Retrieves the list of source files that will be used for the
     * jar archive creation.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the jar archive's source files
     * @since 1.5
     */
    public List<NamedFile> sourceFiles() {
        return sourceFiles_;
    }

    /**
     * Retrieves the destination directory in which the jar archive will
     * be created.
     *
     * @return the jar archive's destination directory
     * @since 1.5
     */
    public File destinationDirectory() {
        return destinationDirectory_;
    }

    /**
     * Retrieves the destination file name that will be used for the jar
     * archive creation.
     *
     * @return the jar archive's destination file name
     * @since 1.5
     */
    public String destinationFileName() {
        return destinationFileName_;
    }

    /**
     * Retrieves the list of patterns that will be evaluated to determine which files
     * will be included in the jar archive.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the jar's archive's inclusion patterns
     * @since 1.5
     */
    public List<Pattern> included() {
        return included_;
    }

    /**
     * Retrieves the list of patterns that will be evaluated to determine which files
     * will be excluded the jar archive.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the jar's archive's exclusion patterns
     * @since 1.5
     */
    public List<Pattern> excluded() {
        return excluded_;
    }
}
