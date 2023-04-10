/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * A utility class for handling files.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class FileUtils {
    public static final Pattern JAVA_FILE_PATTERN = Pattern.compile("^.*\\.java$");
    public static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.*\\.jar$");

    private FileUtils() {
        // no-op
    }

    /**
     * Returns a list of files in the given directory.
     *
     * @param dir The directory to search for files.
     * @return A list of files in the given directory.
     * @since 1.0
     */
    public static List<String> getFileList(File dir) {
        return getFileList(dir, null, null, true);
    }

    /**
     * Returns a list of files in the given directory that match the specified
     * inclusion and exclusion patterns.
     *
     * @param dir      The directory to search for files.
     * @param included A pattern for files to include.
     * @param excluded A pattern for files to exclude.
     * @return A list of files in the given directory that match the specified inclusion and exclusion patterns.
     * @since 1.0
     */
    public static List<String> getFileList(File dir, Pattern included, Pattern excluded) {
        return getFileList(dir, new Pattern[]{included}, new Pattern[]{excluded}, true);
    }

    /**
     * Returns a list of files in the given directory that match the specified
     * inclusion and exclusion patterns.
     *
     * @param dir      The directory to search for files.
     * @param included A list of patterns for files to include.
     * @param excluded A list of patterns for files to exclude.
     * @return A list of files in the given directory that match the specified inclusion and exclusion patterns.
     * @since 1.0
     */
    public static List<String> getFileList(File dir, List<Pattern> included, List<Pattern> excluded) {
        return getFileList(dir, included.toArray(new Pattern[0]), excluded.toArray(new Pattern[0]), true);
    }

    /**
     * Returns a list of files in the given directory that match the specified
     * inclusion and exclusion patterns.
     *
     * @param dir      The directory to search for files.
     * @param included An array of patterns for files to include.
     * @param excluded An array of patterns for files to exclude.
     * @return A list of files in the given directory that match the specified inclusion and exclusion patterns.
     * @since 1.0
     */
    public static List<String> getFileList(File dir, Pattern[] included, Pattern[] excluded) {
        return getFileList(dir, included, excluded, true);
    }

    private static List<String> getFileList(File file, Pattern[] included, Pattern[] excluded, boolean root) {
        if (null == file) {
            return new ArrayList<>();
        }

        var file_list = new ArrayList<String>();
        if (file.isDirectory()) {
            var list = file.list();
            if (null != list) {
                for (var list_entry : list) {
                    var next_file = new File(file.getAbsolutePath() + File.separator + list_entry);
                    var dir = getFileList(next_file, included, excluded, false);

                    for (var file_name : dir) {
                        if (root) {
                            // if the file is not accepted, don't process it further
                            if (!filter(file_name, included, excluded)) {
                                continue;
                            }
                        } else {
                            file_name = file.getName() + File.separator + file_name;
                        }

                        var filelist_size = file_list.size();
                        for (var j = 0; j < filelist_size; j++) {
                            if (file_list.get(j).compareTo(file_name) > 0) {
                                file_list.add(j, file_name);
                                break;
                            }
                        }
                        if (file_list.size() == filelist_size) {
                            file_list.add(file_name);
                        }
                    }
                }
            }
        } else if (file.isFile()) {
            var file_name = file.getName();

            if (root) {
                if (filter(file_name, included, excluded)) {
                    file_list.add(file_name);
                }
            } else {
                file_list.add(file_name);
            }
        }

        return file_list;
    }

    // added to remove dependency on StringUtils for the use of the build system wrapper
    private static boolean filter(String name, Pattern[] included, Pattern[] excluded) {
        if (null == name) {
            return false;
        }

        var accepted = false;

        // retain only the includes
        if (null == included || included.length == 0) {
            accepted = true;
        } else {
            for (var pattern : included) {
                if (pattern != null &&
                    pattern.matcher(name).matches()) {
                    accepted = true;
                    break;
                }
            }
        }

        // remove the excludes
        if (accepted &&
            excluded != null) {
            for (var pattern : excluded) {
                if (pattern != null &&
                    pattern.matcher(name).matches()) {
                    accepted = false;
                    break;
                }
            }
        }

        return accepted;
    }

    /**
     * Moves the source file to the target location.
     *
     * @param source the file to be moved.
     * @param target the new location to move the file to.
     * @throws FileUtilsErrorException  if an error occurs while moving the file.
     * @throws IllegalArgumentException if either the source or target is null.
     * @since 1.0
     */
    public static void moveFile(File source, File target)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        if (!source.exists()) {
            throw new FileUtilsErrorException("The source file '" + source.getAbsolutePath() + "' does not exist.");
        }

        // copy
        copy(source, target);

        // then delete source file
        deleteFile(source);
    }

    /**
     * Moves the source directory to the target location.
     *
     * @param source the directory to be moved.
     * @param target the new location to move the directory to.
     * @throws FileUtilsErrorException  if an error occurs while moving the directory.
     * @throws IllegalArgumentException if either the source or target is null.
     * @since 1.0
     */
    public static void moveDirectory(File source, File target)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        if (!source.exists()) {
            throw new FileUtilsErrorException("The source directory '" + source.getAbsolutePath() + "' does not exist.");
        }

        // Create target if it does not exist already
        if (!target.exists()) {
            target.mkdirs();
        }

        var file_list = source.list();

        assert file_list != null;
        for (var element : file_list) {
            var current_file = new File(source.getAbsolutePath() + File.separator + element);
            var target_file = new File(target.getAbsolutePath() + File.separator + element);

            if (current_file.isDirectory()) {
                moveDirectory(current_file, target_file);
            } else {
                moveFile(current_file, target_file);
            }
        }

        // If we get here it means we're finished with this directory ... delete it.
        deleteFile(source);
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param source the directory to be deleted.
     * @throws FileUtilsErrorException  if an error occurs while deleting the directory.
     * @throws IllegalArgumentException if the source is null.
     * @since 1.0
     */
    public static void deleteDirectory(File source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        if (!source.exists()) {
            throw new FileUtilsErrorException("The directory '" + source.getAbsolutePath() + "' does not exist");
        }

        var file_list = source.list();
        if (file_list != null) {
            for (var element : file_list) {
                var current_file = new File(source.getAbsolutePath() + File.separator + element);

                if (current_file.isDirectory()) {
                    deleteDirectory(current_file);
                } else {
                    deleteFile(current_file);
                }
            }
        }

        // If we get here it means we're finished with this directory ... delete it.
        deleteFile(source);
    }

    /**
     * Copy the contents of an InputStream to an OutputStream.
     *
     * @param inputStream  the InputStream to copy from. Must not be null.
     * @param outputStream the OutputStream to copy to. Must not be null.
     * @throws FileUtilsErrorException  if there is an error during the copying of streams.
     * @throws IllegalArgumentException if either inputStream or outputStream is null.
     * @since 1.0
     */
    public static void copy(InputStream inputStream, OutputStream outputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");
        if (null == outputStream) throw new IllegalArgumentException("outputStream can't be null.");

        try {
            var buffer = new byte[1024];
            var return_value = -1;

            return_value = inputStream.read(buffer);

            while (-1 != return_value) {
                outputStream.write(buffer, 0, return_value);
                return_value = inputStream.read(buffer);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error during the copying of streams.", e);
        }
    }

    /**
     * Copy the contents of an InputStream to a file.
     *
     * @param inputStream the InputStream to copy from; must not be null
     * @param target      the File to copy to; must not be null
     * @throws FileUtilsErrorException  if an error occurs while copying the stream to the file
     * @throws IllegalArgumentException if inputStream or target is null
     * @since 1.0
     */
    public static void copy(InputStream inputStream, File target)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        try {
            try (var file_output_stream = new FileOutputStream(target)) {
                copy(inputStream, file_output_stream);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying an input stream to file '" + target.getAbsolutePath() + "'.", e);
        }
    }

    /**
     * Copy the contents of a file to an OutputStream.
     *
     * @param source       the File to copy from; must not be null
     * @param outputStream the OutputStream to copy to; must not be null
     * @throws FileUtilsErrorException  if an error occurs while copying the file to the stream
     * @throws IllegalArgumentException if source or outputStream is null
     * @since 1.0
     */
    public static void copy(File source, OutputStream outputStream)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == outputStream) throw new IllegalArgumentException("outputStream can't be null.");

        try {
            try (var file_input_stream = new FileInputStream(source)) {
                copy(file_input_stream, outputStream);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying file '" + source.getAbsolutePath() + "' to an output stream.", e);
        }
    }

    /**
     * Copy the contents of one file to another file.
     *
     * @param source the File to copy from; must not be null
     * @param target the File to copy to; must not be null
     * @throws FileUtilsErrorException  if an error occurs while copying the source file to the target file
     * @throws IllegalArgumentException if source or target is null
     * @since 1.0
     */
    public static void copy(File source, File target)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        try {
            try (var file_input_stream = new FileInputStream(source);
                 var file_output_stream = new FileOutputStream(target)) {
                copy(file_input_stream, file_output_stream);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying file '" + source.getAbsolutePath() + "' to file '" + target.getAbsolutePath() + "'.", e);
        }
    }

    /**
     * Copies all files and directories from the source directory to the target directory.
     *
     * @param sourceDir the directory to be copied from
     * @param targetDir the directory to be copied to
     * @throws FileUtilsErrorException  if there's an error copying the files
     * @throws IllegalArgumentException if either sourceDir or targetDir are null
     * @since 1.0
     */
    public static void copyDirectory(File sourceDir, File targetDir)
    throws FileUtilsErrorException {
        if (null == sourceDir) throw new IllegalArgumentException("sourceDir can't be null.");
        if (null == targetDir) throw new IllegalArgumentException("targetDir can't be null.");

        try {
            var source_path = sourceDir.getAbsolutePath();
            Files.walk(Paths.get(source_path))
                .forEach(source -> {
                    var destination_name = source.toString().substring(source_path.length());
                    if (!destination_name.isEmpty()) {
                        Path destination = Paths.get(targetDir.getAbsolutePath(), destination_name);
                        try {
                            Files.copy(source, destination, REPLACE_EXISTING, COPY_ATTRIBUTES);
                        } catch (IOException e) {
                            throw new InnerClassException("Error while copying file '" + source.toFile().getAbsolutePath() + "' to file '" + destination.toFile().getAbsolutePath() + "'.", e);
                        }
                    }
                });
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying directory '" + sourceDir.getAbsolutePath() + "' to directory '" + targetDir.getAbsolutePath() + "'.", e);
        } catch (InnerClassException e) {
            throw new FileUtilsErrorException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Reads the complete contents of an input stream and returns it as a ByteArrayOutputStream object.
     *
     * @param inputStream the input stream to be read.
     * @return a ByteArrayOutputStream object containing the complete contents of the specified input stream.
     * @throws FileUtilsErrorException  if there is an error while reading the input stream.
     * @throws IllegalArgumentException if the inputStream parameter is null.
     * @since 1.0
     */
    public static ByteArrayOutputStream readStream(InputStream inputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        try {
            var buffer = new byte[1024];
            var return_value = -1;
            var output_stream = new ByteArrayOutputStream(buffer.length);

            return_value = inputStream.read(buffer);

            while (-1 != return_value) {
                output_stream.write(buffer, 0, return_value);
                return_value = inputStream.read(buffer);
            }

            output_stream.close();

            inputStream.close();

            return output_stream;
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading the complete contents of an input stream.", e);
        }
    }

    /**
     * Reads the entire contents of an InputStream and returns it as a String, using UTF-8 encoding.
     *
     * @param inputStream The InputStream to read the contents from.
     * @return A String containing the complete contents of the InputStream.
     * @throws FileUtilsErrorException  If there was an error while reading the InputStream.
     * @throws IllegalArgumentException If the inputStream is null.
     * @since 1.0
     */
    public static String readString(InputStream inputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        return readStream(inputStream).toString(StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire contents of a Reader and returns it as a String.
     *
     * @param reader The Reader to read the contents from.
     * @return A String containing the complete contents of the Reader.
     * @throws FileUtilsErrorException  If there was an error while reading the Reader.
     * @throws IllegalArgumentException If the reader is null.
     * @since 1.0
     */
    public static String readString(Reader reader)
    throws FileUtilsErrorException {
        if (null == reader) throw new IllegalArgumentException("reader can't be null.");

        try {
            var buffer = new char[1024];
            var result = new StringBuilder();

            var size = reader.read(buffer);
            while (size != -1) {
                result.append(buffer, 0, size);
                size = reader.read(buffer);
            }

            return result.toString();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading the complete contents of an reader.", e);
        }
    }

    /**
     * Reads the entire contents of an InputStream and returns it as a String, using the specified character encoding.
     *
     * @param inputStream The InputStream to read the contents from.
     * @param encoding    The name of the character encoding to use.
     * @return A String containing the complete contents of the InputStream.
     * @throws FileUtilsErrorException  If there was an error while reading the InputStream.
     * @throws IllegalArgumentException If the inputStream is null.
     * @since 1.0
     */
    public static String readString(InputStream inputStream, String encoding)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        try {
            return readStream(inputStream).toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new FileUtilsErrorException("Encoding '" + encoding + "' is not supported.", e);
        }
    }

    /**
     * Reads the content of the given URL into a string.
     *
     * @param source   the URL to read from
     * @param encoding the character encoding to use, or null to use the default encoding
     * @return the content of the URL as a string
     * @throws FileUtilsErrorException  if an error occurs while reading the URL
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static String readString(URL source, String encoding)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var connection = source.openConnection();
            connection.setUseCaches(false);
            try (var input_stream = connection.getInputStream()) {
                if (null == encoding) {
                    return readString(input_stream);
                } else {
                    return readString(input_stream, encoding);
                }
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source + ".", e);
        }
    }

    /**
     * Reads the content of the given file into a string using the default encoding.
     *
     * @param source the file to read from
     * @return the content of the file as a string
     * @throws FileUtilsErrorException  if an error occurs while reading the file
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static String readString(File source)
    throws FileUtilsErrorException {
        return readString(source, null);
    }

    /**
     * Reads the content of the given file into a string using the specified character encoding.
     *
     * @param source   the file to read from
     * @param encoding the character encoding to use, or null to use the default encoding
     * @return the content of the file as a string
     * @throws FileUtilsErrorException  if an error occurs while reading the file
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static String readString(File source, String encoding)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            try (var file_input_stream = new FileInputStream(source)) {
                if (null == encoding) {
                    return readString(file_input_stream);
                } else {
                    return readString(file_input_stream, encoding);
                }
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source.getAbsolutePath() + ".", e);
        }
    }

    /**
     * Reads all characters from the {@link java.net.URL} and returns its contents as a string.
     *
     * @param source the URL to read from
     * @return a string containing all the characters read from the URL
     * @throws FileUtilsErrorException  if an error occurs while reading the URL
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static String readString(URL source)
    throws FileUtilsErrorException {
        return readString(source, null);
    }

    /**
     * Reads all the bytes from the {@link java.io.InputStream}.
     *
     * @param inputStream the inputStream to read from
     * @return a byte array containing all the bytes read from the input stream
     * @throws FileUtilsErrorException  if an error occurs while reading the input stream
     * @throws IllegalArgumentException if inputStream is null
     * @since 1.0
     */
    public static byte[] readBytes(InputStream inputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        return readStream(inputStream).toByteArray();
    }

    /**
     * Reads all the bytes from the file denoted by the specified {@link java.io.File} object.
     *
     * @param source the file to read from
     * @return a byte array containing all the bytes read from the file
     * @throws FileUtilsErrorException  if an error occurs while reading the file
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static byte[] readBytes(URL source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var connection = source.openConnection();
            connection.setUseCaches(false);
            try (var input_stream = connection.getInputStream()) {
                return readBytes(input_stream);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source + ".", e);
        }
    }

    /**
     * Reads all the bytes from the {@link java.net.URL}.
     *
     * @param source the URL to read from
     * @return a byte array containing all the bytes read from the URL
     * @throws FileUtilsErrorException  if an error occurs while reading the URL
     * @throws IllegalArgumentException if source is null
     * @since 1.0
     */
    public static byte[] readBytes(File source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            try (var file_input_stream = new FileInputStream(source)) {
                return readBytes(file_input_stream);
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading file '" + source.getAbsolutePath() + ".", e);
        }
    }

    /**
     * Writes a byte array to a given file.
     *
     * @param content     the byte array to be written
     * @param destination the file to which the byte array should be written
     * @throws FileUtilsErrorException  if there is an error writing the byte array to the file
     * @throws IllegalArgumentException if either the content or destination arguments are null
     * @since 1.0
     */
    public static void writeBytes(byte[] content, File destination)
    throws FileUtilsErrorException {
        if (null == content) throw new IllegalArgumentException("content can't be null.");
        if (null == destination) throw new IllegalArgumentException("destination can't be null.");

        try {
            try (var file_output_stream = new FileOutputStream(destination)) {
                file_output_stream.write(content);
                file_output_stream.flush();
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while write a string to '" + destination.getAbsolutePath() + ".", e);
        }
    }

    /**
     * Writes a string to a given file.
     *
     * @param content     the string to be written
     * @param destination the file to which the string should be written
     * @throws FileUtilsErrorException  if there is an error writing the string to the file
     * @throws IllegalArgumentException if either the content or destination arguments are null
     * @since 1.0
     */
    public static void writeString(String content, File destination)
    throws FileUtilsErrorException {
        if (null == content) throw new IllegalArgumentException("content can't be null.");
        if (null == destination) throw new IllegalArgumentException("destination can't be null.");

        try {
            try (var file_writer = new FileWriter(destination)) {
                file_writer.write(content, 0, content.length());
                file_writer.flush();
            }
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while write a string to '" + destination.getAbsolutePath() + ".", e);
        }
    }

    /**
     * Deletes a given file.
     *
     * @param file The file to be deleted.
     * @throws IllegalArgumentException if file is null.
     * @since 1.0
     */
    public static void deleteFile(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        file.delete();
    }

    /**
     * Generates a unique filename.
     *
     * @return A unique filename string.
     * @since 1.0
     */
    public static String getUniqueFilename() {
        var current_date = new Date();

        return current_date.getTime() + "-" + (long) (1000000 * Math.random());
    }

    /**
     * Unzips a file from the source location to a destination.
     *
     * @param source      the file to unzip (must not be null)
     * @param destination the location to unzip the file to (must not be null)
     * @throws FileUtilsErrorException if an error occurs while unzipping the file or creating directories
     * @since 1.0
     */
    public static void unzipFile(File source, File destination)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == destination) throw new IllegalArgumentException("destination can't be null.");

        ZipFile zip_file = null;
        Enumeration entries = null;

        try {
            zip_file = new ZipFile(source);
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while creating the zipfile '" + source.getAbsolutePath() + "'.", e);
        }
        entries = zip_file.entries();

        var buffer = new byte[1024];
        while (entries.hasMoreElements()) {
            ZipEntry entry = null;
            InputStream input_stream = null;
            String output_filename = null;
            File output_file = null;
            StringBuilder output_file_directory_name = null;
            File output_file_directory = null;
            FileOutputStream file_output_stream = null;
            var count = -1;

            entry = (ZipEntry) entries.nextElement();
            try {
                input_stream = zip_file.getInputStream(entry);
            } catch (IOException e) {
                throw new FileUtilsErrorException("Error while obtaining the input stream for entry '" + entry.getName() + "'.", e);
            }

            output_filename = destination.getAbsolutePath() + File.separator + entry.getName().replace('/', File.separatorChar);
            output_file = new File(output_filename);
            if (entry.isDirectory()) {
                output_file_directory = new File(output_filename);
                if (!output_file_directory.exists()) {
                    if (!output_file_directory.mkdirs()) {
                        throw new FileUtilsErrorException("Couldn't create directory '" + output_file_directory.getAbsolutePath() + "' and its parents.");
                    }
                } else {
                    if (!output_file_directory.isDirectory()) {
                        throw new FileUtilsErrorException("Destination '" + output_file_directory.getAbsolutePath() + "' exists and is not a directory.");
                    }
                }
            } else {
                output_file_directory_name = new StringBuilder(output_file.getPath());
                output_file_directory_name.setLength(output_file_directory_name.length() - output_file.getName().length() - File.separator.length());
                output_file_directory = new File(output_file_directory_name.toString());
                if (!output_file_directory.exists()) {
                    if (!output_file_directory.mkdirs()) {
                        throw new FileUtilsErrorException("Couldn't create directory '" + output_file_directory.getAbsolutePath() + "' and its parents.");
                    }
                } else {
                    if (!output_file_directory.isDirectory()) {
                        throw new FileUtilsErrorException("Destination '" + output_file_directory.getAbsolutePath() + "' exists and is not a directory.");
                    }
                }

                try {
                    file_output_stream = new FileOutputStream(output_filename);
                } catch (IOException e) {
                    throw new FileUtilsErrorException("Error while creating the output stream for file '" + output_filename + "'.", e);
                }

                try {
                    while ((count = input_stream.read(buffer)) != -1) {
                        file_output_stream.write(buffer, 0, count);
                    }
                } catch (IOException e) {
                    throw new FileUtilsErrorException("Error while uncompressing entry '" + output_filename + "'.", e);
                }

                try {
                    file_output_stream.close();
                } catch (IOException e) {
                    throw new FileUtilsErrorException("Error while closing the output stream for file '" + output_filename + "'.", e);
                }
                try {
                    input_stream.close();
                } catch (IOException e) {
                    throw new FileUtilsErrorException("Error while closing the input stream for entry '" + entry.getName() + "'.", e);
                }
            }
        }

        try {
            zip_file.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while closing the zip file '" + source.getAbsolutePath() + "'.", e);
        }
    }

    /**
     * Returns the base name of the specified file, without the extension.
     *
     * @param file The file whose base name is to be returned.
     * @return The base name of the file.
     * @since 1.0
     */
    public static String getBaseName(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        return getBaseName(file.getName());
    }

    /**
     * Returns the base name of the specified file name, without the extension.
     *
     * @param fileName The name of the file whose base name is to be returned.
     * @return The base name of the file.
     * @throws IllegalArgumentException if fileName is null.
     * @since 1.0
     */
    public static String getBaseName(String fileName) {
        if (null == fileName) throw new IllegalArgumentException("fileName can't be null.");

        var basename = fileName;

        var index = fileName.lastIndexOf('.');
        if (index >= 0) {
            basename = fileName.substring(0, index);
        }

        return basename;
    }

    /**
     * Returns the extension of the specified {@link File}.
     *
     * @param file the file to get the extension from
     * @return the extension of the given file or null if the file has no extension.
     * @throws IllegalArgumentException if file is null
     * @since 1.0
     */
    public static String getExtension(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        return getExtension(file.getName());
    }

    /**
     * Returns the extension of the specified file name.
     *
     * @param fileName the name of the file to get the extension from
     * @return the extension of the given file name or null if the file name has no extension.
     * @throws IllegalArgumentException if fileName is null
     * @since 1.0
     */
    public static String getExtension(String fileName) {
        if (null == fileName) throw new IllegalArgumentException("fileName can't be null.");

        String ext = null;

        var index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            ext = fileName.substring(index + 1).toLowerCase();
        }

        return ext;
    }

    /**
     * Combines an arbitrary number of lists of files into a single list of absolute file paths.
     *
     * @param files zero or more lists of files to be combined
     * @return the list of absolute file paths resulting from combining the input lists
     * @since 1.5.2
     */
    @SafeVarargs
    public static List<String> combineToAbsolutePaths(List<File>... files) {
        if (files == null) {
            return Collections.emptyList();
        }
        var result = new ArrayList<String>();
        for (var list : files) {
            for (var file : list) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    /**
     * Generates a sorted directory listing of all files and subdirectories in the specified directory.
     *
     * @param directory The root directory to list.
     * @return A string containing a sorted directory listing of all files and subdirectories in the specified directory.
     * @throws IOException If an error occurs while listing the directory.
     * @since 1.5.0
     */
    public static String generateDirectoryListing(File directory)
    throws IOException {
        return Files.walk(Path.of(directory.getAbsolutePath()))
            .map(path -> path.toAbsolutePath().toString().substring(directory.getAbsolutePath().length()))
            .filter(s -> !s.isEmpty())
            .sorted()
            .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Joins a list of file paths into a single string with the file path separator.
     *
     * @param paths A list of file paths to join.
     * @return A single string consisting of all the input file paths separated by the file path separator, or an empty string if input list is null.
     * @since 1.5.2
     */
    public static String joinPaths(List<String> paths) {
        if (paths == null) {
            return "";
        }

        return String.join(File.pathSeparator, paths);
    }

    /**
     * Returns a list of Java files in the specified directory.
     *
     * @param directory The directory to search for Java files.
     * @return A list of File objects pointing to the Java files in the directory, or an empty list if input directory is null.
     * @since 1.5.10
     */
    public static List<File> getJavaFileList(File directory) {
        if (directory == null) {
            return Collections.emptyList();
        }

        var dir_abs = directory.getAbsoluteFile();
        return FileUtils.getFileList(dir_abs, JAVA_FILE_PATTERN, null)
            .stream().map(file -> new File(dir_abs, file)).toList();
    }

    /**
     * Transforms all files in the specified directory using the provided transformation function.
     *
     * @param directory   The directory containing the files to be transformed.
     * @param transformer The transformation function to be applied to each file.
     * @throws FileUtilsErrorException If there is an error while reading or writing a file.
     * @since 1.5.10
     */
    public static void transformFiles(File directory, Function<String, String> transformer)
    throws FileUtilsErrorException {
        transformFiles(directory, null, (Pattern[]) null, transformer);
    }

    /**
     * Transforms files in the specified directory that match the included pattern and do not match the excluded pattern
     * using the provided transformation function.
     *
     * @param directory   The directory containing the files to be transformed.
     * @param included    A regex pattern specifying which files to include.
     * @param excluded    A regex pattern specifying which files to exclude.
     * @param transformer The transformation function to be applied to each file.
     * @throws FileUtilsErrorException If there is an error while reading or writing a file.
     * @since 1.5.10
     */
    public static void transformFiles(File directory, Pattern included, Pattern excluded, Function<String, String> transformer)
    throws FileUtilsErrorException {
        transformFiles(directory, new Pattern[]{included}, new Pattern[]{excluded}, transformer);
    }

    /**
     * Transforms files in the specified directory that match at least one of the included patterns and do not match any
     * of the excluded patterns using the provided transformation function.
     *
     * @param directory   The directory containing the files to be transformed.
     * @param included    A list of regex patterns specifying which files to include.
     * @param excluded    A list of regex patterns specifying which files to exclude.
     * @param transformer The transformation function to be applied to each file.
     * @throws FileUtilsErrorException If there is an error while reading or writing a file.
     * @since 1.5.10
     */
    public static void transformFiles(File directory, List<Pattern> included, List<Pattern> excluded, Function<String, String> transformer)
    throws FileUtilsErrorException {
        transformFiles(directory, included.toArray(new Pattern[0]), excluded.toArray(new Pattern[0]), transformer);
    }

    /**
     * Transforms files in the specified directory that match at least one of the included patterns and do not match any
     * of the excluded patterns using the provided transformation function.
     *
     * @param directory   The directory containing the files to be transformed.
     * @param included    An array of regex patterns specifying which files to include.
     * @param excluded    An array of regex patterns specifying which files to exclude.
     * @param transformer The transformation function to be applied to each file.
     * @throws FileUtilsErrorException If there is an error while reading or writing a file.
     * @since 1.5.10
     */
    public static void transformFiles(File directory, Pattern[] included, Pattern[] excluded, Function<String, String> transformer)
    throws FileUtilsErrorException {
        for (var entry : FileUtils.getFileList(directory, included, excluded)) {
            var file = new File(directory, entry);
            var contents = FileUtils.readString(file);
            var transformed = transformer.apply(contents);
            if (!transformed.equals(contents)) {
                FileUtils.writeString(transformed, file);
            }
        }
    }

    private static final int S_IRUSR = 0000400;
    private static final int S_IWUSR = 0000200;
    private static final int S_IXUSR = 0000100;
    private static final int S_IRGRP = 0000040;
    private static final int S_IWGRP = 0000020;
    private static final int S_IXGRP = 0000010;
    private static final int S_IROTH = 0000004;
    private static final int S_IWOTH = 0000002;
    private static final int S_IXOTH = 0000001;

    /**
     * Creates a new set of {@code PosixFilePermission} based on the given Posix mode.
     * <p>
     * Standard Posix permissions can be provided on octal numbers, for instance {@code 0755} and {@code 0644}.
     *
     * @param mode an integer containing permissions for the owner, group and others
     * @return a Set of PosixFilePermission
     * @since 1.5.19
     */
    public static Set<PosixFilePermission> permissionsFromMode(int mode) {
        var perms = new HashSet<PosixFilePermission>();
        if ((mode & S_IRUSR) != 0) perms.add(OWNER_READ);
        if ((mode & S_IWUSR) != 0) perms.add(OWNER_WRITE);
        if ((mode & S_IXUSR) != 0) perms.add(OWNER_EXECUTE);
        if ((mode & S_IRGRP) != 0) perms.add(GROUP_READ);
        if ((mode & S_IWGRP) != 0) perms.add(GROUP_WRITE);
        if ((mode & S_IXGRP) != 0) perms.add(GROUP_EXECUTE);
        if ((mode & S_IROTH) != 0) perms.add(OTHERS_READ);
        if ((mode & S_IWOTH) != 0) perms.add(OTHERS_WRITE);
        if ((mode & S_IXOTH) != 0) perms.add(OTHERS_EXECUTE);
        return perms;
    }

    /**
     * Constructs a new {@code Path} instance using the specified file and additional path elements.
     *
     * @param file  The file to use as the starting point for the Path.
     * @param paths Additional path elements to add to the Path.
     * @return A new Path instance.
     * @since 1.5.19
     */
    public static Path path(File file, String... paths) {
        return Path.of(file.getAbsolutePath(), paths);
    }

    /**
     * Constructs a new {@code Path} instance using the specified file and additional path elements.
     *
     * @param path  The path to use as the starting point for the Path.
     * @param paths Additional path elements to add to the Path.
     * @return A new Path instance.
     * @since 1.5.19
     */
    public static Path path(Path path, String... paths) {
        return Path.of(path.toAbsolutePath().toString(), paths);
    }
}
