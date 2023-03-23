/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld;

import rife.bld.help.WarHelp;
import rife.bld.operations.UberJarOperation;
import rife.bld.operations.WarOperation;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Provides the configuration and commands of a Java web project for the
 * build system.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */public class WebProject extends Project {
    protected String warFileName = null;
    protected File srcMainWebappDirectory = null;

    /*
     * Standard build commands
     */

    /**
     * Standard build command, creates an UberJar archive for the web project.
     *
     * @since 1.5
     */
    @Override
    public void uberjar()
    throws Exception {
        jar();
        var operation = new UberJarOperation()
            .fromProject(this)
            .sourceDirectories(List.of(new NamedFile("webapp", srcMainWebappDirectory())));
        operation.jarSourceFiles().addAll(standaloneClasspathJars());
        operation.execute();
    }

    /**
     * Standard build command, creates a war archive for the web project.
     *
     * @since 1.5
     */
    @BuildCommand(help = WarHelp.class)
    public void war()
    throws Exception {
        jar();
        new WarOperation().fromProject(this).execute();
    }

    /*
     * Project directories
     */

    @Override
    public File libStandaloneDirectory() {
        return Objects.requireNonNullElseGet(libStandaloneDirectory, () -> new File(libDirectory(), "standalone"));
    }

    /**
     * Returns the project main webapp directory.
     * Defaults to {@code "webapp"} relative to {@link #srcMainDirectory()}.
     *
     * @since 1.5
     */
    public File srcMainWebappDirectory() {
        return Objects.requireNonNullElseGet(srcMainWebappDirectory, () -> new File(srcMainDirectory(), "webapp"));
    }

    /*
     * Project options
     */

    /**
     * Returns filename to use for the main war archive creation.
     * By default, appends the version and the {@code war} extension to the {@link #archiveBaseName()}.
     *
     * @since 1.5.0
     */
    public String warFileName() {
        return Objects.requireNonNullElseGet(warFileName, () -> archiveBaseName() + "-" + version() + ".war");
    }
}
