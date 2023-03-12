/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.operations.*;

public class Cli extends BuildExecutor {
    @BuildCommand(help = CreateOperation.Help.class)
    public void create()
    throws Exception {
        new CreateOperation().fromArguments(arguments()).execute();
    }

    public static void main(String[] arguments)
    throws Exception {
        new Cli().processArguments(arguments);
    }
}
