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
 * This exception will be thrown when a call continuation is triggered.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class CallException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = 4288971544223559163L;

    private final ContinuationContext context_;
    private final Object target_;

    /**
     * [PRIVATE AND UNSUPPORTED] Instantiates a new call exception.
     * <p>This is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param context the active continuation context
     * @param target  the call target
     * @since 1.0
     */
    public CallException(ContinuationContext context, Object target) {
        super();

        context.setPaused(true);

        context_ = context;
        target_ = target;
    }

    /**
     * Retrieves the context of this call continuation.
     *
     * @return this call continuation's context
     * @since 1.0
     */
    public ContinuationContext getContext() {
        return context_;
    }

    /**
     * Retrieves the target of this call continuation.
     *
     * @return this call continuation's target
     * @since 1.0
     */
    public Object getTarget() {
        return target_;
    }
}
