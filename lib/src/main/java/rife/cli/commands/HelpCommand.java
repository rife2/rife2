/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.commands;

import rife.Version;
import rife.cli.CliCommand;
import rife.tools.StringUtils;

import java.util.List;

public class HelpCommand implements CliCommand {
    public static final String NAME = "help";

    private final List<String> arguments_;

    public HelpCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public boolean execute() {
        var topic = "";
        if (!arguments_.isEmpty()) {
            topic = arguments_.remove(0);
        }

        System.err.println("Welcome to the RIFE2 v" + Version.getVersion() + " CLI.");
        System.err.println();

        switch (topic) {
            case NewCommand.NAME -> System.err.println(new NewCommand(arguments_).getHelp());
            case BuildCommand.NAME -> System.err.println(new BuildCommand(arguments_).getHelp());
            case EncryptCommand.NAME -> System.err.println(new EncryptCommand(arguments_).getHelp());
            case PrecompileCommand.NAME -> System.err.println(new PrecompileCommand(arguments_).getHelp());
            default -> System.err.println(getHelp());
        }

        return true;
    }

    public String getHelp() {
        return StringUtils.replace("""
            The RIFE2 CLI provides its features through a series of commands that
            perform specific tasks.
            
            The help command provides more information about the other commands.
            Usage : ${commandName} [command]

            The following commands are supported.
                        
            Common:
              help        Provides help about any of the other commands
              new         Creates a new RIFE2 application
              download    Downloads the application dependencies
              build       Compiles a RIFE2 application
              clean       Cleans the RIFE2 build files
              run         Compiles and runs a RIFE2 application
              jar         Creates an uberJar archive for a RIFE2 application
              war         Creates a war archive for a RIFE2 application
                        
            Tools:
              encrypt     Encrypts strings for usage with RIFE2
              precompile  Compiles RIFE2 templates to class files""", "${commandName}", NAME);
    }
}