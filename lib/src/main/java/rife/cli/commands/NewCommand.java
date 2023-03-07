/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.commands;

import rife.cli.CliCommand;
import rife.tools.StringEncryptor;

import java.util.List;

public class NewCommand implements CliCommand {
    public static final String NAME = "new";

    private final List<String> arguments_;

    public NewCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public void execute() {
    }

    public String getHelp() {
        return """
            Creates a new RIFE2 project.
            
            Usage : [name]
              name  The name of the project to create""";
    }
}
