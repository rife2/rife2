/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WarOperation {
    private final List<File> libSourceDirectories_ = new ArrayList<>();
    private final List<File> classesSourceDirectories_ = new ArrayList<>();
    private final List<NamedFile> jarSourceFiles_ = new ArrayList<>();
    private File webappDirectory_;
    private File webXmlFile_;
    private File destinationDirectory_;
    private String destinationFileName_;

    public void execute()
    throws Exception {
        var tmp_dir = Files.createTempDirectory("war").toFile();

        try {
            var web_inf_dir = executeCreateWebInfDirectory(tmp_dir);
            executeCopyWebappDirectory(tmp_dir);
            executeCopyWebInfLibJars(web_inf_dir);
            executeCopyWebInfClassesFiles(web_inf_dir);
            executeCopyWebXmlFile(web_inf_dir);

            executeCreateWarArchive(tmp_dir);
        } finally {
            FileUtils.deleteDirectory(tmp_dir);
        }
    }

    public File executeCreateWebInfDirectory(File stagingDirectory) {
        var web_inf_dir = new File(stagingDirectory, "WEB-INF");
        web_inf_dir.mkdirs();
        return web_inf_dir;
    }

    public void executeCopyWebappDirectory(File stagingDirectory)
    throws FileUtilsErrorException {
        if (webappDirectory() != null) {
            FileUtils.copyDirectory(webappDirectory(), stagingDirectory);
        }
    }

    public void executeCopyWebInfLibJars(File stagingWebInfDirectory)
    throws FileUtilsErrorException {
        var web_inf_lib_dir = new File(stagingWebInfDirectory, "lib");
        if (!libSourceDirectories().isEmpty()) {
            web_inf_lib_dir.mkdirs();
            for (var dir : libSourceDirectories()) {
                FileUtils.copyDirectory(dir, web_inf_lib_dir);
            }
        }

        if (!jarSourceFiles().isEmpty()) {
            web_inf_lib_dir.mkdirs();
            for (var file : jarSourceFiles()) {
                FileUtils.copy(file.file(), new File(web_inf_lib_dir, file.name()));
            }
        }
    }

    public void executeCopyWebInfClassesFiles(File stagingWebInfDirectory)
    throws FileUtilsErrorException {
        var web_inf_classes_dir = new File(stagingWebInfDirectory, "classes");
        if (!classesSourceDirectories().isEmpty()) {
            web_inf_classes_dir.mkdirs();
            for (var dir : classesSourceDirectories()) {
                FileUtils.copyDirectory(dir, web_inf_classes_dir);
            }
        }
    }

    public void executeCopyWebXmlFile(File stagingWebInfDirectory)
    throws FileUtilsErrorException {
        if (webXmlFile() != null) {
            FileUtils.copy(webXmlFile(), new File(stagingWebInfDirectory, "web.xml"));
        }
    }

    public void executeCreateWarArchive(File stagingDirectory)
    throws Exception {
        new JarOperation()
            .sourceDirectories(List.of(stagingDirectory))
            .destinationDirectory(destinationDirectory())
            .destinationFileName(destinationFileName())
            .execute();
    }

    public WarOperation fromProject(WebProject project) {
        return libSourceDirectories(List.of(project.libCompileDirectory(), project.libRuntimeDirectory()))
            .jarSourceFiles(List.of(new NamedFile(project.jarFileName(), new File(project.buildDistDirectory(), project.jarFileName()))))
            .webappDirectory(project.srcMainWebappDirectory())
            .destinationDirectory(project.buildDistDirectory())
            .destinationFileName(project.warFileName());
    }

    public WarOperation libSourceDirectories(List<File> sources) {
        libSourceDirectories_.addAll(sources);
        return this;
    }

    public List<File> libSourceDirectories() {
        return libSourceDirectories_;
    }

    public WarOperation classesSourceDirectories(List<File> sources) {
        classesSourceDirectories_.addAll(sources);
        return this;
    }

    public List<File> classesSourceDirectories() {
        return classesSourceDirectories_;
    }

    public WarOperation jarSourceFiles(List<NamedFile> files) {
        jarSourceFiles_.addAll(files);
        return this;
    }

    public List<NamedFile> jarSourceFiles() {
        return jarSourceFiles_;
    }

    public WarOperation webappDirectory(File directory) {
        webappDirectory_ = directory;
        return this;
    }

    public File webappDirectory() {
        return webappDirectory_;
    }

    public WarOperation webXmlFile(File directory) {
        webXmlFile_ = directory;
        return this;
    }

    public File webXmlFile() {
        return webXmlFile_;
    }

    public WarOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    public File destinationDirectory() {
        return destinationDirectory_;
    }

    public WarOperation destinationFileName(String name) {
        destinationFileName_ = name;
        return this;
    }

    public String destinationFileName() {
        return destinationFileName_;
    }
}
