/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.attribute.PosixFilePermission.*;
import static org.junit.jupiter.api.Assertions.*;
import static rife.tools.FileUtils.permissionsFromMode;

public class TestFileUtils {
    @TempDir
    public Path tempDir;

    @Test
    void testMoveFile()
    throws IOException {
        // create source and target files
        var source = new File(tempDir.toFile(), "source.txt");
        var target = new File(tempDir.toFile(), "target.txt");

        source.createNewFile();

        // move file
        FileUtils.moveFile(source, target);

        // check if source and target files exist
        assertFalse(source.exists());
        assertTrue(target.exists());
    }

    @Test
    void testMoveFileNullSource() {
        var target = new File(tempDir.toFile(), "target.txt");

        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFile(null, target));
    }

    @Test
    void testMoveFileNullTarget() {
        var source = new File(tempDir.toFile(), "source.txt");

        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveFile(source, null));
    }

    @Test
    void testMoveFileMissingSource() {
        var source = new File(tempDir.toFile(), "non_existing_source.txt");
        var target = new File(tempDir.toFile(), "target.txt");

        assertThrows(FileUtilsErrorException.class, () -> FileUtils.moveFile(source, target));
    }

    @Test
    void testMoveDirectory()
    throws IOException, FileUtilsErrorException {
        // create a source directory with some files
        var source_dir = new File(tempDir.toFile(), "source");
        source_dir.mkdir();
        var file1 = new File(source_dir, "file1.txt");
        file1.createNewFile();
        var sub_dir = new File(source_dir, "subdir");
        sub_dir.mkdir();
        var file2 = new File(sub_dir, "file2.txt");
        file2.createNewFile();

        // create a target directory
        var targetDir = new File(tempDir.toFile(), "target");

        // move the source directory to the target directory
        FileUtils.moveDirectory(source_dir, targetDir);

        // assert that the source directory and its contents were deleted
        assertFalse(source_dir.exists());
        assertFalse(file1.exists());
        assertFalse(sub_dir.exists());
        assertFalse(file2.exists());

        // assert that the target directory and its contents were created
        assertTrue(targetDir.exists());
        assertTrue(new File(targetDir, "file1.txt").exists());
        assertTrue(new File(targetDir, "subdir/file2.txt").exists());
    }

    @Test
    void testMoveDirectoryNullSource() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveDirectory(null, new File(tempDir.toFile(), "target")));
    }

    @Test
    void testMoveDirectoryNullTarget() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.moveDirectory(new File(tempDir.toFile(), "source"), null));
    }

    @Test
    void testMoveDirectoryNonexistentSource() {
        var sourceDir = new File(tempDir.toFile(), "source");
        var targetDir = new File(tempDir.toFile(), "target");
        assertThrows(FileUtilsErrorException.class, () -> FileUtils.moveDirectory(sourceDir, targetDir));
    }

    @Test
    void testDeleteDirectoryNullSource() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.deleteDirectory(null));
    }

    @Test
    void testDeleteDirectoryNonExistingSource() {
        assertThrows(FileUtilsErrorException.class, () -> FileUtils.deleteDirectory(new File("non_existing_directory")));
    }

    @Test
    void testDeleteDirectory()
    throws IOException {
        // Create directory with files and subdirectories
        var directory = tempDir.resolve("test_directory").toFile();
        directory.mkdirs();
        var file1 = new File(directory, "file1.txt");
        file1.createNewFile();
        var subdirectory = new File(directory, "subdirectory");
        subdirectory.mkdir();
        var file2 = new File(subdirectory, "file2.txt");
        file2.createNewFile();

        // Verify that directory and files exist before deleting it
        assertTrue(directory.exists());
        assertTrue(file1.exists());
        assertTrue(subdirectory.exists());
        assertTrue(file2.exists());

        // Delete directory and verify that it doesn't exist anymore
        FileUtils.deleteDirectory(directory);
        assertFalse(directory.exists());
        assertFalse(file1.exists());
        assertFalse(subdirectory.exists());
        assertFalse(file2.exists());
    }

    @Test
    void testCopyStreamToFile()
    throws IOException {
        var input = new ByteArrayInputStream("Hello world!".getBytes());
        var output_file = File.createTempFile("output", ".txt");

        assertDoesNotThrow(() -> FileUtils.copy(input, output_file));
        assertEquals("Hello world!", FileUtils.readString(output_file));

        output_file.delete();
    }

    @Test
    void testCopyFileToStream()
    throws IOException {
        var input_file = File.createTempFile("input", ".txt");
        var output = new ByteArrayOutputStream();

        FileUtils.writeString("Hello world!", input_file);

        assertDoesNotThrow(() -> FileUtils.copy(input_file, output));
        assertEquals("Hello world!", output.toString());

        input_file.delete();
    }

    @Test
    void testCopyFileToFile()
    throws IOException {
        var input_file = File.createTempFile("input", ".txt");
        var output_file = File.createTempFile("output", ".txt");

        FileUtils.writeString("Hello world!", input_file);

        assertDoesNotThrow(() -> FileUtils.copy(input_file, output_file));
        assertEquals("Hello world!", FileUtils.readString(output_file));

        input_file.delete();
        output_file.delete();
    }

    @Test
    void testNullInputStream() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copy((InputStream) null, new ByteArrayOutputStream()));
    }

    @Test
    void testNullOutputStream() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copy(new ByteArrayInputStream("Hello World!".getBytes()), (OutputStream) null));
    }

    @Test
    void testNullSourceFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copy((File) null, new File("output.txt")));
    }

    @Test
    void testNullTargetFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copy(new File("input.txt"), (File) null));
    }

    @Test
    void testCopyDirectory()
    throws Exception {
        // create source and target directories with some files
        var source_directory = new File(tempDir.toFile(), "source");
        source_directory.mkdirs();
        new File(source_directory, "dir1").mkdirs();
        var target_directory = new File(tempDir.toFile(), "target");
        target_directory.mkdirs();
        Files.write(Paths.get(source_directory.getAbsolutePath(), "file1.txt"), "Test file 1 contents".getBytes());
        Files.write(Paths.get(source_directory.getAbsolutePath(), "dir1", "file2.txt"), "Test file 2 contents".getBytes());

        // call the method to copy the directory
        FileUtils.copyDirectory(source_directory, target_directory);

        // check that all files in the source directory are copied to target directory
        assertEquals(FileUtils.generateDirectoryListing(source_directory), FileUtils.generateDirectoryListing(target_directory));
    }

    @Test
    void testCopyDirectorySourceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(null, new File("target")));
    }

    @Test
    void testCopyDirectoryTargetIsNull() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.copyDirectory(new File("source"), null));
    }

    @Test
    void testReadStreamNull() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readStream(null));
    }

    @Test
    void testReadStreamEmptyStream()
    throws IOException {
        var input = new ByteArrayInputStream(new byte[0]);
        var result = FileUtils.readStream(input);
        assertEquals(0, result.size());
    }

    @Test
    void testReadStreamSmallStream()
    throws IOException {
        var input_bytes = new byte[]{1, 2, 3};
        var input = new ByteArrayInputStream(input_bytes);
        var result = FileUtils.readStream(input);
        assertArrayEquals(input_bytes, result.toByteArray());
    }

    @Test
    void testReadStreamLargeStream()
    throws IOException {
        var input_bytes = new byte[1024 * 1024];
        for (var i = 0; i < input_bytes.length; i++) {
            input_bytes[i] = (byte) (i % 256); // Fill buffer with a repeating pattern
        }
        var input = new ByteArrayInputStream(input_bytes);
        var result = FileUtils.readStream(input);
        assertArrayEquals(input_bytes, result.toByteArray());
    }

    private static final String TEST_STRING = "Hello, world!\n";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);

    @Test
    void testReadStringFromInputStream()
    throws IOException {
        InputStream input = new ByteArrayInputStream(TEST_BYTES);
        var result = FileUtils.readString(input);

        assertEquals(TEST_STRING, result);
    }

    @Test
    void testReadStringFromNullInputStream() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((InputStream) null));
    }

    @Test
    void testReadStringFromReader()
    throws IOException {
        var reader = new StringReader(TEST_STRING);
        var result = FileUtils.readString(reader);

        assertEquals(TEST_STRING, result);
    }

    @Test
    void testReadStringFromNullReader() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((Reader) null));
    }

    @Test
    void testReadStringFromInputStreamWithEncoding()
    throws IOException {
        InputStream input = new ByteArrayInputStream(TEST_BYTES);
        var result = FileUtils.readString(input, StandardCharsets.UTF_8.name());

        assertEquals(TEST_STRING, result);
    }

    @Test
    void testReadStringFromInputStreamWithInvalidEncoding() {
        InputStream input = new ByteArrayInputStream(TEST_BYTES);
        var invalidEncoding = "invalid-encoding";

        assertThrows(FileUtilsErrorException.class, () -> FileUtils.readString(input, invalidEncoding));
    }

    @Test
    void testReadStringFromNullInputStreamWithEncoding() {
        var encoding = StandardCharsets.UTF_8.name();

        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((InputStream) null, encoding));
    }

    @Test
    void testReadStringFromURLWithCharset()
    throws Exception {
        var url = new URL("https://rife2.com");
        var actual = FileUtils.readString(url, "UTF-8");
        assertTrue(Pattern.compile(".*<html[^>]*>.*</html>.*", Pattern.DOTALL).matcher(actual).matches());
    }

    @Test
    void testReadStringFromURL()
    throws Exception {
        var url = new URL("https://rife2.com");
        var actual = FileUtils.readString(url);
        assertTrue(Pattern.compile(".*<html[^>]*>.*</html>.*", Pattern.DOTALL).matcher(actual).matches());
    }

    @Test
    void testReadStringFromURLWithNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((URL) null, "UTF-8"));
    }

    @Test
    void testReadStringFromFileWithCharset()
    throws Exception {
        // Create a temporary file with some content
        var tmp_file = File.createTempFile("test", ".txt");
        tmp_file.deleteOnExit();
        var file_writer = new FileWriter(tmp_file);
        var expected = "This is a test.";
        file_writer.write(expected);
        file_writer.close();

        // Read the string from the temporary file and compare it to the expected value
        var actual = FileUtils.readString(tmp_file, "UTF-8");
        assertEquals(expected, actual.trim());
    }

    @Test
    void testReadStringFromFileWithNullFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((File) null));
    }

    @Test
    void testReadBytesWithNullInputStream() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readBytes((InputStream) null));
    }

    @Test
    void testReadStringWithNullFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readString((File) null));
    }

    @Test
    void testReadBytesWithNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.readBytes((URL) null));
    }

    @Test
    void testReadBytesWithInputStream()
    throws Exception {
        var inStream = new ByteArrayInputStream("This is a test string".getBytes());
        var byteArray = FileUtils.readBytes(inStream);
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);
    }

    @Test
    void testReadBytesWithUrl()
    throws Exception {
        var url = new URL("https://www.google.com");
        var byteArray = FileUtils.readBytes(url);
        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);
    }

    @Test
    void testReadBytesWithFile()
    throws Exception {
        // Create a temporary file with some content
        var tmp_file = File.createTempFile("test", ".txt");
        tmp_file.deleteOnExit();
        var file_writer = new FileWriter(tmp_file);
        var expected = "This is a test.";
        file_writer.write(expected);
        file_writer.close();

        var result = FileUtils.readBytes(tmp_file);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testWriteBytesToFile()
    throws Exception {
        // setup
        var content = "Hello World".getBytes(StandardCharsets.UTF_8);
        var file = new File(tempDir.toFile(), "test.txt");

        // execute
        FileUtils.writeBytes(content, file);

        // verify
        assertTrue(file.exists());
        assertEquals(content.length, file.length());

        var readContent = new byte[content.length];
        try (var fis = new FileInputStream(file)) {
            fis.read(readContent);
        }
        assertArrayEquals(content, readContent);
    }

    @Test
    void testWriteBytesContentIsNull() {
        // setup
        byte[] content = null;
        var file = new File(tempDir.toFile(), "test.txt");

        // execute & verify
        var exception = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.writeBytes(content, file));
        assertEquals("content can't be null.", exception.getMessage());
    }

    @Test
    void testWriteBytesDestinationIsNull() {
        // setup
        var content = "Hello World".getBytes(StandardCharsets.UTF_8);
        File file = null;

        // execute & verify
        var exception = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.writeBytes(content, file));
        assertEquals("destination can't be null.", exception.getMessage());
    }

    @Test
    void testWriteStringToFile()
    throws Exception {
        // setup
        var content = "Hello World";
        var file = new File(tempDir.toFile(), "test.txt");

        // execute
        FileUtils.writeString(content, file);

        // verify
        assertTrue(file.exists());
        assertEquals(content.length(), file.length());

        var readContent = new char[content.length()];
        try (var fr = new FileReader(file)) {
            fr.read(readContent);
        }
        assertEquals(content, new String(readContent));
    }

    @Test
    void testWriteStringContentIsNull() {
        // setup
        String content = null;
        var file = new File(tempDir.toFile(), "test.txt");

        // execute & verify
        var exception = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.writeString(content, file));
        assertEquals("content can't be null.", exception.getMessage());
    }

    @Test
    void testWriteStringDestinationIsNull() {
        // setup
        var content = "Hello World";
        File file = null;

        // execute & verify
        var exception = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.writeString(content, file));
        assertEquals("destination can't be null.", exception.getMessage());
    }

    @Test
    void testDeleteFileNullFil() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.deleteFile(null));
    }

    @Test
    void testDeleteFile()
    throws Exception {
        var testFile = Files.createTempFile(tempDir, "test", ".txt").toFile();
        assertTrue(testFile.exists());

        FileUtils.deleteFile(testFile);

        assertFalse(testFile.exists());
    }

    @Test
    void testGetUniqueFilename() {
        var filename1 = FileUtils.getUniqueFilename();
        var filename2 = FileUtils.getUniqueFilename();

        assertNotEquals(filename1, filename2);
    }

    @Test
    void testUnzipFile()
    throws IOException, FileUtilsErrorException {
        // create temporary source zip file
        var source = createTempZipFile("test.txt", "test file data");
        source.deleteOnExit();

        // unzip the file
        FileUtils.unzipFile(source, tempDir.toFile());

        // check that output file exists
        var outputFile = new File(tempDir.toFile().getAbsolutePath() + File.separator + "test.txt");
        assertTrue(outputFile.exists(), "Unzipped file should exist at destination.");

        // clean up test files/directories
        outputFile.delete();
    }

    @Test
    void testUnzipFileNullSource() {
        // test null source file argument
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.unzipFile(null, new File("destination"));
        });
    }

    @Test
    void testUnzipFileNullDestination() {
        // test null destination argument
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.unzipFile(new File("source"), null);
        });
    }

    @Test
    void testUnzipFileBadZipFile() {
        // test invalid zip file argument
        assertThrows(FileUtilsErrorException.class, () -> {
            FileUtils.unzipFile(new File("bad_zip_file"), new File("destination"));
        });
    }

    @Test
    void testUnzipFileBadOutputPath()
    throws IOException {
        // create temporary source zip file
        var source = createTempZipFile("test.txt", "test file data");
        source.deleteOnExit();

        // create temporary directory that should not be writable
        var destination = Files.createTempDirectory("test_dir").toFile();
        destination.deleteOnExit();
        destination.setWritable(false);

        // test unzipping to invalid output path
        assertThrows(FileUtilsErrorException.class, () -> {
            FileUtils.unzipFile(source, destination);
        });

        // clean up test files/directories
        destination.setWritable(true);
        destination.delete();
    }

    private File createTempZipFile(String entryFileName, String entryFileData)
    throws IOException {
        var file = Files.createTempFile("test_file", ".zip").toFile();

        try (var out = new ZipOutputStream(new FileOutputStream(file))) {
            var entry = new ZipEntry(entryFileName);
            out.putNextEntry(entry);
            out.write(entryFileData.getBytes());
            out.closeEntry();
        }

        return file;
    }

    @Test
    void testGetBaseNameWithFile() {
        var file = new File("/path/to/file.txt");
        var expectedBaseName = "file";
        var actualBaseName = FileUtils.getBaseName(file);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetBaseNameWithString() {
        var fileName = "file.txt";
        var expectedBaseName = "file";
        var actualBaseName = FileUtils.getBaseName(fileName);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetBaseNameWithNullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.getBaseName((File) null);
        });
    }

    @Test
    void testGetBaseNameWithNullFileName() {
        assertThrows(IllegalArgumentException.class, () -> {
            FileUtils.getBaseName((String) null);
        });
    }

    @Test
    void testGetBaseNameWithNoExtension() {
        var fileName = "file";
        var expectedBaseName = "file";
        var actualBaseName = FileUtils.getBaseName(fileName);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetBaseNameWithLeadingDot() {
        var fileName = ".file.txt";
        var expectedBaseName = ".file";
        var actualBaseName = FileUtils.getBaseName(fileName);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetBaseNameWithTrailingDot() {
        var fileName = "file.";
        var expectedBaseName = "file";
        var actualBaseName = FileUtils.getBaseName(fileName);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetBaseNameWithMultipleDots() {
        var fileName = "file.name.txt";
        var expectedBaseName = "file.name";
        var actualBaseName = FileUtils.getBaseName(fileName);
        assertEquals(expectedBaseName, actualBaseName);
    }

    @Test
    void testGetExtensionWithFile() {
        var file = new File("test.txt");
        var actual = FileUtils.getExtension(file);
        var expected = "txt";
        assertEquals(expected, actual);
    }

    @Test
    void testGetExtensionWithFileName() {
        var fileName = "test.txt";
        var actual = FileUtils.getExtension(fileName);
        var expected = "txt";
        assertEquals(expected, actual);
    }

    @Test
    void testGetExtensionWithoutExtension() {
        var fileName = "test";
        var actual = FileUtils.getExtension(fileName);
        assertNull(actual);
    }

    @Test
    void testGetExtensionWithEmptyString() {
        var fileName = "";
        var actual = FileUtils.getExtension(fileName);
        assertNull(actual);
    }

    @Test
    void testGetExtensionWithNullFile() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.getExtension((File) null));
    }

    @Test
    void testGetExtensionWithNullFileName() {
        assertThrows(IllegalArgumentException.class, () -> FileUtils.getExtension((String) null));
    }

    @Test
    public void testCombineToAbsolutePathsEmpty() {
        var result = FileUtils.combineToAbsolutePaths();
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testCombineToAbsolutePathsNull() {
        var result = FileUtils.combineToAbsolutePaths(null);
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testCombineToAbsolutePathsEmptyListInMiddle() {
        var file1 = new File("file1.txt");
        var list1 = List.of(file1);
        var result = FileUtils.combineToAbsolutePaths(list1, Collections.emptyList(), list1);
        assertEquals(2, result.size());
        assertEquals(file1.getAbsolutePath(), result.get(0));
        assertEquals(file1.getAbsolutePath(), result.get(1));
    }

    @Test
    public void testCombineToAbsolutePathsDuplicateFiles() {
        var file1 = new File("file1.txt");
        var file2 = new File("file2.txt");
        var list1 = List.of(file1);
        var list2 = Arrays.asList(file1, file2, file2);
        var result = FileUtils.combineToAbsolutePaths(list1, list2);
        assertEquals(4, result.size());
        assertEquals(file1.getAbsolutePath(), result.get(0));
        assertEquals(file1.getAbsolutePath(), result.get(1));
        assertEquals(file2.getAbsolutePath(), result.get(2));
        assertEquals(file2.getAbsolutePath(), result.get(3));
    }

    @Test
    public void testCombineToAbsolutePaths() {
        // given
        var file1 = new File("file1.txt");
        var file2 = new File("file2.txt");
        var file3 = new File("/path/to/file3.txt");
        var list1 = Arrays.asList(file1, file2);
        var list2 = List.of(file3);

        // when
        var result = FileUtils.combineToAbsolutePaths(list1, list2);

        // then
        assertEquals(3, result.size());
        assertEquals(file1.getAbsolutePath(), result.get(0));
        assertEquals(file2.getAbsolutePath(), result.get(1));
        assertEquals(file3.getAbsolutePath(), result.get(2));
    }

    @Test
    public void testGenerateDirectoryListingWithNullDirectory() {
        assertThrows(NullPointerException.class, () -> {
            FileUtils.generateDirectoryListing(null);
        });
    }

    @Test
    public void testGenerateDirectoryListingWithNonexistentDirectory() {
        var directory = new File("nonexistent");
        assertThrows(IOException.class, () -> {
            FileUtils.generateDirectoryListing(directory);
        });
    }

    @Test
    public void testGenerateDirectoryListingWithEmptyDirectory()
    throws IOException {
        var directory = File.createTempFile("test", "");
        directory.delete();
        directory.mkdir();

        assertEquals("", FileUtils.generateDirectoryListing(directory));
        directory.delete();
    }

    @Test
    public void testGenerateDirectoryListingWithDirectoryContainingFilesAndSubdirectories()
    throws IOException {
        var directory = File.createTempFile("test", "");
        directory.delete();
        directory.mkdir();

        new File(directory, "file1.txt").createNewFile();
        new File(directory, "file2.txt").createNewFile();

        var subdirectory1 = new File(directory, "subdirectory1");
        subdirectory1.mkdir();
        new File(subdirectory1, "file3.txt").createNewFile();

        var subdirectory2 = new File(directory, "subdirectory2");
        subdirectory2.mkdir();
        new File(subdirectory2, "file4.txt").createNewFile();

        var expectedListing = "/file1.txt\n" +
            "/file2.txt\n" +
            "/subdirectory1\n" +
            "/subdirectory1/file3.txt\n" +
            "/subdirectory2\n" +
            "/subdirectory2/file4.txt";
        assertEquals(expectedListing, FileUtils.generateDirectoryListing(directory));
        FileUtils.deleteDirectory(directory);
    }

    @Test
    public void testJoinPathsSinglePath() {
        var paths = Collections.singletonList("/path/to/file");
        var expected = "/path/to/file";
        var actual = FileUtils.joinPaths(paths);
        assertEquals(expected, actual);
    }

    @Test
    public void testJoinPathsMultiplePaths() {
        var paths = Arrays.asList("/path/to/file1", "/path/to/file2", "/path/to/file3");
        var expected = "/path/to/file1" + File.pathSeparator + "/path/to/file2" + File.pathSeparator + "/path/to/file3";
        var actual = FileUtils.joinPaths(paths);
        assertEquals(expected, actual);
    }

    @Test
    public void testJoinPathsNullPaths() {
        List<String> paths = null;
        var expected = "";
        var actual = FileUtils.joinPaths(paths);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetJavaFileListSingleFile()
    throws Exception {
        var tempFile = new File(tempDir.toFile(), "test.java");
        tempFile.createNewFile();

        var expected = Collections.singletonList(tempFile);
        var actual = FileUtils.getJavaFileList(tempDir.toFile());

        assertIterableEquals(expected, actual);
    }

    @Test
    public void testGetJavaFileListNestedDirectories()
    throws Exception {
        var nestedDir = new File(tempDir.toFile(), "nested");
        var tempFile1 = new File(tempDir.toFile(), "test.java");
        var tempFile2 = new File(nestedDir, "nested_test.java");
        tempDir.toFile().mkdirs();
        nestedDir.mkdirs();
        tempFile1.createNewFile();
        tempFile2.createNewFile();

        var expected = Arrays.asList(tempFile1, tempFile2);
        var actual = FileUtils.getJavaFileList(tempDir.toFile());

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void testGetJavaFileListNullDirectory() {
        File directory = null;
        List<File> expected = Collections.emptyList();
        var actual = FileUtils.getJavaFileList(directory);
        assertIterableEquals(expected, actual);
    }

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
    void testPathWithFile() {
        var file = new File("file");
        var path = FileUtils.path(file);
        assertEquals(new File("file").getAbsolutePath(), path.toString());
    }

    @Test
    void testPathWithFileAndPaths() {
        var file = new File("file");
        var path = FileUtils.path(file, "dir1", "dir2", "file2.txt");
        assertEquals(new File("file/dir1/dir2/file2.txt").getAbsolutePath(), path.toString());
    }

    @Test
    void testPathWithPath() {
        var basePath = Path.of("path");
        var path = FileUtils.path(basePath);
        assertEquals(new File("path").getAbsolutePath(), path.toString());
    }

    @Test
    void testPathWithPathAndPaths() {
        var basePath = Path.of("path");
        var path = FileUtils.path(basePath, "dir1", "dir2", "file2.txt");
        assertEquals(new File("path/dir1/dir2/file2.txt").getAbsolutePath(), path.toString());
    }
}
