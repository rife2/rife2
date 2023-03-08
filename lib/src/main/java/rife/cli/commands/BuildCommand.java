/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cli.commands;

import rife.cli.CliCommand;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class BuildCommand implements CliCommand {
    public static final String NAME = "build";

    private final List<String> arguments_;

    public BuildCommand(List<String> arguments) {
        arguments_ = arguments;
    }

    public boolean execute()
    throws Exception {
        if (arguments_.size() != 0) {
            System.err.println("ERROR: No arguments are expected for the build command.");
            System.err.println();
            return false;
        }

        // create the project directories
        var src_main_java_dir =
            Path.of("src", "main", "java").toFile();
        var src_test_java_dir =
            Path.of("src", "test", "java").toFile();
        var lib_dir =
            Path.of("lib").toFile();
        var build_main_dir =
            Path.of("build", "main").toFile();
        var build_test_dir =
            Path.of("build", "test").toFile();

        build_main_dir.mkdirs();
        build_test_dir.mkdirs();

        // detect the jar files in the lib directory
        var lib_dir_abs = lib_dir.getAbsoluteFile();
        var lib_jar_files = FileUtils.getFileList(lib_dir_abs, Pattern.compile("^.*\\.jar$"), null);

        // get all the main java sources
        var src_main_java_dir_abs = src_main_java_dir.getAbsoluteFile();
        var main_java_files = FileUtils.getFileList(src_main_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_main_java_dir_abs, file)).toList();

        // get the main output path
        var main_build_path = build_main_dir.getAbsolutePath();

        // get all the test java sources
        var src_test_java_dir_abs = src_test_java_dir.getAbsoluteFile();
        var test_java_files = FileUtils.getFileList(src_test_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_test_java_dir_abs, file)).toList();

        // get the test output path
        var build_test_path = build_test_dir.getAbsolutePath();

        // build the classpath
        var classpath_paths = new ArrayList<>(lib_jar_files.stream().map(file -> new File(lib_dir_abs, file).getAbsolutePath()).toList());
        classpath_paths.add(0, main_build_path);
        var classpath = StringUtils.join(classpath_paths, File.pathSeparator);

        // compile both the main and the test java sources
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var file_manager = compiler.getStandardFileManager(null, null, null)) {
            buildProjectSources(compiler, file_manager, classpath, main_java_files, main_build_path);
            buildProjectSources(compiler, file_manager, classpath, test_java_files, build_test_path);
        }

        return true;
    }

    private static void buildProjectSources(JavaCompiler compiler, StandardJavaFileManager fileManager, String classpath, List<File> sources, String destination)
    throws IOException {
        var compilation_units = fileManager.getJavaFileObjectsFromFiles(sources);
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var options = List.of("-d", destination, "-cp", classpath);
        var compilation_task = compiler.getTask(null, fileManager, diagnostics, options, null, compilation_units);
        if (!compilation_task.call()) {
            outputDiagnostics(diagnostics);
        }
    }

    private static void outputDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics)
    throws IOException {
        for (var diagnostic : diagnostics.getDiagnostics()) {
            var source = diagnostic.getSource().getCharContent(true).toString();
            var lines = StringUtils.split(source, "\n");
            var message = diagnostic.getMessage(Locale.getDefault());
            var message_lines = StringUtils.split(message, "\n");
            var main_message = "";
            var remaining_message = "";
            int line_number = (int) diagnostic.getLineNumber() - 1;
            int column_number = (int) diagnostic.getColumnNumber() - 1;

            if (!message_lines.isEmpty()) {
                main_message = message_lines.remove(0);
                remaining_message = StringUtils.join(message_lines, "\n");
            }
            System.out.format("%s:%d: %s: %s%n",
                diagnostic.getSource().toUri().getPath(),
                diagnostic.getLineNumber(),
                diagnostic.getKind().name().toLowerCase(),
                main_message);

            if (line_number >= 0 && line_number < lines.size()) {
                var line = lines.get(line_number);
                System.out.println(line);
                if (column_number >= 0 && column_number < line.length()) {
                    System.out.println(StringUtils.repeat(" ", column_number) + "^");
                }
            }
            if (!remaining_message.isEmpty()) {
                System.out.println(remaining_message);
            }
        }
    }

    public String getHelp() {
        return """
            Builds a new RIFE2 project.""";
    }
}
