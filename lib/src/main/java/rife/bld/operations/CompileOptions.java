/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.tools.Convert;
import rife.tools.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Options for the standard javac tool.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.12
 */
public class CompileOptions extends ArrayList<String> {
    public enum DebuggingInfo {
        ALL, NONE, LINES, VAR, SOURCE
    }

    public enum Implicit {
        NONE, CLASS
    }

    public enum Processing {
        NONE, ONLY
    }

    /**
     * Option to pass to annotation processors
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions annotationOption(String key, String value) {
        add("-A" + key + "=" + value);
        return this;
    }

    /**
     * Root modules to resolve in addition to the initial modules,
     * or all modules on the module path if a module is
     * ALL-MODULE-PATH.
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions addModules(String... modules) {
        add("--add-modules");
        add(StringUtils.join(modules, ","));
        return this;
    }

    /**
     * Specify character encoding used by source files
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions encoding(String name) {
        add("-encoding");
        add("name");
        return this;
    }

    /**
     * Output source locations where deprecated APIs are used
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions deprecation() {
        add("-deprecation");
        return this;
    }

    /**
     * Enable preview language features. To be used in conjunction with {@link #release}.
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions enablePreview() {
        add("--enable-preview");
        return this;
    }

    /**
     * Override location of endorsed standards path
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions endorsedDirs(File... dirs) {
        add("-endorseddirs");
        add(Arrays.stream(dirs).map(File::getAbsolutePath).collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Override location of installed extensions
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions extDirs(File... dirs) {
        add("-extdirs");
        add(Arrays.stream(dirs).map(File::getAbsolutePath).collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Compile for the specified Java SE release.
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions release(int version) {
        add("--release");
        add(Convert.toString(version));
        return this;
    }

    /**
     * Generate debugging info
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions debuggingInfo(DebuggingInfo option) {
        if (option.equals(DebuggingInfo.ALL)) {
            add("-g");
        } else {
            add("-g:" + option.name().toLowerCase());
        }
        return this;
    }

    /**
     * Specify where to place generated native header files
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions nativeHeaders(File path) {
        add("-h");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Specify whether or not to generate class files for implicitly referenced files
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions implicit(Implicit option) {
        add("-implicit:" + option.name().toLowerCase());
        return this;
    }

    /**
     * Limit the universe of observable modules
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions limitModules(String... modules) {
        add("--limit-modules");
        add(StringUtils.join(modules, ","));
        return this;
    }

    /**
     * Compile only the specified module(s), check timestamps
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions module(String... modules) {
        add("--module");
        add(StringUtils.join(modules, ","));
        return this;
    }

    /**
     * Specify where to find application modules
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions modulePath(File path) {
        add("--module-path");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Specify where to find input source files for multiple modules
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions moduleSourcePath(File path) {
        add("--module-source-path");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Specify version of modules that are being compiled
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions moduleSourcePath(String version) {
        add("--module-version");
        add(version);
        return this;
    }

    /**
     * Generate no warnings
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions noWarn() {
        add("-nowarn");
        return this;
    }

    /**
     * Generate metadata for reflection on method parameters
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions parameters() {
        add("-parameters");
        return this;
    }

    /**
     * Control whether annotation processing and/or compilation is done.
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions process(Processing option) {
        add("-proc:" + option.name().toLowerCase());
        return this;
    }

    /**
     * Names of the annotation processors to run; bypasses default discovery process
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions processors(String... classnames) {
        add("-processor");
        add(StringUtils.join(classnames, ","));
        return this;
    }

    /**
     * Specify a module path where to find annotation processors
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions processorModulePath(File path) {
        add("--processor-module-path");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Specify where to find annotation processors
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions processorPath(File path) {
        add("--processor-path");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Check that API used is available in the specified profile
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions profile(String profile) {
        add("-profile");
        add(profile);
        return this;
    }

    /**
     * Override location of system modules. Option is &lt;jdk&gt; or none.
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions system(String option) {
        add("--system");
        add(option);
        return this;
    }

    /**
     * Override location of upgradeable modules
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions upgradeModulePath(File path) {
        add("--upgrade-module-path");
        add(path.getAbsolutePath());
        return this;
    }

    /**
     * Terminate compilation if warnings occur
     *
     * @return this list of options
     * @since 1.5.12
     */
    public CompileOptions warningError() {
        add("-Werror");
        return this;
    }
}