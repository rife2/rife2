/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.exceptions;

import rife.continuations.ContinuationContext;
import rife.continuations.ContinuationManager;
import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.LightweightError;

import java.io.Serial;

/**
 * This exception will be thrown when a stepBack continuation is triggered.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class StepBackException extends LightweightError implements ControlFlowRuntimeException {
    @Serial private static final long serialVersionUID = -5005676849123442618L;

    private final ContinuationContext context_;

    /**
     * [PRIVATE AND UNSUPPORTED] Instantiates a new step-back exception.
     * <p>This is used by the instrumented bytecode that provides
     * continuations support, it's not intended for general use.
     *
     * @param context the active continuation context
     * @since 1.0
     */
    public StepBackException(ContinuationContext context) {
        context.setPaused(true);

        context_ = context;
    }

    /**
     * Retrieves the context of this step-back continuation.
     *
     * @return this step-back continuation's context
     * @since 1.0
     */
    public ContinuationContext getContext() {
        return context_;
    }

    /**
     * Looks up the ID of the target continuation of the step-back.
     *
     * @return the target continuation ID of the step-back; or
     * <p>{@code null} if the target continuation couldn't be found
     * @since 1.0
     */
    public String lookupStepBackId() {
        var manager = context_.getManager();

        // try to obtain the label of the previous continuation,
        // if there is no previous continuation, simply start from the beginning again
        var parent_id = context_.getParentId();
        var parent_context = manager.getContext(parent_id);
        if (parent_context != null) {
            var grandparent_id = parent_context.getParentId();
            var grandparent_context = manager.getContext(grandparent_id);

            // if the parent context exists, set up this context to resume execution
            // where the parent context resumed it
            if (grandparent_context != null) {
                context_.setLabel(grandparent_context.getLabel());
                context_.setParentId(grandparent_context.getParentId());
                context_.addRelatedId(parent_id);
                return context_.getId();
            }
        }

        return null;
    }
}

