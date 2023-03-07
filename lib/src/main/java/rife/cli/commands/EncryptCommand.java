/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.commands;

import rife.cli.CliCommand;
import rife.tools.StringEncryptor;

import java.util.List;

public class EncryptCommand implements CliCommand {
    public static final String NAME = "encrypt";

    private final List<String> arguments_;

    public EncryptCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public void execute() {
        var array = new String[arguments_.size()];
        arguments_.toArray(array);

        StringEncryptor.main(array);
    }

    public String getHelp() {
        return StringEncryptor.getHelp(NAME);
    }
}
