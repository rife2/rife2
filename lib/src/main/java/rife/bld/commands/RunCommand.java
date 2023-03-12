/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.StringUtils;

public class RunCommand {
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

    private final Project project_;

    public RunCommand(Project project) {
        project_ = project;
    }

    public void execute() {
    }
}
