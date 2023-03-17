/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.Project;
import rife.bld.operations.exceptions.OperationOptionException;
import rife.tools.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs a Java application.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class RunOperation {
    public static final String DEFAULT_JAVA_TOOL = "java";

    private File workDirectory_ = new File(System.getProperty("user.dir"));
    private String javaTool_ = DEFAULT_JAVA_TOOL;
    private final List<String> runJavaOptions_ = new ArrayList<>();
    private final List<String> runClasspath_ = new ArrayList<>();
    private String mainClass_;
    private Consumer<String> runOutputConsumer_;
    private Consumer<String> runErrorConsumer_;
    private Process process_;

    /**
     * Performs the run operation.
     *
     * @throws Exception when an error occurred during the run operation
     * @since 1.5
     */
    public void execute()
    throws Exception {
        process_ = executeStartProcess();
        process_.waitFor();
        executeHandleProcessOutput(
            FileUtils.readString(process_.getInputStream()),
            FileUtils.readString(process_.getErrorStream()));
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     *
     * @since 1.5
     */
    public List<String> executeConstructProcessCommandList() {
        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(runJavaOptions());
        args.add("-cp");
        args.add(Project.joinPaths(runClasspath()));
        args.add(mainClass());
        return args;
    }

    /**
     * Part of the {@link #execute} operation, starts the process.
     *
     * @since 1.5
     */
    public Process executeStartProcess()
    throws Exception {
        var builder = new ProcessBuilder(executeConstructProcessCommandList());
        builder.directory(workDirectory());
        if (runOutputConsumer() == null) {
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        } else {
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        }
        if (runErrorConsumer() == null) {
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        } else {
            builder.redirectError(ProcessBuilder.Redirect.PIPE);
        }
        return builder.start();
    }

    /**
     * Part of the {@link #execute} operation, handles providing the
     * output and error data to the configured consumers.
     *
     * @since 1.5
     */
    public void executeHandleProcessOutput(String output, String error) {
        if (runOutputConsumer() != null) {
            runOutputConsumer().accept(output);
        }
        if (runErrorConsumer() != null) {
            runErrorConsumer().accept(error);
        }
    }

    /**
     * Configures a run operation from a {@link Project}.
     *
     * @param project the project to configure the run operation from
     * @since 1.5
     */
    public RunOperation fromProject(Project project) {
        return workDirectory(project.workDirectory())
            .javaTool(project.javaTool())
            .runJavaOptions(project.runJavaOptions())
            .runClasspath(project.runClasspath())
            .mainClass(project.mainClass());
    }

    /**
     * Provides the work directory in which the run operation will be performed.
     * <p>
     * If no work directory is provided, the JVM working directory will be used.
     *
     * @param directory the directory to use as a work directory
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation workDirectory(File directory) {
        if (!directory.exists()) {
            throw new OperationOptionException("ERROR: The work directory '" + directory + "' doesn't exist.");
        }
        if (!directory.isDirectory()) {
            throw new OperationOptionException("ERROR: '" + directory + "' is not a directory.");
        }
        if (!directory.canWrite()) {
            throw new OperationOptionException("ERROR: The work directory '" + directory + "' is not writable.");
        }

        workDirectory_ = directory;
        return this;
    }

    /**
     * Provides the name of the tool to use for {@code java} execution.
     * <p>
     * If no java tool is provided {@code java} will be used.
     *
     * @param tool the name of the java tool
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation javaTool(String tool) {
        javaTool_ = tool;
        return this;
    }

    /**
     * Provides the options to provide to the java tool.
     *
     * @param options the java tool's options
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation runJavaOptions(List<String> options) {
        runJavaOptions_.addAll(options);
        return this;
    }

    /**
     * Provides the classpath to use for the run operation.
     *
     * @param classpath the run operation's classpath
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation runClasspath(List<String> classpath) {
        runClasspath_.addAll(classpath);
        return this;
    }

    /**
     * Provides the main class to run with the java tool.
     *
     * @param klass the main class to run
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation mainClass(String klass) {
        mainClass_ = klass;
        return this;
    }

    /**
     * Provides the consumer that will be used to capture the process output.
     *
     * @param consumer the output consumer
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation runOutputConsumer(Consumer<String> consumer) {
        runOutputConsumer_ = consumer;
        return this;
    }

    /**
     * Provides the consumer that will be used to capture the process errors.
     *
     * @param consumer the error consumer
     * @return this operation instance
     * @since 1.5
     */
    public RunOperation runErrorConsumer(Consumer<String> consumer) {
        runErrorConsumer_ = consumer;
        return this;
    }

    /**
     * Retrieves the work directory in which the run operation will be performed.
     *
     * @return the directory to use as a work directory
     * @since 1.5
     */
    public File workDirectory() {
        return workDirectory_;
    }

    /**
     * retrieves the name of the tool to use for {@code java} execution.
     *
     * @return the name of the java tool
     * @since 1.5
     */
    public String javaTool() {
        return javaTool_;
    }

    /**
     * Retrieves the options to provide to the java tool.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the java tool's options
     * @since 1.5
     */
    public List<String> runJavaOptions() {
        return runJavaOptions_;
    }

    /**
     * Retrieves the classpath to use for the run operation.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the run operation's classpath
     * @since 1.5
     */
    public List<String> runClasspath() {
        return runClasspath_;
    }

    /**
     * Retrieves the main class to run with the java tool.
     *
     * @return the main class to run
     * @since 1.5
     */
    public String mainClass() {
        return mainClass_;
    }

    /**
     * Retrieves the consumer that will be used to capture the process output.
     *
     * @return the output consumer
     * @since 1.5
     */
    public Consumer<String> runOutputConsumer() {
        return runOutputConsumer_;
    }

    /**
     * Retrieves the consumer that will be used to capture the process errors.
     *
     * @return the error consumer
     * @since 1.5
     */
    public Consumer<String> runErrorConsumer() {
        return runErrorConsumer_;
    }

    /**
     * Retrieves the process that was used for the execution.
     *
     * @return the process that was executed
     * @since 1.5
     */
    public Process process() {
        return process_;
    }
}
