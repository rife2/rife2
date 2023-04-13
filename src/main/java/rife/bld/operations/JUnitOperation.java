/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.tools.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests a Java application with JUnit.
 * <p>
 * If no JUnit options are specified, the {@link JUnitOptions#defaultOptions()}
 * are used. To tweak the default options, manually add them with this method
 * and use the other desired options.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.20
 */
public class JUnitOperation extends TestOperation<JUnitOperation, JUnitOptions> {
    public static final String DEFAULT_TEST_TOOL_JUNIT5 = "org.junit.platform.console.ConsoleLauncher";

    @Override
    protected JUnitOptions createTestToolOptions() {
        return new JUnitOptions();
    }

    /**
     * Part of the {@link #execute} operation, constructs the command list
     * to use for building the process, defaults to adding JUnit options.
     *
     * @since 1.5.20
     */
    @Override
    protected List<String> executeConstructProcessCommandList() {
        var args = new ArrayList<String>();
        args.add(javaTool());
        args.addAll(javaOptions());
        args.add("-cp");
        args.add(FileUtils.joinPaths(classpath()));

        var main_class = mainClass();
        if (main_class == null) {
            main_class = DEFAULT_TEST_TOOL_JUNIT5;
        }
        args.add(main_class);

        if (testToolOptions().isEmpty() && main_class.equals(DEFAULT_TEST_TOOL_JUNIT5)) {
            args.addAll(new JUnitOptions().defaultOptions());
        } else {
            args.addAll(testToolOptions());
        }

        return args;
    }
}
