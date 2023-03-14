/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.operations.*;
import rife.tools.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Base class that executes build commands from a list of arguments.
 *
 * @see BuildCommand
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class BuildExecutor {
    private List<String> arguments_ = Collections.emptyList();
    private Map<String, Method> buildCommands_ = null;

    /**
     * Execute the build commands from the provided arguments.
     * <p>
     * While the build is executing, the arguments can be retrieved
     * using {@link #arguments()}.
     *
     * @param arguments the arguments to execute the build with
     * @since 1.5
     */
    public void processArguments(String[] arguments) {
        arguments_ = new ArrayList<>(Arrays.asList(arguments));

        var show_help = arguments_.isEmpty();

        while (!arguments_.isEmpty()) {
            var command = arguments_.remove(0);

            try {
                if (!executeCommand(command)) {
                    break;
                }
            } catch (Throwable e) {
                new HelpOperation(this, arguments()).printFullHelp();
                System.err.println();
                System.err.println(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        if (show_help) {
            help();
        }
    }

    /**
     * Retrieves the list of arguments that are being processed.
     *
     * @return the list of arguments
     * @since 1.5
     */
    public List<String> arguments() {
        return arguments_;
    }

    /**
     * Retrieves the commands that can be executed by this {@code BuildExecutor}.
     *
     * @return a map containing the name of the build command and the method that
     * corresponds to execution
     * @see BuildCommand
     * @since 1.5
     */
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

            buildCommands_ = Collections.unmodifiableMap(build_commands);
        }

        return buildCommands_;
    }

    /**
     * Performs the execution of a single command.
     *
     * @param command the name of the command to execute
     * @return {@code true} when the command was found and executed; or
     * {@code false} if the command couldn't be found
     * @throws Throwable when an exception occurred during the command execution
     * @see BuildCommand
     * @since 1.5
     */
    public boolean executeCommand(String command)
    throws Throwable {
        var method = buildCommands().get(command);
        if (method != null) {
            try {
                method.invoke(this);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } else {
            System.err.println("ERROR: unknown command '" + command + "'");
            System.err.println();
            new HelpOperation(this, arguments()).printFullHelp();
            return false;
        }
        return true;
    }

    /**
     * The standard {@code help} command.
     *
     * @since 1.5
     */
    @BuildCommand(help = HelpOperation.Help.class)
    public void help() {
        new HelpOperation(this, arguments()).execute();
    }
}
