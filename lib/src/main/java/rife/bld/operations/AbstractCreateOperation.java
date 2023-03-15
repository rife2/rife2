/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.dependencies.DependencyResolver;
import rife.bld.dependencies.Scope;
import rife.bld.operations.exceptions.OperationOptionException;
import rife.template.TemplateFactory;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.ValidityChecks;

import java.io.File;
import java.util.List;

abstract class AbstractCreateOperation<T extends AbstractCreateOperation<T, P>, P extends Project> {
    final String templateBase_;

    File workDirectory_ = new File(System.getProperty("user.dir"));
    String packageName_;
    String projectName_;
    boolean downloadDependencies_;

    P project_;

    String projectClassName_;
    String projectBuildName_;
    String projectMainName_;
    String projectMainUberName_;
    String projectTestName_;

    File bldPackageDirectory_;
    File mainPackageDirectory_;
    File testPackageDirectory_;
    File ideaDirectory_;
    File ideaLibrariesDirectory_;
    File ideaRunConfigurationsDirectory_;

    protected AbstractCreateOperation(String templateBase) {
        templateBase_ = templateBase;
    }

    /**
     * Performs the creation operation.
     *
     * @throws Exception when an error occurred during the creation operation
     * @since 1.5
     */
    public void execute()
    throws Exception {
        if (packageName() == null || projectName() == null) {
            System.err.println("ERROR: Missing package or project name.");
            return;
        }

        executeConfigure();
        executeCreateProjectStructure();
        executePopulateProjectStructure();
        executePopulateIdeaProject();
        if (downloadDependencies()) {
            executeDownloadDependencies();
        }
    }

    abstract P createProjectBlueprint();

    /**
     * Part of the {@link #execute} operation, configures the project.
     *
     * @since 1.5
     */
    public void executeConfigure() {
        project_ = createProjectBlueprint();

        // standard names
        projectClassName_ = StringUtils.capitalize(project_.name());
        projectBuildName_ = projectClassName_ + "Build";
        projectMainName_ = projectClassName_;
        projectMainUberName_ = projectClassName_;
        projectTestName_ = projectClassName_ + "Test";

        // create the main project structure
        ideaDirectory_ = new File(project_.workDirectory(), ".idea");
        ideaLibrariesDirectory_ = new File(ideaDirectory_, "libraries");
        ideaRunConfigurationsDirectory_ = new File(ideaDirectory_, "runConfigurations");

        var package_dir = project_.pkg().replace('.', File.separatorChar);
        bldPackageDirectory_ = new File(project_.srcBldJavaDirectory(), package_dir);
        mainPackageDirectory_ = new File(project_.srcMainJavaDirectory(), package_dir);
        testPackageDirectory_ = new File(project_.srcTestJavaDirectory(), package_dir);
    }

    /**
     * Part of the {@link #execute} operation, creates the project structure.
     *
     * @since 1.5
     */
    public void executeCreateProjectStructure() {
        project_.createProjectStructure();

        bldPackageDirectory_.mkdirs();
        mainPackageDirectory_.mkdirs();
        testPackageDirectory_.mkdirs();

        ideaDirectory_.mkdirs();
        ideaLibrariesDirectory_.mkdirs();
        ideaRunConfigurationsDirectory_.mkdirs();
    }


