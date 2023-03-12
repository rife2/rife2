/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.template.TemplateDeployer;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PrecompileCommand implements BuildHelp {
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

    public final Project project;

    public PrecompileCommand(Project project) {
        this.project = project;
    }

    public void execute() {
        createTemplateDeployer().execute();
    }

    public List<TemplateFactory> getTemplateFactories() {
        var template_factories = new ArrayList<TemplateFactory>();
        for (var type : project.precompiledTemplateTypes) {
            var factory = TemplateFactory.getFactory(type.identifier());
            if (factory == null) {
                System.err.println("ERROR: unknown template type '" + type.identifier() + "'");
            } else {
                template_factories.add(factory);
            }
        }

        return template_factories;
    }

    public TemplateDeployer createTemplateDeployer() {
        return new TemplateDeployer()
            .verbose(true)
            .directoryPaths(List.of(project.srcMainResourcesTemplatesDirectory().getAbsolutePath()))
            .generationPath(project.buildTemplatesDirectory().getAbsolutePath())
            .templateFactories(getTemplateFactories());
    }
}
