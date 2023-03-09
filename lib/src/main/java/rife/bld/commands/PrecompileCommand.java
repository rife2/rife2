/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.CliCommand;
import rife.template.TemplateDeployer;

import java.util.*;

public class PrecompileCommand implements CliCommand {
    public static final String NAME = "precompile";

    private final List<String> arguments_;

    public PrecompileCommand() {
        arguments_ = Collections.emptyList();
    }

    public PrecompileCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public boolean execute() {
        var array = new String[arguments_.size()];
        arguments_.toArray(array);

        TemplateDeployer.main(array);
        return true;
    }

    public String getHelp() {
        return TemplateDeployer.getHelp(NAME);
    }
}
