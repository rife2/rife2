/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.commands.*;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public abstract class Project extends BuildExecutor {
    public List<Repository> repositories = Collections.emptyList();
    public String pkg = null;
    public String name = null;
    public VersionNumber version = null;
    public String mainClass = null;

    public DependencyScopes dependencies = new DependencyScopes();

    public abstract void setup();

    /*
     * Standard build commands
     */

    @BuildCommand(help = CleanCommand.Help.class)
    public void clean()
    throws Exception {
        new CleanCommand(this).execute();
    }

    @BuildCommand(help = CompileCommand.Help.class)
    public void compile()
    throws Exception {
        new CompileCommand(this).execute();
    }

    @BuildCommand(help = DownloadCommand.Help.class)
    public void download() {
        new DownloadCommand(this).execute();
    }

    @BuildCommand(help = EncryptCommand.Help.class)
    public void encrypt() {
        new EncryptCommand(arguments()).execute();
    }

    @BuildCommand(help = PrecompileCommand.Help.class)
    public void precompile() {
        new PrecompileCommand(arguments()).execute();
    }

    @BuildCommand(help = RunCommand.Help.class)
    public void run() {
        new RunCommand(this).execute();
    }

    /*
     * Useful methods
     */

    public static VersionNumber version(int major) {
        return new VersionNumber(major);
    }

    public static VersionNumber version(int major, int minor) {
        return new VersionNumber(major, minor);
    }

    public static VersionNumber version(int major, int minor, int revision) {
        return new VersionNumber(major, minor, revision);
    }

    public static VersionNumber version(int major, int minor, int revision, String qualifier) {
        return new VersionNumber(major, minor, revision, qualifier);
    }

    public static VersionNumber version(String version) {
        return VersionNumber.parse(version);
    }

    public DependencySet scope(Scope scope) {
        return dependencies.scope(scope);
    }

    public static Dependency dependency(String groupId, String artifactId) {
        return new Dependency(groupId, artifactId);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version) {
        return new Dependency(groupId, artifactId, version);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version, String classifier) {
        return new Dependency(groupId, artifactId, version, classifier);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type) {
        return new Dependency(groupId, artifactId, version, classifier, type);
    }

    public static String joinPaths(List<String> paths) {
        return StringUtils.join(paths, File.pathSeparator);
    }

    /*
     * Project directories
     */

    public File srcDirectory() {
        return new File("src");
    }

    public File srcMainDirectory() {
        return new File(srcDirectory(), "main");
    }

    public File srcMainJavaDirectory() {
        return new File(srcMainDirectory(), "java");
    }

    public File srcMainResourcesDirectory() {
        return new File(srcMainDirectory(), "resources");
    }

    public File srcMainResourcesTemplatesDirectory() {
        return new File(srcMainResourcesDirectory(), "templates");
    }

    public File srcMainWebappDirectory() {
        return new File(srcMainDirectory(), "webapp");
    }

    public File srcProjectDirectory() {
        return new File(srcDirectory(), "project");
    }

    public File srcProjectJavaDirectory() {
        return new File(srcProjectDirectory(), "java");
    }

    public File srcTestJDirectory() {
        return new File(srcDirectory(), "test");
    }

    public File srcTestJavaDirectory() {
        return new File(srcTestJDirectory(), "java");
    }

    public File libDirectory() {
        return new File("lib");
    }

    public File libCompileDirectory() {
        return new File(libDirectory(), "compile");
    }

    public File libProjectDirectory() {
        return new File(libDirectory(), "project");
    }

    public File libRuntimeDirectory() {
        return new File(libDirectory(), "runtime");
    }

    public File libStandaloneDirectory() {
        return new File(libDirectory(), "standalone");
    }

    public File libTestDirectory() {
        return new File(libDirectory(), "test");
    }

    public File buildDirectory() {
        return new File("build");
    }

    public File buildDistDirectory() {
        return new File(buildDirectory(), "dist");
    }

    public File buildMainDirectory() {
        return new File(buildDirectory(), "main");
    }

    public File buildProjectDirectory() {
        return new File(buildDirectory(), "project");
    }

    public File buildTestDirectory() {
        return new File(buildDirectory(), "test");
    }

    /*
     * File collections
     */

    public List<File> mainSourceFiles() {
        // get all the main java sources
        var src_main_java_dir_abs = srcMainJavaDirectory().getAbsoluteFile();
        return FileUtils.getFileList(src_main_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_main_java_dir_abs, file)).toList();
    }

    public List<File> testSourceFiles() {
        // get all the test java sources
        var src_test_java_dir_abs = srcTestJavaDirectory().getAbsoluteFile();
        return FileUtils.getFileList(src_test_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_test_java_dir_abs, file)).toList();
    }

    /*
     * Project classpaths
     */

    public List<String> compileClasspath() {
        // detect the jar files in the compile lib directory
        var lib_compile_dir_abs = libCompileDirectory().getAbsoluteFile();
        var lib_compile_jar_files = FileUtils.getFileList(lib_compile_dir_abs, Pattern.compile("^.*\\.jar$"), null);

        // build the compilation classpath
        var compile_classpath_paths = new ArrayList<>(lib_compile_jar_files.stream().map(file -> new File(lib_compile_dir_abs, file).getAbsolutePath()).toList());
        compile_classpath_paths.add(0, buildMainDirectory().getAbsolutePath());

        return compile_classpath_paths;
    }

    public List<String> testClasspath() {
        // detect the jar files in the test lib directory
        var lib_test_dir_abs = libTestDirectory().getAbsoluteFile();
        var lib_test_jar_files = FileUtils.getFileList(lib_test_dir_abs, Pattern.compile("^.*\\.jar$"), null);

        // build the test classpath
        var test_classpath_paths = new ArrayList<>(lib_test_jar_files.stream().map(file -> new File(lib_test_dir_abs, file).getAbsolutePath()).toList());
        test_classpath_paths.addAll(0, compileClasspath());

        return test_classpath_paths;
    }

    public void start(String[] args) {
        setup();
        processArguments(args);
    }
}
