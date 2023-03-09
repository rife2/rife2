/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.CliCommand;
import rife.tools.StringEncryptor;

import java.util.Collections;
import java.util.List;

public class EncryptCommand implements CliCommand {
    public static final String NAME = "encrypt";

    private final List<String> arguments_;

    public EncryptCommand() {
        arguments_ = Collections.emptyList();
    }
    public EncryptCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public boolean execute() {
        var array = new String[arguments_.size()];
        arguments_.toArray(array);

        StringEncryptor.main(array);
        return true;
    }

    public String getHelp() {
        return StringEncryptor.getHelp(NAME);
    }
}
