/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

public class TestReloadScanner {
    static Path watched(String path)
    throws IOException {
        var result = Path.of(path).toAbsolutePath().normalize();
        return Files.exists(result) ? result.toRealPath() : result;
    }

    // a change is only reported once the files have stopped changing for
    // several scans, so a build that writes in bursts reloads once
    static void assertSettlesAfterChange(ReloadScanner scanner) {
        assertFalse(scanner.settled(), "the change was seen but hasn't settled");
        assertFalse(scanner.settled(), "one quiet scan isn't enough");
        assertTrue(scanner.settled(), "the files stopped changing");
        assertFalse(scanner.settled(), "a settled change is only reported once");
    }

    @Test
    void testSettlesOnceChangesStop(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "a");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertFalse(scanner.settled(), "nothing changed yet");

        Files.writeString(class_file, "aa");
        assertSettlesAfterChange(scanner);
    }

    @Test
    void testDeletedFileIsAChange(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "a");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertFalse(scanner.settled());

        Files.delete(class_file);
        assertSettlesAfterChange(scanner);
    }

    @Test
    void testOnlyTheSelectedFilesCount(@TempDir Path directory)
    throws Exception {
        Files.writeString(directory.resolve("A.class"), "a");
        var resource = directory.resolve("notes.txt");
        Files.writeString(resource, "notes");

        var classes = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        var others = new ReloadScanner(List.of(directory), ReloadScanner.OTHER_FILES);

        Files.writeString(resource, "other notes");
        assertFalse(classes.settled());
        assertFalse(classes.settled());
        assertFalse(classes.settled(), "a resource never restarts the application");

        assertSettlesAfterChange(others);
    }

    // a recompiled class regularly keeps its size, only its modification time moves
    @Test
    void testChangeOfTheSameSizeIsSeen(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "aa");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertFalse(scanner.settled());

        Files.writeString(class_file, "bb");
        Files.setLastModifiedTime(class_file, FileTime.fromMillis(System.currentTimeMillis() + 5000));
        assertSettlesAfterChange(scanner);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testFollowsSymbolicLinks(@TempDir Path directory)
    throws Exception {
        var real = Files.createDirectories(directory.resolve("real"));
        var class_file = real.resolve("A.class");
        Files.writeString(class_file, "a");

        var link = directory.resolve("link");
        Files.createSymbolicLink(link, real);
        var outer = Files.createDirectories(directory.resolve("outer"));
        Files.createSymbolicLink(outer.resolve("nested"), real);

        var linked = new ReloadScanner(List.of(link), ReloadScanner.CLASS_FILES);
        var nested = new ReloadScanner(List.of(outer), ReloadScanner.CLASS_FILES);

        Files.writeString(class_file, "aa");
        assertSettlesAfterChange(linked);
        assertSettlesAfterChange(nested);
    }

    // links towards each other must neither loop nor be walked repeatedly
    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testSurvivesSymbolicLinkWebs(@TempDir Path directory)
    throws Exception {
        var nested = Files.createDirectories(directory.resolve("nested"));
        Files.writeString(nested.resolve("A.class"), "a");
        Files.createSymbolicLink(nested.resolve("loop"), directory);

        var fanned = Files.createDirectories(directory.resolve("fanned"));
        Files.createSymbolicLink(fanned.resolve("one"), nested);
        Files.createSymbolicLink(fanned.resolve("two"), nested);

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertFalse(scanner.settled());

        Files.writeString(nested.resolve("B.class"), "b");
        assertSettlesAfterChange(scanner);
    }

    @Test
    void testAddedDirectoryIsNotAChange(@TempDir Path directory)
    throws Exception {
        var base = Files.createDirectories(directory.resolve("base"));
        Files.writeString(base.resolve("style.css"), "p{}");

        var scanner = new ReloadScanner(List.of(), ReloadScanner.OTHER_FILES);
        scanner.add(base);
        assertFalse(scanner.settled(), "watching a directory doesn't reload by itself");
        assertFalse(scanner.settled());
        assertFalse(scanner.settled());

        Files.writeString(base.resolve("style.css"), "p{color:red}");
        assertSettlesAfterChange(scanner);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void testUnreadableDirectoryIsNotADeletion(@TempDir Path directory)
    throws Exception {
        var nested = Files.createDirectories(directory.resolve("nested"));
        Files.writeString(nested.resolve("A.class"), "a");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assumeTrue(nested.toFile().setReadable(false), "the directory has to become unreadable for this test");
        assumeFalse(Files.isReadable(nested), "the directory has to be unreadable for this test");
        try {
            assertFalse(scanner.settled());
            assertFalse(scanner.settled());
            assertFalse(scanner.settled(), "a directory that can't be read doesn't mean its files were deleted");
        } finally {
            nested.toFile().setReadable(true);
        }
    }

    // a build removes its output directory before it writes the new classes
    @Test
    void testRemovedDirectoryIsNotADeletion(@TempDir Path directory)
    throws Exception {
        var classes = Files.createDirectories(directory.resolve("classes"));
        var class_file = classes.resolve("A.class");
        Files.writeString(class_file, "a");

        var scanner = new ReloadScanner(List.of(classes), ReloadScanner.CLASS_FILES);
        assertFalse(scanner.settled());

        Files.delete(class_file);
        Files.delete(classes);
        assertFalse(scanner.settled());
        assertFalse(scanner.settled());
        assertFalse(scanner.settled(), "the directory is being rebuilt, its classes aren't gone");

        Files.createDirectories(classes);
        Files.writeString(class_file, "aaa");
        assertSettlesAfterChange(scanner);
    }

    @Test
    void testDirectoriesSkipArchives()
    throws Exception {
        var path = String.join(File.pathSeparator, "build/main", "lib/one.jar", "lib/two.ZIP", "lib/*", "", "build/test");
        assertEquals(List.of(watched("build/main"), watched("build/test")), ReloadScanner.directories(path));
        assertTrue(ReloadScanner.directories((String) null).isEmpty());
        assertTrue(ReloadScanner.directories("").isEmpty());
    }

    @Test
    void testDirectoriesCombinePathsWithoutDuplicates()
    throws Exception {
        var classpath = String.join(File.pathSeparator, "build/main", "./build/main");
        assertEquals(List.of(watched("build/main"), watched("build/test")),
            ReloadScanner.directories(classpath, "build/test"));
    }

    @Test
    void testDirectoryRejectsUnusablePaths()
    throws Exception {
        assertNull(ReloadScanner.directory("with\0null"));
        assertEquals(watched("build/main"), ReloadScanner.directory("build/main"));
    }
}
