/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static rife.tools.FileUtils.permissionsFromMode;

/**
 * Directory and file structure builder, using a fluent API and lambdas
 * to convey the hierarchical structure of on the filesystem in code.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see DirAction
 * @see DirBuilder
 * @see FileAction
 * @since 1.5.19
 */
public class FileBuilder {
    private final Path file;

    /**
     * Constructs a new {@code FileBuilder} object with the specified file.
     *
     * @param file The file object to perform operations on
     * @since 1.5.19
     */
    public FileBuilder(File file) {
        this.file = file.toPath();
    }

    /**
     * Constructs a new {@code FileBuilder} object with the specified path.
     *
     * @param path The path to perform operations on
     * @since 1.5.19
     */
    public FileBuilder(Path path) {
        this.file = path;
    }

    /**
     * Constructs a new {@code FileBuilder} object with the specified file and performs an action on it.
     *
     * @param file   The file object to perform operations on
     * @param action The action to be performed on the file object
     * @throws IOException if an error occurs while executing the action.
     * @since 1.5.19
     */
    public FileBuilder(File file, FileAction action)
    throws IOException {
        this.file = file.toPath();
        action.use(this);
    }

    /**
     * Constructs a new {@code FileBuilder} object with the specified path and performs an action on it.
     *
     * @param path   The path to perform operations on
     * @param action The action to be performed on the file object
     * @throws IOException if an error occurs while executing the action.
     * @since 1.5.19
     */
    public FileBuilder(Path path, FileAction action)
    throws IOException {
        this.file = path;
        action.use(this);
    }

    /**
     * Copies the specified source file to the file of this {@code FileBuilder}.
     *
     * @param source The source file to be copied
     * @return this {@code FileBuilder} object
     * @throws IOException if an error occurs while copying the file.
     * @since 1.5.19
     */
    public FileBuilder copy(Path source)
    throws IOException {
        Files.copy(source, file);
        return this;
    }

    /**
     * Moves the specified source file to the file of this {@code FileBuilder}.
     *
     * @param source The source file to be moved
     * @return this {@code FileBuilder} object
     * @throws IOException if an error occurs while moving the file.
     * @since 1.5.19
     */
    public FileBuilder move(Path source)
    throws IOException {
        Files.move(source, file);
        return this;
    }

    /**
     * Writes the specified text to the file of this {@code FileBuilder}.
     *
     * @param text The text to be written to the file
     * @return this {@code FileBuilder} object
     * @throws IOException if an error occurs while moving the file.
     * @since 1.5.19
     */
    public FileBuilder write(String text)
    throws IOException {
        Files.writeString(file, text);
        return this;
    }

    /**
     * Touches the file of this {@code FileBuilder}.
     * <p>
     * If the file does not exist, it creates a new file.
     * If the file exists, it updates the last access and last modification time of the file.
     *
     * @return this {@code FileBuilder} object
     * @throws IOException if an error occurs while touching the file.
     * @since 1.5.19
     */
    public FileBuilder touch()
    throws IOException {
        if (!Files.exists(file)) {
            Files.createFile(file);
        } else {
            var now = System.currentTimeMillis();
            var time = FileTime.fromMillis(now);
            Files.setAttribute(file, "lastAccessTime", time);
            Files.setLastModifiedTime(file, time);
        }
        return this;
    }

    /**
     * Deletes the file object of this {@code FileBuilder}.
     *
     * @return this {@code FileBuilder} object
     * @throws IOException if an error occurs while touching the file.
     * @since 1.5.19
     */
    public FileBuilder delete()
    throws IOException {
        Files.deleteIfExists(file);
        return this;
    }

    /**
     * Sets the permissions for the file represented by this {@code FileBuilder}
     * using the specified Posix mode.
     *
     * @param mode The mode to use for setting the permissions.
     * @return this {@code FileBuilder} instance.
     * @throws IOException if an error occurs while setting the permissions.
     * @since 1.5.19
     */
    public FileBuilder perms(int mode)
    throws IOException {
        Files.setPosixFilePermissions(file, permissionsFromMode(mode));
        return this;
    }

    /**
     * Sets the permissions for the file represented by this {@code FileBuilder}
     * using the specified permissions.
     *
     * @param permissions The permissions to use for setting the permissions.
     * @return this {@code FileBuilder} instance.
     * @throws IOException if an error occurs while setting the permissions.
     * @since 1.5.19
     */
    public FileBuilder perms(Set<PosixFilePermission> permissions)
    throws IOException {
        Files.setPosixFilePermissions(file, permissions);
        return this;
    }
}
