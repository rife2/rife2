/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.template.TemplateDeployer;

import java.util.*;

public class PrecompileCommand implements BuildHelp {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Compiles RIFE2 templates to class files";
        }

        public String getHelp(String topic) {
            return TemplateDeployer.getHelp(topic);
        }
    }

    private final List<String> arguments_;

    public PrecompileCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public void execute() {
        var array = new String[arguments_.size()];
        arguments_.toArray(array);

        TemplateDeployer.main(array);
    }
}
