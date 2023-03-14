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

public class RunOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Runs the project";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Runs the project.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private String javaTool_;
    private List<String> runJavaOptions_ = new ArrayList<>();
    private List<String> runClasspath_ = new ArrayList<>();
    private String mainClass_;

    public void execute()
    throws Exception {
        executeStartProcess().waitFor();
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
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder.start();
    }

    public RunOperation fromProject(Project project) {
        return javaTool(project.javaTool())
            .runJavaOptions(project.runJavaOptions())
            .runClasspath(project.runClasspath())
            .mainClass(project.mainClass());
    }

    public RunOperation javaTool(String tool) {
        javaTool_ = tool;
        return this;
    }

    public RunOperation runJavaOptions(List<String> options) {
        runJavaOptions_ = new ArrayList<>(options);
        return this;
    }

    public RunOperation runClasspath(List<String> classpath) {
        runClasspath_ = new ArrayList<>(classpath);
        return this;
    }

    public RunOperation mainClass(String klass) {
        mainClass_ = klass;
        return this;
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
}
