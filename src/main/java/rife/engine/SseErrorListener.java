/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * The method of a {@code SseErrorListener} will be executed when an event
 * couldn't be converted or delivered to a {@code SseBroadcaster}, and it is
 * registered with whatever feeds that broadcaster, like
 * {@code SseGqmBridge.onError}.
 * <p>Such a failure happens after the operation that triggered it has
 * completed, which means that it can't affect the outcome of that operation
 * and that these listeners are the way for you to detect that something went
 * wrong. When no listener has been registered, the failures are logged to
 * the {@code rife.engine} logger instead.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.database.querymanagers.generic.SseGqmBridge#onError
 * @since 1.10
 */
@FunctionalInterface
public interface SseErrorListener {
    /**
     * Executed when an event couldn't be converted or delivered.
     * <p>This method will be executed on the thread that was performing the
     * work that failed.
     *
     * @param error the exception that was thrown
     * @since 1.10
     */
    void errorOccurred(Throwable error);
}
