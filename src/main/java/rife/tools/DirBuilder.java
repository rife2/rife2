/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static rife.tools.FileUtils.permissionsFromMode;

/**
 * Directory and file structure builder, using a fluent API and lambdas
 * to convey the hierarchical structure of on the filesystem in code.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DirAction
 * @see FileAction
 * @see FileBuilder
 * @since 1.5.19
 */
public class DirBuilder {
    private final Path dir;

    /**
     * Constructs a new {@code DirBuilder} instance with the specified directory.
     *
     * @param dir The directory to use for this DirBuilder.
     * @since 1.5.19
     */
    public DirBuilder(File dir) {
        this.dir = dir.toPath();
    }

    /**
     * Constructs a new {@code DirBuilder} instance with the specified path.
     *
     * @param path The path to use for this DirBuilder.
     * @since 1.5.19
     */
    public DirBuilder(Path path) {
        this.dir = path;
    }

    /**
     * Constructs a new {@code DirBuilder} instance with the specified path and executes an action on it.
     *
     * @param dir    The directory to use for this {@code DirBuilder}.
     * @param action The action to execute on the directory.
     * @throws IOException if an error occurs while executing the action.
     * @since 1.5.19
     */
    public DirBuilder(File dir, DirAction action)
    throws IOException {
        this.dir = dir.toPath();
        action.use(this);
    }

    /**
     * Constructs a new {@code DirBuilder} instance with the specified directory and executes an action on it.
     *
     * @param path The path to use for this DirBuilder.
     * @param action The action to execute on the directory.
     * @throws IOException if an error occurs while executing the action.
     * @since 1.5.19
     */
    public DirBuilder(Path path, DirAction action)
    throws IOException {
        this.dir = path;
        action.use(this);
    }

    /**
     * Creates a new subdirectory with the specified name under the directory represented by this {@code DirBuilder}.
     *
     * @param dir The name of the subdirectory to create.
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while creating the subdirectory.
     * @since 1.5.19
     */
    public DirBuilder dir(String dir)
    throws IOException {
        Files.createDirectories(this.dir.resolve(dir));
        return this;
    }

    /**
     * Creates a new subdirectory with the specified name under the directory represented
     * by this {@code DirBuilder} and executes the specified action on it.
     *
     * @param dir    The name of the subdirectory to create.
     * @param action The action to execute on the subdirectory.
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while creating the subdirectory or executing the action.
     * @since 1.5.19
     */
    public DirBuilder dir(String dir, DirAction action)
    throws IOException {
        var child = this.dir.resolve(dir);
        Files.createDirectories(child);
        new DirBuilder(child, action);
        return this;
    }

    /**
     * Specify a new file under the directory represented by this {@code DirBuilder}
     * and executes the specified action on it.
     *
     * @param file   The name of the file to create.
     * @param action The action to execute on the file.
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while creating the file or executing the action.
     * @since 1.5.19
     */
    public DirBuilder file(String file, FileAction action)
    throws IOException {
        var child = this.dir.resolve(file);
        action.use(new FileBuilder(child));
        return this;
    }

    /**
     * Deletes the directory represented by this {@code DirBuilder} and all of its contents.
     *
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while deleting the directory.
     * @since 1.5.19
     */
    public DirBuilder delete()
    throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
        return this;
    }

    /**
     * Sets the permissions for the directory represented by this {@code DirBuilder}
     * using the specified Posix mode.
     *
     * @param mode The mode to use for setting the permissions.
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while setting the permissions.
     * @since 1.5.19
     */
    public DirBuilder perms(int mode)
    throws IOException {
        Files.setPosixFilePermissions(dir, permissionsFromMode(mode));
        return this;
    }

    /**
     * Sets the permissions for the directory represented by this {@code DirBuilder}
     * using the specified permissions.
     *
     * @param permissions The permissions to use for setting the permissions.
     * @return this {@code DirBuilder} instance.
     * @throws IOException if an error occurs while setting the permissions.
     * @since 1.5.19
     */
    public DirBuilder perms(Set<PosixFilePermission> permissions)
    throws IOException {
        Files.setPosixFilePermissions(dir, permissions);
        return this;
    }
}
