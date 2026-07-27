/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * The method of a {@code SseConnectionFilter} will be executed for each of
 * the connections of a {@code SseBroadcaster}, so that an operation is
 * limited to the connections that it accepts.
 * <p>This makes it possible to address the clients that belong together,
 * for instance the ones that are watching the same document or that are
 * logged in as the same user, which relies on the attributes that you gave
 * the connections when they were established.
 * <p>Note that a filter is executed again when a client reconnects and the
 * events that it missed are replayed from the history, so that it has to
 * keep deciding the same way to stay consistent.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseBroadcaster#send(ServerSentEvent, SseConnectionFilter)
 * @see SseBroadcaster#close(SseConnectionFilter)
 * @since 1.10
 */
@FunctionalInterface
public interface SseConnectionFilter {
    /**
     * Executed to determine whether an operation applies to a connection.
     *
     * @param connection the connection that is being considered
     * @return {@code true} if the operation applies to the connection; or
     * <p>{@code false} if it doesn't
     * @since 1.10
     */
    boolean accepts(SseConnection connection);
}
