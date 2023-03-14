/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.template.TemplateDeployer;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrecompileOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Compiles RIFE2 templates to class files";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Compiles RIFE2 templates to class files
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private List<TemplateType> precompiledTemplateTypes_ = new ArrayList<>();
    private File srcMainResourcesTemplatesDirectory_;
    private File buildTemplatesDirectory_;

    public PrecompileOperation() {
    }

    public void execute() {
        executeCreateTemplateDeployer().execute();
    }

    public List<TemplateFactory> executeGetTemplateFactories() {
        var template_factories = new ArrayList<TemplateFactory>();
        for (var type : precompiledTemplateTypes()) {
            var factory = TemplateFactory.getFactory(type.identifier());
            if (factory == null) {
                System.err.println("ERROR: unknown template type '" + type.identifier() + "'/");
            } else {
                template_factories.add(factory);
            }
        }

        return template_factories;
    }

    public TemplateDeployer executeCreateTemplateDeployer() {
        return new TemplateDeployer()
            .verbose(true)
            .directoryPaths(List.of(srcMainResourcesTemplatesDirectory().getAbsolutePath()))
            .generationPath(buildTemplatesDirectory().getAbsolutePath())
            .templateFactories(executeGetTemplateFactories());
    }

    public PrecompileOperation fromProject(Project project) {
        return precompiledTemplateTypes(project.precompiledTemplateTypes())
            .srcMainResourcesTemplatesDirectory(project.srcMainResourcesTemplatesDirectory())
            .buildTemplatesDirectory(project.buildTemplatesDirectory());
    }

    public PrecompileOperation precompiledTemplateTypes(List<TemplateType> types) {
        precompiledTemplateTypes_ = new ArrayList<>(types);
        return this;
    }

    public PrecompileOperation srcMainResourcesTemplatesDirectory(File directory) {
        srcMainResourcesTemplatesDirectory_ = directory;
        return this;
    }

    public PrecompileOperation buildTemplatesDirectory(File directory) {
        buildTemplatesDirectory_ = directory;
        return this;
    }

    public List<TemplateType> precompiledTemplateTypes() {
        return precompiledTemplateTypes_;
    }

    public File srcMainResourcesTemplatesDirectory() {
        return srcMainResourcesTemplatesDirectory_;
    }

    public File buildTemplatesDirectory() {
        return buildTemplatesDirectory_;
    }
}
