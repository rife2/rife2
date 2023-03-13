/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.operations.exceptions.OperationOptionException;
import rife.bld.dependencies.*;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.ValidityChecks;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CreateOperation {
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
    
    private String packageName_;
    private String projectName_;

    private String projectClassName_;
    private String projectBuildName_;
    private String projectSiteName_;
    private String projectTestName_;

    private File projectDir_;
    private File srcMainJavaDir_;
    private File srcMainResourcesTemplatesDir_;
    private File srcMainWebappCssDir_;
    private File srcMainWebappWebInfDir_;
    private File srcProjectJavaDir_;
    private File srcTestJavaDir_;
    private File libDir_;
    private File libCompileDir_;
    private File libProjectDir_;
    private File libRuntimeDir_;
    private File libStandaloneDir_;
    private File libTestDir_;
    private File ideaDir_;
    private File ideaLibrariesDir_;
    private File ideaRunConfigurationsDir_;
    private File javaPackageDir_;
    private File projectPackageDir_;
    private File testPackageDir_;

    public CreateOperation() {
    }

    public void execute()
    throws Exception {
        if (packageName() == null || projectName() == null) {
            System.err.println("ERROR: Missing package or project name.");
            return;
        }

        // standard names
        projectClassName_ = StringUtils.capitalize(projectName());
        projectBuildName_ = projectClassName_ + "Build";
        projectSiteName_ = projectClassName_ + "Site";
        projectTestName_ = projectClassName_ + "Test";

        // create the main project structure
        projectDir_ =
            Path.of(projectName()).toFile();
        srcMainJavaDir_ =
            Path.of(projectName(), "src", "main", "java").toFile();
        srcMainResourcesTemplatesDir_ =
            Path.of(projectName(), "src", "main", "resources", "templates").toFile();
        srcMainWebappCssDir_ =
            Path.of(projectName(), "src", "main", "webapp", "css").toFile();
        srcMainWebappWebInfDir_ =
            Path.of(projectName(), "src", "main", "webapp", "WEB-INF").toFile();
        srcProjectJavaDir_ =
            Path.of(projectName(), "src", "project", "java").toFile();
        srcTestJavaDir_ =
            Path.of(projectName(), "src", "test", "java").toFile();
        libDir_ =
            Path.of(projectName(), "lib").toFile();
        libCompileDir_ =
            Path.of(projectName(), "lib", "compile").toFile();
        libProjectDir_ =
            Path.of(projectName(), "lib", "project").toFile();
        libRuntimeDir_ =
            Path.of(projectName(), "lib", "runtime").toFile();
        libStandaloneDir_ =
            Path.of(projectName(), "lib", "standalone").toFile();
        libTestDir_ =
            Path.of(projectName(), "lib", "test").toFile();
        ideaDir_ =
            Path.of(projectName(), ".idea").toFile();
        ideaLibrariesDir_ =
            Path.of(projectName(), ".idea", "libraries").toFile();
        ideaRunConfigurationsDir_ =
            Path.of(projectName(), ".idea", "runConfigurations").toFile();

        var package_dir = packageName().replace('.', File.separatorChar);
        javaPackageDir_ = new File(srcMainJavaDir_, package_dir);
        projectPackageDir_ = new File(srcProjectJavaDir_, package_dir);
        testPackageDir_ = new File(srcTestJavaDir_, package_dir);

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
        srcMainWebappWebInfDir_.mkdirs();
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
            TemplateFactory.TXT.get("bld.rife2_hello.project_gitignore").getContent(),
            new File(projectDir_, ".gitignore"));

        // project site
        var site_template = TemplateFactory.TXT.get("bld.rife2_hello.project_site");
        site_template.setValue("package", packageName());
        site_template.setValue("projectSite", projectSiteName_);
        var project_site_file = new File(javaPackageDir_, projectSiteName_ + ".java");
        FileUtils.writeString(site_template.getContent(), project_site_file);

        // project template
        var template_template = TemplateFactory.HTML.get("bld.rife2_hello.project_template");
        template_template.setValue("project", projectClassName_);
        var project_template_file = new File(srcMainResourcesTemplatesDir_, "hello.html");
        FileUtils.writeString(template_template.getContent(), project_template_file);

        // project css
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.rife2_hello.project_style").getContent(),
            new File(srcMainWebappCssDir_, "style.css"));

        // project web.xml
        var web_xml_template = TemplateFactory.XML.get("bld.rife2_hello.project_web");
        web_xml_template.setValue("package", packageName());
        web_xml_template.setValue("projectSite", projectSiteName_);
        var project_web_xml_file = new File(srcMainWebappWebInfDir_, "web.xml");
        FileUtils.writeString(web_xml_template.getContent(), project_web_xml_file);

        // project test
        var test_template = TemplateFactory.TXT.get("bld.rife2_hello.project_test");
        test_template.setValue("package", packageName());
        test_template.setValue("projectTest", projectTestName_);
        test_template.setValue("projectSite", projectSiteName_);
        test_template.setValue("project", projectClassName_);
        var project_test_file = new File(testPackageDir_, projectTestName_ + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // project build
        var build_template = TemplateFactory.TXT.get("bld.rife2_hello.project_build");
        build_template.setValue("projectBuild", projectBuildName_);
        build_template.setValue("package", packageName());
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
            TemplateFactory.XML.get("bld.rife2_hello.idea.app_iml").getContent(),
            new File(ideaDir_, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.misc").getContent(),
            new File(ideaDir_, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.modules").getContent(),
            new File(ideaDir_, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.project_iml").getContent(),
            new File(ideaDir_, "project.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.compile").getContent(),
            new File(ideaLibrariesDir_, "compile.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.project").getContent(),
            new File(ideaLibrariesDir_, "project.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.runtime").getContent(),
            new File(ideaLibrariesDir_, "runtime.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.standalone").getContent(),
            new File(ideaLibrariesDir_, "standalone.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.test").getContent(),
            new File(ideaLibrariesDir_, "test.xml"));

        // IDEA run site
        var run_site_template = TemplateFactory.XML.get("bld.rife2_hello.idea.runConfigurations.Run_Site");
        run_site_template.setValue("package", packageName());
        run_site_template.setValue("projectSite", projectSiteName_);
        var run_site_file = new File(ideaRunConfigurationsDir_, "Run Site.xml");
        FileUtils.writeString(run_site_template.getContent(), run_site_file);

        // IDEA run tests
        var run_tests_template = TemplateFactory.XML.get("bld.rife2_hello.idea.runConfigurations.Run_Tests");
        run_tests_template.setValue("package", packageName());
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

    public CreateOperation fromArguments(List<String> arguments) {
        if (arguments.size() < 2) {
            throw new OperationOptionException("ERROR: Expecting the package and project names as the arguments.");
        }

        return packageName(arguments.remove(0)).projectName(arguments.remove(0));
    }

    public CreateOperation packageName(String name) {
        packageName_ = StringUtils.trim(name);
        if (packageName_.isEmpty()) {
            throw new OperationOptionException("ERROR: The package name should not be blank.");
        }

        if (!ValidityChecks.checkJavaPackage(packageName_)) {
            throw new OperationOptionException("ERROR: The package name is invalid.");
        }

        packageName_ = name;
        return this;
    }

    public CreateOperation projectName(String name) {
        projectName_ = StringUtils.trim(name);
        if (projectName_.isEmpty()) {
            throw new OperationOptionException("ERROR: The project name should not be blank.");
        }

        if (!ValidityChecks.checkJavaIdentifier(projectName_)) {
            throw new OperationOptionException("ERROR: The project name is invalid.");
        }
        projectName_ = name;
        return this;
    }

    public String packageName() {
        return packageName_;
    }

    public String projectName() {
        return projectName_;
    }
}
