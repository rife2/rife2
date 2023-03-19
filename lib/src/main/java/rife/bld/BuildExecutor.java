/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.help.HelpHelp;
import rife.bld.operations.HelpOperation;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.ioc.HierarchicalProperties;
import rife.tools.ExceptionUtils;

import java.util.*;

/**
 * Base class that executes build commands from a list of arguments.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see BuildCommand
 * @see CommandDefinition
 * @since 1.5
 */
public class BuildExecutor {
    private final HierarchicalProperties properties_;
    private List<String> arguments_ = Collections.emptyList();
    private Map<String, CommandDefinition> buildCommands_ = null;
    private int exitStatus_ = 0;

    /**
     * Creates a new build executor instance.
     * @since 1.5
     */
    public BuildExecutor() {
        properties_ = new HierarchicalProperties().parent(HierarchicalProperties.createSystemInstance());
    }

    /**
     * Returns the properties uses by this conversation.
     *
     * @return the instance of {@code HierarchicalProperties} that is used
     * by this build executor
     * @since 1.5
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Set the exist status to use at the end of the execution.
     * 
     * @param status sets the exit status
     * @since 1.5.1
     */
    public void exitStatus(int status) {
        exitStatus_ = status;
    }

    /**
     * Retrieves the exit status.
     *
     * @return the exit status
     * @since 1.5.1
     */
    public int exitStatus() {
        return exitStatus_;
    }

    /**
     * Execute the build commands from the provided arguments.
     * <p>
     * While the build is executing, the arguments can be retrieved
     * using {@link #arguments()}.
     *
     * @param arguments the arguments to execute the build with
     * @return the exist status
     * @since 1.5.1
     */
    public int execute(String[] arguments) {
        arguments_ = new ArrayList<>(Arrays.asList(arguments));

        var show_help = arguments_.isEmpty();

        while (!arguments_.isEmpty()) {
            var command = arguments_.remove(0);

            try {
                if (!executeCommand(command)) {
                    break;
                }
            } catch (Throwable e) {
                exitStatus(1);
                new HelpOperation(this, arguments()).executePrintOverviewHelp();
                System.err.println();
                System.err.println(ExceptionUtils.getExceptionStackTrace(e));
            }
        }

        if (show_help) {
            help();
        }
        
        return exitStatus_;
    }

    /**
     * Starts the execution of the build. This method will call
     * System.exit() when done with the appropriate exit status.
     * 
     * @param arguments the arguments to execute the build with
     * @see #execute 
     * @since 1.5.1
     */
    public void start(String[] arguments) {
        System.exit(execute(arguments));
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
    public Map<String, CommandDefinition> buildCommands() {
        if (buildCommands_ == null) {
            var build_commands = new TreeMap<String, CommandDefinition>();

            Class<?> klass = getClass();

            try {
                while (klass != null) {
                    for (var method : klass.getDeclaredMethods()) {
                        if (method.getParameters().length == 0 && method.isAnnotationPresent(BuildCommand.class)) {
                            method.setAccessible(true);

                            var name = method.getName();

                            var annotation = method.getAnnotation(BuildCommand.class);

                            var annotation_name = annotation.value();
                            if (annotation_name != null && !annotation_name.isEmpty()) {
                                name = annotation_name;
                            }

                            var build_help = annotation.help();
                            CommandHelp command_help = null;
                            if (build_help != null) {
                                command_help = build_help.getDeclaredConstructor().newInstance();
                            }

                            build_commands.put(name, new CommandAnnotated(this, method, command_help));
                        }
                    }

                    klass = klass.getSuperclass();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            buildCommands_ = build_commands;
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
        var definition = buildCommands().get(command);
        if (definition != null) {
            try {
                definition.execute();
            } catch (ExitStatusException e) {
                exitStatus(e.getExitStatus());
                return e.getExitStatus() == ExitStatusException.EXIT_SUCCESS;
            }
        } else {
            System.err.println("ERROR: unknown command '" + command + "'");
            System.err.println();
            new HelpOperation(this, arguments()).executePrintOverviewHelp();
            return false;
        }
        return true;
    }

    /**
     * The standard {@code help} command.
     *
     * @since 1.5
     */
    @BuildCommand(help = HelpHelp.class)
    public void help() {
        new HelpOperation(this, arguments()).execute();
    }
}