    /**
     * Part of the {@link #execute} operation, populates the project structure.
     *
     * @since 1.5
     */
    public void executePopulateProjectStructure()
    throws Exception {
        // project gitignore
        FileUtils.writeString(
            TemplateFactory.TXT.get(templateBase_ + "project_gitignore").getContent(),
            new File(project_.workDirectory(), ".gitignore"));

        // project main
        var site_template = TemplateFactory.TXT.get(templateBase_ + "project_main");
        site_template.setValue("package", project_.pkg());
        site_template.setValue("projectMain", projectMainName_);
        var project_main_file = new File(mainPackageDirectory_, projectMainName_ + ".java");
        FileUtils.writeString(site_template.getContent(), project_main_file);

        // project test
        var test_template = TemplateFactory.TXT.get(templateBase_ + "project_test");
        test_template.setValue("package", project_.pkg());
        test_template.setValue("projectTest", projectTestName_);
        test_template.setValue("projectMain", projectMainName_);
        if (test_template.hasValueId("project")) test_template.setValue("project", projectClassName_);
        var project_test_file = new File(testPackageDirectory_, projectTestName_ + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // project build
        var build_template = TemplateFactory.TXT.get(templateBase_ + "project_build");
        build_template.setValue("projectBuild", projectBuildName_);
        build_template.setValue("package", project_.pkg());
        build_template.setValue("project", projectClassName_);
        build_template.setValue("projectMain", projectMainName_);
        if (build_template.hasValueId("projectMainUber")) build_template.setValue("projectMainUber", projectMainUberName_);
        for (var entry : project_.dependencies().entrySet()) {
            build_template.blankValue("dependencies");

            for (var dependency : entry.getValue()) {
                build_template.setValue("groupId", dependency.groupId());
                build_template.setValue("artifactId", dependency.artifactId());
                var version = dependency.version();
                var version_string = version.major() + "," + version.minor() + "," + version.revision();
                if (!version.qualifier().isEmpty()) {
                    version_string += ",\"" + version.qualifier() + "\"";
                }
                build_template.setValue("version", version_string);
                build_template.appendBlock("dependencies", "dependency");
            }
            build_template.setValue("name", entry.getKey().name());
            build_template.appendBlock("scopes", "scope");
        }
        var project_build_file = new File(bldPackageDirectory_, projectBuildName_ + ".java");
        FileUtils.writeString(build_template.getContent(), project_build_file);

        // build shell scripts
        var build_sh_template = TemplateFactory.TXT.get("bld.bld_sh");
        build_sh_template.setValue("projectBuildPath", project_build_file.getPath().substring(project_.workDirectory().getPath().length() + 1));
        var build_sh_file = new File(project_.workDirectory(), "bld.sh");
        FileUtils.writeString(build_sh_template.getContent(), build_sh_file);
        build_sh_file.setExecutable(true);
    }

    /**
     * Part of the {@link #execute} operation, populates the IDEA project structure.
     *
     * @since 1.5
     */
    public void executePopulateIdeaProject()
    throws FileUtilsErrorException {
        // IDEA project files
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.app_iml").getContent(),
            new File(ideaDirectory_, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.bld_iml").getContent(),
            new File(ideaDirectory_, "bld.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.misc").getContent(),
            new File(ideaDirectory_, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.modules").getContent(),
            new File(ideaDirectory_, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.libraries.bld").getContent(),
            new File(ideaLibrariesDirectory_, "bld.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.libraries.compile").getContent(),
            new File(ideaLibrariesDirectory_, "compile.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.libraries.runtime").getContent(),
            new File(ideaLibrariesDirectory_, "runtime.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get(templateBase_ + "idea.libraries.test").getContent(),
            new File(ideaLibrariesDirectory_, "test.xml"));

        // IDEA run site
        var run_site_template = TemplateFactory.XML.get(templateBase_ + "idea.runConfigurations.Run_Main");
        run_site_template.setValue("package", project_.pkg());
        run_site_template.setValue("projectMain", projectMainName_);
        var run_site_file = new File(ideaRunConfigurationsDirectory_, "Run Main.xml");
        FileUtils.writeString(run_site_template.getContent(), run_site_file);

        // IDEA run tests
        var run_tests_template = TemplateFactory.XML.get(templateBase_ + "idea.runConfigurations.Run_Tests");
        run_tests_template.setValue("package", project_.pkg());
        run_tests_template.setValue("projectTest", projectTestName_);
        var run_tests_file = new File(ideaRunConfigurationsDirectory_, "Run Tests.xml");
        FileUtils.writeString(run_tests_template.getContent(), run_tests_file);
    }

    /**
     * Part of the {@link #execute} operation, downloads the dependencies, when enabled.
     *
     * @since 1.5
     */
    public void executeDownloadDependencies() {
        var compile_dependencies = project_.dependencies().get(Scope.compile);
        if (compile_dependencies != null) {
            for (var dependency : compile_dependencies) {
                new DependencyResolver(project_.repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(project_.libCompileDirectory(), Scope.compile);
            }
        }

        var test_dependencies = project_.dependencies().get(Scope.test);
        if (test_dependencies != null) {
            for (var dependency : test_dependencies) {
                new DependencyResolver(project_.repositories(), dependency)
                    .downloadTransitivelyIntoDirectory(project_.libTestDirectory(), Scope.compile, Scope.runtime);
            }
        }
    }

    /**
     * Configures a creation operation from command-line arguments.
     *
     * @param arguments the arguments that will be considered
     * @return this operation instance
     * @since 1.5
     */
    public T fromArguments(List<String> arguments) {
        if (arguments.size() < 2) {
            throw new OperationOptionException("ERROR: Expecting the package and project names as the arguments.");
        }

        return workDirectory(new File(System.getProperty("user.dir")))
            .packageName(arguments.remove(0))
            .projectName(arguments.remove(0))
            .downloadDependencies(true);
    }

    /**
     * Provides the work directory in which the project will be created.
     * <p>
     * If no work directory is provided, the JVM working directory will be used.
     *
     * @param directory the directory to use as a work directory
     * @return this operation instance
     * @since 1.5
     */
    public T workDirectory(File directory) {
        if (!directory.exists()) {
            throw new OperationOptionException("ERROR: The work directory '" + directory + "' doesn't exist.");
        }
        if (!directory.isDirectory()) {
            throw new OperationOptionException("ERROR: '" + directory + "' is not a directory.");
        }
        if (!directory.canWrite()) {
            throw new OperationOptionException("ERROR: The work directory '" + directory + "' is not writable.");
        }

        workDirectory_ = directory;
        return (T) this;
    }

    /**
     * Provides the package of the project that will be created.
     *
     * @param name the package name
     * @return this operation instance
     * @since 1.5
     */
    public T packageName(String name) {
        packageName_ = StringUtils.trim(name);
        if (packageName_.isEmpty()) {
            throw new OperationOptionException("ERROR: The package name should not be blank.");
        }

        if (!ValidityChecks.checkJavaPackage(packageName_)) {
            throw new OperationOptionException("ERROR: The package name is invalid.");
        }

        packageName_ = name;
        return (T) this;
    }

    /**
     * Provides the name of the project that will be created.
     *
     * @param name the project name
     * @return this operation instance
     * @since 1.5
     */
    public T projectName(String name) {
        projectName_ = StringUtils.trim(name);
        if (projectName_.isEmpty()) {
            throw new OperationOptionException("ERROR: The project name should not be blank.");
        }

        if (!ValidityChecks.checkJavaIdentifier(projectName_)) {
            throw new OperationOptionException("ERROR: The project name is invalid.");
        }
        projectName_ = name;
        return (T) this;
    }

    /**
     * Indicates whether the dependencies for the project should be downloaded
     * upon creation, by default this is {@code false}.
     *
     * @param flag {@code true} if the dependencies should be downloaded; or
     *             {@code false} otherwise
     * @return this operation instance
     * @since 1.5
     */
    public T downloadDependencies(boolean flag) {
        downloadDependencies_ = flag;
        return (T) this;
    }

    /**
     * Retrieves the work directory that is used for the project creation.
     *
     * @return the work directory
     * @since 1.5
     */
    public File workDirectory() {
        return workDirectory_;
    }

    /**
     * Retrieves the package that is used for the project creation.
     *
     * @return the package name
     * @since 1.5
     */
    public String packageName() {
        return packageName_;
    }

    /**
     * Retrieves the name that is used for the project creation.
     *
     * @return the project name
     * @since 1.5
     */
    public String projectName() {
        return projectName_;
    }

    /**
     * Retrieves whether dependencies will be downloaded at project creation.
     *
     * @return {@code true} if dependencies will be downloaded; or
     * {@code false} otherwise
     * @since 1.5
     */
    public boolean downloadDependencies() {
        return downloadDependencies_;
    }

    /**
     * Retrieves the project instance that was used as a blueprint for the
     * project creation.
     *
     * @return the project creation blueprint instance
     * @since 1.5
     */
    public Project project() {
        return project_;
    }
}
