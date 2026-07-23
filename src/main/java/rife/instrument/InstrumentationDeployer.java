/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.instrument;

import rife.continuations.ContinuationConfigInstrument;
import rife.continuations.instrument.ContinuationsBytecodeTransformer;
import rife.database.querymanagers.generic.instrument.LazyLoadAccessorsBytecodeTransformer;
import rife.engine.EngineContinuationConfigInstrument;
import rife.tools.ClassBytesLoader;
import rife.tools.FileUtils;
import rife.validation.instrument.ConstrainedDetector;
import rife.validation.instrument.MetaDataInstrumenter;
import rife.workflow.config.ContinuationInstrument;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Applies RIFE2's bytecode instrumentation ahead of time to compiled
 * classes, as an alternative to the java agent that performs the same
 * transformations at class loading time.
 * <p>The same transformations are applied in the same order as the agent:
 * web engine continuations, workflow continuations, meta-data merging,
 * and lazy-loading of {@code GenericQueryManager} relationships. Classes
 * that don't use any of these capabilities are left untouched, and
 * instrumenting already instrumented classes makes no changes, so the
 * agent can still be active at run time, for instance during development.
 * <p>Ahead-of-time instrumented classes make the agent unnecessary at run
 * time, and they are the way to use continuations, the workflow engine
 * and meta-data merging inside a GraalVM native image, where load-time
 * instrumentation isn't possible. For every class that received
 * continuations support, a GraalVM reflection configuration entry is
 * written alongside the classes, so that native images can invoke their
 * entry methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
public class InstrumentationDeployer {
    private static final Pattern CLASS_PATTERN = Pattern.compile(".*\\.class$");

    /**
     * Instruments the classes of the provided directories into a target
     * directory.
     * <p>Only the classes that were modified are written to the target
     * directory, which can be the same directory as the one that is being
     * instrumented. When classes received continuations support, their
     * GraalVM reflection configuration is written to
     * {@code META-INF/native-image/rife-instrumented/reflect-config.json}
     * in the target directory.
     *
     * @param classDirectories the directories with the compiled classes to
     *                         instrument
     * @param targetDirectory  the directory the instrumented classes are
     *                         written to
     * @param verbose          whether every instrumented class is reported
     *                         on the standard output
     * @return the names of the classes that were instrumented
     * @throws Exception when an error occurred during the instrumentation
     * @since 1.10
     */
    public static List<String> instrument(List<File> classDirectories, File targetDirectory, boolean verbose)
    throws Exception {
        var urls = new ArrayList<URL>();
        for (var dir : classDirectories) {
            urls.add(dir.toURI().toURL());
        }

        var instrumented = new ArrayList<String>();
        var continuations_entries = new TreeMap<String, ContinuationConfigInstrument>();
        ContinuationConfigInstrument[] continuations_configs = {
            new EngineContinuationConfigInstrument(),
            new ContinuationInstrument()
        };

        try (var loader = new URLClassLoader(urls.toArray(new URL[0]), InstrumentationDeployer.class.getClassLoader())) {
            for (var dir : classDirectories) {
                for (var file : FileUtils.getFileList(dir, CLASS_PATTERN, null)) {
                    var normalized = file.replace(File.separatorChar, '/');
                    var name = normalized.substring(0, normalized.length() - ".class".length()).replace('/', '.');
                    var original = FileUtils.readBytes(new File(dir, file));
                    var current = original;

                    // the transformations are applied in the same order as
                    // the java agent registers its transformers
                    for (var config : continuations_configs) {
                        try {
                            var result = ContinuationsBytecodeTransformer.transformIntoResumableBytes(config, current, name, loader);
                            if (result != null) {
                                current = result;
                                continuations_entries.put(name, config);
                            }
                        } catch (ClassNotFoundException e) {
                            // the class hierarchy couldn't be resolved,
                            // leave the class untouched like the agent does
                        }
                    }

                    var merged = MetaDataInstrumenter.instrument(loader, name, current);
                    if (merged != null) {
                        current = merged;
                    }

                    try {
                        if (new ConstrainedDetector(new ClassBytesLoader(loader)).isConstrained(name, current)) {
                            var lazy = LazyLoadAccessorsBytecodeTransformer.addLazyLoadToBytes(current, loader);
                            if (lazy != null) {
                                current = lazy;
                            }
                        }
                    } catch (Throwable ignored) {
                        // mirrors the agent, which leaves classes untouched
                        // when the lazy-load detection fails
                    }

                    if (!Arrays.equals(original, current)) {
                        var target = new File(targetDirectory, file);
                        target.getParentFile().mkdirs();
                        FileUtils.writeBytes(current, target);
                        instrumented.add(name);
                        if (verbose) {
                            System.out.println("instrumented: " + name + " (" + original.length + " -> " + current.length + " bytes)");
                        }
                    }
                }
            }
        }

        if (!continuations_entries.isEmpty()) {
            writeReflectionConfig(continuations_entries, targetDirectory);
        }

        return instrumented;
    }

