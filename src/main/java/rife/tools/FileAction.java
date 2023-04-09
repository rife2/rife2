/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.io.IOException;

/**
 * Functional interface that captures an action to execute
 * on a {@link FileBuilder} instance.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5.19
 */
@FunctionalInterface
public interface FileAction {
    /**
     * Executes the action on the specified {@code FileBuilder} instance.
     *
     * @param f The {@code FileBuilder} instance on which to execute the action.
     * @throws IOException if an exception occurs while executing the action.
     * @since 1.5.19
     */
    void use(FileBuilder f)
    throws IOException;
}
