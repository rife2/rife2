/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

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

    public TestOperation() {
    }

    public void execute()
    throws Exception {
        startProcess().waitFor();
    }

    public List<String> processCommandList() {
        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(testJavaOptions());
        args.add("-cp");
        args.add(Project.joinPaths(testClasspath()));
        args.add(testToolMainClass());
        args.addAll(testToolOptions());
        return args;
    }

    public Process startProcess()
    throws Exception {
        var builder = new ProcessBuilder(processCommandList());
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder.start();
    }

    public TestOperation fromProject(Project project) {
        return javaTool(project.javaTool())
            .testJavaOptions(project.testJavaOptions())
            .testClasspath(project.testClasspath())
            .testToolMainClass(project.testToolMainClass())
            .testToolOptions(project.testToolOptions());
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
}