    private static void writeReflectionConfig(TreeMap<String, ContinuationConfigInstrument> entries, File targetDirectory)
    throws Exception {
        var config = new StringBuilder("[\n");
        var first = true;
        for (var entry : entries.entrySet()) {
            if (!first) {
                config.append(",\n");
            }
            first = false;
            var instrument = entry.getValue();
            config.append("""
                  {
                    "name": "%s",
                    "methods": [
                      {
                        "name": "<init>",
                        "parameterTypes": []
                      },
                      {
                        "name": "%s",
                        "parameterTypes": [%s]
                      }
                    ]
                  }""".formatted(entry.getKey(), instrument.getEntryMethodName(),
                                 descriptorParameters(instrument.getEntryMethodDescriptor())));
        }
        config.append("\n]\n");

        var config_directory = new File(targetDirectory, "META-INF/native-image/rife-instrumented");
        config_directory.mkdirs();
        FileUtils.writeString(config.toString(), new File(config_directory, "reflect-config.json"));
    }

    private static String descriptorParameters(String descriptor) {
        var parameters = new ArrayList<String>();
        var i = 1;
        while (descriptor.charAt(i) != ')') {
            var start = i;
            while (descriptor.charAt(i) == '[') {
                i += 1;
            }
            if (descriptor.charAt(i) == 'L') {
                i = descriptor.indexOf(';', i) + 1;
            } else {
                i += 1;
            }
            var parameter = rife.asm.Type.getType(descriptor.substring(start, i)).getClassName();
            parameters.add("\"" + parameter + "\"");
        }
        return String.join(", ", parameters);
    }

    private static String getHelp(String command) {
        if (null == command) {
            command = "java " + InstrumentationDeployer.class.getName();
        }

        return """
            Usage : %s [-verbose] -d <directory> <classdirs>
              Instruments compiled classes ahead of time, as an alternative
              to the java agent.
              -d <directory>  Specify where to place the instrumented classes
              -verbose        Report every instrumented class""".formatted(command);
    }

    public static void main(String[] arguments) {
        var valid_arguments = arguments.length >= 3;
        var verbose = false;
        File target = null;
        var directories = new ArrayList<File>();

        for (var i = 0; i < arguments.length && valid_arguments; i++) {
            if (arguments[i].equals("-verbose")) {
                verbose = true;
            } else if (arguments[i].equals("-d")) {
                i += 1;
                if (i >= arguments.length || arguments[i].startsWith("-")) {
                    valid_arguments = false;
                } else {
                    target = new File(arguments[i]);
                }
            } else if (arguments[i].startsWith("-")) {
                valid_arguments = false;
            } else {
                directories.add(new File(arguments[i]));
            }
        }

        if (null == target || directories.isEmpty()) {
            valid_arguments = false;
        }

        if (!valid_arguments) {
            System.err.println(getHelp(null));
            System.exit(1);
        }

        try {
            var instrumented = instrument(directories, target, verbose);
            System.out.println("Instrumented " + instrumented.size() + " classes.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
