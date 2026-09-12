/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestJavaCommand {
    static List<String> forMain(String classpathPrefix, String mainClass, String... arguments) {
        var command = new ArrayList<String>();
        command.add(ProcessHandle.current().info().command().orElseThrow());
        command.add("-cp");
        var classpath = System.getProperty("java.class.path");
        command.add(null == classpathPrefix ? classpath : classpathPrefix + File.pathSeparator + classpath);
        var module_path = System.getProperty("jdk.module.path");
        if (module_path != null && !module_path.isEmpty()) {
            command.add("-p");
            command.add(module_path);
            command.add("--add-modules");
            command.add("ALL-MODULE-PATH");
        }
        command.add(mainClass);
        command.addAll(List.of(arguments));
        return command;
    }
}
