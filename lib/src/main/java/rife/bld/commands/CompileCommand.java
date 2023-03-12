/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.commands;

import rife.bld.BuildHelp;
import rife.bld.Project;
import rife.tools.StringUtils;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

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

    public final Project project;

    public CompileCommand(Project project) {
        this.project = project;
    }

    public void execute()
    throws Exception {
        project.buildMainDirectory().mkdirs();
        project.buildProjectDirectory().mkdirs();
        project.buildTestDirectory().mkdirs();

        // compile both the main and the test java sources
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var file_manager = compiler.getStandardFileManager(null, null, null)) {
            buildProjectSources(compiler, file_manager,
                Project.joinPaths(project.compileClasspath()),
                project.mainSourceFiles(),
                project.buildMainDirectory().getAbsolutePath());
            buildProjectSources(compiler, file_manager,
                Project.joinPaths(project.testClasspath()),
                project.testSourceFiles(),
                project.buildTestDirectory().getAbsolutePath());
        }
    }

    public void buildProjectSources(JavaCompiler compiler, StandardJavaFileManager fileManager, String classpath, List<File> sources, String destination)
    throws IOException {
        var compilation_units = fileManager.getJavaFileObjectsFromFiles(sources);
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var options = List.of("-d", destination, "-cp", classpath);
        var compilation_task = compiler.getTask(null, fileManager, diagnostics, options, null, compilation_units);
        if (!compilation_task.call()) {
            outputDiagnostics(diagnostics);
        }
    }

    public void outputDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics)
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
