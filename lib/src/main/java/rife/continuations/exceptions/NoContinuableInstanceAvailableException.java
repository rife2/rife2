/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;

import java.io.Serial;

/**
 * Thrown when a continuable instance can't be found
 * {@code ContinuableObject}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class NoContinuableInstanceAvailableException extends RuntimeException implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = -430892335163405749L;

    /**
     * Instantiates a new exception.
     *
     * @since 1.0
     */
    public NoContinuableInstanceAvailableException() {
        super("Neither an object nor a class was provided to be able to start a continuable.");
    }
}
