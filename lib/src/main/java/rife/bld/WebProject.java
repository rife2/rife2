/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.help.*;
import rife.bld.operations.*;
import rife.tools.FileUtils;

import java.io.File;
import java.util.*;

public class WebProject extends Project {
    protected String warFileName = null;
    protected File srcMainWebappDirectory = null;
    protected File libStandaloneDirectory = null;

    /*
     * Standard build commands
     */

    public void download() {
        new DownloadOperation()
            .fromProject(this)
            .libStandaloneDirectory(libStandaloneDirectory())
            .execute();
    }

    @Override
    public void uberjar()
    throws Exception {
        jar();
        var operation = new UberJarOperation()
            .fromProject(this)
            .resourceSourceDirectories(List.of(new NamedFile("webapp", srcMainWebappDirectory())));
        operation.jarSourceFiles().addAll(standaloneClasspathJars());
        operation.execute();
    }

    @BuildCommand(help = WarHelp.class)
    public void war()
    throws Exception {
        jar();
        new WarOperation().fromProject(this).execute();
    }

    /*
     * Project directories
     */

    public File srcMainWebappDirectory() {
        return Objects.requireNonNullElseGet(srcMainWebappDirectory, () -> new File(srcMainDirectory(), "webapp"));
    }

    public File libStandaloneDirectory() {
        return Objects.requireNonNullElseGet(libStandaloneDirectory, () -> new File(libDirectory(), "standalone"));
    }

    @Override
    public void createProjectStructure() {
        super.createProjectStructure();

        libStandaloneDirectory().mkdirs();
    }

    /*
     * Project options
     */

    public String warFileName() {
        return Objects.requireNonNullElseGet(warFileName, () -> archiveBaseName() + "-" + version() + ".war");
    }

    /*
     * Project classpaths
     */

    public List<File> standaloneClasspathJars() {
        // detect the jar files in the standalone lib directory
        var dir_abs = libStandaloneDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the standalone classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    public List<String> runClasspath() {
        return combineLists(super.runClasspath(), Project.combineToAbsolutePaths(standaloneClasspathJars()));
    }

    public List<String> testClasspath() {
        return combineLists(super.testClasspath(), Project.combineToAbsolutePaths(standaloneClasspathJars()));
    }
}
