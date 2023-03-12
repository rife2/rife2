/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CompileOperation {
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

    public final Project project;

    public CompileOperation(Project project) {
        this.project = project;
    }

    public void execute()
    throws Exception {
        project.buildMainDirectory().mkdirs();
        project.buildProjectDirectory().mkdirs();
        project.buildTestDirectory().mkdirs();

        buildMainSources();
        buildTestSources();
    }

    public void buildMainSources()
    throws IOException {
        buildSources(
            Project.joinPaths(project.compileMainClasspath()),
            project.mainSourceFiles(),
            project.buildMainDirectory().getAbsolutePath());
    }

    public void buildTestSources()
    throws IOException {
        buildSources(
            Project.joinPaths(project.compileTestClasspath()),
            project.testSourceFiles(),
            project.buildTestDirectory().getAbsolutePath());
    }

    public List<String> compileOptions() {
        return project.compileJavacOptions();
    }

    public void buildSources(String classpath, List<File> sources, String destination)
    throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var file_manager = compiler.getStandardFileManager(null, null, null)) {
            var compilation_units = file_manager.getJavaFileObjectsFromFiles(sources);
            var diagnostics = new DiagnosticCollector<JavaFileObject>();
            var options = new ArrayList<>(List.of("-d", destination, "-cp", classpath));
            options.addAll(compileOptions());
            var compilation_task = compiler.getTask(null, file_manager, diagnostics, options, null, compilation_units);
            if (!compilation_task.call()) {
                outputDiagnostics(diagnostics);
            }
        }
    }

    public void outputDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics)
    throws IOException {
        for (var diagnostic : diagnostics.getDiagnostics()) {
            System.err.print(formatDiagnostic(diagnostic));
        }
    }

    public String formatDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic)
    throws IOException {
        var formatted = new StringBuilder();
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

        formatted.append(String.format("%s:%d: %s: %s%n",
            diagnostic.getSource().toUri().getPath(),
            diagnostic.getLineNumber(),
            diagnostic.getKind().name().toLowerCase(),
            main_message));

        if (line_number >= 0 && line_number < lines.size()) {
            var line = lines.get(line_number);
            line = StringUtils.replace(line, "\r", "");
            formatted.append(line).append(System.lineSeparator());
            if (column_number >= 0 && column_number < line.length()) {
                formatted.append(StringUtils.repeat(" ", column_number)).append("^").append(System.lineSeparator());
            }
        }
        if (!remaining_message.isEmpty()) {
            formatted.append(remaining_message).append(System.lineSeparator());
        }
        return formatted.toString();
    }
}
