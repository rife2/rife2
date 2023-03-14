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

/**
 * Cleans by deleting a list of directories and all their contents.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
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

    /**
     * Perform the clean operation.
     *
     * @since 1.5
     */
    public void execute() {
        for (var directory : directories()) {
            executeCleanDirectory(directory);
        }
    }

    /**
     * Part of the {@link #execute} operation, cleans an individual directory.
     *
     * @param directory the directory to clean.
     * @since 1.5
     */
    public void executeCleanDirectory(File directory) {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (FileUtilsErrorException e) {
            // no-op
        }
    }

    /**
     * Configures a clean operation from a {@link Project}.
     *
     * @param project the project to configure the clean operation from
     * @since 1.5
     */
    public CleanOperation fromProject(Project project) {
        return directories(List.of(
            project.buildDistDirectory(),
            project.buildMainDirectory(),
            project.buildProjectDirectory(),
            project.buildTestDirectory()));
    }

    /**
     * Provides a list of directories to clean.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param directories the directories to clean
     * @return this {@code CleanOperation} instance
     * @since 1.5
     */
    public CleanOperation directories(List<File> directories) {
        directories_ = new ArrayList<>(directories);
        return this;
    }

    /**
     * Retrieves the list of directories to clean.
     * <p>
     * This is a modifiable list that can be retrieved and changed.
     *
     * @return the list of directories to clean.
     * @since 1.5
     */
    public List<File> directories() {
        return directories_;
    }
}