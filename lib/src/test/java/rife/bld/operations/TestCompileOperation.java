/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import org.junit.jupiter.api.Test;
import rife.bld.Project;
import rife.bld.WebProject;
import rife.tools.FileUtils;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestCompileOperation {
    @Test
    void testInstantiation() {
        var operation = new CompileOperation();
        assertNull(operation.buildMainDirectory());
        assertNull(operation.buildTestDirectory());
        assertTrue(operation.compileMainClasspath().isEmpty());
        assertTrue(operation.compileTestClasspath().isEmpty());
        assertTrue(operation.mainSourceFiles().isEmpty());
        assertTrue(operation.testSourceFiles().isEmpty());
        assertTrue(operation.compileOptions().isEmpty());
        assertTrue(operation.diagnostics().isEmpty());
    }

    @Test
    void testPopulation() {
        var buildMainDirectory = new File("buildMainDirectory");
        var buildTestDirectory = new File("buildTestDirectory");
        var compileMainClasspath1 = "compileMainClasspath1";
        var compileMainClasspath2 = "compileMainClasspath2";
        var compileTestClasspath1 = "compileTestClasspath1";
        var compileTestClasspath2 = "compileTestClasspath2";
        var mainSourceFile1 = new File("mainSourceFiles1");
        var mainSourceFile2 = new File("mainSourceFiles2");
        var testSourceFile1 = new File("testSourceFiles1");
        var testSourceFile2 = new File("testSourceFiles2");
        var compileOption1 = "compileOption1";
        var compileOption2 = "compileOption2";

        var operation1 = new CompileOperation()
            .buildMainDirectory(buildMainDirectory)
            .buildTestDirectory(buildTestDirectory)
            .compileMainClasspath(List.of(compileMainClasspath1, compileMainClasspath2))
            .compileTestClasspath(List.of(compileTestClasspath1, compileTestClasspath2))
            .mainSourceFiles(List.of(mainSourceFile1, mainSourceFile2))
            .testSourceFiles(List.of(testSourceFile1, testSourceFile2))
            .compileOptions(List.of(compileOption1, compileOption2));

        assertEquals(buildMainDirectory, operation1.buildMainDirectory());
        assertEquals(buildTestDirectory, operation1.buildTestDirectory());
        assertTrue(operation1.compileMainClasspath().contains(compileMainClasspath1));
        assertTrue(operation1.compileMainClasspath().contains(compileMainClasspath2));
        assertTrue(operation1.compileTestClasspath().contains(compileTestClasspath1));
        assertTrue(operation1.compileTestClasspath().contains(compileTestClasspath2));
        assertTrue(operation1.mainSourceFiles().contains(mainSourceFile1));
        assertTrue(operation1.mainSourceFiles().contains(mainSourceFile2));
        assertTrue(operation1.testSourceFiles().contains(testSourceFile1));
        assertTrue(operation1.testSourceFiles().contains(testSourceFile2));
        assertTrue(operation1.compileOptions().contains(compileOption1));
        assertTrue(operation1.compileOptions().contains(compileOption2));

        var operation2 = new CompileOperation()
            .buildMainDirectory(buildMainDirectory)
            .buildTestDirectory(buildTestDirectory);
        operation2.compileMainClasspath().add(compileMainClasspath1);
        operation2.compileMainClasspath().add(compileMainClasspath2);
        operation2.compileTestClasspath().add(compileTestClasspath1);
        operation2.compileTestClasspath().add(compileTestClasspath2);
        operation2.mainSourceFiles().add(mainSourceFile1);
        operation2.mainSourceFiles().add(mainSourceFile2);
        operation2.testSourceFiles().add(testSourceFile1);
        operation2.testSourceFiles().add(testSourceFile2);
        operation2.compileOptions().add(compileOption1);
        operation2.compileOptions().add(compileOption2);

        assertEquals(buildMainDirectory, operation2.buildMainDirectory());
        assertEquals(buildTestDirectory, operation2.buildTestDirectory());
        assertTrue(operation2.compileMainClasspath().contains(compileMainClasspath1));
        assertTrue(operation2.compileMainClasspath().contains(compileMainClasspath2));
        assertTrue(operation2.compileTestClasspath().contains(compileTestClasspath1));
        assertTrue(operation2.compileTestClasspath().contains(compileTestClasspath2));
        assertTrue(operation2.mainSourceFiles().contains(mainSourceFile1));
        assertTrue(operation2.mainSourceFiles().contains(mainSourceFile2));
        assertTrue(operation2.testSourceFiles().contains(testSourceFile1));
        assertTrue(operation2.testSourceFiles().contains(testSourceFile2));
        assertTrue(operation2.compileOptions().contains(compileOption1));
        assertTrue(operation2.compileOptions().contains(compileOption2));
    }

    @Test
    void testExecute()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var source_file1 = new File(tmp, "Source1.java");
            var source_file2 = new File(tmp, "Source2.java");
            var source_file3 = new File(tmp, "Source3.java");

            FileUtils.writeString("""
                public class Source1 {
                    public final String name_;
                    public Source1() {
                        name_ = "source1";
                    }
                }
                """, source_file1);

            FileUtils.writeString("""
                public class Source2 {
                    public final String name_;
                    public Source2(Source1 source1) {
                        name_ = source1.name_;
                    }
                }
                """, source_file2);

            FileUtils.writeString("""
                public class Source3 {
                    public final String name1_;
                    public final String name2_;
                    public Source3(Source1 source1, Source2 source2) {
                        name1_ = source1.name_;
                        name2_ = source2.name_;
                    }
                }
                """, source_file3);
            var build_main = new File(tmp, "buildMain");
            var build_test = new File(tmp, "buildTest");
            var build_main_class1 = new File(build_main, "Source1.class");
            var build_main_class2 = new File(build_main, "Source2.class");
            var build_test_class3 = new File(build_test, "Source3.class");

            assertFalse(build_main_class1.exists());
            assertFalse(build_main_class2.exists());
            assertFalse(build_test_class3.exists());

            var operation = new CompileOperation()
                .buildMainDirectory(build_main)
                .buildTestDirectory(build_test)
                .compileMainClasspath(List.of(build_main.getAbsolutePath()))
                .compileTestClasspath(List.of(build_main.getAbsolutePath(), build_test.getAbsolutePath()))
                .mainSourceFiles(List.of(source_file1, source_file2))
                .testSourceFiles(List.of(source_file3));
            operation.execute();
            assertTrue(operation.diagnostics().isEmpty());

            assertTrue(build_main_class1.exists());
            assertTrue(build_main_class2.exists());
            assertTrue(build_test_class3.exists());
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
                .packageName("tst")
                .projectName("app")
                .downloadDependencies(true);
            create_operation.execute();

            var compile_operation = new CompileOperation()
                .fromProject(create_operation.project());

            var main_app_class = new File(new File(compile_operation.buildMainDirectory(), "tst"), "App.class");
            var test_app_class = new File(new File(compile_operation.buildTestDirectory(), "tst"), "AppTest.class");
            assertFalse(main_app_class.exists());
            assertFalse(test_app_class.exists());

            compile_operation.execute();
            assertTrue(compile_operation.diagnostics().isEmpty());

            assertTrue(main_app_class.exists());
            assertTrue(test_app_class.exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testExecuteCompilationErrors()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var source_file1 = new File(tmp, "Source1.java");
            var source_file2 = new File(tmp, "Source2.java");
            var source_file3 = new File(tmp, "Source3.java");

            FileUtils.writeString("""
                public class Source1 {
                    public final String;
                    public Source1() {
                        name_ = "source1";
                    }
                }
                """, source_file1);

            FileUtils.writeString("""
                public class Source2 {
                    public final String name_;
                    public Source2(Source1B source1) {
                        noName_ = source1.name_;
                    }
                }
                """, source_file2);

            FileUtils.writeString("""
                public class Source3 {
                    public final String name1_;
                    public final String name2_;
                    public Source3(Source1 source1, Source2 source2) {
                        name_ = source1.name_;
                        name_ = source2.name_;
                    }
                }
                """, source_file3);
            var build_main = new File(tmp, "buildMain");
            var build_test = new File(tmp, "buildTest");
            var build_main_class1 = new File(build_main, "Source1.class");
            var build_main_class2 = new File(build_main, "Source2.class");
            var build_test_class3 = new File(build_test, "Source3.class");

            assertFalse(build_main_class1.exists());
            assertFalse(build_main_class2.exists());
            assertFalse(build_test_class3.exists());

            var operation = new CompileOperation() {
                public void executeProcessDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
                    // don't output diagnostics
                }
            };
            operation.buildMainDirectory(build_main)
                .buildTestDirectory(build_test)
                .compileMainClasspath(List.of(build_main.getAbsolutePath()))
                .compileTestClasspath(List.of(build_main.getAbsolutePath(), build_test.getAbsolutePath()))
                .mainSourceFiles(List.of(source_file1, source_file2))
                .testSourceFiles(List.of(source_file3));
            operation.execute();
            assertEquals(5, operation.diagnostics().size());

            var diagnostic1 = operation.diagnostics().get(0);
            var diagnostic2 = operation.diagnostics().get(1);
            var diagnostic3 = operation.diagnostics().get(2);
            var diagnostic4 = operation.diagnostics().get(3);
            var diagnostic5 = operation.diagnostics().get(4);

            assertEquals("/Source1.java", diagnostic1.getSource().toUri().getPath().substring(tmp.getAbsolutePath().length()));
            assertEquals("/Source3.java", diagnostic2.getSource().toUri().getPath().substring(tmp.getAbsolutePath().length()));
            assertEquals("/Source3.java", diagnostic3.getSource().toUri().getPath().substring(tmp.getAbsolutePath().length()));
            assertEquals("/Source3.java", diagnostic4.getSource().toUri().getPath().substring(tmp.getAbsolutePath().length()));
            assertEquals("/Source3.java", diagnostic5.getSource().toUri().getPath().substring(tmp.getAbsolutePath().length()));

            assertFalse(build_main_class1.exists());
            assertFalse(build_main_class2.exists());
            assertFalse(build_test_class3.exists());
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
