/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import org.junit.jupiter.api.Test;
import rife.bld.WebProject;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestCleanOperation {
    @Test
    void testInstantiation() {
        var operation = new CleanOperation();
        assertTrue(operation.directories().isEmpty());
    }

    @Test
    void testExecute()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
            var dir1 = new File(tmp, "dir1");
            var dir2 = new File(tmp, "dir2");
            var dir3 = new File(tmp, "dir3");
            dir1.mkdirs();
            dir2.mkdirs();
            dir3.mkdirs();

            var file1 = new File(dir1, "file1");
            var file2 = new File(dir2, "file2");
            var file3 = new File(dir3, "file3");
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();

            FileUtils.writeString("content1", file1);
            FileUtils.writeString("content2", file2);
            FileUtils.writeString("content3", file3);

            assertEquals(1, dir1.list().length);
            assertEquals(1, dir2.list().length);
            assertEquals(1, dir3.list().length);

            new CleanOperation().directories(List.of(dir1, dir2)).execute();

            assertFalse(dir1.exists());
            assertFalse(dir2.exists());
            assertTrue(dir3.exists());

            assertEquals(1, dir3.list().length);
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
            project.createBuildStructure();
            assertEquals("""
                    /build
                    /build/dist
                    /build/main
                    /build/project
                    /build/test
                    /lib
                    /lib/compile
                    /lib/project
                    /lib/runtime
                    /lib/standalone
                    /lib/test
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

            new CleanOperation().fromProject(project).execute();
            assertEquals("""
                    /build
                    /lib
                    /lib/compile
                    /lib/project
                    /lib/runtime
                    /lib/standalone
                    /lib/test
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
