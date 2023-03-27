/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import com.reposilite.ReposiliteLauncherKt;
import org.junit.jupiter.api.Test;
import rife.bld.WebProject;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestPublishOperation {
    @Test
    void testInstantiation() {
        var operation = new PublishOperation();
        assertNull(operation.repository());
        assertTrue(operation.dependencies().isEmpty());
        assertNotNull(operation.info());
        assertNull(operation.info().groupId());
        assertNull(operation.info().artifactId());
        assertNull(operation.info().version());
        assertNull(operation.info().name());
        assertNull(operation.info().description());
        assertNull(operation.info().url());
        assertTrue(operation.info().licenses().isEmpty());
        assertTrue(operation.info().developers().isEmpty());
        assertNull(operation.info().scm());
        assertTrue(operation.artifacts().isEmpty());
    }

    @Test
    void testPopulation() {
        var repository = new Repository("repository1");

        var operation1 = new PublishOperation()
            .repository(repository);
        assertEquals(repository, operation1.repository());
    }

    @Test
    void testExecution()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        var tmp2 = Files.createTempDirectory("test").toFile();
        try {
            var repository = ReposiliteLauncherKt.createWithParameters(
                "-wd", tmp2.getAbsolutePath(),
                "-p", "8081",
                "--token", "manager:passwd");
            repository.launch();

            // wait for full startup
            Thread.sleep(4000);

            var create_operation = new CreateBlankOperation()
                .workDirectory(tmp)
                .packageName("test.pkg")
                .projectName("myapp")
                .downloadDependencies(true);
            create_operation.execute();

            new CompileOperation()
                .fromProject(create_operation.project())
                .execute();

            var jar_operation = new JarOperation()
                .fromProject(create_operation.project());
            jar_operation.execute();

            var operation = new PublishOperation()
                .fromProject(create_operation.project())
                .repository(new Repository("http://localhost:8081/releases", "manager", "passwd"));
            operation.execute();

            repository.shutdown();
        } finally {
            FileUtils.deleteDirectory(tmp2);
            FileUtils.deleteDirectory(tmp);
        }
    }

    @Test
    void testFromProject()
    throws Exception {
        var tmp = Files.createTempDirectory("test").toFile();
        try {
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }
}
