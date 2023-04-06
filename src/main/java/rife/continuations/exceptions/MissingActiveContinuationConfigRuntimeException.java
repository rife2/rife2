/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.tools.exceptions.ControlFlowRuntimeException;

import java.io.Serial;

/**
 * Thrown when the active continuation runtime configuration isn't set.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MissingActiveContinuationConfigRuntimeException extends RuntimeException implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 7401871623085473212L;

    /**
     * Instantiates a new exception.
     *
     * @since 1.0
     */
    public MissingActiveContinuationConfigRuntimeException() {
        super("The active ContinuationConfigRuntime instance is not set, this is required for continuations to be able to execute. Always call ContinuationConfigRuntime.setActiveConfigRuntime(config) in your continuations runner before executing the ContinuableObject.");
    }
}
