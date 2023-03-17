/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import org.junit.jupiter.api.Test;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class TestRunOperation {
    @Test
    void testInstantiation() {
        var operation = new RunOperation();
        assertNotNull(operation.workDirectory());
        assertTrue(operation.workDirectory().exists());
        assertTrue(operation.workDirectory().isDirectory());
        assertTrue(operation.workDirectory().canWrite());
        assertEquals("java", operation.javaTool());
        assertTrue(operation.runJavaOptions().isEmpty());
        assertTrue(operation.runClasspath().isEmpty());
        assertNull(operation.mainClass());
        assertNull(operation.runOutputConsumer());
        assertNull(operation.runErrorConsumer());
        assertNull(operation.process());
    }

    @Test
    void testPopulation()
    throws Exception {
        var work_directory = Files.createTempDirectory("test").toFile();
        try {
            var java_tool = "javatool";
            var run_java_option1 = "runJavaOption1";
            var run_java_option2 = "runJavaOption2";
            var run_classpath1 = "runClasspath1";
            var run_classpath2 = "runClasspath2";
            var main_class = "mainClass";
            Consumer<String> run_output_consumer = (String) -> {
            };
            Consumer<String> run_error_consumer = (String) -> {
            };

            var operation1 = new RunOperation();
            operation1
                .workDirectory(work_directory)
                .javaTool(java_tool)
                .runJavaOptions(List.of(run_java_option1, run_java_option2))
                .runClasspath(List.of(run_classpath1, run_classpath2))
                .mainClass(main_class)
                .runOutputConsumer(run_output_consumer)
                .runErrorConsumer(run_error_consumer);

            assertEquals(work_directory, operation1.workDirectory());
            assertEquals(java_tool, operation1.javaTool());
            assertTrue(operation1.runJavaOptions().contains(run_java_option1));
            assertTrue(operation1.runJavaOptions().contains(run_java_option2));
            assertTrue(operation1.runClasspath().contains(run_classpath1));
            assertTrue(operation1.runClasspath().contains(run_classpath2));
            assertEquals(main_class, operation1.mainClass());
            assertSame(run_output_consumer, operation1.runOutputConsumer());
            assertSame(run_error_consumer, operation1.runErrorConsumer());

            var operation2 = new RunOperation();
            operation2.workDirectory(work_directory);
            operation2.javaTool(java_tool);
            operation2.runJavaOptions().add(run_java_option1);
            operation2.runJavaOptions().add(run_java_option2);
            operation2.runClasspath().add(run_classpath1);
            operation2.runClasspath().add(run_classpath2);
            operation2.mainClass(main_class);
            operation2.runOutputConsumer(run_output_consumer);
            operation2.runErrorConsumer(run_error_consumer);

            assertEquals(work_directory, operation2.workDirectory());
            assertEquals(java_tool, operation2.javaTool());
            assertTrue(operation2.runJavaOptions().contains(run_java_option1));
            assertTrue(operation2.runJavaOptions().contains(run_java_option2));
            assertTrue(operation2.runClasspath().contains(run_classpath1));
            assertTrue(operation2.runClasspath().contains(run_classpath2));
            assertEquals(main_class, operation2.mainClass());
            assertSame(run_output_consumer, operation2.runOutputConsumer());
            assertSame(run_error_consumer, operation2.runErrorConsumer());
        } finally {
            FileUtils.deleteDirectory(work_directory);
        }
    }

    @Test
    void testExecute()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var source_file1 = new File(tmp, "Source1.java");

            FileUtils.writeString("""
                public class Source1 {
                    public final String name_;
                    public Source1() {
                        name_ = "source1";
                    }
                    
                    public static void main(String[] arguments)
                    throws Exception {
                        System.out.print(new Source1().name_);
                    }
                }
                """, source_file1);
            var build_main = new File(tmp, "buildMain");

            var compile_operation = new CompileOperation()
                .buildMainDirectory(build_main)
                .compileMainClasspath(List.of(build_main.getAbsolutePath()))
                .mainSourceFiles(List.of(source_file1));
            compile_operation.execute();
            assertTrue(compile_operation.diagnostics().isEmpty());

            var output = new StringBuilder();
            var run_operation = new RunOperation()
                .mainClass("Source1")
                .runClasspath(List.of(build_main.getAbsolutePath()))
                .runOutputConsumer(output::append);
            run_operation.execute();

            assertEquals("source1", output.toString());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testFromProject()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var create_operation = new CreateBlankOperation()
                .workDirectory(tmp)
                .packageName("com.example")
                .projectName("myapp")
                .downloadDependencies(true);
            create_operation.execute();

            new CompileOperation()
                .fromProject(create_operation.project()).execute();

            var check_result = new StringBuilder();
            new RunOperation()
                .fromProject(create_operation.project())
                .runOutputConsumer(check_result::append)
                .execute();
            assertEquals("""
                Hello World!
                """, check_result.toString());

        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
