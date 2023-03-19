/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.help.*;
import rife.bld.operations.*;

/**
 * Implements the RIFE2 CLI build executor that is available when running
 * the RIFE2 jar as an executable jar.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class Cli extends BuildExecutor {
    /**
     * The standard {@code create} command.
     *
     * @throws Exception when an error occurred during the creation process
     * @since 1.5
     */
    @BuildCommand(help = CreateRife2Help.class)
    public void create()
    throws Exception {
        new CreateRife2Operation().fromArguments(arguments()).execute();
    }

    /**
     * The standard {@code create-blank} command.
     *
     * @throws Exception when an error occurred during the creation process
     * @since 1.5
     */
    @BuildCommand(value = "create-blank", help = CreateBlankHelp.class)
    public void createBlank()
    throws Exception {
        new CreateBlankOperation().fromArguments(arguments()).execute();
    }

    /**
     * The standard {@code upgrade} command.
     *
     * @throws Exception when an error occurred during the upgrade process
     * @since 1.5
     */
    @BuildCommand(help = UpgradeHelp.class)
    public void upgrade()
    throws Exception {
        new UpgradeOperation().fromArguments(arguments()).execute();
    }

    public static void main(String[] arguments)
    throws Exception {
        new Cli().start(arguments);
    }
}
