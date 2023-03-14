/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.bld.operations.exceptions.OperationOptionException;
import rife.bld.dependencies.*;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.ValidityChecks;

import java.io.File;
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

    private File workDirectory_;
    private String packageName_;
    private String projectName_;
    private boolean downloadDependencies_;

    private Project project_;

    private String projectClassName_;
    private String projectBuildName_;
    private String projectSiteName_;
    private String projectSiteUberName_;
    private String projectTestName_;

    private File srcMainWebappCssDirectory_;
    private File srcMainWebappWebInfDirectory_;
    private File ideaDirectory_;
    private File ideaLibrariesDirectory_;
    private File ideaRunConfigurationsDirectory_;
    private File javaPackageDirectory_;
    private File projectPackageDirectory_;
    private File testPackageDirectory_;

    public CreateOperation() {
    }

    public void execute()
    throws Exception {
        if (packageName() == null || projectName() == null) {
            System.err.println("ERROR: Missing package or project name.");
            return;
        }

        project_ = new NewProjectTemplate(new File(workDirectory(), projectName()), packageName(), projectName());

        // standard names
        projectClassName_ = StringUtils.capitalize(project_.name());
        projectBuildName_ = projectClassName_ + "Build";
        projectSiteName_ = projectClassName_ + "Site";
        projectSiteUberName_ = projectSiteName_ + "Uber";
        projectTestName_ = projectClassName_ + "Test";

        // create the main project structure
        srcMainWebappCssDirectory_ = new File(project_.srcMainWebappDirectory(), "css");
        srcMainWebappWebInfDirectory_ = new File(project_.srcMainWebappDirectory(), "WEB-INF");
        ideaDirectory_ = new File(project_.workDirectory(), ".idea");
        ideaLibrariesDirectory_ = new File(ideaDirectory_, "libraries");
        ideaRunConfigurationsDirectory_ = new File(ideaDirectory_, "runConfigurations");

        var package_dir = project_.pkg().replace('.', File.separatorChar);
        javaPackageDirectory_ = new File(project_.srcMainJavaDirectory(), package_dir);
        projectPackageDirectory_ = new File(project_.srcProjectJavaDirectory(), package_dir);
        testPackageDirectory_ = new File(project_.srcTestJavaDirectory(), package_dir);

        executeCreateProjectStructure();
        executePopulateProjectStructure();
        executePopulateIdeaProject();
        if (downloadDependencies()) {
            executeDownloadDependencies();
        }
    }

    public void executeCreateProjectStructure() {
        project_.createProjectStructure();

        srcMainWebappCssDirectory_.mkdirs();
        srcMainWebappWebInfDirectory_.mkdirs();
        ideaDirectory_.mkdirs();
        ideaLibrariesDirectory_.mkdirs();
        ideaRunConfigurationsDirectory_.mkdirs();
        javaPackageDirectory_.mkdirs();
        projectPackageDirectory_.mkdirs();
        testPackageDirectory_.mkdirs();
    }

    public void executePopulateProjectStructure()
    throws FileUtilsErrorException {
        // project gitignore
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.rife2_hello.project_gitignore").getContent(),
            new File(project_.workDirectory(), ".gitignore"));

        // project site
        var site_template = TemplateFactory.TXT.get("bld.rife2_hello.project_site");
        site_template.setValue("package", project_.pkg());
        site_template.setValue("projectSite", projectSiteName_);
        var project_site_file = new File(javaPackageDirectory_, projectSiteName_ + ".java");
        FileUtils.writeString(site_template.getContent(), project_site_file);

        // project site uber
        var site_uber_template = TemplateFactory.TXT.get("bld.rife2_hello.project_site_uber");
        site_uber_template.setValue("package", project_.pkg());
        site_uber_template.setValue("projectSite", projectSiteName_);
        site_uber_template.setValue("projectSiteUber", projectSiteUberName_);
        var project_site_uber_file = new File(javaPackageDirectory_, projectSiteUberName_ + ".java");
        FileUtils.writeString(site_uber_template.getContent(), project_site_uber_file);

        // project template
        var template_template = TemplateFactory.HTML.get("bld.rife2_hello.project_template");
        template_template.setValue("project", projectClassName_);
        var project_template_file = new File(project_.srcMainResourcesTemplatesDirectory(), "hello.html");
        FileUtils.writeString(template_template.getContent(), project_template_file);

        // project css
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.rife2_hello.project_style").getContent(),
            new File(srcMainWebappCssDirectory_, "style.css"));

        // project web.xml
        var web_xml_template = TemplateFactory.XML.get("bld.rife2_hello.project_web");
        web_xml_template.setValue("package", project_.pkg());
        web_xml_template.setValue("projectSite", projectSiteName_);
        var project_web_xml_file = new File(srcMainWebappWebInfDirectory_, "web.xml");
        FileUtils.writeString(web_xml_template.getContent(), project_web_xml_file);

        // project test
        var test_template = TemplateFactory.TXT.get("bld.rife2_hello.project_test");
        test_template.setValue("package", project_.pkg());
        test_template.setValue("projectTest", projectTestName_);
        test_template.setValue("projectSite", projectSiteName_);
        test_template.setValue("project", projectClassName_);
        var project_test_file = new File(testPackageDirectory_, projectTestName_ + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // project build
        var build_template = TemplateFactory.TXT.get("bld.rife2_hello.project_build");
        build_template.setValue("projectBuild", projectBuildName_);
        build_template.setValue("package", project_.pkg());
        build_template.setValue("project", projectClassName_);
        build_template.setValue("projectSite", projectSiteName_);
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
        var project_build_file = new File(projectPackageDirectory_, projectBuildName_ + ".java");
        FileUtils.writeString(build_template.getContent(), project_build_file);

        // build shell scripts
        var build_sh_template = TemplateFactory.TXT.get("bld.bld_sh");
        build_sh_template.setValue("projectBuildPath", project_build_file.getPath().substring(project_.workDirectory().getPath().length() + 1));
        var build_sh_file = new File(project_.workDirectory(), "bld.sh");
        FileUtils.writeString(build_sh_template.getContent(), build_sh_file);
        build_sh_file.setExecutable(true);
    }

    public void executePopulateIdeaProject()
    throws FileUtilsErrorException {
        // IDEA project files
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.app_iml").getContent(),
            new File(ideaDirectory_, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.misc").getContent(),
            new File(ideaDirectory_, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.modules").getContent(),
            new File(ideaDirectory_, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.project_iml").getContent(),
            new File(ideaDirectory_, "project.iml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.compile").getContent(),
            new File(ideaLibrariesDirectory_, "compile.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.project").getContent(),
            new File(ideaLibrariesDirectory_, "project.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.runtime").getContent(),
            new File(ideaLibrariesDirectory_, "runtime.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.standalone").getContent(),
            new File(ideaLibrariesDirectory_, "standalone.xml"));
        FileUtils.writeString(
            TemplateFactory.XML.get("bld.rife2_hello.idea.libraries.test").getContent(),
            new File(ideaLibrariesDirectory_, "test.xml"));

        // IDEA run site
        var run_site_template = TemplateFactory.XML.get("bld.rife2_hello.idea.runConfigurations.Run_Site");
        run_site_template.setValue("package", project_.pkg());
        run_site_template.setValue("projectSite", projectSiteName_);
        var run_site_file = new File(ideaRunConfigurationsDirectory_, "Run Site.xml");
        FileUtils.writeString(run_site_template.getContent(), run_site_file);

        // IDEA run tests
        var run_tests_template = TemplateFactory.XML.get("bld.rife2_hello.idea.runConfigurations.Run_Tests");
        run_tests_template.setValue("package", project_.pkg());
        run_tests_template.setValue("projectTest", projectTestName_);
        var run_tests_file = new File(ideaRunConfigurationsDirectory_, "Run Tests.xml");
        FileUtils.writeString(run_tests_template.getContent(), run_tests_file);
    }

    public void executeDownloadDependencies() {
        for (var dependency : project_.dependencies().get(Scope.compile)) {
            new DependencyResolver(project_.repositories(), dependency)
                .downloadTransitivelyIntoDirectory(project_.libCompileDirectory(), Scope.compile);
        }
        for (var dependency : project_.dependencies().get(Scope.test)) {
            new DependencyResolver(project_.repositories(), dependency)
                .downloadTransitivelyIntoDirectory(project_.libTestDirectory(), Scope.compile, Scope.runtime);
        }
        for (var dependency : project_.dependencies().get(Scope.standalone)) {
            new DependencyResolver(project_.repositories(), dependency)
                .downloadTransitivelyIntoDirectory(project_.libStandaloneDirectory(), Scope.compile, Scope.runtime);
        }
    }

    public CreateOperation fromArguments(List<String> arguments) {
        if (arguments.size() < 2) {
            throw new OperationOptionException("ERROR: Expecting the package and project names as the arguments.");
        }

        return workDirectory(new File(System.getProperty("user.dir")))
            .packageName(arguments.remove(0))
            .projectName(arguments.remove(0))
            .downloadDependencies(true);
    }

    public CreateOperation workDirectory(File directory) {
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
        return this;
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

    public CreateOperation downloadDependencies(boolean flag) {
        downloadDependencies_ = flag;
        return this;
    }

    public File workDirectory() {
        return workDirectory_;
    }

    public String packageName() {
        return packageName_;
    }

    public String projectName() {
        return projectName_;
    }

    public boolean downloadDependencies() {
        return downloadDependencies_;
    }
}
