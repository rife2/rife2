/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.continuations.ContinuationContext;
import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

/**
 * This exception will be thrown when a pause continuation is triggered.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PauseException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 181863837154043654L;

    private final ContinuationContext context_;

    /**
     * [PRIVATE AND UNSUPPORTED] Instantiates a new pause exception.
     * <p>This is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param context the active continuation context
     * @since 1.0
     */
    public PauseException(ContinuationContext context) {
        context.setPaused(true);

        context_ = context;
    }

    /**
     * Retrieves the context of this pause continuation.
     *
     * @return this pause continuation's context
     * @since 1.0
     */
    public ContinuationContext getContext() {
        return context_;
    }
}

