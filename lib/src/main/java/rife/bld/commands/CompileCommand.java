/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.bld.commands.exceptions.CommandCreationException;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CompileCommand {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Compiles a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Compiles a RIFE2 application.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private final Project project_;

    public CompileCommand(Project project) {
        this(project, null);
    }

    public CompileCommand(Project project, List<String> arguments) {
        if (arguments != null && arguments.size() != 0) {
            throw new CommandCreationException("ERROR: No arguments are expected for compilation.");
        }

        project_ = project;
    }

    public void execute()
    throws Exception {
        project_.buildMainDirectory().mkdirs();
        project_.buildProjectDirectory().mkdirs();
        project_.buildTestDirectory().mkdirs();

        // detect the jar files in the compile lib directory
        var lib_compile_dir_abs = project_.libCompileDirectory().getAbsoluteFile();
        var lib_compile_jar_files = FileUtils.getFileList(lib_compile_dir_abs, Pattern.compile("^.*\\.jar$"), null);

        // detect the jar files in the test lib directory
        var lib_test_dir_abs = project_.libTestDirectory().getAbsoluteFile();
        var lib_test_jar_files = FileUtils.getFileList(lib_test_dir_abs, Pattern.compile("^.*\\.jar$"), null);

        // get all the main java sources
        var src_main_java_dir_abs = project_.srcMainJavaDirectory().getAbsoluteFile();
        var main_java_files = FileUtils.getFileList(src_main_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_main_java_dir_abs, file)).toList();

        // get the main output path
        var main_build_path = project_.buildMainDirectory().getAbsolutePath();

        // get all the test java sources
        var src_test_java_dir_abs = project_.srcTestJavaDirectory().getAbsoluteFile();
        var test_java_files = FileUtils.getFileList(src_test_java_dir_abs, Pattern.compile("^.*\\.java$"), null)
            .stream().map(file -> new File(src_test_java_dir_abs, file)).toList();

        // get the test output path
        var build_test_path = project_.buildTestDirectory().getAbsolutePath();

        // build the compilation classpath
        var compile_classpath_paths = new ArrayList<>(lib_compile_jar_files.stream().map(file -> new File(lib_compile_dir_abs, file).getAbsolutePath()).toList());
        compile_classpath_paths.add(0, main_build_path);
        var compile_classpath = StringUtils.join(compile_classpath_paths, File.pathSeparator);

        // build the test classpath
        var test_classpath_paths = new ArrayList<>(lib_test_jar_files.stream().map(file -> new File(lib_test_dir_abs, file).getAbsolutePath()).toList());
        test_classpath_paths.addAll(0, compile_classpath_paths);
        var test_classpath = StringUtils.join(test_classpath_paths, File.pathSeparator);

        // compile both the main and the test java sources
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var file_manager = compiler.getStandardFileManager(null, null, null)) {
            buildProjectSources(compiler, file_manager, compile_classpath, main_java_files, main_build_path);
            buildProjectSources(compiler, file_manager, test_classpath, test_java_files, build_test_path);
        }
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
                main_message = StringUtils.replace(main_message, "\r", "");
                remaining_message = StringUtils.join(message_lines, "\n");
            }
            System.err.format("%s:%d: %s: %s%n",
                diagnostic.getSource().toUri().getPath(),
                diagnostic.getLineNumber(),
                diagnostic.getKind().name().toLowerCase(),
                main_message);

            if (line_number >= 0 && line_number < lines.size()) {
                var line = lines.get(line_number);
                line = StringUtils.replace(line, "\r", "");
                System.err.println(line);
                if (column_number >= 0 && column_number < line.length()) {
                    System.err.println(StringUtils.repeat(" ", column_number) + "^");
                }
            }
            if (!remaining_message.isEmpty()) {
                System.err.println(remaining_message);
            }
        }
    }
}
