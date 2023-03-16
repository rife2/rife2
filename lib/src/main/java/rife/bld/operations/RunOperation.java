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

public class RunOperation {
    private File workDirectory_ = new File(System.getProperty("user.dir"));
    private String javaTool_;
    private final List<String> runJavaOptions_ = new ArrayList<>();
    private final List<String> runClasspath_ = new ArrayList<>();
    private String mainClass_;
    private Consumer<String> runOutputConsumer_;
    private Consumer<String> runErrorConsumer_;
    private Process process_;

    public void execute()
    throws Exception {
        process_ = executeStartProcess();
        process_.waitFor();
        executeHandleProcessOutput(
            FileUtils.readString(process_.getInputStream()),
            FileUtils.readString(process_.getErrorStream()));
    }

    public List<String> executeConstructProcessCommandList() {
        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(runJavaOptions());
        args.add("-cp");
        args.add(Project.joinPaths(runClasspath()));
        args.add(mainClass());
        return args;
    }

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

    public void executeHandleProcessOutput(String output, String error) {
        if (runOutputConsumer() != null) {
            runOutputConsumer().accept(output);
        }
        if (runErrorConsumer() != null) {
            runErrorConsumer().accept(error);
        }
    }

    public RunOperation fromProject(Project project) {
        return workDirectory(project.workDirectory())
            .javaTool(project.javaTool())
            .runJavaOptions(project.runJavaOptions())
            .runClasspath(project.runClasspath())
            .mainClass(project.mainClass());
    }

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

    public RunOperation javaTool(String tool) {
        javaTool_ = tool;
        return this;
    }

    public RunOperation runJavaOptions(List<String> options) {
        runJavaOptions_.addAll(options);
        return this;
    }

    public RunOperation runClasspath(List<String> classpath) {
        runClasspath_.addAll(classpath);
        return this;
    }

    public RunOperation mainClass(String klass) {
        mainClass_ = klass;
        return this;
    }

    public RunOperation runOutputConsumer(Consumer<String> consumer) {
        runOutputConsumer_ = consumer;
        return this;
    }

    public RunOperation runErrorConsumer(Consumer<String> consumer) {
        runErrorConsumer_ = consumer;
        return this;
    }

    public File workDirectory() {
        return workDirectory_;
    }

    public String javaTool() {
        return javaTool_;
    }

    public List<String> runJavaOptions() {
        return runJavaOptions_;
    }

    public List<String> runClasspath() {
        return runClasspath_;
    }

    public String mainClass() {
        return mainClass_;
    }

    public Process process() {
        return process_;
    }

    public Consumer<String> runOutputConsumer() {
        return runOutputConsumer_;
    }

    public Consumer<String> runErrorConsumer() {
        return runErrorConsumer_;
    }
}
