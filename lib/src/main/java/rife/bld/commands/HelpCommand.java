/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.Version;
import rife.bld.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.util.Comparator.comparingInt;

public class HelpCommand {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Provides help about any of the other commands";
        }
    }

    private final BuildExecutor executor_;
    private final List<String> arguments_;

    public HelpCommand(BuildExecutor executor, List<String> arguments) {
        executor_ = executor;
        arguments_ = arguments;
    }

    public void execute() {
        var topic = "";
        if (!arguments_.isEmpty()) {
            topic = arguments_.remove(0);
        }

        System.err.println("Welcome to the RIFE2 v" + Version.getVersion() + " CLI.");
        System.err.println();

        boolean print_full_help = true;
        try {
            var commands = executor_.getBuildCommands();
            if (commands.containsKey(topic)) {
                var method = commands.get(topic);
                var annotation = method.getAnnotation(BuildCommand.class);
                var build_help = annotation.help();
                if (build_help != BuildHelp.class) {
                    var help = build_help.getDeclaredConstructor().newInstance().getHelp(topic);
                    if (!help.isEmpty()) {
                        System.err.println(help);
                        print_full_help = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (print_full_help) {
            try {
                printFullHelp();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printFullHelp()
    throws Exception {
        var commands = executor_.getBuildCommands();

        System.err.println("""
            The RIFE2 CLI provides its features through a series of commands that
            perform specific tasks.
                        
            The help command provides more information about the other commands.
            Usage : help [command]

            The following commands are supported.
            """);

//        run         Compiles and runs a RIFE2 application
//        jar         Creates an uberJar archive for a RIFE2 application
//        war         Creates a war archive for a RIFE2 application

        var command_length = commands.keySet().stream().max(comparingInt(String::length)).get().length() + 2;
        for (var command : commands.entrySet()) {
            System.err.print("  ");
            System.err.printf("%-" + command_length + "s", command.getKey());
            var method = command.getValue();
            var annotation = method.getAnnotation(BuildCommand.class);
            var build_help = annotation.help();
            if (build_help != BuildHelp.class) {
                System.err.print(build_help.getDeclaredConstructor().newInstance().getDescription());
            }
            System.err.println();
        }
    }
}