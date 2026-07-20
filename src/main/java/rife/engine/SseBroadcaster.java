/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.template.Template;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Manages a collection of detached server-sent events connections and
 * broadcasts events to all of them.
 * <p>A broadcaster is typically stored in a field of your {@link Site} and
 * passed to {@link Context#sse(SseBroadcaster)} inside the
 * element of an SSE route. The element returns immediately while the
 * connection stays open, and events can be pushed afterwards from anywhere
 * in the application: another element, a scheduled task, a workflow, or any
 * other thread.
 * <pre>public class MySite extends Site {
 *     final SseBroadcaster ticker = new SseBroadcaster();
 *     Route events = get("/events", c -&gt; c.sse(ticker));
 * }
 *
 * // from anywhere else:
 * site.ticker.send(new ServerSentEvent().name("tick").data("42"));</pre>
 * <p>Connections whose clients have disconnected are automatically removed
 * when sending to them fails. Sending a periodic
 * {@link #comment(String) heartbeat comment} both prevents intermediaries
 * from closing idle connections and reaps disconnected ones.
 * <p>With {@link #history(int) event history} enabled, clients that
 * reconnect automatically receive the events they missed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseConnection
 * @see ServerSentEvent
 * @since 1.10
 */
public class SseBroadcaster implements AutoCloseable {
    private final Set<SseConnection> connections_ = ConcurrentHashMap.newKeySet();

    private static final AtomicInteger HEARTBEAT_THREAD_SEQUENCE = new AtomicInteger();

    private final Object heartbeatLock_ = new Object();
    private ScheduledExecutorService heartbeatExecutor_ = null;

    private final Object historyLock_ = new Object();
    private volatile ArrayDeque<HistoryEntry> history_ = null;
    private int historyCapacity_ = 0;
    // a random nonce rather than wall-clock time, so that broadcasters
    // created within the same millisecond can't share an ID namespace
    private final long epoch_ = ThreadLocalRandom.current().nextLong() & Long.MAX_VALUE;
    private long lastEventId_ = 0;
    private long replays_ = 0;
    private long gaps_ = 0;
    private long maxMissedEvents_ = 0;

    private record HistoryEntry(long id, ServerSentEvent event, Predicate<SseConnection> filter) {
    }

    /**
     * Enables event history with the provided capacity, so that clients
     * that reconnect automatically receive the events they missed.
     * <p>With history enabled, the broadcaster assigns the SSE ID of every
     * event that carries a name, data or a template. IDs are composed of an
     * epoch that identifies the broadcaster instance and a monotonically
     * increasing sequence number, so that IDs from before an application
     * restart can't be mistaken for positions in the new instance's
     * sequence. Since application-assigned event IDs and
     * broadcaster history are alternative reconnection strategies that
     * can't be combined, sending an event that carries its own ID throws an
     * {@code IllegalArgumentException}. The last {@code capacity} events
     * are buffered, with template data captured at the moment of
     * broadcasting.
     * <p>When a browser reconnects, it transmits the ID of the last event
     * it received, and the buffered events after that ID are replayed to it
     * before it rejoins the live stream. Events that were
     * {@link #send(ServerSentEvent, Predicate) targeted with a filter} are
     * only replayed to connections that match the filter: the filters are
     * re-evaluated at replay time, and an exception thrown by a filter
     * propagates to the reconnecting request. The
     * {@code lastEventId} request parameter is honored the same way as the
     * reconnection header, which allows pages to receive the events that
     * occurred between their rendering and the establishment of the stream.
     * <p>When a client reconnects after missing more events than the
     * history contains, nothing is replayed and the reconnection is
     * reported by {@link #historyStats()} and logged to the
     * {@code rife.engine} logger. Reconnections that present an ID from
     * before an application restart are reported the same way, since the
     * history doesn't bridge restarts. The capacity should be sized to
     * cover the events that can occur during the longest disconnection that
     * should be bridged seamlessly, applications typically handle longer
     * gaps by rendering the full state on the page. Since the reconnection
     * ID is provided by the client, each reconnection can request up to
     * {@code capacity} events to be replayed, keep the capacity
     * proportionate for streams that are accessible without
     * authentication.
     * <p>Heartbeat comments are not buffered and don't receive IDs. Only
     * events that are sent through the broadcaster are part of the history,
     * events that are sent directly to an individual connection bypass it.
     * The history is kept in memory and doesn't survive an application
     * restart.
     * <p>Events are delivered to the connections outside of the internal
     * history lock. When multiple threads broadcast concurrently, the order
     * in which their events reach an individual connection isn't
     * guaranteed, even though the history preserves the ID order, and such
     * events can be delivered again after a reconnection. Events that are
     * broadcast sequentially from a single thread always arrive in order.
     * A connection whose client stops reading without disconnecting can
     * block a sending thread while writing to it, and since the write
     * doesn't fail, the heartbeat can't reap such a connection: it's only
     * reaped when the servlet container's connection timeout fails the
     * write. Bound that timeout in the container configuration — with the
     * embedded servers through {@link Server#connectionIdleTimeout} or
     * {@link TomcatServer#connectionTimeout} — and keep it longer than the
     * heartbeat interval.
     *
     * @param capacity the number of events to buffer
     * @return this broadcaster instance
     * @see #historyStats()
     * @since 1.10
     */
    public SseBroadcaster history(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be at least 1");

        synchronized (historyLock_) {
            historyCapacity_ = capacity;
            if (history_ == null) {
                history_ = new ArrayDeque<>(capacity);
            } else {
                while (history_.size() > historyCapacity_) {
                    history_.removeFirst();
                }
            }
        }
        return this;
    }

    void register(SseConnection connection) {
        connection.setBroadcaster(this);

        if (history_ == null) {
            synchronized (connection) {
                // a connection that was closed while registering isn't added
                if (connection.isOpen()) {
                    connections_.add(connection);
                }
            }
            return;
        }

        // hold the connection's monitor from before the connection becomes
        // visible to broadcasts until the replay has completed, so that
        // concurrent broadcasts can't deliver a live event to this
        // connection before its replayed events
        synchronized (connection) {
            if (!connection.isOpen()) {
                return;
            }

            List<HistoryEntry> replay;
            synchronized (historyLock_) {
                replay = selectReplay(connection);
                connections_.add(connection);
            }

            var complete = true;
            for (var entry : replay) {
                if (!entry.filter().test(connection)) {
                    continue;
                }
                if (!connection.send(entry.event(), formatId(entry.id()))) {
                    complete = false;
                    break;
                }
            }

            if (complete &&
                !replay.isEmpty()) {
                synchronized (historyLock_) {
                    replays_ += 1;
                }
            }
        }

        // the replay can have detected that the client is gone
        if (!connection.isOpen()) {
            unregister(connection);
        }
    }

    void unregister(SseConnection connection) {
        connections_.remove(connection);
    }

    private String formatId(long sequence) {
        return epoch_ + "-" + sequence;
    }

    private List<HistoryEntry> selectReplay(SseConnection connection) {
        var last_raw = connection.lastEventId();
        if (last_raw == null) {
            return List.of();
        }

        var separator = last_raw.indexOf('-');
        if (separator == -1) {
            return List.of();
        }

        long last_epoch;
        long last;
        try {
            last_epoch = Long.parseLong(last_raw.substring(0, separator));
            last = Long.parseLong(last_raw.substring(separator + 1));
        } catch (NumberFormatException e) {
            return List.of();
        }

        if (last < 0) {
            return List.of();
        }

        if (last_epoch != epoch_ ||
            last > lastEventId_) {
            // the ID originates from another broadcaster instance, most
            // likely from before an application restart, the history can't
            // bridge it
            gaps_ += 1;
            Logger.getLogger("rife.engine").fine(() ->
                "An SSE client reconnected with last event ID " + last_raw + " while the newest " +
                "ID is " + formatId(lastEventId_) + "; the ID originates from another " +
                "broadcaster instance, likely from before an application restart, and no " +
                "events were replayed");
            return List.of();
        }

        if (last == lastEventId_) {
            return List.of();
        }

        var missed = lastEventId_ - last;
        if (missed > maxMissedEvents_) {
            maxMissedEvents_ = missed;
        }

        var oldest = history_.isEmpty() ? lastEventId_ + 1 : history_.peekFirst().id();
        if (last < oldest - 1) {
            gaps_ += 1;
            var capacity = historyCapacity_;
            Logger.getLogger("rife.engine").fine(() ->
                "An SSE client reconnected after missing " + missed + " events, while the " +
                "history buffers " + (lastEventId_ - oldest + 1) + " events back to ID " + oldest + "; " +
                "a history capacity of at least " + missed + " would have covered this " +
                "reconnection (currently " + capacity + ")");
            return List.of();
        }

        var replay = new ArrayList<HistoryEntry>();
        for (var entry : history_) {
            if (entry.id() > last) {
                replay.add(entry);
            }
        }
        return replay;
    }

    /**
     * Sends an event to all the open connections of this broadcaster.
     * <p>Connections that fail to receive the event are closed and removed.
     *
     * @param event the event to broadcast
     * @return the number of connections that received the event; an event
     * without any fields set isn't sent and returns {@code 0}
     * @throws IllegalArgumentException when the event is {@code null}
     * @since 1.10
     */
    public int send(ServerSentEvent event) {
        return send(event, connection -> true);
    }

    /**
     * Sends an event to the open connections of this broadcaster that match
     * a filter.
     * <p>The filter receives each connection and can inspect its
     * {@link SseConnection#context() context} to decide whether the event
     * should be sent to it, for instance based on the authenticated identity
     * or on request parameters:
     * <pre>broadcaster.send(event, connection -&gt; {
     *     var identity = Identified.getIdentity(connection.context());
     *     return identity != null &amp;&amp; identity.getAttributes().isInRole("admin");
     * });</pre>
     * <p>A connection's context is the request that established it, so the
     * authentication state and the parameters that a filter sees are those
     * from the moment the client connected. Connections whose context is no
     * longer appropriate, for instance after a logout, can be terminated
     * with {@link #close(Predicate)}.
     * <p>Connections that fail to receive the event are closed and removed.
     * Connections that are filtered out are left untouched.
     * <p>Exceptions thrown by the filter propagate to the caller, and the
     * connections that weren't evaluated yet don't receive the event.
     *
     * @param event  the event to broadcast
     * @param filter the filter that determines which connections receive
     *               the event
     * @return the number of connections that received the event; an event
     * without any fields set isn't sent and returns {@code 0}
     * @throws IllegalArgumentException when the event or the filter is
     *                                  {@code null}
     * @since 1.10
     */
    public int send(ServerSentEvent event, Predicate<SseConnection> filter) {
        if (null == event) throw new IllegalArgumentException("event can't be null");
        if (null == filter) throw new IllegalArgumentException("filter can't be null");

        if (event.isEmpty()) {
            return 0;
        }

        if (history_ == null) {
            return sendToConnections(connections_, event, filter, null);
        }

        if (event.id() != null) {
            throw new IllegalArgumentException(
                "event IDs are assigned by the broadcaster when history is enabled, " +
                "remove the id() from the event, or manage reconnection with your " +
                "own IDs without history()");
        }

        String id_override = null;
        List<SseConnection> targets;
        synchronized (historyLock_) {
            if (carriesState(event)) {
                var id = ++lastEventId_;
                id_override = formatId(id);
                history_.addLast(new HistoryEntry(id, snapshotEvent(event), filter));
                while (history_.size() > historyCapacity_) {
                    history_.removeFirst();
                }
            }
            // capture the recipients atomically with the ID assignment, so
            // that a connection that registers concurrently either receives
            // this event through the replay or through this delivery
            targets = new ArrayList<>(connections_);
        }

        // deliver outside the history lock, so that a slow client doesn't
        // hold up other broadcasts or new registrations
        return sendToConnections(targets, event, filter, id_override);
    }

    private int sendToConnections(Iterable<SseConnection> targets, ServerSentEvent event, Predicate<SseConnection> filter, String idOverride) {
        // events without a template have the same payload for every
        // recipient and are formatted only once
        byte[] preformatted = null;
        if (event.template() == null) {
            preformatted = SseConnection.formatDataEventBytes(event, idOverride);
        }

        var sent = 0;
        for (var connection : targets) {
            if (!filter.test(connection)) {
                continue;
            }
            var delivered = preformatted != null ?
                connection.sendPreformatted(preformatted) :
                connection.send(event, idOverride);
            if (delivered) {
                sent += 1;
            } else {
                connections_.remove(connection);
            }
        }
        return sent;
    }

    private static boolean carriesState(ServerSentEvent event) {
        return event.name() != null ||
               !event.dataValues().isEmpty() ||
               event.template() != null;
    }

    private static ServerSentEvent snapshotEvent(ServerSentEvent event) {
        var snapshot = new ServerSentEvent();
        if (event.name() != null) {
            snapshot.name(event.name());
        }
        if (event.retryValue() >= 0) {
            snapshot.retry(event.retryValue());
        }
        for (var comment : event.comments()) {
            snapshot.comment(comment);
        }
        for (var data : event.dataValues()) {
            snapshot.data(data.toString());
        }
        var template = event.template();
        if (template != null) {
            // capture the template with the value assignments that are
            // active at the moment of broadcasting, the filtered tags are
            // resolved against each receiving connection at replay time
            var capture = (Template) template.clone();
            if (event.templateBlockId() != null) {
                snapshot.templateBlock(capture, event.templateBlockId());
            } else {
                snapshot.template(capture);
            }
        }
        return snapshot;
    }

    /**
     * Sends an event with the provided text as its data to all the open
     * connections of this broadcaster.
     *
     * @param data the data of the event
     * @return the number of connections that received the event
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public int send(CharSequence data) {
        return send(new ServerSentEvent().data(data));
    }

    /**
     * Sends an event with the content of the provided template as its data
     * to all the open connections of this broadcaster.
     * <p>The template's filtered tags are resolved against the context of
     * each individual connection.
     *
     * @param template the template whose content will be used as event data
     * @return the number of connections that received the event
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public int send(Template template) {
        return send(new ServerSentEvent().template(template));
    }

    /**
     * Sends an event with the content of a single template block as its
     * data to all the open connections of this broadcaster.
     * <p>The template's filtered tags are resolved against the context of
     * each individual connection.
     *
     * @param template the template that contains the block
     * @param blockId  the ID of the block whose content will be used as
     *                 event data
     * @return the number of connections that received the event
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public int send(Template template, String blockId) {
        return send(new ServerSentEvent().templateBlock(template, blockId));
    }

    /**
     * Sends a comment line to all the open connections, typically used as a
     * keep-alive heartbeat that also reaps stale connections.
     *
     * @param comment the comment text
     * @return the number of connections that received the comment
     * @see #send(ServerSentEvent)
     * @see #heartbeat
     * @since 1.10
     */
    public int comment(String comment) {
        return send(new ServerSentEvent().comment(comment));
    }

    /**
     * Automatically sends a keep-alive comment to all the open connections
     * at a fixed interval.
     * <p>Heartbeats prevent intermediaries from closing idle connections
     * and reap connections whose clients have disconnected, which would
     * otherwise hold on to their resources until the next
     * application-initiated broadcast. A client that stops reading without
     * disconnecting stalls its write instead of failing it, such a
     * connection is only reaped when the servlet container's connection
     * timeout fails the write. Intervals in the range of 15 to 30 seconds
     * are typical, and should stay shorter than the container's connection
     * timeout so that healthy idle streams are kept alive: the embedded
     * Jetty {@link Server} closes connections that are idle for 30 seconds
     * by default, so with that default a 15 second heartbeat is
     * appropriate, while a 30 second heartbeat races the timeout. Embedded
     * Tomcat defaults to 60 seconds.
     * <p>The heartbeat runs on a daemon thread. Calling this method again
     * replaces the previous interval, {@link #stopHeartbeat()} stops the
     * heartbeat, and {@link #close()} stops it as well.
     *
     * @param interval the interval between heartbeats
     * @return this broadcaster instance
     * @see #comment
     * @see #stopHeartbeat
     * @since 1.10
     */
    public SseBroadcaster heartbeat(Duration interval) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("interval must be positive");
        }

        synchronized (heartbeatLock_) {
            stopHeartbeat();
            heartbeatExecutor_ = Executors.newSingleThreadScheduledExecutor(runnable -> {
                var thread = new Thread(runnable, "sse-heartbeat-" + HEARTBEAT_THREAD_SEQUENCE.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });
            heartbeatExecutor_.scheduleAtFixedRate(() -> {
                try {
                    comment("keep-alive");
                } catch (Throwable e) {
                    // failures of individual connections are already handled
                    // by the send logic, never let anything else cancel the
                    // heartbeat schedule
                }
            }, interval.toNanos(), interval.toNanos(), TimeUnit.NANOSECONDS);
        }
        return this;
    }

    /**
     * Stops the {@link #heartbeat heartbeat}.
     * <p>The connections and the event history are unaffected, and a new
     * heartbeat can be established afterwards. Calling this method when no
     * heartbeat is active has no effect.
     *
     * @return this broadcaster instance
     * @see #heartbeat
     * @since 1.10
     */
    public SseBroadcaster stopHeartbeat() {
        synchronized (heartbeatLock_) {
            if (heartbeatExecutor_ != null) {
                heartbeatExecutor_.shutdownNow();
                heartbeatExecutor_ = null;
            }
        }
        return this;
    }

    /**
     * Retrieves the number of connections that are currently registered
     * with this broadcaster.
     * <p>Stale connections are only detected when sending to them fails,
     * so this count can include clients that have already disconnected.
     *
     * @return the number of registered connections
     * @since 1.10
     */
    public int connectionCount() {
        return connections_.size();
    }

    /**
     * Retrieves the ID of the most recently sent event when
     * {@link #history(int) history} is enabled.
     * <p>Pages can embed this ID in the URL of their SSE connection as the
     * {@code lastEventId} parameter, so that the events that occur between
     * the page render and the establishment of the stream are replayed.
     *
     * @return the ID of the most recently sent event; or the ID with
     * sequence number {@code 0} when no event has been sent yet
     * @see #history
     * @since 1.10
     */
    public String lastEventId() {
        synchronized (historyLock_) {
            return formatId(lastEventId_);
        }
    }

    /**
     * Retrieves a snapshot of the event history state, intended to help
     * with tuning the {@link #history(int) history} capacity.
     *
     * @return the current history statistics
     * @see SseHistoryStats
     * @since 1.10
     */
    public SseHistoryStats historyStats() {
        synchronized (historyLock_) {
            var oldest = history_ == null || history_.isEmpty() ? 0 : history_.peekFirst().id();
            var buffered = history_ == null ? 0 : history_.size();
            return new SseHistoryStats(historyCapacity_, buffered, oldest, lastEventId_,
                replays_, gaps_, maxMissedEvents_);
        }
    }

    /**
     * Closes the connections of this broadcaster that match a filter.
     * <p>This allows connections to be terminated when their context is no
     * longer appropriate, for instance after the user they belong to logged
     * out or lost a permission:
     * <pre>broadcaster.close(connection -&gt; {
     *     var identity = Identified.getIdentity(connection.context());
     *     return identity != null &amp;&amp; identity.getLogin().equals(login);
     * });</pre>
     * <p>The filter receives each connection like
     * {@link #send(ServerSentEvent, Predicate)} does, with the context of
     * the request that established the connection. The event history and
     * the {@link #heartbeat heartbeat} are unaffected.
     *
     * @param filter the filter that determines which connections are closed
     * @return the number of connections that were closed
     * @throws IllegalArgumentException when the filter is {@code null}
     * @since 1.10
     */
    public int close(Predicate<SseConnection> filter) {
        if (null == filter) throw new IllegalArgumentException("filter can't be null");

        var closed = 0;
        for (var connection : connections_) {
            if (filter.test(connection)) {
                connection.close();
                closed += 1;
            }
        }
        return closed;
    }

    /**
     * Closes all the current connections of this broadcaster, clears the
     * event history and stops the {@link #heartbeat heartbeat}.
     * <p>The broadcaster itself remains usable: new connections can still
     * register, subsequent events will be sent to them, and a heartbeat can
     * be established again.
     *
     * @since 1.10
     */
    @Override
    public void close() {
        stopHeartbeat();
        for (var connection : connections_) {
            connection.close();
        }
        connections_.clear();
        synchronized (historyLock_) {
            if (history_ != null) {
                history_.clear();
            }
        }
    }
}
