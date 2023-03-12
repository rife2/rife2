/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.tools.StringEncryptor;

import java.util.List;

public class EncryptCommand {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Encrypts strings for usage with RIFE2";
        }

        public String getHelp(String topic) {
            return StringEncryptor.getHelp(topic);
        }
    }

    private final List<String> arguments_;

    public EncryptCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public void execute() {
        var array = new String[arguments_.size()];
        arguments_.toArray(array);

        StringEncryptor.main(array);
    }
}
