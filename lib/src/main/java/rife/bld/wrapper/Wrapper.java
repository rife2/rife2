/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.wrapper;

import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;

import static rife.tools.FileUtils.JAR_FILE_PATTERN;

/**
 * Wrapper implementation for the build system that ensures the RIFE2
 * jar gets downloaded locally and that the classpath for running the
 * build logic is properly setup.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
public class Wrapper {
    private static final String DOWNLOAD_LOCATION = "https://repo1.maven.org/maven2/com/uwyn/rife2/rife2/${version}/";
    private static final String RIFE2_FILENAME = "rife2-${version}.jar";
    private static final String RIFE_VERSION = "RIFE_VERSION";
    private static final String WRAPPER_PROPERTIES = "bld-wrapper.properties";
    private static final String WRAPPER_JAR = "bld-wrapper.jar";
    private static final String PROPERTY_VERSION = "rife2.version";
    private static final String PROPERTY_DOWNLOAD_LOCATION = "rife2.downloadLocation";
    private static final File USER_DIR = new File(System.getProperty("user.home"), ".rife2");
    private static final File DISTRIBUTIONS_DIR = new File(USER_DIR, "dist");

    private File currentDir_ = new File(System.getProperty("user.dir"));
    private final Properties wrapperProperties_ = new Properties();
    private final byte[] buffer_ = new byte[1024];

    /**
     * Launches the wrapper.
     *
     * @param arguments the command line arguments to pass on to the build logic
     * @since 1.5
     */
    public static void main(String[] arguments) {
        System.exit(new Wrapper().installAndLaunch(new ArrayList<>(Arrays.asList(arguments))));
    }

    /**
     * Creates the files required to use the wrapper.
     *
     * @param destinationDirectory the directory to put those files in
     * @param version              the RIFE2 version they should be using
     * @throws IOException when an error occurred during the creation of the wrapper files
     * @since 1.5
     */
    public void createWrapperFiles(File destinationDirectory, String version)
    throws IOException {
        createWrapperProperties(destinationDirectory, version);
        createWrapperJar(destinationDirectory);
    }

    private void createWrapperProperties(File destinationDirectory, String version)
    throws IOException {
        var file = new File(destinationDirectory, WRAPPER_PROPERTIES);
        if (file.exists()) {
            wrapperProperties_.load(new FileReader(file));
        }
        wrapperProperties_.put(PROPERTY_VERSION, version);
        Files.createDirectories(file.getAbsoluteFile().toPath().getParent());
        Files.deleteIfExists(file.toPath());
        wrapperProperties_.store(new FileWriter(file), null);
    }

    private void createWrapperJar(File destinationDirectory)
    throws IOException {
        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, getClass().getName());

        try (var jar = new JarOutputStream(new FileOutputStream(new File(destinationDirectory, WRAPPER_JAR)), manifest)) {
            addFileToJar(jar, Wrapper.class.getName().replace('.', '/') + ".class");
            addFileToJar(jar, FileUtils.class.getName().replace('.', '/') + ".class");
            addFileToJar(jar, FileUtilsErrorException.class.getName().replace('.', '/') + ".class");
            addFileToJar(jar, InnerClassException.class.getName().replace('.', '/') + ".class");
            addFileToJar(jar, RIFE_VERSION);
            jar.flush();
        }
    }

    private void addFileToJar(JarOutputStream jar, String name)
    throws IOException {
        var resource = getClass().getResource("/" + name);
        if (resource == null) {
            throw new IOException("Couldn't find resource '" + name + "'");
        }

        InputStream stream = null;
        try {
            var connection = resource.openConnection();
            connection.setUseCaches(false);
            stream = connection.getInputStream();
            var entry = new JarEntry(name);
            jar.putNextEntry(entry);

            try (var in = new BufferedInputStream(stream)) {
                int count;
                while ((count = in.read(buffer_)) != -1) {
                    jar.write(buffer_, 0, count);
                }
                jar.closeEntry();
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // couldn't close stream since it probably already has been
                    // closed after an exception
                    // proceed without reporting an error message.
                }
            }
        }
    }

    private String getVersion()
    throws IOException {
        try (InputStream in = getClass().getResource("/" + RIFE_VERSION).openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private int installAndLaunch(List<String> arguments) {
        if (!arguments.isEmpty()) {
            File current_file = null;
            try {
                current_file = new File(arguments.remove(0)).getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            currentDir_ = new File(current_file.getParent());
        }
        try {
            initWrapperProperties(getVersion());
            File distribution;
            try {
                distribution = installDistribution();
            } catch (IOException e) {
                return -1;
            }
            return launchMain(distribution, arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path getLibBldPath() {
        return Path.of(currentDir_.getAbsolutePath(), "lib", "bld");
    }

    private Path getLibPath(String path) {
        return Path.of(currentDir_.getAbsolutePath(), "..", "lib", path);
    }

    private Path getLibBldPath(String path) {
        return Path.of(currentDir_.getAbsolutePath(), "lib", "bld", path);
    }

    private void initWrapperProperties(String version)
    throws IOException {
        wrapperProperties_.put(PROPERTY_VERSION, version);
        wrapperProperties_.put(PROPERTY_DOWNLOAD_LOCATION, DOWNLOAD_LOCATION);
        var config = getLibBldPath(WRAPPER_PROPERTIES).toFile();
        if (config.exists()) {
            wrapperProperties_.load(new FileReader(config));
        } else {
            config = getLibPath(WRAPPER_PROPERTIES).toFile();
            if (config.exists()) {
                wrapperProperties_.load(new FileReader(config));
            }
        }
    }

    private String getWrapperVersion()
    throws IOException {
        return wrapperProperties_.getProperty(PROPERTY_VERSION, getVersion());
    }

    private String getWrapperDownloadLocation() {
        return wrapperProperties_.getProperty(PROPERTY_DOWNLOAD_LOCATION, DOWNLOAD_LOCATION);
    }

    private String downloadUrl(String version) {
        var location = replaceVersion(getWrapperDownloadLocation(), version);
        var result = new StringBuilder(location);
        if (!location.endsWith("/")) {
            result.append("/");
        }
        result.append(rife2FileName(version));
        return result.toString();
    }

    private String rife2FileName(String version) {
        return replaceVersion(RIFE2_FILENAME, version);
    }

    private String replaceVersion(String text, String version) {
        return text.replaceAll("\\$\\{version}", version);
    }

    private File installDistribution()
    throws IOException {
        try {
            Files.createDirectories(DISTRIBUTIONS_DIR.toPath());
        } catch (IOException e) {
            System.err.println("Failed to create distribution directory " + DISTRIBUTIONS_DIR.getAbsolutePath() + ".");
            throw e;
        }

        String version;
        try {
            version = getWrapperVersion();
        } catch (IOException e) {
            System.err.println("Failed to retrieve wrapper version number.");
            throw e;
        }
        var distribution_file = new File(DISTRIBUTIONS_DIR, rife2FileName(version));
        if (!distribution_file.exists()) {
            download(distribution_file, version);
        }
        return distribution_file;
    }

    private int launchMain(File jarFile, List<String> arguments)
    throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();
        args.add("java");

        // jvm parameters must go before -jar
        var i = arguments.iterator();
        while (i.hasNext()) {
            var arg = i.next();
            if (arg.matches("-D(.+?)=(.*)")) {
                args.add(arg);
                i.remove();
            }
        }

        args.add("-cp");
        var bld_classpath = bldClasspathJars();
        bld_classpath.add(jarFile);
        args.add(FileUtils.joinPaths(FileUtils.combineToAbsolutePaths(bld_classpath)));

        if (arguments.isEmpty() || !arguments.get(0).endsWith(".java")) {
            args.add("-jar");
            args.add(jarFile.getAbsolutePath());
        }

        args.addAll(arguments);

        var process_builder = new ProcessBuilder(args);
        process_builder.inheritIO();
        var process = process_builder.start();
        return process.waitFor();
    }

    private List<File> bldClasspathJars() {
        // detect the jar files in the compile lib directory
        var dir_abs = getLibBldPath().toFile().getAbsoluteFile();
        var jar_files = FileUtils.getFileList(dir_abs, JAR_FILE_PATTERN, Pattern.compile(WRAPPER_JAR));

        // build the compilation classpath
        return new ArrayList<>(jar_files.stream().map(file -> new File(dir_abs, file)).toList());
    }

    private void download(File file, String version)
    throws IOException {
        var download_url = downloadUrl(version);

        try {
            System.out.print("Downloading: " + download_url + " ... ");
            System.out.flush();
            var url = new URL(download_url);
            var readableByteChannel = Channels.newChannel(url.openStream());
            try (var fileOutputStream = new FileOutputStream(file)) {
                var fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                System.out.print("done");
            }
        } catch (FileNotFoundException e) {
            System.err.println("not found");
            System.err.println("Failed to download file " + file + ".");
            throw e;
        } catch (IOException e) {
            System.err.println("error");
            System.err.println("Failed to download file " + file + " due to I/O issue.");
            Files.deleteIfExists(file.toPath());
            throw e;
        } finally {
            System.out.println();
        }
    }
}
