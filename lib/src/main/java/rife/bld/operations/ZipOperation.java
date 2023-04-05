/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.NamedFile;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.regex.Pattern;

/**
 * Creates a zip archive of the provided sources and directories.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.18
 */
public class ZipOperation extends AbstractOperation<ZipOperation> {
    private final List<File> sourceDirectories_ = new ArrayList<>();
    private final List<NamedFile> sourceFiles_ = new ArrayList<>();
    private File destinationDirectory_;
    private String destinationFileName_;
    private final List<Pattern> included_ = new ArrayList<>();
    private final List<Pattern> excluded_ = new ArrayList<>();

    private final byte[] buffer_ = new byte[1024];

    /**
     * Performs the zip operation.
     *
     * @throws IOException when an exception occurred during the zip creation process
     * @since  1.5.18
     */
    public void execute()
    throws IOException {
        executeCreateDestinationDirectory();
        executeCreateZipArchive();

        if (!silent()) {
            System.out.println("The zip archive was created at '" + destinationFile() + "'");
        }
    }

    /**
     * Part of the {@link #execute} operation, create the destination directory.
     *
     * @since 1.5.18
     */
    protected void executeCreateDestinationDirectory() {
        destinationDirectory().mkdirs();
    }

    /**
     * Part of the {@link #execute} operation, create the zip archive.
     *
     * @since 1.5.18
     */
    protected void executeCreateZipArchive()
    throws IOException {
        var out_file = new File(destinationDirectory(), destinationFileName());
        try (var zip = new ZipOutputStream(new FileOutputStream(out_file))) {
            for (var source_dir : sourceDirectories()) {
                for (var file_name : FileUtils.getFileList(source_dir)) {
                    var file = new File(source_dir, file_name);
                    if (StringUtils.filter(file.getAbsolutePath(), included(), excluded(), false)) {
                        executeAddFileToZip(zip, new NamedFile(file_name, file));
                    }
                }
            }
            for (var source_file : sourceFiles()) {
                if (StringUtils.filter(source_file.file().getAbsolutePath(), included(), excluded(), false)) {
                    executeAddFileToZip(zip, source_file);
                }
            }
            zip.flush();
        }
    }

    /**
     * Part of the {@link #execute} operation, add a single file to the zip archive.
     *
     * @since  1.5.18
     */
    protected void executeAddFileToZip(ZipOutputStream zip, NamedFile file)
    throws IOException {
        var entry = new ZipEntry(file.name().replace('\\', '/'));
        entry.setTime(file.file().lastModified());
        zip.putNextEntry(entry);

        try (var in = new BufferedInputStream(new FileInputStream(file.file()))) {
            int count;
            while ((count = in.read(buffer_)) != -1) {
                zip.write(buffer_, 0, count);
            }
            zip.closeEntry();
        }
    }

    /**
     * Provides source directories that will be used for the zip archive creation.
     *
     * @param directories source directories
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation sourceDirectories(File... directories) {
        sourceDirectories_.addAll(List.of(directories));
        return this;
    }

    /**
     * Provides a list of source directories that will be used for the zip archive creation.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param directories a list of source directories
     * @return this operation instance
     * @since  1.5.18
     */
    public ZipOperation sourceDirectories(List<File> directories) {
        sourceDirectories_.addAll(directories);
        return this;
    }

    /**
     * Provides source files that will be used for the zip archive creation.
     *
     * @param files source files
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation sourceFiles(NamedFile... files) {
        sourceFiles_.addAll(List.of(files));
        return this;
    }

    /**
     * Provides a list of source files that will be used for the zip archive creation.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param files a list of source files
     * @return this operation instance
     * @since  1.5.18
     */
    public ZipOperation sourceFiles(List<NamedFile> files) {
        sourceFiles_.addAll(files);
        return this;
    }

    /**
     * Provides the destination directory in which the zip archive will be created.
     *
     * @param directory the zip destination directory
     * @return this operation instance
     * @since  1.5.18
     */
    public ZipOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    /**
     * Provides the destination file name that will be used for the zip archive creation.
     *
     * @param name the zip archive destination file name
     * @return this operation instance
     * @since  1.5.18
     */
    public ZipOperation destinationFileName(String name) {
        destinationFileName_ = name;
        return this;
    }

    /**
     * Provides regex patterns that will be found to determine which files
     * will be included in the javadoc generation.
     *
     * @param included inclusion patterns
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation included(String... included) {
        included_.addAll(Arrays.stream(included).map(Pattern::compile).toList());
        return this;
    }

    /**
     * Provides patterns that will be found to determine which files
     * will be included in the zip archive.
     *
     * @param included inclusion patterns
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation included(Pattern... included) {
        included_.addAll(List.of(included));
        return this;
    }

    /**
     * Provides a list of patterns that will be found to determine which files
     * will be included in the zip archive.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param included a list of inclusion patterns
     * @return this operation instance
     * @since  1.5.18
     */
    public ZipOperation included(List<Pattern> included) {
        included_.addAll(included);
        return this;
    }

    /**
     * Provides regex patterns that will be found to determine which files
     * will be excluded from the javadoc generation.
     *
     * @param excluded exclusion patterns
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation excluded(String... excluded) {
        excluded_.addAll(Arrays.stream(excluded).map(Pattern::compile).toList());
        return this;
    }

    /**
     * Provides patterns that will be found to determine which files
     * will be excluded from the zip archive.
     *
     * @param excluded exclusion patterns
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation excluded(Pattern... excluded) {
        excluded_.addAll(List.of(excluded));
        return this;
    }

    /**
     * Provides a list of patterns that will be found to determine which files
     * will be excluded from the zip archive.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param excluded a list of exclusion patterns
     * @return this operation instance
     * @since 1.5.18
     */
    public ZipOperation excluded(List<Pattern> excluded) {
        excluded_.addAll(excluded);
        return this;
    }

    /**
     * Retrieves the list of source directories that will be used for the
     * zip archive creation.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the zip archive's source directories
     * @since 1.5.18
     */
    public List<File> sourceDirectories() {
        return sourceDirectories_;
    }

    /**
     * Retrieves the list of source files that will be used for the
     * zip archive creation.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the zip archive's source files
     * @since 1.5.18
     */
    public List<NamedFile> sourceFiles() {
        return sourceFiles_;
    }

    /**
     * Retrieves the destination directory in which the zip archive will
     * be created.
     *
     * @return the zip archive's destination directory
     * @since 1.5.18
     */
    public File destinationDirectory() {
        return destinationDirectory_;
    }

    /**
     * Retrieves the destination file name that will be used for the zip
     * archive creation.
     *
     * @return the zip archive's destination file name
     * @since 1.5.18
     */
    public String destinationFileName() {
        return destinationFileName_;
    }

    /**
     * Retrieves the destination file where the zip archive will be created.
     *
     * @return the zip archive's destination file
     * @since 1.5.18
     */
    public File destinationFile() {
        return new File(destinationDirectory(), destinationFileName());
    }

    /**
     * Retrieves the list of patterns that will be evaluated to determine which files
     * will be included in the zip archive.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the zip's archive's inclusion patterns
     * @since 1.5.18
     */
    public List<Pattern> included() {
        return included_;
    }

    /**
     * Retrieves the list of patterns that will be evaluated to determine which files
     * will be excluded the zip archive.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the zip's archive's exclusion patterns
     * @since 1.5.18
     */
    public List<Pattern> excluded() {
        return excluded_;
    }
}
