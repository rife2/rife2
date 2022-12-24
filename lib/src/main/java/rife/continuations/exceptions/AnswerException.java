/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.continuations.ContinuationContext;
import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

/**
 * This exception will be thrown when an answer continuation is triggered.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class AnswerException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 4501524247064256632L;

    private final ContinuationContext context_;
    private final Object callAnswer_;

    /**
     * [PRIVATE AND UNSUPPORTED] Instantiates a new answer exception.
     * <p>This is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param context the active continuation context
     * @param answer  the answered value
     * @since 1.0
     */
    public AnswerException(ContinuationContext context, Object answer) {
        super();

        context_ = context;
        callAnswer_ = answer;
    }

    /**
     * Retrieves the context of this answer continuation.
     *
     * @return this answer continuation's context
     * @since 1.0
     */
    public ContinuationContext getContext() {
        return context_;
    }

    /**
     * Retrieves the answered value.
     *
     * @return this answer continuation's anwered value; or
     * <p>{@code null} if no answer was provided
     * @since 1.0
     */
    public Object getAnswer() {
        return callAnswer_;
    }
}
