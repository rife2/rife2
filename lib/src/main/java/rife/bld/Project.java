/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.commands.*;
import rife.bld.dependencies.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public abstract class Project extends BuildExecutor {
    public List<Repository> repositories = Collections.emptyList();
    public String pkg = null;
    public String name = null;
    public VersionNumber version = null;
    public String mainClass = null;

    public final DependencyScopes dependencies_ = new DependencyScopes();

    public abstract void setup();

    @BuildCommand(help = CleanCommand.Help.class)
    public void clean()
    throws Exception {
        new CleanCommand(this, arguments_).execute();
    }

    @BuildCommand(help = CompileCommand.Help.class)
    public void compile()
    throws Exception {
        new CompileCommand(this, arguments_).execute();
    }

    @BuildCommand(help = DownloadCommand.Help.class)
    public void download() {
        new DownloadCommand(this, arguments_).execute();
    }

    @BuildCommand(help = EncryptCommand.Help.class)
    public void encrypt() {
        new EncryptCommand(arguments_).execute();
    }

    @BuildCommand(help = PrecompileCommand.Help.class)
    public void precompile() {
        new PrecompileCommand(arguments_).execute();
    }

    public static VersionNumber version(int major) {
        return new VersionNumber(major);
    }

    public static VersionNumber version(int major, int minor) {
        return new VersionNumber(major, minor);
    }

    public static VersionNumber version(int major, int minor, int revision) {
        return new VersionNumber(major, minor, revision);
    }

    public static VersionNumber version(int major, int minor, int revision, String qualifier) {
        return new VersionNumber(major, minor, revision, qualifier);
    }

    public static VersionNumber version(String version) {
        return VersionNumber.parse(version);
    }

    public DependencySet scope(Scope scope) {
        return dependencies_.scope(scope);
    }

    public static Dependency dependency(String groupId, String artifactId) {
        return new Dependency(groupId, artifactId);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version) {
        return new Dependency(groupId, artifactId, version);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version, String classifier) {
        return new Dependency(groupId, artifactId, version, classifier);
    }

    public static Dependency dependency(String groupId, String artifactId, VersionNumber version, String classifier, String type) {
        return new Dependency(groupId, artifactId, version, classifier, type);
    }

    /*
     * Project directories
     */

    public File srcMainJavaDirectory() {
        return Path.of("src", "main", "java").toFile();
    }

    public File srcProjectJavaDirectory() {
        return Path.of("src", "project", "java").toFile();
    }

    public File srcTestJavaDirectory() {
        return Path.of("src", "test", "java").toFile();
    }

    public File libCompileDirectory() {
        return Path.of("lib", "compile").toFile();
    }

    public File libProjectDirectory() {
        return Path.of("lib", "project").toFile();
    }

    public File libRuntimeDirectory() {
        return Path.of("lib", "runtime").toFile();
    }

    public File libStandaloneDirectory() {
        return Path.of("lib", "standalone").toFile();
    }

    public File libTestDirectory() {
        return Path.of("lib", "test").toFile();
    }

    public File buildMainDirectory() {
        return Path.of("build", "main").toFile();
    }

    public File buildProjectDirectory() {
        return Path.of("build", "project").toFile();
    }

    public File buildTestDirectory() {
        return Path.of("build", "test").toFile();
    }

    public void start(String[] args) {
        setup();
        processArguments(args);
    }
}
