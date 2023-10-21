/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.WebProject;

import java.io.File;

import static rife.bld.operations.TemplateType.HTML;

public class ExamplesBuild extends WebProject {
    public ExamplesBuild(Rife2Build mainBuild) {
        pkg = "rife";
        name = "Examples";
        mainClass = "rife.HelloAll";
        version = version(1,0,0);

        javaRelease = 17;

        srcDirectory = new File(workDirectory(), "examples");
        buildMainDirectory = new File(buildDirectory(), "main_examples");
        buildTestDirectory = new File(buildDirectory(), "test_examples");
        libStandaloneDirectory = libTestDirectory();

        precompileOperation()
            .templateTypes(HTML);
        compileOperation()
            .compileMainClasspath(mainBuild.buildMainDirectory().getAbsolutePath())
            .compileTestClasspath(mainBuild.buildMainDirectory().getAbsolutePath());
        runOperation()
            .classpath(mainBuild.testsBadgeOperation.classpath())
            .classpath(mainBuild.buildMainDirectory().getAbsolutePath())
            .javaOptions().javaAgent(new File(buildDistDirectory(), mainBuild.jarAgentOperation.destinationFileName()));
        testOperation()
            .classpath(mainBuild.testsBadgeOperation.classpath())
            .classpath(mainBuild.buildMainDirectory().getAbsolutePath())
            .javaOptions().javaAgent(new File(buildDistDirectory(), mainBuild.jarAgentOperation.destinationFileName()));
    }

    public void compile()
    throws Exception {
        super.compile();
        precompile();
    }
}