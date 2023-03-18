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

    public File srcMainWebappDirectory() {
        return Objects.requireNonNullElseGet(srcMainWebappDirectory, () -> new File(srcMainDirectory(), "webapp"));
    }

    /*
     * Project options
     */

    public String warFileName() {
        return Objects.requireNonNullElseGet(warFileName, () -> archiveBaseName() + "-" + version() + ".war");
    }
}
