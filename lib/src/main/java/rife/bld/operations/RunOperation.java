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
            return "Runs a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Runs a RIFE2 application.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    public final Project project;

    public RunOperation(Project project) {
        this.project = project;
    }

    public void execute()
    throws Exception {
        startProcess().waitFor();
    }

    public List<String> processCommandList() {
        var args = new ArrayList<String>();
        args.add(project.javaTool());
        args.addAll(project.runJavaOptions());
        args.add("-cp");
        args.add(Project.joinPaths(project.runClasspath()));
        args.add(project.mainClass);
        return args;
    }

    public Process startProcess()
    throws Exception {
        var builder = new ProcessBuilder(processCommandList());
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder.start();
    }
}
