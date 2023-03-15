/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;

public class UberJarOperation {
    private List<File> jarSourceFiles_ = new ArrayList<>();
    private List<NamedFile> resourceSourceDirectories_ = new ArrayList<>();
    private File destinationDirectory_;
    private String destinationFileName_;
    private String mainClass_;

    public void execute()
    throws Exception {
        var tmp_dir = Files.createTempDirectory("uberjar").toFile();
        try {
            executeCollectSourceJarContents(tmp_dir);
            executeCollectSourceResources(tmp_dir);
            executeCreateUberJarArchive(tmp_dir);
        } finally {
            FileUtils.deleteDirectory(tmp_dir);
        }
    }

    public void executeCollectSourceJarContents(File tmp_dir)
    throws FileUtilsErrorException {
        for (var jar : jarSourceFiles()) {
            FileUtils.unzipFile(jar, tmp_dir);
        }
    }

    public void executeCollectSourceResources(File tmp_dir)
    throws FileUtilsErrorException {
        for (var named_file : resourceSourceDirectories()) {
            if (named_file.file().exists()) {
                var destination = new File(tmp_dir, named_file.name());
                destination.mkdirs();
                FileUtils.copyDirectory(named_file.file(), destination);
            }
        }
    }

    public void executeCreateUberJarArchive(File stagingDirectory)
    throws Exception {
        var existing_manifest = new File(new File(stagingDirectory, "META-INF"), "MANIFEST.MF");
        existing_manifest.delete();

        new JarOperation()
            .manifestAttributes(Map.of(
                Attributes.Name.MANIFEST_VERSION, "1.0",
                Attributes.Name.MAIN_CLASS, mainClass()))
            .sourceDirectories(List.of(stagingDirectory))
            .destinationDirectory(destinationDirectory())
            .destinationFileName(destinationFileName())
            .execute();
    }

    public UberJarOperation fromProject(Project project) {
        var jars = new ArrayList<>(project.compileClasspathJars());
        jars.addAll(project.runtimeClasspathJars());
        jars.add(new File(project.buildDistDirectory(), project.jarFileName()));

        return jarSourceFiles(jars)
            .destinationDirectory(project.buildDistDirectory())
            .destinationFileName(project.uberJarFileName())
            .mainClass(project.uberJarMainClass());
    }

    public UberJarOperation jarSourceFiles(List<File> files) {
        jarSourceFiles_ = new ArrayList<>(files);
        return this;
    }

    public List<File> jarSourceFiles() {
        return jarSourceFiles_;
    }

    public UberJarOperation resourceSourceDirectories(List<NamedFile> directories) {
        resourceSourceDirectories_ = new ArrayList<>(directories);
        return this;
    }

    public List<NamedFile> resourceSourceDirectories() {
        return resourceSourceDirectories_;
    }

    public UberJarOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    public File destinationDirectory() {
        return destinationDirectory_;
    }

    public UberJarOperation destinationFileName(String name) {
        destinationFileName_ = name;
        return this;
    }

    public String destinationFileName() {
        return destinationFileName_;
    }

    public UberJarOperation mainClass(String name) {
        mainClass_ = name;
        return this;
    }

    public String mainClass() {
        return mainClass_;
    }
}
