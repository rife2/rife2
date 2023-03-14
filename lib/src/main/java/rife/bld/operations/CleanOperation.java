/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.FileUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.util.*;

public class CleanOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Cleans the RIFE2 build files";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Cleans the RIFE2 build files.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private List<File> directories_ = new ArrayList<>();

    public CleanOperation() {
    }

    public void execute() {
        for (var directory : directories()) {
            executeCleanDirectory(directory);
        }
    }

    public void executeCleanDirectory(File directory) {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    public CleanOperation fromProject(Project project) {
        return directories(List.of(
            project.buildDistDirectory(),
            project.buildMainDirectory(),
            project.buildProjectDirectory(),
            project.buildTestDirectory()));
    }

    public CleanOperation directories(List<File> directories) {
        directories_ = new ArrayList<>(directories);
        return this;
    }

    public List<File> directories() {
        return directories_;
    }
}