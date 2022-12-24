/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;

import java.io.Serial;

/**
 * Thrown when a call target couldn't be resolved to a proper
 * {@code ContinuableObject}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class CallTargetNotFoundException extends RuntimeException implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 8455201993543302381L;

    /**
     * Instantiates a new exception.
     *
     * @param target the original call target
     * @param cause  the cause of the retrieval failure; or
     *               <p>{@code null} if there was no exception cause
     * @since 1.0
     */
    public CallTargetNotFoundException(Object target, Throwable cause) {
        super("The ContinuableObject that corresponds to the call target " + target + " couldn't be found.", cause);
    }
}
