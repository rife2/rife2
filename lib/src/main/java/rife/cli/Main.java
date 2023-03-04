/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli;

import rife.Version;
import rife.template.TemplateDeployer;
import rife.tools.StringEncryptor;

import java.util.*;

public class Main {
    public static void main(String[] arguments) {
        var args = new ArrayList<>(Arrays.asList(arguments));
        var command = "help";
        if (arguments.length > 0) {
            command = args.remove(0);
        }
        switch (command) {
            default -> showHelp(args);
            case "encrypt" -> {
                var array = new String[args.size()];
                args.toArray(array);
                StringEncryptor.main(array);
            }
            case "precompile" -> {
                var array = new String[args.size()];
                args.toArray(array);
                TemplateDeployer.main(array);
            }
        }
    }

    public static String getHelp() {
        return """
            The following commands are supported:
              help        Provides help about any of the other commands
              encrypt     Encrypts strings for usage with RIFE2
              precompile  Compiles RIFE2 templates to class files""";
    }

    public static void showHelp(List<String> args) {
        String topic = "";
        if (!args.isEmpty()) {
            topic = args.remove(0);
        }

        System.err.println("Welcome to the RIFE2 v" + Version.getVersion() + " CLI.");
        System.err.println();

        switch (topic) {
            case "encrypt" -> System.err.println(StringEncryptor.getHelp());
            case "precompile" -> System.err.println(TemplateDeployer.getHelp());
            default -> System.err.println(getHelp());
        }

    }
}
