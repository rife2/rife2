/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import com.reposilite.ReposiliteLauncherKt;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import rife.bld.WebProject;
import rife.bld.dependencies.*;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
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
            Thread.sleep(2000);

            assertThrows(FileUtilsErrorException.class, () -> FileUtils.readString(new URL("http://localhost:8081/api/maven/details/releases/test/pkg/myapp")));
            assertThrows(FileUtilsErrorException.class, () -> FileUtils.readString(new URL("http://localhost:8081/api/maven/details/releases/test/pkg/myapp/0.0.1")));

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

            var dir_json = new JSONObject(FileUtils.readString(new URL("http://localhost:8081/api/maven/details/releases/test/pkg/myapp")));
            assertEquals("myapp", dir_json.get("name"));
            var dir_files_json = dir_json.getJSONArray("files");
            assertEquals(6, dir_files_json.length());
            assertEquals("0.0.1", dir_files_json.getJSONObject(0).get("name"));
            assertEquals("maven-metadata.xml.md5", dir_files_json.getJSONObject(1).get("name"));
            assertEquals("maven-metadata.xml.sha1", dir_files_json.getJSONObject(2).get("name"));
            assertEquals("maven-metadata.xml.sha256", dir_files_json.getJSONObject(3).get("name"));
            assertEquals("maven-metadata.xml.sha512", dir_files_json.getJSONObject(4).get("name"));
            assertEquals("maven-metadata.xml", dir_files_json.getJSONObject(5).get("name"));

            var version_json = new JSONObject(FileUtils.readString(new URL("http://localhost:8081/api/maven/details/releases/test/pkg/myapp/0.0.1")));
            assertEquals("0.0.1", version_json.get("name"));
            var version_files_json = version_json.getJSONArray("files");
            assertEquals(10, version_files_json.length());
            assertEquals("myapp-0.0.1.jar.md5", version_files_json.getJSONObject(0).get("name"));
            assertEquals("myapp-0.0.1.jar.sha1", version_files_json.getJSONObject(1).get("name"));
            assertEquals("myapp-0.0.1.jar.sha256", version_files_json.getJSONObject(2).get("name"));
            assertEquals("myapp-0.0.1.jar.sha512", version_files_json.getJSONObject(3).get("name"));
            assertEquals("myapp-0.0.1.jar", version_files_json.getJSONObject(4).get("name"));
            assertEquals("myapp-0.0.1.pom.md5", version_files_json.getJSONObject(5).get("name"));
            assertEquals("myapp-0.0.1.pom.sha1", version_files_json.getJSONObject(6).get("name"));
            assertEquals("myapp-0.0.1.pom.sha256", version_files_json.getJSONObject(7).get("name"));
            assertEquals("myapp-0.0.1.pom.sha512", version_files_json.getJSONObject(8).get("name"));
            assertEquals("myapp-0.0.1.pom", version_files_json.getJSONObject(9).get("name"));

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
