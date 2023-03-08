/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli;

import rife.cli.commands.*;
import rife.tools.exceptions.FileUtilsErrorException;

import java.util.*;

public class Main {
    public static void main(String[] arguments)
    throws Exception {
        var args = new ArrayList<>(Arrays.asList(arguments));

        var command = HelpCommand.NAME;
        if (arguments.length > 0) {
            command = args.remove(0);
        }

        boolean success;
        switch (command) {
            case NewCommand.NAME -> success = new NewCommand(args).execute();
            case BuildCommand.NAME -> success = new BuildCommand(args).execute();
            case EncryptCommand.NAME -> success = new EncryptCommand(args).execute();
            case PrecompileCommand.NAME -> success = new PrecompileCommand(args).execute();
            default -> success = new HelpCommand(args).execute();
        }

        if (!success) {
            new HelpCommand(args).execute();
        }
    }
}