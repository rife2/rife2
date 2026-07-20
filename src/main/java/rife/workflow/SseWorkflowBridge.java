/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import rife.engine.SseBroadcaster;
import rife.engine.ServerSentEvent;

import java.util.function.Function;

/**
 * Bridges workflow events to a server-sent events (SSE)
 * {@link SseBroadcaster}, so that every event that is triggered or
 * informed about in a {@link Workflow} is pushed to all the connected SSE
 * clients.
 * <p>The bridge is registered like any other workflow listener:
 * <pre>workflow.addListener(new SseWorkflowBridge(broadcaster));</pre>
 * <p>By default, a workflow event is converted with the string
 * representation of its type as the SSE event name, and the string
 * representation of its data as the SSE event data. A custom converter can
 * be provided instead, which also makes it possible to render templates as
 * event data or to filter events by returning {@code null}:
 * <pre>workflow.addListener(new SseWorkflowBridge(broadcaster, event -&gt; {
 *     if (event.getType() != MyTypes.PROGRESS) return null;
 *     var t = TemplateFactory.HTML.get("progress_fragment");
 *     t.setValueEncoded("step", event.getData());
 *     return new ServerSentEvent().name("progress").template(t);
 * }));</pre>
 * <p>The converter runs on the thread that triggers the workflow event, and
 * exceptions it throws propagate to the caller of the trigger, while paused
 * work is still resumed.
 *
 * @rife.apiNote The workflow engine is in a BETA STAGE and might still change.
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseBroadcaster
 * @see Workflow#addListener
 * @since 1.10
 */
public class SseWorkflowBridge implements EventListener {
    private final SseBroadcaster broadcaster_;
    private final Function<Event, ServerSentEvent> converter_;

    /**
     * Creates a new bridge that converts workflow events with the default
     * conversion: the string representation of the event type becomes the
     * SSE event name and the string representation of the event data, when
     * present, becomes the SSE event data.
     *
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @since 1.10
     */
    public SseWorkflowBridge(SseBroadcaster broadcaster) {
        this(broadcaster, SseWorkflowBridge::convertEvent);
    }

    /**
     * Creates a new bridge with a custom event converter.
     * <p>The converter receives every workflow event and returns the
     * server-sent event to broadcast, or {@code null} to not broadcast
     * anything for that workflow event.
     *
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @param converter   the converter that will be used to convert
     *                    workflow events into server-sent events
     * @since 1.10
     */
    public SseWorkflowBridge(SseBroadcaster broadcaster, Function<Event, ServerSentEvent> converter) {
        if (null == broadcaster) throw new IllegalArgumentException("broadcaster can't be null");
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        broadcaster_ = broadcaster;
        converter_ = converter;
    }

    /**
     * Creates a new bridge whose conversion transmits JSON event data,
     * suitable for clients that parse the events with
     * {@code JSON.parse(event.data)}.
     * <p>The string representation of the workflow event type becomes the
     * SSE event name, like the default conversion, and the event data,
     * when present, is converted with {@link ServerSentEvent#json}.
     *
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @return the new bridge instance
     * @since 1.10
     */
    public static SseWorkflowBridge json(SseBroadcaster broadcaster) {
        return new SseWorkflowBridge(broadcaster, event -> {
            var server_sent_event = new ServerSentEvent().name(String.valueOf(event.getType()));
            if (event.getData() != null) {
                server_sent_event.json(event.getData());
            }
            return server_sent_event;
        });
    }

    public void eventTriggered(Event event) {
        var server_sent_event = converter_.apply(event);
        if (server_sent_event != null) {
            broadcaster_.send(server_sent_event);
        }
    }

    private static ServerSentEvent convertEvent(Event event) {
        var server_sent_event = new ServerSentEvent().name(String.valueOf(event.getType()));
        if (event.getData() != null) {
            server_sent_event.data(String.valueOf(event.getData()));
        }
        return server_sent_event;
    }
}
