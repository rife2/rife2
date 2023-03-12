/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.bld.dependencies.*;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.ValidityChecks;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CreateCommand {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Creates a new RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Creates a new RIFE2 application.
                            
                Usage : ${topic} [package] [name]
                  package  The package of the project to create
                  name     The name of the project to create""", "${topic}", topic);
        }
    }

    private final String packageName_;
    private final String projectName_;

    private final String projectClassName_;
    private final String projectBuildName_;
    private final String projectSiteName_;
    private final String projectTestName_;

    private final File projectDir_;
    private final File srcMainJavaDir_;
    private final File srcMainResourcesTemplatesDir_;
    private final File srcMainWebappCssDir_;
    private final File srcProjectJavaDir_;
    private final File srcTestJavaDir_;
    private final File libDir_;
    private final File libCompileDir_;
    private final File libProjectDir_;
    private final File libRuntimeDir_;
    private final File libStandaloneDir_;
    private final File libTestDir_;
    private final File ideaDir_;
    private final File ideaLibrariesDir_;
    private final File ideaRunConfigurationsDir_;
    private final File javaPackageDir_;
    private final File projectPackageDir_;
    private final File testPackageDir_;

    public CreateCommand(List<String> arguments) {
        if (arguments.size() < 2) {
            throw new CommandCreationException("ERROR: Expecting the package and project names as the arguments.");
        }

        var packageName = arguments.remove(0);
        var projectName = arguments.remove(0);

        packageName_ = StringUtils.trim(packageName);
        if (packageName_.isEmpty()) {
            throw new CommandCreationException("ERROR: The package name should not be blank.");
        }

        projectName_ = StringUtils.trim(projectName);
        if (projectName_.isEmpty()) {
            throw new CommandCreationException("ERROR: The project name should not be blank.");
        }

        if (!ValidityChecks.checkJavaPackage(packageName_)) {
            throw new CommandCreationException("ERROR: The package name is invalid.");
        }
        if (!ValidityChecks.checkJavaIdentifier(projectName_)) {
            throw new CommandCreationException("ERROR: The project name is invalid.");
        }

        // standard names
        projectClassName_ = StringUtils.capitalize(projectName_);
        projectBuildName_ = projectClassName_ + "Build";
        projectSiteName_ = projectClassName_ + "Site";
        projectTestName_ = projectClassName_ + "Test";

        // create the main project structure
        projectDir_ =
            Path.of(projectName_).toFile();
        srcMainJavaDir_ =
            Path.of(projectName_, "src", "main", "java").toFile();
        srcMainResourcesTemplatesDir_ =
            Path.of(projectName_, "src", "main", "resources", "templates").toFile();
        srcMainWebappCssDir_ =
            Path.of(projectName_, "src", "main", "webapp", "css").toFile();
        srcProjectJavaDir_ =
            Path.of(projectName_, "src", "project", "java").toFile();
        srcTestJavaDir_ =
            Path.of(projectName_, "src", "test", "java").toFile();
        libDir_ =
            Path.of(projectName_, "lib").toFile();
        libCompileDir_ =
            Path.of(projectName_, "lib", "compile").toFile();
        libProjectDir_ =
            Path.of(projectName_, "lib", "project").toFile();
        libRuntimeDir_ =
            Path.of(projectName_, "lib", "runtime").toFile();
        libStandaloneDir_ =
            Path.of(projectName_, "lib", "standalone").toFile();
        libTestDir_ =
            Path.of(projectName_, "lib", "test").toFile();
        ideaDir_ =
            Path.of(projectName_, ".idea").toFile();
        ideaLibrariesDir_ =
            Path.of(projectName_, ".idea", "libraries").toFile();
        ideaRunConfigurationsDir_ =
            Path.of(projectName_, ".idea", "runConfigurations").toFile();

        var package_dir = packageName_.replace('.', File.separatorChar);
        javaPackageDir_ = new File(srcMainJavaDir_, package_dir);
        projectPackageDir_ = new File(srcProjectJavaDir_, package_dir);
        testPackageDir_ = new File(srcTestJavaDir_, package_dir);
    }

    public void execute()
    throws Exception {
        if (packageName_ == null || projectName_ == null) {
            return;
        }

        createProjectStructure();
        populateProjectStructure();
        populateIdeaProject();
        downloadDependencies();
    }

    public void createProjectStructure() {
        projectDir_.mkdirs();
        srcMainJavaDir_.mkdirs();
        srcMainResourcesTemplatesDir_.mkdirs();
        srcMainWebappCssDir_.mkdirs();
        srcProjectJavaDir_.mkdirs();
        srcTestJavaDir_.mkdirs();
        libDir_.mkdirs();
        libCompileDir_.mkdirs();
        libProjectDir_.mkdirs();
        libRuntimeDir_.mkdirs();
        libStandaloneDir_.mkdirs();
        libTestDir_.mkdirs();
        ideaDir_.mkdirs();
        ideaLibrariesDir_.mkdirs();
        ideaRunConfigurationsDir_.mkdirs();
        javaPackageDir_.mkdirs();
        projectPackageDir_.mkdirs();
        testPackageDir_.mkdirs();
    }

    public void populateProjectStructure()
    throws FileUtilsErrorException {
        // project gitignore
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.project_gitignore").getContent(),
            new File(projectDir_, ".gitignore"));

        // project site
        var site_template = TemplateFactory.TXT.get("bld.project_site");
        site_template.setValue("package", packageName_);
        site_template.setValue("projectSite", projectSiteName_);
        var project_site_file = new File(javaPackageDir_, projectSiteName_ + ".java");
        FileUtils.writeString(site_template.getContent(), project_site_file);

        // project template
        var template_template = TemplateFactory.HTML.get("bld.project_template");
        template_template.setValue("project", projectClassName_);
        var project_template_file = new File(srcMainResourcesTemplatesDir_, "hello.html");
        FileUtils.writeString(template_template.getContent(), project_template_file);

        // project css
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.project_style").getContent(),
            new File(srcMainWebappCssDir_, "style.css"));

        // project test
        var test_template = TemplateFactory.TXT.get("bld.project_test");
        test_template.setValue("package", packageName_);
        test_template.setValue("projectTest", projectTestName_);
        test_template.setValue("projectSite", projectSiteName_);
        test_template.setValue("project", projectClassName_);
        var project_test_file = new File(testPackageDir_, projectTestName_ + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // project build
        var build_template = TemplateFactory.TXT.get("bld.project_build");
        build_template.setValue("projectBuild", projectBuildName_);
        build_template.setValue("package", packageName_);
        build_template.setValue("project", projectClassName_);
        build_template.setValue("projectSite", projectSiteName_);
        for (var entry : NewProjectInfo.DEPENDENCIES.entrySet()) {
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
        var project_build_file = new File(projectPackageDir_, projectBuildName_ + ".java");
        FileUtils.writeString(build_template.getContent(), project_build_file);

        // build shell scripts
        var build_sh_template = TemplateFactory.TXT.get("bld.bld_sh");
        build_sh_template.setValue("projectBuildPath", project_build_file.getPath().substring(projectDir_.getPath().length() + 1));
        var build_sh_file = new File(projectDir_, "bld.sh");
        FileUtils.writeString(build_sh_template.getContent(), build_sh_file);
        build_sh_file.setExecutable(true);
    }

    public void populateIdeaProject()
    throws FileUtilsErrorException {
        // IDEA project files
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.app_iml").getContent(),
            new File(ideaDir_, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.misc_xml").getContent(),
            new File(ideaDir_, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.modules_xml").getContent(),
            new File(ideaDir_, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.project_iml").getContent(),
            new File(ideaDir_, "project.iml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.compile_xml").getContent(),
            new File(ideaLibrariesDir_, "compile.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.project_xml").getContent(),
            new File(ideaLibrariesDir_, "project.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.runtime_xml").getContent(),
            new File(ideaLibrariesDir_, "runtime.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.standalone_xml").getContent(),
            new File(ideaLibrariesDir_, "standalone.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.test_xml").getContent(),
            new File(ideaLibrariesDir_, "test.xml"));

        // IDEA run site
        var run_site_template = TemplateFactory.TXT.get("bld.idea.runConfigurations.Run_Site_xml");
        run_site_template.setValue("package", packageName_);
        run_site_template.setValue("projectSite", projectSiteName_);
        var run_site_file = new File(ideaRunConfigurationsDir_, "Run Site.xml");
        FileUtils.writeString(run_site_template.getContent(), run_site_file);

        // IDEA run tests
        var run_tests_template = TemplateFactory.TXT.get("bld.idea.runConfigurations.Run_Tests_xml");
        run_tests_template.setValue("package", packageName_);
        run_tests_template.setValue("projectTest", projectTestName_);
        var run_tests_file = new File(ideaRunConfigurationsDir_, "Run Tests.xml");
        FileUtils.writeString(run_tests_template.getContent(), run_tests_file);
    }

    public void downloadDependencies() {
        for (var dependency : NewProjectInfo.DEPENDENCIES.get(Scope.compile)) {
            new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                .downloadTransitivelyIntoFolder(libCompileDir_, Scope.compile);
        }
        for (var dependency : NewProjectInfo.DEPENDENCIES.get(Scope.test)) {
            new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                .downloadTransitivelyIntoFolder(libTestDir_, Scope.compile, Scope.runtime);
        }
        for (var dependency : NewProjectInfo.DEPENDENCIES.get(Scope.standalone)) {
            new DependencyResolver(NewProjectInfo.REPOSITORIES, dependency)
                .downloadTransitivelyIntoFolder(libStandaloneDir_, Scope.compile, Scope.runtime);
        }
    }
}
