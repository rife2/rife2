/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class FileUtils {
    public static final Pattern JAVA_FILE_PATTERN = Pattern.compile("^.*\\.java$");
    public static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.*\\.jar$");

    private FileUtils() {
        // no-op
    }

    public static List<String> getFileList(File file) {
        return getFileList(file, null, null, true);
    }

    public static List<String> getFileList(File file, Pattern included, Pattern excluded) {
        return getFileList(file, new Pattern[]{included}, new Pattern[]{excluded}, true);
    }

    public static List<String> getFileList(File file, List<Pattern> included, List<Pattern> excluded) {
        var included_array = new Pattern[included.size()];
        var excluded_array = new Pattern[excluded.size()];
        included.toArray(included_array);
        excluded.toArray(excluded_array);
        return getFileList(file, included_array, excluded_array, true);
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

    public static void moveFile(File source, File target)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        if (!source.exists()) {
            throw new FileUtilsErrorException("The source file '" + source.getAbsolutePath() + "' does not exist.");
        }

        // copy
        copy(source, target);

        // then delete sourcefile
        deleteFile(source);
    }


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

    public static void deleteDirectory(File source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        if (!source.exists()) {
            throw new FileUtilsErrorException("The directory '" + source.getAbsolutePath() + "' does not exist");
        }

        var file_list = source.list();

        assert file_list != null;
        for (var element : file_list) {
            var current_file = new File(source.getAbsolutePath() + File.separator + element);

            if (current_file.isDirectory()) {
                deleteDirectory(current_file);
            } else {
                deleteFile(current_file);
            }
        }

        // If we get here it means we're finished with this directory ... delete it.
        deleteFile(source);
    }

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

    public static void copy(InputStream inputStream, File target)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        try {
            var file_output_stream = new FileOutputStream(target);

            copy(inputStream, file_output_stream);

            file_output_stream.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying an input stream to file '" + target.getAbsolutePath() + "'.", e);
        }
    }

    public static void copy(File source, OutputStream outputStream)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == outputStream) throw new IllegalArgumentException("outputStream can't be null.");

        try {
            var file_input_stream = new FileInputStream(source);

            copy(file_input_stream, outputStream);

            file_input_stream.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying file '" + source.getAbsolutePath() + "' to an output stream.", e);
        }
    }

    public static void copy(File source, File target)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");
        if (null == target) throw new IllegalArgumentException("target can't be null.");

        try {
            var file_input_stream = new FileInputStream(source);
            var file_output_stream = new FileOutputStream(target);

            copy(file_input_stream, file_output_stream);

            file_output_stream.close();
            file_input_stream.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while copying file '" + source.getAbsolutePath() + "' to file '" + target.getAbsolutePath() + "'.", e);
        }
    }

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

    public static String readString(InputStream inputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        return readStream(inputStream).toString();
    }

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

    public static String readString(InputStream inputStream, String encoding)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        try {
            return readStream(inputStream).toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new FileUtilsErrorException("Encoding '" + encoding + "' is not supported.", e);
        }
    }

    public static byte[] readBytes(InputStream inputStream)
    throws FileUtilsErrorException {
        if (null == inputStream) throw new IllegalArgumentException("inputStream can't be null.");

        return readStream(inputStream).toByteArray();
    }

    public static String readString(URL source)
    throws FileUtilsErrorException {
        return readString(source, null);
    }

    public static String readString(URL source, String encoding)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var connection = source.openConnection();
            connection.setUseCaches(false);
            var input_stream = connection.getInputStream();
            String content = null;
            if (null == encoding) {
                content = readString(input_stream);
            } else {
                content = readString(input_stream, encoding);
            }
            input_stream.close();
            return content;
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source + ".", e);
        }
    }

    public static byte[] readBytes(URL source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var connection = source.openConnection();
            connection.setUseCaches(false);
            var input_stream = connection.getInputStream();
            var content = readBytes(input_stream);
            input_stream.close();
            return content;
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source + ".", e);
        }
    }

    public static String readString(File source)
    throws FileUtilsErrorException {
        return readString(source, null);
    }

    public static String readString(File source, String encoding)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var file_input_stream = new FileInputStream(source);
            String content = null;
            if (null == encoding) {
                content = readString(file_input_stream);
            } else {
                content = readString(file_input_stream, encoding);
            }
            file_input_stream.close();
            return content;
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading url '" + source.getAbsolutePath() + ".", e);
        }
    }

    public static byte[] readBytes(File source)
    throws FileUtilsErrorException {
        if (null == source) throw new IllegalArgumentException("source can't be null.");

        try {
            var file_input_stream = new FileInputStream(source);
            var content = readBytes(file_input_stream);
            file_input_stream.close();
            return content;
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while reading file '" + source.getAbsolutePath() + ".", e);
        }
    }

    public static void writeBytes(byte[] content, File destination)
    throws FileUtilsErrorException {
        if (null == content) throw new IllegalArgumentException("content can't be null.");
        if (null == destination) throw new IllegalArgumentException("destination can't be null.");

        try {
            var file_output_stream = new FileOutputStream(destination);
            file_output_stream.write(content);
            file_output_stream.flush();
            file_output_stream.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while write a string to '" + destination.getAbsolutePath() + ".", e);
        }
    }

    public static void writeString(String content, File destination)
    throws FileUtilsErrorException {
        if (null == content) throw new IllegalArgumentException("content can't be null.");
        if (null == destination) throw new IllegalArgumentException("destination can't be null.");

        try {
            var file_writer = new FileWriter(destination);
            file_writer.write(content, 0, content.length());
            file_writer.flush();
            file_writer.close();
        } catch (IOException e) {
            throw new FileUtilsErrorException("Error while write a string to '" + destination.getAbsolutePath() + ".", e);
        }
    }

    public static void deleteFile(File file) {
        if (null == file) throw new IllegalArgumentException("file can't be null.");

        file.delete();
    }

    public static String getUniqueFilename() {
        var current_date = new Date();

        return current_date.getTime() + "-" + (long) (1000000 * Math.random());
    }

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

    public static String getBaseName(File file) {
        return getBaseName(file.getName());
    }

    public static String getBaseName(String fileName) {
        if (null == fileName) throw new IllegalArgumentException("fileName can't be null.");

        String basename = null;

        var index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            basename = fileName.substring(0, index);
        }

        return basename;
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    public static String getExtension(String fileName) {
        if (null == fileName) throw new IllegalArgumentException("fileName can't be null.");

        String ext = null;

        var index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            ext = fileName.substring(index + 1).toLowerCase();
        }

        return ext;
    }

    @SafeVarargs
    public static List<String> combineToAbsolutePaths(List<File>... files) {
        var result = new ArrayList<String>();
        for (var list : files) {
            for (var file : list) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    public static String generateDirectoryListing(File directory)
    throws IOException {
        return Files.walk(Path.of(directory.getAbsolutePath()))
            .map(path -> path.toAbsolutePath().toString().substring(directory.getAbsolutePath().length()))
            .filter(s -> !s.isEmpty())
            .sorted()
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String joinPaths(List<String> paths) {
        return String.join(File.pathSeparator, paths);
    }
}
