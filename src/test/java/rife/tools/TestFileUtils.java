/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static java.nio.file.attribute.PosixFilePermission.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rife.tools.FileUtils.permissionsFromMode;

public class TestFileUtils {
    @Test
    void testPermissionsFromMode() {
        assertTrue(permissionsFromMode(0000).isEmpty());
        assertTrue(permissionsFromMode(0001).containsAll(Set.of(OTHERS_EXECUTE)));
        assertTrue(permissionsFromMode(0002).containsAll(Set.of(OTHERS_WRITE)));
        assertTrue(permissionsFromMode(0004).containsAll(Set.of(OTHERS_READ)));
        assertTrue(permissionsFromMode(0003).containsAll(Set.of(OTHERS_EXECUTE, OTHERS_WRITE)));
        assertTrue(permissionsFromMode(0005).containsAll(Set.of(OTHERS_EXECUTE, OTHERS_READ)));
        assertTrue(permissionsFromMode(0006).containsAll(Set.of(OTHERS_WRITE, OTHERS_READ)));
        assertTrue(permissionsFromMode(0007).containsAll(Set.of(OTHERS_EXECUTE, OTHERS_WRITE, OTHERS_READ)));

        assertTrue(permissionsFromMode(0010).containsAll(Set.of(GROUP_EXECUTE)));
        assertTrue(permissionsFromMode(0020).containsAll(Set.of(GROUP_WRITE)));
        assertTrue(permissionsFromMode(0040).containsAll(Set.of(GROUP_READ)));
        assertTrue(permissionsFromMode(0030).containsAll(Set.of(GROUP_EXECUTE, GROUP_WRITE)));
        assertTrue(permissionsFromMode(0050).containsAll(Set.of(GROUP_EXECUTE, GROUP_READ)));
        assertTrue(permissionsFromMode(0060).containsAll(Set.of(GROUP_WRITE, GROUP_READ)));
        assertTrue(permissionsFromMode(0070).containsAll(Set.of(GROUP_EXECUTE, GROUP_WRITE, GROUP_READ)));

        assertTrue(permissionsFromMode(0100).containsAll(Set.of(OWNER_EXECUTE)));
        assertTrue(permissionsFromMode(0200).containsAll(Set.of(OWNER_WRITE)));
        assertTrue(permissionsFromMode(0400).containsAll(Set.of(OWNER_READ)));
        assertTrue(permissionsFromMode(0300).containsAll(Set.of(OWNER_EXECUTE, OWNER_WRITE)));
        assertTrue(permissionsFromMode(0500).containsAll(Set.of(OWNER_EXECUTE, OWNER_READ)));
        assertTrue(permissionsFromMode(0600).containsAll(Set.of(OWNER_WRITE, OWNER_READ)));
        assertTrue(permissionsFromMode(0700).containsAll(Set.of(OWNER_EXECUTE, OWNER_WRITE, OWNER_READ)));

        assertTrue(permissionsFromMode(0744).containsAll(Set.of(
            OWNER_EXECUTE, OWNER_WRITE, OWNER_READ,
            GROUP_READ,
            OTHERS_READ)));
        assertTrue(permissionsFromMode(0755).containsAll(Set.of(
            OWNER_EXECUTE, OWNER_WRITE, OWNER_READ,
            GROUP_READ, GROUP_EXECUTE,
            OTHERS_READ, OTHERS_EXECUTE)));
        assertTrue(permissionsFromMode(0777).containsAll(Set.of(
            OWNER_EXECUTE, OWNER_WRITE, OWNER_READ,
            GROUP_EXECUTE, GROUP_WRITE, GROUP_READ,
            OTHERS_EXECUTE, OTHERS_WRITE, OTHERS_READ)));
    }

    @Test
    void pathWithFile() {
        var file = new File("file");
        var path = FileUtils.path(file);
        assertEquals(new File("file").getAbsolutePath(), path.toString());
    }

    @Test
    void pathWithFileAndPaths() {
        var file = new File("file");
        var path = FileUtils.path(file, "dir1", "dir2", "file2.txt");
        assertEquals(new File("file/dir1/dir2/file2.txt").getAbsolutePath(), path.toString());
    }

    @Test
    void pathWithPath() {
        var basePath = Path.of("path");
        var path = FileUtils.path(basePath);
        assertEquals(new File("path").getAbsolutePath(), path.toString());
    }

    @Test
    void pathWithPathAndPaths() {
        var basePath = Path.of("path");
        var path = FileUtils.path(basePath, "dir1", "dir2", "file2.txt");
        assertEquals(new File("path/dir1/dir2/file2.txt").getAbsolutePath(), path.toString());
    }
}
