/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli;

import rife.cli.commands.*;

import java.util.*;

public class Main {
    public static void main(String[] arguments) {
        var args = new ArrayList<>(Arrays.asList(arguments));

        var command = HelpCommand.NAME;
        if (arguments.length > 0) {
            command = args.remove(0);
        }

        switch (command) {
            case NewCommand.NAME -> new NewCommand(args).execute();
            case EncryptCommand.NAME -> new EncryptCommand(args).execute();
            case PrecompileCommand.NAME -> new PrecompileCommand(args).execute();
            default -> new HelpCommand(args).execute();
        }
    }
}
