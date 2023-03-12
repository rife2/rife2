/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.commands.*;
import rife.tools.ExceptionUtils;

import java.lang.reflect.Method;
import java.util.*;

public class BuildExecutor {
    private List<String> arguments_ = Collections.emptyList();
    private Map<String, Method> buildCommands_ = null;

    public List<String> arguments() {
        return arguments_;
    }

    public Map<String, Method> buildCommands() {
        if (buildCommands_ == null) {
            var build_commands = new TreeMap<String, Method>();

            Class<?> klass = getClass();
            while (klass != null) {
                for (var method : klass.getDeclaredMethods()) {
                    if (method.getParameters().length == 0 && method.isAnnotationPresent(BuildCommand.class)) {
                        method.setAccessible(true);

                        var name = method.getName();

                        var annotation_name = method.getAnnotation(BuildCommand.class).value();
                        if (annotation_name != null && !annotation_name.isEmpty()) {
                            name = annotation_name;
                        }

                        build_commands.put(name, method);
                    }
                }

                klass = klass.getSuperclass();
            }

            buildCommands_ = build_commands;
        }

        return buildCommands_;
    }

    public void processArguments(String[] arguments) {
        arguments_ = new ArrayList<>(Arrays.asList(arguments));

        var show_help = arguments_.isEmpty();

        while (!arguments_.isEmpty()) {
            var command = arguments_.remove(0);

            try {
                if (executeCommand(command)) {
                    break;
                }
            } catch (Exception e) {
                show_help = true;
                System.err.println(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        if (show_help) {
            help();
        }
    }

    public boolean executeCommand(String command)
    throws Exception {
        var method = buildCommands().get(command);
        if (method != null) {
            method.invoke(this);
        } else {
            System.err.println("ERROR: unknown command '" + command + "'");
            System.out.println();
            new HelpCommand(this, arguments_).printFullHelp();
            return true;
        }
        return false;
    }

    @BuildCommand(help = HelpCommand.Help.class)
    public void help() {
        new HelpCommand(this, arguments_).execute();
    }
}
