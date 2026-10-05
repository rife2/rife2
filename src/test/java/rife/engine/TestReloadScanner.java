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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    static Set<Path> assertSettlesAfterChange(ReloadScanner scanner) {
        assertTrue(scanner.settled().isEmpty(), "the change was seen but hasn't settled");
        assertTrue(scanner.settled().isEmpty(), "one quiet scan isn't enough");
        var settled = scanner.settled();
        assertFalse(settled.isEmpty(), "the files stopped changing");
        assertTrue(scanner.settled().isEmpty(), "a settled change is only reported once");
        return settled;
    }

    @Test
    void testSettledNamesTheChangedFiles(@TempDir Path directory)
    throws Exception {
        Files.writeString(directory.resolve("A.class"), "a");
        Files.writeString(directory.resolve("B.class"), "b");
        Files.writeString(directory.resolve("C.class"), "c");
        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);

        Files.writeString(directory.resolve("A.class"), "aa");
        Files.delete(directory.resolve("B.class"));
        Files.writeString(directory.resolve("D.class"), "d");
        assertEquals(Set.of(directory.resolve("A.class"), directory.resolve("B.class"), directory.resolve("D.class")),
            assertSettlesAfterChange(scanner));

        Files.writeString(directory.resolve("C.class"), "cc");
        assertTrue(scanner.settled().isEmpty());
        Files.writeString(directory.resolve("D.class"), "dd");
        assertEquals(Set.of(directory.resolve("C.class"), directory.resolve("D.class")), assertSettlesAfterChange(scanner),
            "every file that changed before things settled is named");
    }

    @Test
    void testDescribeNamesAFewFiles() {
        assertEquals("", ReloadScanner.describe(Set.of()));
        assertEquals("A.class, B.class", ReloadScanner.describe(Set.of(Path.of("x/B.class"), Path.of("y/A.class"))));
        var many = new LinkedHashSet<Path>();
        for (var i = 0; i < 7; i++) {
            many.add(Path.of("dir", "F" + i + ".class"));
        }
        assertEquals("F0.class, F1.class, F2.class, F3.class, F4.class and 2 more", ReloadScanner.describe(many));
    }

    @Test
    void testSettlesOnceChangesStop(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "a");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertTrue(scanner.settled().isEmpty(), "nothing changed yet");

        Files.writeString(class_file, "aa");
        assertSettlesAfterChange(scanner);
    }

    @Test
    void testDeletedFileIsAChange(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "a");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertTrue(scanner.settled().isEmpty());

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
        assertTrue(classes.settled().isEmpty());
        assertTrue(classes.settled().isEmpty());
        assertTrue(classes.settled().isEmpty(), "a resource never restarts the application");

        assertSettlesAfterChange(others);
    }

    // a recompiled class regularly keeps its size, only its modification time moves
    @Test
    void testChangeOfTheSameSizeIsSeen(@TempDir Path directory)
    throws Exception {
        var class_file = directory.resolve("A.class");
        Files.writeString(class_file, "aa");

        var scanner = new ReloadScanner(List.of(directory), ReloadScanner.CLASS_FILES);
        assertTrue(scanner.settled().isEmpty());

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
        assertTrue(scanner.settled().isEmpty());

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
        assertTrue(scanner.settled().isEmpty(), "watching a directory doesn't reload by itself");
        assertTrue(scanner.settled().isEmpty());
        assertTrue(scanner.settled().isEmpty());

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
            assertTrue(scanner.settled().isEmpty());
            assertTrue(scanner.settled().isEmpty());
            assertTrue(scanner.settled().isEmpty(), "a directory that can't be read doesn't mean its files were deleted");
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
        assertTrue(scanner.settled().isEmpty());

        Files.delete(class_file);
        Files.delete(classes);
        assertTrue(scanner.settled().isEmpty());
        assertTrue(scanner.settled().isEmpty());
        assertTrue(scanner.settled().isEmpty(), "the directory is being rebuilt, its classes aren't gone");

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
