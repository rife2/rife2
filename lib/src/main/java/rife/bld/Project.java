/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.help.*;
import rife.bld.operations.*;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Project extends BuildExecutor {
    public File workDirectory = new File(System.getProperty("user.dir"));
    public String pkg = null;
    public String name = null;
    public VersionNumber version = null;
    public String mainClass = null;

    protected List<Repository> repositories = new ArrayList<>();
    protected DependencyScopes dependencies = new DependencyScopes();

    protected List<TemplateType> precompiledTemplateTypes = new ArrayList<>();
    protected List<String> compileJavacOptions = new ArrayList<>();
    protected String javaTool = null;
    protected List<String> runJavaOptions = new ArrayList<>();
    protected List<String> testJavaOptions = new ArrayList<>();
    protected String testToolMainClass;
    protected List<String> testToolOptions = new ArrayList<>();
    protected String archiveBaseName = null;
    protected String jarFileName = null;
    protected String uberJarFileName = null;
    protected String uberJarMainClass = null;

    protected File srcDirectory = null;
    protected File srcBldDirectory = null;
    protected File srcBldJavaDirectory = null;
    protected File srcMainDirectory = null;
    protected File srcMainJavaDirectory = null;
    protected File srcMainResourcesDirectory = null;
    protected File srcMainResourcesTemplatesDirectory = null;
    protected File srcTestJDirectory = null;
    protected File srcTestJavaDirectory = null;
    protected File libBldDirectory = null;
    protected File libDirectory = null;
    protected File libCompileDirectory = null;
    protected File libRuntimeDirectory = null;
    protected File libStandaloneDirectory = null;
    protected File libTestDirectory = null;
    protected File buildDirectory = null;
    protected File buildBldDirectory = null;
    protected File buildDistDirectory = null;
    protected File buildMainDirectory = null;
    protected File buildTemplatesDirectory = null;
    protected File buildTestDirectory = null;

    /*
     * Standard build commands
     */

    @BuildCommand(help = CleanHelp.class)
    public void clean()
    throws Exception {
        new CleanOperation().fromProject(this).execute();
    }

    @BuildCommand(help = CompileHelp.class)
    public void compile()
    throws Exception {
        new CompileOperation().fromProject(this).execute();
    }

    @BuildCommand(help = DownloadHelp.class)
    public void download() {
        new DownloadOperation().fromProject(this).execute();
    }

    @BuildCommand(help = PrecompileHelp.class)
    public void precompile() {
        new PrecompileOperation().fromProject(this).execute();
    }

    @BuildCommand(help = JarHelp.class)
    public void jar()
    throws Exception {
        clean();
        compile();
        precompile();
        new JarOperation().fromProject(this).execute();
    }

    @BuildCommand(help = UberJarHelp.class)
    public void uberjar()
    throws Exception {
        jar();
        new UberJarOperation().fromProject(this).execute();
    }

    @BuildCommand(help = RunHelp.class)
    public void run()
    throws Exception {
        new RunOperation().fromProject(this).execute();
    }

    @BuildCommand(help = TestHelp.class)
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
        return dependencies().scope(scope);
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

    public File workDirectory() {
        return Objects.requireNonNullElseGet(workDirectory, () -> new File(System.getProperty("user.dir")));
    }

    public File srcDirectory() {
        return Objects.requireNonNullElseGet(srcDirectory, () -> new File(workDirectory(), "src"));
    }

    public File srcBldDirectory() {
        return Objects.requireNonNullElseGet(srcBldDirectory, () -> new File(srcDirectory(), "bld"));
    }

    public File srcBldJavaDirectory() {
        return Objects.requireNonNullElseGet(srcBldJavaDirectory, () -> new File(srcBldDirectory(), "java"));
    }

    public File srcMainDirectory() {
        return Objects.requireNonNullElseGet(srcMainDirectory, () -> new File(srcDirectory(), "main"));
    }

    public File srcMainJavaDirectory() {
        return Objects.requireNonNullElseGet(srcMainJavaDirectory, () -> new File(srcMainDirectory(), "java"));
    }

    public File srcMainResourcesDirectory() {
        return Objects.requireNonNullElseGet(srcMainResourcesDirectory, () -> new File(srcMainDirectory(), "resources"));
    }

    public File srcMainResourcesTemplatesDirectory() {
        return Objects.requireNonNullElseGet(srcMainResourcesTemplatesDirectory, () -> new File(srcMainResourcesDirectory(), "templates"));
    }

    public File srcTestJDirectory() {
        return Objects.requireNonNullElseGet(srcTestJDirectory, () -> new File(srcDirectory(), "test"));
    }

    public File srcTestJavaDirectory() {
        return Objects.requireNonNullElseGet(srcTestJavaDirectory, () -> new File(srcTestJDirectory(), "java"));
    }

    public File libDirectory() {
        return Objects.requireNonNullElseGet(libDirectory, () -> new File(workDirectory(), "lib"));
    }

    public File libBldDirectory() {
        return Objects.requireNonNullElseGet(libBldDirectory, () -> new File(libDirectory(), "bld"));
    }

    public File libCompileDirectory() {
        return Objects.requireNonNullElseGet(libCompileDirectory, () -> new File(libDirectory(), "compile"));
    }

    public File libRuntimeDirectory() {
        return Objects.requireNonNullElseGet(libRuntimeDirectory, () -> new File(libDirectory(), "runtime"));
    }

    public File libStandaloneDirectory() {
        return null;
    }

    public File libTestDirectory() {
        return Objects.requireNonNullElseGet(libTestDirectory, () -> new File(libDirectory(), "test"));
    }

    public File buildDirectory() {
        return Objects.requireNonNullElseGet(buildDirectory, () -> new File(workDirectory(), "build"));
    }

    public File buildBldDirectory() {
        return Objects.requireNonNullElseGet(buildBldDirectory, () -> new File(buildDirectory(), "bld"));
    }

    public File buildDistDirectory() {
        return Objects.requireNonNullElseGet(buildDistDirectory, () -> new File(buildDirectory(), "dist"));
    }

    public File buildMainDirectory() {
        return Objects.requireNonNullElseGet(buildMainDirectory, () -> new File(buildDirectory(), "main"));
    }

    public File buildTemplatesDirectory() {
        return Objects.requireNonNullElseGet(buildTemplatesDirectory, this::buildMainDirectory);
    }

    public File buildTestDirectory() {
        return Objects.requireNonNullElseGet(buildTestDirectory, () -> new File(buildDirectory(), "test"));
    }

    public void createProjectStructure() {
        srcMainJavaDirectory().mkdirs();
        srcMainResourcesTemplatesDirectory().mkdirs();
        srcBldJavaDirectory().mkdirs();
        srcTestJavaDirectory().mkdirs();
        libCompileDirectory().mkdirs();
        libBldDirectory().mkdirs();
        libRuntimeDirectory().mkdirs();
        libTestDirectory().mkdirs();
        if (libStandaloneDirectory() != null) {
            libStandaloneDirectory().mkdirs();
        }
    }

    public void createBuildStructure() {
        buildBldDirectory().mkdirs();
        buildDistDirectory().mkdirs();
        buildMainDirectory().mkdirs();
        buildTestDirectory().mkdirs();
    }

    /*
     * Project options
     */

    public List<Repository> repositories() {
        return Objects.requireNonNullElse(repositories, Collections.emptyList());
    }

    public String pkg() {
        if (pkg == null) {
            throw new IllegalStateException("The pkg variable has to be set.");
        }
        return pkg;
    }

    public String name() {
        if (name == null) {
            throw new IllegalStateException("The name variable has to be set.");
        }
        return name;
    }

    public VersionNumber version() {
        if (version == null) {
            throw new IllegalStateException("The version variable has to be set.");
        }
        return version;
    }

    public String mainClass() {
        if (mainClass == null) {
            throw new IllegalStateException("The mainClass variable has to be set.");
        }
        return mainClass;
    }

    public DependencyScopes dependencies() {
        return Objects.requireNonNullElseGet(dependencies, DependencyScopes::new);
    }

    public List<TemplateType> precompiledTemplateTypes() {
        return Objects.requireNonNullElse(precompiledTemplateTypes, Collections.emptyList());
    }

    public List<String> compileJavacOptions() {
        return Objects.requireNonNullElse(compileJavacOptions, Collections.emptyList());
    }

    public String javaTool() {
        return Objects.requireNonNullElse(javaTool, "java");
    }

    public List<String> runJavaOptions() {
        return Objects.requireNonNullElse(runJavaOptions, Collections.emptyList());
    }

    public List<String> testJavaOptions() {
        return Objects.requireNonNullElse(testJavaOptions, Collections.emptyList());
    }

    public String testToolMainClass() {
        return Objects.requireNonNullElse(testToolMainClass, "org.junit.platform.console.ConsoleLauncher");

    }

    public List<String> testToolOptions() {
        if (testToolOptions == null || testToolOptions.isEmpty()) {
            var result = new ArrayList<String>();
            result.add("--scan-classpath");
            result.add("--disable-banner");
            result.add("--disable-ansi-colors");
            result.add("--exclude-engine=junit-platform-suite");
            result.add("--exclude-engine=junit-vintage");
            return result;
        }

        return testToolOptions;
    }

    public String archiveBaseName() {
        return Objects.requireNonNullElseGet(archiveBaseName, () -> name().toLowerCase(Locale.ENGLISH));
    }

    public String jarFileName() {
        return Objects.requireNonNullElseGet(jarFileName, () -> archiveBaseName() + "-" + version() + ".jar");
    }

    public String uberJarMainClass() {
        return Objects.requireNonNullElseGet(uberJarMainClass, this::mainClass);
    }

    public String uberJarFileName() {
        return Objects.requireNonNullElseGet(uberJarFileName, () -> archiveBaseName() + "-" + version() + "-uber.jar");
    }

    /*
     * File collections
     */

    public static final Pattern JAVA_FILE_PATTERN = Pattern.compile("^.*\\.java$");
    public static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.*\\.jar$");

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

    public List<File> compileClasspathJars() {
        // detect the jar files in the compile lib directory
        var dir_abs = libCompileDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the compilation classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    public List<File> runtimeClasspathJars() {
        // detect the jar files in the runtime lib directory
        var dir_abs = libRuntimeDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the runtime classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    public List<File> standaloneClasspathJars() {
        if (libStandaloneDirectory() == null) {
            return Collections.emptyList();
        }

        // detect the jar files in the standalone lib directory
        var dir_abs = libStandaloneDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the standalone classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    public List<File> testClasspathJars() {
        // detect the jar files in the test lib directory
        var dir_abs = libTestDirectory().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, null);

        // build the test classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    public List<String> compileMainClasspath() {
        return FileUtils.combineToAbsolutePaths(compileClasspathJars());
    }

    public List<String> compileTestClasspath() {
        var paths = FileUtils.combineToAbsolutePaths(compileClasspathJars(), testClasspathJars());
        paths.add(buildMainDirectory().getAbsolutePath());
        return paths;
    }

    public List<String> runClasspath() {
        var paths = FileUtils.combineToAbsolutePaths(compileClasspathJars(), runtimeClasspathJars(), standaloneClasspathJars());
        paths.add(srcMainResourcesDirectory().getAbsolutePath());
        paths.add(buildMainDirectory().getAbsolutePath());
        return paths;
    }

    public List<String> testClasspath() {
        var paths = FileUtils.combineToAbsolutePaths(compileClasspathJars(), runtimeClasspathJars(), standaloneClasspathJars(), testClasspathJars());
        paths.add(srcMainResourcesDirectory().getAbsolutePath());
        paths.add(buildMainDirectory().getAbsolutePath());
        paths.add(buildTestDirectory().getAbsolutePath());
        return paths;
    }
}
