/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

/**
 * Contains the state of a call continuation.
 * <p>The only really important property is the continuation ID, which should
 * contain the ID of the continuation context that was active when the call
 * was triggered.
 * <p>The state property is totally optional and is there to be used in case
 * other state needs to be preserved for a call continuation. RIFE2's web
 * engine, for instance, uses it to tie an element's execution state to a call
 * continuation.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class CallState {
    private final String continuationId_;
    private final Object state_;

    /**
     * Creates a new {@code CallState} instance.
     *
     * @param continuationId the continuation context ID that triggered the
     *                       call continuation
     * @param state          the optional state that should be tied to the call
     *                       continuation
     * @since 1.0
     */
    public CallState(final String continuationId, final Object state) {
        continuationId_ = continuationId;
        state_ = state;
    }

    /**
     * Retrieves the ID of the continuation that triggered the call.
     *
     * @return the call continuation's ID
     * @since 1.0
     */
    public String getContinuationId() {
        return continuationId_;
    }

    /**
     * Retrieves the optional state that is tied to the call continuation.
     *
     * @return the call continuation's optional state; or
     * <p>{@code null} if no additional state is tied to it
     * @since 1.0
     */
    public Object getState() {
        return state_;
    }
}