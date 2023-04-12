/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BaseProject;
import rife.bld.Project;
import rife.tools.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests a Java application.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class TestOperation extends AbstractProcessOperation<TestOperation> {
    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process.
     *
     * @since 1.5
     */
    protected List<String> executeConstructProcessCommandList() {
        if (mainClass() == null) {
            throw new IllegalArgumentException("ERROR: Missing main class for test execution.");
        }

        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(javaOptions());
        args.add("-cp");
        args.add(FileUtils.joinPaths(classpath()));
        args.add(mainClass());
        return args;
    }

    /**
     * Configures a test operation from a {@link BaseProject}.
     *
     * @param project the project to configure the test operation from
     * @since 1.5
     */
    public TestOperation fromProject(BaseProject project) {
        var operation = workDirectory(project.workDirectory())
            .javaTool(project.javaTool())
            .classpath(project.testClasspath());
        if (project.usesRife2Agent()) {
            operation.javaOptions().javaAgent(project.getRife2AgentFile());
        }
        return operation;
    }

    /**
     * Provides options to provide to the test tool.
     *
     * @param options test tool options
     * @return this operation instance
     * @since 1.5.18
     */
    public TestOperation testToolOptions(String... options) {
        processOptions_.addAll(List.of(options));
        return this;
    }

    /**
     * Provides options to provide to the test tool.
     * <p>
     * A copy will be created to allow this list to be independently modifiable.
     *
     * @param options test tool options
     * @return this operation instance
     * @since 1.5
     */
    public TestOperation testToolOptions(List<String> options) {
        processOptions_.addAll(options);
        return this;
    }

    /**
     * Retrieves the options for the test tool.
     *
     * @return the test tool's options
     * @since 1.5
     */
    public List<String> testToolOptions() {
        return processOptions_;
    }
}
