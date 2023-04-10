/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFileUtilsFileList {
    @TempDir
    File testDir;

    Pattern[] included;
    Pattern[] excluded;

    @BeforeEach
    void setUp()
    throws IOException {
        // create some files in the test directory
        new File(testDir, "file1.txt").createNewFile();
        new File(testDir, "file2.java").createNewFile();
        new File(testDir, "file3.sh").createNewFile();

        // set up include and exclude patterns
        included = new Pattern[]{Pattern.compile("^.*\\.txt$"), Pattern.compile("^.*\\.java$")};
        excluded = new Pattern[]{Pattern.compile("^file1\\.txt$")};
    }

    @Test
    void testGetFileList() {
        var file_list = FileUtils.getFileList(testDir, included, excluded);
        assertEquals(1, file_list.size());
        assertEquals("file2.java", file_list.get(0));
    }

    @Test
    void testGetFileListWithNullDir() {
        var file_list = FileUtils.getFileList(null, included, excluded);
        assertEquals(0, file_list.size());
    }

    @Test
    void testGetFileListWithNullIncluded() {
        var file_list = FileUtils.getFileList(testDir, null, excluded);
        assertEquals(2, file_list.size());
        assertTrue(file_list.contains("file2.java"));
        assertTrue(file_list.contains("file3.sh"));
    }

    @Test
    void testGetFileListWithNullExcluded() {
        var file_list = FileUtils.getFileList(testDir, included, null);
        assertEquals(2, file_list.size());
        assertTrue(file_list.contains("file1.txt"));
        assertTrue(file_list.contains("file2.java"));
    }

    @Test
    void testGetFileListWithEmptyIncluded() {
        var file_list = FileUtils.getFileList(testDir, new Pattern[0], excluded);
        assertEquals(2, file_list.size());
        assertTrue(file_list.contains("file2.java"));
        assertTrue(file_list.contains("file3.sh"));
    }

    @Test
    void testGetFileListWithEmptyExcluded() {
        var file_list = FileUtils.getFileList(testDir, included, new Pattern[0]);
        assertEquals(2, file_list.size());
        assertTrue(file_list.contains("file1.txt"));
        assertTrue(file_list.contains("file2.java"));
    }
}