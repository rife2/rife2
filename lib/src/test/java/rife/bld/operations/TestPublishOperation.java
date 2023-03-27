/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

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
        try {
            var operation = new PublishOperation();
            operation.execute();
        } finally {
            FileUtils.deleteDirectory(tmp);
        }
    }

    static class TestProject extends WebProject {
        public TestProject(File tmp) {
            workDirectory = tmp;
            pkg = "test.pkg";
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
