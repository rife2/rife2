/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.commands.*;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.tools.ExceptionUtils;

import java.util.*;

class Cli implements CliCommands {
    private List<String> arguments_;

    void processArguments(CliCommands commands, String[] arguments) {
        arguments_ = new ArrayList<>(Arrays.asList(arguments));

        var command = HelpCommand.NAME;
        if (!arguments_.isEmpty()) {
            command = arguments_.remove(0);
        }

        boolean success;
        try {
            switch (command) {
                case CreateCommand.NAME -> success = commands.create();
                case CompileCommand.NAME -> success = commands.compile();
                case EncryptCommand.NAME -> success = commands.encrypt();
                case PrecompileCommand.NAME -> success = commands.precompile();
                default -> success = help();
            }
        } catch (CommandCreationException e) {
            success = false;
            System.err.println(e.getMessage());
        } catch (Exception e) {
            success = false;
            System.err.println(ExceptionUtils.getExceptionStackTrace(e));
        }

        if (!success) {
            new HelpCommand(arguments_).execute();
        }
    }

    public boolean create()
    throws Exception {
        return CreateCommand.from(arguments_).execute();
    }

    public boolean download()
    throws Exception {
        return false;
    }

    public boolean compile()
    throws Exception {
        return CompileCommand.from(arguments_).execute();
    }

    public boolean clean()
    throws Exception {
        return false;
    }

    public boolean run()
    throws Exception {
        return false;
    }

    public boolean jar()
    throws Exception {
        return false;
    }

    public boolean war()
    throws Exception {
        return false;
    }

    public boolean encrypt()
    throws Exception {
        return new EncryptCommand(arguments_).execute();
    }

    public boolean precompile()
    throws Exception {
        return new PrecompileCommand(arguments_).execute();
    }

    public boolean help()
    throws Exception {
        return new HelpCommand(arguments_).execute();
    }

    public static void main(String[] arguments)
    throws Exception {
        var cli = new Cli();
        cli.processArguments(cli, arguments);
    }
}
