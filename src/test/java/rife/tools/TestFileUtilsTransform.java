/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFileUtilsTransform {
    @TempDir
    Path tempDir;

    private File createFileWithContent(String fileName, String content)
    throws Exception {
        var file = new File(tempDir.toFile(), fileName);
        FileUtils.writeString(content, file);
        return file;
    }

    @BeforeEach
    public void initFiles()
    throws Exception {
        createFileWithContent("file.txt", "Hello world!");
        createFileWithContent("file2.txt", "Hello, world!");
        createFileWithContent("file.xml", "1234");
    }

    @Test
    public void testTransformFilesWithoutPatterns()
    throws Exception {
        // Given
        var directory = tempDir.toFile();
        Function<String, String> transformer = String::toUpperCase;

        // When
        FileUtils.transformFiles(directory, transformer);

        // Then
        assertEquals("HELLO WORLD!", FileUtils.readString(new File(directory, "file.txt")));
        assertEquals("HELLO, WORLD!", FileUtils.readString(new File(directory, "file2.txt")));
        assertEquals("1234", FileUtils.readString(new File(directory, "file.xml")));
    }

    @Test
    public void testTransformFilesWithPatterns()
    throws Exception {
        // Given
        var directory = tempDir.toFile();
        var included = Pattern.compile(".*\\.txt");
        var excluded = Pattern.compile(".*\\.bak");

        // When
        FileUtils.transformFiles(directory, included, excluded, String::toUpperCase);

        // Then
        assertEquals("HELLO WORLD!", FileUtils.readString(new File(directory, "file.txt")));
        assertEquals("HELLO, WORLD!", FileUtils.readString(new File(directory, "file2.txt")));
        assertEquals("1234", FileUtils.readString(new File(directory, "file.xml")));
    }

    @Test
    public void testTransformFilesWithPatternLists()
    throws Exception {
        // Given
        var directory = tempDir.toFile();
        List<Pattern> includedList = new ArrayList<>();
        includedList.add(Pattern.compile(".*\\.txt"));
        List<Pattern> excludedList = new ArrayList<>();
        excludedList.add(Pattern.compile(".*\\.bak"));

        // When
        FileUtils.transformFiles(directory, includedList, excludedList, String::toUpperCase);

        // Then
        assertEquals("HELLO WORLD!", FileUtils.readString(new File(directory, "file.txt")));
        assertEquals("HELLO, WORLD!", FileUtils.readString(new File(directory, "file2.txt")));
        assertEquals("1234", FileUtils.readString(new File(directory, "file.xml")));
    }

    @Test
    public void testTransformFilesWithPatternArrays()
    throws Exception {
        // Given
        var directory = tempDir.toFile();
        var includedArray = new Pattern[]{Pattern.compile(".*\\.txt")};
        var excludedArray = new Pattern[]{Pattern.compile(".*\\.bak")};

        // When
        FileUtils.transformFiles(directory, includedArray, excludedArray, String::toUpperCase);

        // Then
        assertEquals("HELLO WORLD!", FileUtils.readString(new File(directory, "file.txt")));
        assertEquals("HELLO, WORLD!", FileUtils.readString(new File(directory, "file2.txt")));
        assertEquals("1234", FileUtils.readString(new File(directory, "file.xml")));
    }

    @Test
    public void testTransformFilesWithInvalidDirectory()
    throws FileUtilsErrorException {
        // Given
        var directory = new File(tempDir.toFile(), "non-existing-directory");

        // When
        FileUtils.transformFiles(directory, String::toUpperCase);
    }
}
