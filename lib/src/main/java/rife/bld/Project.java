/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.operations.*;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public abstract class Project extends BuildExecutor {
    public List<Repository> repositories = new ArrayList<>();
    public String pkg = null;
    public String name = null;
    public VersionNumber version = null;
    public String mainClass = null;

    public DependencyScopes dependencies = new DependencyScopes();

    public List<TemplateType> precompiledTemplateTypes = new ArrayList<>();

    public abstract void setup();

    /*
     * Standard build commands
     */

    @BuildCommand(help = CleanOperation.Help.class)
    public void clean()
    throws Exception {
        new CleanOperation().fromProject(this).execute();
    }

    @BuildCommand(help = CompileOperation.Help.class)
    public void compile()
    throws Exception {
        new CompileOperation().fromProject(this).execute();
    }

    @BuildCommand(help = DownloadOperation.Help.class)
    public void download() {
        new DownloadOperation().fromProject(this).execute();
    }

    @BuildCommand(help = PrecompileOperation.Help.class)
    public void precompile() {
        new PrecompileOperation().fromProject(this).execute();
    }

    @BuildCommand(help = JarOperation.Help.class)
    public void jar()
    throws Exception {
        clean();
        compile();
        precompile();
        new JarOperation().fromProject(this).execute();
    }

    @BuildCommand(help = RunOperation.Help.class)
    public void run()
    throws Exception {
        new RunOperation().fromProject(this).execute();
    }

    @BuildCommand(help = TestOperation.Help.class)
    public void test()
    throws Exception {
        new TestOperation().fromProject(this).execute();
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

    @SafeVarargs
    public static List<String> combinePaths(List<String>... paths) {
        var result = new ArrayList<String>();
        for (var p : paths) {
            result.addAll(p);
        }
        return result;
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

    public File buildTemplatesDirectory() {
        return buildMainDirectory();
    }

    public File buildTestDirectory() {
        return new File(buildDirectory(), "test");
    }

    /*
     * Process options
     */

    public List<String> compileJavacOptions() {
        return Collections.emptyList();
    }

    public String javaTool() {
        return "java";
    }

    public List<String> runJavaOptions() {
        return Collections.emptyList();
    }

    public List<String> testJavaOptions() {
        return Collections.emptyList();
    }

    public String testToolMainClass() {
        return "org.junit.platform.console.ConsoleLauncher";
    }

    public List<String> testToolOptions() {
        var result = new ArrayList<String>();
        result.add("--scan-classpath");
        result.add("--exclude-engine=junit-platform-suite");
        result.add("--exclude-engine=junit-vintage");
        return result;
    }

    public String jarFileName() {
        return name + "-" + version + ".jar";
    }

    /*
     * File collections
     */

    private static final Pattern JAVA_FILE_PATTERN = Pattern.compile("^.*\\.java$");

    public List<File> mainSourceFiles() {
        // get all the main java sources
        var src_main_java_dir_abs = srcMainJavaDirectory().getAbsoluteFile();
        return FileUtils.getFileList(src_main_java_dir_abs, JAVA_FILE_PATTERN, null)
            .stream().map(file -> new File(src_main_java_dir_abs, file)).toList();
    }

    public List<File> testSourceFiles() {
        // get all the test java sources
        var src_test_java_dir_abs = srcTestJavaDirectory().getAbsoluteFile();
        return FileUtils.getFileList(src_test_java_dir_abs, JAVA_FILE_PATTERN, null)
            .stream().map(file -> new File(src_test_java_dir_abs, file)).toList();
    }

    /*
     * Project classpaths
     */

    private static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.*\\.jar$");

    public List<String> compileClasspathJars() {
        // detect the jar files in the compile lib directory
        var dir_abs = libCompileDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the compilation classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file).getAbsolutePath()).toList());
    }

    public List<String> runtimeClasspathJars() {
        // detect the jar files in the runtime lib directory
        var dir_abs = libRuntimeDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the runtime classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file).getAbsolutePath()).toList());
    }

    public List<String> standaloneClasspathJars() {
        // detect the jar files in the standalone lib directory
        var dir_abs = libStandaloneDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the standalone classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file).getAbsolutePath()).toList());
    }

    public List<String> testClasspathJars() {
        // detect the jar files in the test lib directory
        var dir_abs = libTestDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the test classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file).getAbsolutePath()).toList());
    }

    public List<String> compileMainClasspath() {
        return compileClasspathJars();
    }

    public List<String> compileTestClasspath() {
        var paths = Project.combinePaths(compileClasspathJars(), testClasspathJars());
        paths.add(buildMainDirectory().getAbsolutePath());
        return paths;
    }

    public List<String> runClasspath() {
        var paths = Project.combinePaths(compileClasspathJars(), runtimeClasspathJars(), standaloneClasspathJars());
        paths.add(srcMainResourcesDirectory().getAbsolutePath());
        paths.add(buildMainDirectory().getAbsolutePath());
        return paths;
    }

    public List<String> testClasspath() {
        var paths = Project.combinePaths(compileClasspathJars(), runtimeClasspathJars(), standaloneClasspathJars(), testClasspathJars());
        paths.add(srcMainResourcesDirectory().getAbsolutePath());
        paths.add(buildMainDirectory().getAbsolutePath());
        paths.add(buildTestDirectory().getAbsolutePath());
        return paths;
    }

    public void start(String[] args) {
        setup();
        processArguments(args);
    }
}
