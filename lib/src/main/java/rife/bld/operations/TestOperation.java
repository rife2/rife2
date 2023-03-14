/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Tests a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Tests a RIFE2 application.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private String javaTool_;
    private List<String> testJavaOptions_ = new ArrayList<>();
    private List<String> testClasspath_ = new ArrayList<>();
    private String testToolMainClass_;
    private List<String> testToolOptions_ = new ArrayList<>();
    private Consumer<String> testOutputConsumer_;
    private Consumer<String> testErrorConsumer_;

    public void execute()
    throws Exception {
        var process = executeStartProcess();
        process.waitFor();
        executeHandleProcessOutput(
            FileUtils.readString(process.getInputStream()),
            FileUtils.readString(process.getErrorStream()));
    }

    public List<String> executeConstructProcessCommandList() {
        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(testJavaOptions());
        args.add("-cp");
        args.add(Project.joinPaths(testClasspath()));
        args.add(testToolMainClass());
        args.addAll(testToolOptions());
        return args;
    }

    public Process executeStartProcess()
    throws Exception {
        var builder = new ProcessBuilder(executeConstructProcessCommandList());
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        builder.redirectError(ProcessBuilder.Redirect.PIPE);
        return builder.start();
    }

    public void executeHandleProcessOutput(String output, String error) {
        if (testOutputConsumer() != null) {
            testOutputConsumer().accept(output);
        }
        if (testErrorConsumer() != null) {
            testErrorConsumer().accept(error);
        }
    }

    public TestOperation fromProject(Project project) {
        return javaTool(project.javaTool())
            .testJavaOptions(project.testJavaOptions())
            .testClasspath(project.testClasspath())
            .testToolMainClass(project.testToolMainClass())
            .testToolOptions(project.testToolOptions())
            .testOutputConsumer(System.out::print)
            .testErrorConsumer(System.err::print);
    }

    public TestOperation javaTool(String tool) {
        javaTool_ = tool;
        return this;
    }

    public TestOperation testJavaOptions(List<String> options) {
        testJavaOptions_ = new ArrayList<>(options);
        return this;
    }

    public TestOperation testClasspath(List<String> classpath) {
        testClasspath_ = new ArrayList<>(classpath);
        return this;
    }

    public TestOperation testToolMainClass(String klass) {
        testToolMainClass_ = klass;
        return this;
    }

    public TestOperation testToolOptions(List<String> options) {
        testToolOptions_ = new ArrayList<>(options);
        return this;
    }

    public TestOperation testOutputConsumer(Consumer<String> consumer) {
        testOutputConsumer_ = consumer;
        return this;
    }

    public TestOperation testErrorConsumer(Consumer<String> consumer) {
        testErrorConsumer_ = consumer;
        return this;
    }

    public String javaTool() {
        return javaTool_;
    }

    public List<String> testJavaOptions() {
        return testJavaOptions_;
    }

    public List<String> testClasspath() {
        return testClasspath_;
    }

    public String testToolMainClass() {
        return testToolMainClass_;
    }

    public List<String> testToolOptions() {
        return testToolOptions_;
    }

    public Consumer<String> testOutputConsumer() {
        return testOutputConsumer_;
    }

    public Consumer<String> testErrorConsumer() {
        return testErrorConsumer_;
    }
}
