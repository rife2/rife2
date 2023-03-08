/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.commands;

import rife.cli.CliCommand;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.validation.ValidityChecks;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class NewCommand implements CliCommand {
    public static final String NAME = "new";

    private final List<String> arguments_;

    public NewCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public boolean execute()
    throws Exception {
        if (arguments_.size() != 2) {
            System.err.println("ERROR: Expecting the package and project names as the arguments.");
            System.err.println();
            return false;
        }

        var package_name = StringUtils.trim(arguments_.remove(0));
        if (package_name.isEmpty()) {
            System.err.println("ERROR: The package name should not be blank.");
            System.err.println();
            return false;
        }

        var project_name = StringUtils.trim(arguments_.remove(0));
        if (project_name.isEmpty()) {
            System.err.println("ERROR: The project name should not be blank.");
            System.err.println();
            return false;
        }

        if (!ValidityChecks.checkJavaPackage(package_name)) {
            System.err.println("ERROR: The package name is invalid.");
            System.err.println();
            return false;
        }
        if (!ValidityChecks.checkJavaIdentifier(project_name)) {
            System.err.println("ERROR: The project name is invalid.");
            System.err.println();
            return false;
        }

        // create the main project directories
        var project_dir =
            Path.of(project_name).toFile();
        var src_main_java_dir =
            Path.of(project_name, "src", "main", "java").toFile();
        var src_main_resources_templates_dir =
            Path.of(project_name, "src", "main", "resources", "templates").toFile();
        var src_main_webapp_css_dir =
            Path.of(project_name, "src", "main", "webapp", "css").toFile();
        var src_test_java_dir =
            Path.of(project_name, "src", "test", "java").toFile();
        var lib_dir =
            Path.of(project_name, "lib").toFile();
        var idea_dir =
            Path.of(project_name, ".idea").toFile();
        var idea_libraries_dir =
            Path.of(project_name, ".idea", "libraries").toFile();

        project_dir.mkdirs();
        src_main_java_dir.mkdirs();
        src_main_resources_templates_dir.mkdirs();
        src_main_webapp_css_dir.mkdirs();
        src_test_java_dir.mkdirs();
        lib_dir.mkdirs();
        idea_dir.mkdirs();
        idea_libraries_dir.mkdirs();

        // create directories for the java package
        var package_dir = package_name.replace('.', File.separatorChar);
        var java_package_dir = new File(src_main_java_dir, package_dir);
        var test_package_dir = new File(src_test_java_dir, package_dir);
        java_package_dir.mkdirs();
        test_package_dir.mkdirs();

        // project gitignore
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.project_gitignore").getContent(),
            new File(project_dir, ".gitignore"));

        // create files in directories
        var project_class_name = StringUtils.capitalize(project_name);

        // project site
        var site_template = TemplateFactory.TXT.get("cli.project_site");
        var project_site_name = project_class_name + "Site";
        site_template.setValue("package", package_name);
        site_template.setValue("projectSite", project_site_name);
        var project_site_file = new File(java_package_dir, project_site_name + ".java");
        FileUtils.writeString(site_template.getContent(), project_site_file);

        // project template
        var template_template = TemplateFactory.HTML.get("cli.project_template");
        template_template.setValue("project", project_class_name);
        var project_template_file = new File(src_main_resources_templates_dir, "hello.html");
        FileUtils.writeString(template_template.getContent(), project_template_file);

        // project css
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.project_style").getContent(),
            new File(src_main_webapp_css_dir, "style.css"));

        // project test
        var test_template = TemplateFactory.TXT.get("cli.project_test");
        var project_test_name = project_class_name + "Test";
        test_template.setValue("package", package_name);
        test_template.setValue("projectTest", project_test_name);
        test_template.setValue("projectSite", project_site_name);
        test_template.setValue("project", project_class_name);
        var project_test_file = new File(test_package_dir, project_test_name + ".java");
        FileUtils.writeString(test_template.getContent(), project_test_file);

        // IDEA project files
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.idea.app_iml").getContent(),
            new File(idea_dir, "app.iml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.idea.misc_xml").getContent(),
            new File(idea_dir, "misc.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.idea.modules_xml").getContent(),
            new File(idea_dir, "modules.xml"));
        FileUtils.writeString(
            TemplateFactory.TXT.get("cli.idea.libraries.lib_xml").getContent(),
            new File(idea_libraries_dir, "lib.xml"));

        return true;
    }

    public String getHelp() {
        return """
            Creates a new RIFE2 project.
                        
            Usage : [package] [name]
              name  The name of the project to create""";
    }
}
