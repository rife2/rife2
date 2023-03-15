/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import org.junit.jupiter.api.Test;
import rife.bld.*;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestDownloadOperation {
    @Test
    void testInstantiation() {
        var operation = new DownloadOperation();
        assertTrue(operation.dependencies().isEmpty());
        assertTrue(operation.repositories().isEmpty());
        assertNull(operation.libCompileDirectory());
        assertNull(operation.libRuntimeDirectory());
        assertNull(operation.libStandaloneDirectory());
        assertNull(operation.libTestDirectory());
    }

    @Test
    void testExecution()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var dir1 = new File(tmp, "dir1");
            var dir2 = new File(tmp, "dir2");
            var dir3 = new File(tmp, "dir3");
            var dir4 = new File(tmp, "dir4");
            dir1.mkdirs();
            dir2.mkdirs();
            dir3.mkdirs();
            dir4.mkdirs();

            var operation = new DownloadOperation()
                .repositories(List.of(Repository.MAVEN_CENTRAL))
                .libCompileDirectory(dir1)
                .libRuntimeDirectory(dir2)
                .libStandaloneDirectory(dir3)
                .libTestDirectory(dir4);
            operation.dependencies().scope(Scope.compile)
                .include(new Dependency("org.apache.commons", "commons-lang3", new VersionNumber(3, 12, 0)));
            operation.dependencies().scope(Scope.runtime)
                .include(new Dependency("org.apache.commons", "commons-collections4", new VersionNumber(4, 4)));
            operation.dependencies().scope(Scope.standalone)
                .include(new Dependency("org.slf4j", "slf4j-simple", new VersionNumber(2, 0, 6)));
            operation.dependencies().scope(Scope.test)
                .include(new Dependency("org.apache.httpcomponents.client5", "httpclient5", new VersionNumber(5, 2, 1)));

            operation.execute();

            assertEquals("""
                    /dir1
                    /dir1/commons-lang3-3.12.0.jar
                    /dir2
                    /dir2/commons-collections4-4.4.jar
                    /dir3
                    /dir3/slf4j-api-2.0.6.jar
                    /dir3/slf4j-simple-2.0.6.jar
                    /dir4
                    /dir4/httpclient5-5.2.1.jar
                    /dir4/httpcore5-5.2.jar
                    /dir4/httpcore5-h2-5.2.jar
                    /dir4/slf4j-api-1.7.36.jar""",
                Files.walk(Path.of(tmp.getAbsolutePath()))
                    .map(path -> path.toAbsolutePath().toString().substring(tmp.getAbsolutePath().length()))
                    .filter(s -> !s.isEmpty())
                    .sorted()
                    .collect(Collectors.joining("\n")));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testFromProject()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var project = new WebProject();
            project.workDirectory = tmp;
            project.pkg = "test.pkg";
            project.createProjectStructure();
            project.repositories().add(Repository.MAVEN_CENTRAL);
            project.dependencies().scope(Scope.compile)
                .include(new Dependency("org.apache.commons", "commons-lang3", new VersionNumber(3, 12, 0)));
            project.dependencies().scope(Scope.runtime)
                .include(new Dependency("org.apache.commons", "commons-collections4", new VersionNumber(4, 4)));
            project.dependencies().scope(Scope.standalone)
                .include(new Dependency("org.slf4j", "slf4j-simple", new VersionNumber(2, 0, 6)));
            project.dependencies().scope(Scope.test)
                .include(new Dependency("org.apache.httpcomponents.client5", "httpclient5", new VersionNumber(5, 2, 1)));

            var operation = new DownloadOperation()
                .fromProject(project);

            operation.execute();

            assertEquals("""
                    /lib
                    /lib/compile
                    /lib/compile/commons-lang3-3.12.0.jar
                    /lib/project
                    /lib/runtime
                    /lib/runtime/commons-collections4-4.4.jar
                    /lib/standalone
                    /lib/standalone/slf4j-api-2.0.6.jar
                    /lib/standalone/slf4j-simple-2.0.6.jar
                    /lib/test
                    /lib/test/httpclient5-5.2.1.jar
                    /lib/test/httpcore5-5.2.jar
                    /lib/test/httpcore5-h2-5.2.jar
                    /lib/test/slf4j-api-1.7.36.jar
                    /src
                    /src/main
                    /src/main/java
                    /src/main/resources
                    /src/main/resources/templates
                    /src/project
                    /src/project/java
                    /src/test
                    /src/test/java""",
                Files.walk(Path.of(tmp.getAbsolutePath()))
                    .map(path -> path.toAbsolutePath().toString().substring(tmp.getAbsolutePath().length()))
                    .filter(s -> !s.isEmpty())
                    .sorted()
                    .collect(Collectors.joining("\n")));
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
