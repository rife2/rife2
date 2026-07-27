/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Provides a snapshot of the state of the event history of an
 * {@link SseBroadcaster}, which is intended to help you tune the history
 * capacity.
 * <p>The most important numbers for tuning are {@code gaps}, which counts
 * the reconnections that couldn't be fully served since the events had
 * already been evicted from the history, and {@code maxMissedEvents}, which
 * is the largest number of events that any reconnecting client had missed.
 * A capacity of about twice the maximum miss will cover every reconnection
 * that has been observed so far.
 *
 * @param capacity    the configured history capacity
 * @param buffered    the number of events that are currently buffered
 * @param oldestId    the ID of the oldest buffered event; or {@code 0}
 *                    when nothing is buffered
 * @param newestId    the ID of the most recently sent event; or {@code 0}
 *                    when nothing has been sent yet
 * @param replays     the number of reconnections whose missed events were
 *                    replayed from the history
 * @param gaps        the number of reconnections that couldn't be replayed
 *                    since the events had already been evicted, or since
 *                    the last received ID originated from a previous
 *                    application instance
 * @param maxMissedEvents the largest number of events that any reconnecting
 *                    client had missed
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseBroadcaster#historyStats()
 * @see SseBroadcaster#history(int)
 * @since 1.10
 */
public record SseHistoryStats(int capacity, int buffered, long oldestId, long newestId,
                              long replays, long gaps, long maxMissedEvents) {
    public String toString() {
        return "capacity: " + capacity +
               ", buffered: " + buffered +
               ", ids: " + oldestId + ".." + newestId +
               ", replays: " + replays +
               ", gaps: " + gaps +
               ", max missed events: " + maxMissedEvents;
    }
}
