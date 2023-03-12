/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.commands.*;

public class Cli extends BuildExecutor {
    @BuildCommand(help = CreateCommand.Help.class)
    public void create()
    throws Exception {
        new CreateCommand(arguments_).execute();
    }

    @BuildCommand(help = EncryptCommand.Help.class)
    public void encrypt() {
        new EncryptCommand(arguments_).execute();
    }

    @BuildCommand(help = PrecompileCommand.Help.class)
    public void precompile() {
        new PrecompileCommand(arguments_).execute();
    }

    public static void main(String[] arguments)
    throws Exception {
        new Cli().processArguments(arguments);
    }
}
