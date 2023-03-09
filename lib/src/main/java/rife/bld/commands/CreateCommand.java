/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.CliCommand;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.validation.ValidityChecks;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class CreateCommand implements CliCommand {
    public static final String NAME = "create";
    
    private final String packageName_;
    private final String projectName_;
    
    public static CreateCommand from(List<String> arguments) {
        if (arguments.size() != 2) {
            throw new CommandCreationException(NAME, "ERROR: Expecting the package and project names as the arguments.");
        }
        return new CreateCommand(arguments.remove(0), arguments.remove(0));
    }

    public CreateCommand() {
        packageName_ = null;
        projectName_ = null;
    }

    public CreateCommand(String packageName, String projectName) {
        packageName_ = StringUtils.trim(packageName);
        if (packageName_.isEmpty()) {
            throw new CommandCreationException(NAME, "ERROR: The package name should not be blank.");
        }

        projectName_ = StringUtils.trim(projectName);
        if (projectName_.isEmpty()) {
            throw new CommandCreationException(NAME, "ERROR: The project name should not be blank.");
        }

        if (!ValidityChecks.checkJavaPackage(packageName_)) {
            throw new CommandCreationException(NAME, "ERROR: The package name is invalid.");
        }
        if (!ValidityChecks.checkJavaIdentifier(projectName_)) {
            throw new CommandCreationException(NAME, "ERROR: The project name is invalid.");
        }
    }

    public boolean execute()
    throws Exception {
        if (packageName_ == null || projectName_ == null) {
            return false;
        }

        // create the main project directories
        var project_dir =
            Path.of(projectName_).toFile();
        var src_main_java_dir =
            Path.of(projectName_, "src", "main", "java").toFile();
        var src_main_resources_templates_dir =
            Path.of(projectName_, "src", "main", "resources", "templates").toFile();
        var src_main_webapp_css_dir =
            Path.of(projectName_, "src", "main", "webapp", "css").toFile();
        var src_test_java_dir =
            Path.of(projectName_, "src", "test", "java").toFile();
        var lib_dir =
            Path.of(projectName_, "lib").toFile();
        var lib_compile_dir =
            Path.of(projectName_, "lib", "compile").toFile();
        var lib_standalone_dir =
            Path.of(projectName_, "lib", "standalone").toFile();
        var lib_runtime_dir =
            Path.of(projectName_, "lib", "runtime").toFile();
        var lib_test_dir =
            Path.of(projectName_, "lib", "test").toFile();
        var lib_project_dir =
            Path.of(projectName_, "lib", "project").toFile();
        var project_project_dir =
            Path.of(projectName_, "project").toFile();
        var idea_dir =
            Path.of(projectName_, ".idea").toFile();
        var idea_libraries_dir =
            Path.of(projectName_, ".idea", "libraries").toFile();
        var idea_run_configurations_dir =
            Path.of(projectName_, ".idea", "runConfigurations").toFile();

        project_dir.mkdirs();
        src_main_java_dir.mkdirs();
        src_main_resources_templates_dir.mkdirs();
        src_main_webapp_css_dir.mkdirs();
        src_test_java_dir.mkdirs();
        lib_dir.mkdirs();
        lib_compile_dir.mkdirs();
        lib_standalone_dir.mkdirs();
        lib_runtime_dir.mkdirs();
        lib_test_dir.mkdirs();
        lib_project_dir.mkdirs();
        project_project_dir.mkdirs();
        idea_dir.mkdirs();
        idea_libraries_dir.mkdirs();
        idea_run_configurations_dir.mkdirs();

        // create directories for the java package
        var package_dir = packageName_.replace('.', File.separatorChar);
        var java_package_dir = new File(src_main_java_dir, package_dir);
        var test_package_dir = new File(src_test_java_dir, package_dir);
        java_package_dir.mkdirs();
        test_package_dir.mkdirs();

        // project gitignore
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.project_gitignore").getContent(),
            new File(project_dir, ".gitignore"));

        // create files in directories
        var project_class_name = StringUtils.capitalize(projectName_);

        // project site
        var site_template = TemplateFactory.TXT.get("bld.project_site");
        var project_site_name = project_class_name + "Site";
        site_template.setValue("package", packageName_);
        site_template.setValue("projectSite", project_site_name);
        var project_site_file = new File(java_package_dir, project_site_name + ".java");
        FileUtils.writeString(site_template.getContent(), project_site_file);

        // project template
        var template_template = TemplateFactory.HTML.get("bld.project_template");
        template_template.setValue("project", project_class_name);
        var project_template_file = new File(src_main_resources_templates_dir, "hello.html");
        FileUtils.writeString(template_template.getContent(), project_template_file);

        // project css
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.project_style").getContent(),
            new File(src_main_webapp_css_dir, "style.css"));

        // project test
        var test_template = TemplateFactory.TXT.get("bld.project_test");
        var project_test_name = project_class_name + "Test";
        test_template.setValue("package", packageName_);
        test_template.setValue("projectTest", project_test_name);
        test_template.setValue("projectSite", project_site_name);
        test_template.setValue("project", project_class_name);
        var project_test_file = new File(test_package_dir, project_test_name + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // project build
        var build_template = TemplateFactory.TXT.get("bld.project_build");
        build_template.setValue("package", packageName_);
        build_template.setValue("project", project_class_name);
        var project_build_file = new File(project_project_dir, "Build.java");
        FileUtils.writeString(build_template.getContent(), project_build_file);

        // IDEA project files
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.app_iml").getContent(),
            new File(idea_dir, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.misc_xml").getContent(),
            new File(idea_dir, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.modules_xml").getContent(),
            new File(idea_dir, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.project_iml").getContent(),
            new File(idea_dir, "project.iml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.compile_xml").getContent(),
            new File(idea_libraries_dir, "compile.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.project_xml").getContent(),
            new File(idea_libraries_dir, "project.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.runtime_xml").getContent(),
            new File(idea_libraries_dir, "runtime.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.standalone_xml").getContent(),
            new File(idea_libraries_dir, "standalone.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("bld.idea.libraries.test_xml").getContent(),
            new File(idea_libraries_dir, "test.xml"));

        // IDEA run site
        var run_site_template = TemplateFactory.TXT.get("bld.idea.runConfigurations.Run_Site_xml");
        run_site_template.setValue("package", packageName_);
        run_site_template.setValue("projectSite", project_site_name);
        var run_site_file = new File(idea_run_configurations_dir, "Run Site.xml");
        FileUtils.writeString(run_site_template.getContent(), run_site_file);

        // IDEA run tests
        var run_tests_template = TemplateFactory.TXT.get("bld.idea.runConfigurations.Run_Tests_xml");
        run_tests_template.setValue("package", packageName_);
        run_tests_template.setValue("projectTest", project_test_name);
        var run_tests_file = new File(idea_run_configurations_dir, "Run Tests.xml");
        FileUtils.writeString(run_tests_template.getContent(), run_tests_file);

        return true;
    }

    public String getHelp() {
        return """
            Creates a new RIFE2 project.
                        
            Usage : [package] [name]
              name  The name of the project to create""";
    }
}
