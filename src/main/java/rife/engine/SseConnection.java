/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import rife.engine.exceptions.EngineException;
import rife.engine.exceptions.SseAsyncUnsupportedException;
import rife.template.Template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A single server-sent events (SSE) connection to a client.
 * <p>An SSE connection is obtained inside an element through
 * {@link Context#sse()} or
 * {@link Context#sse(SseBroadcaster)}. Creating it prepares
 * the response for event streaming: the content type is set to
 * {@code text/event-stream}, response buffering is disabled, and the
 * headers are flushed to the client so that it knows the stream has been
 * established. Every event that is sent afterwards is immediately flushed
 * to the client.
 * <p>The first variant keeps the element in control of the connection, which
 * stays open for as long as the element is executing:
 * <pre>get("/events", c -&gt; {
 *     var sse = c.sse();
 *     while (sse.isOpen()) {
 *         sse.send(new ServerSentEvent().name("tick").template(c.template("tick_fragment")));
 *         // ...
 *     }
 * });</pre>
 * <p>The second variant registers the connection with an
 * {@link SseBroadcaster} and detaches it from the element, which
 * returns immediately while the connection stays open. Events can then be
 * pushed from anywhere in the application through the broadcaster.
 * <p>Sending an event to a client that has disconnected marks the
 * connection as closed, which can be detected with {@link #isOpen()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see ServerSentEvent
 * @see SseBroadcaster
 * @since 1.10
 */
public class SseConnection implements AutoCloseable {
    /**
     * The content type of server-sent events streams.
     *
     * @since 1.10
     */
    public static final String CONTENT_TYPE_EVENT_STREAM = "text/event-stream";

    private final Context context_;
    private final Response response_;
    private final AsyncContext asyncContext_;
    private volatile boolean open_ = true;
    private volatile SseBroadcaster broadcaster_ = null;

    SseConnection(Context context, boolean detached) {
        context_ = context;
        response_ = context.response();

        // establish the asynchronous context before touching the response,
        // so that a failure to detach leaves the response pristine for the
        // regular error handling
        AsyncContext async_context = null;
        if (detached) {
            var servlet_request = context.request().getHttpServletRequest();
            if (servlet_request != null) {
                // startAsync with explicit arguments doesn't consistently
                // enforce the async-supported state of the filter chain
                // across containers, so it's checked explicitly
                if (!servlet_request.isAsyncSupported()) {
                    throw new SseAsyncUnsupportedException(
                        new IllegalStateException("asynchronous processing isn't supported for this request"));
                }
                try {
                    // pass the request and response explicitly, so that
                    // wrappers that were applied by upstream filters remain
                    // in effect for the asynchronous processing
                    async_context = servlet_request.startAsync(servlet_request, response_.getHttpServletResponse());
                } catch (IllegalStateException e) {
                    throw new SseAsyncUnsupportedException(e);
                }
                async_context.setTimeout(0);
                // the container reports client disconnects through the
                // asynchronous listener, which allows the connection to be
                // closed and deregistered without waiting for a failed send
                async_context.addListener(new AsyncListener() {
                    public void onComplete(AsyncEvent event) {
                        close();
                    }

                    public void onTimeout(AsyncEvent event) {
                        close();
                    }

                    public void onError(AsyncEvent event) {
                        close();
                    }

                    public void onStartAsync(AsyncEvent event) {
                    }
                });
            }
        }
        asyncContext_ = async_context;

        response_.setContentType(CONTENT_TYPE_EVENT_STREAM);
        response_.setHeader("Cache-Control", "no-cache");
        // prevent buffering proxies like nginx from delaying the events
        response_.setHeader("X-Accel-Buffering", "no");
        response_.enableTextBuffer(false);

        if (detached) {
            // only mark the response as detached once the asynchronous
            // context could be established, so that failures to detach are
            // handled like regular request errors
            if (response_ instanceof AbstractResponse abstract_response) {
                abstract_response.markDetached(this);
            }
        }

        // commit the response so the headers reach the client and it
        // knows the event stream has been established
        try {
            response_.getOutputStream().flush();
        } catch (IOException | EngineException e) {
            open_ = false;
            completeAsyncContext();
        }
    }

    void setBroadcaster(SseBroadcaster broadcaster) {
        broadcaster_ = broadcaster;
    }

    /**
     * Sends an event to the client of this connection.
     * <p>The event is immediately flushed. When the client has disconnected,
     * the connection is marked as closed and {@code false} is returned.
     *
     * @param event the event to send
     * @return {@code true} when the event was sent successfully; or
     * <p>{@code false} when the connection was closed
     * @since 1.10
     */
    public boolean send(ServerSentEvent event) {
        return send(event, null);
    }

    boolean send(ServerSentEvent event, String idOverride) {
        if (event == null || event.isEmpty()) {
            return open_;
        }

        if (!open_) {
            return false;
        }

        var formatted = formatEvent(event, idOverride);
        return writePayload(formatted.payload().getBytes(StandardCharsets.UTF_8), formatted.processedTemplate());
    }

    boolean sendPreformatted(byte[] payload) {
        return writePayload(payload, null);
    }

    private boolean writePayload(byte[] payload, Template processedTemplate) {
        if (!open_) {
            return false;
        }

        synchronized (this) {
            // re-check after acquiring the monitor, the connection can have
            // been closed while the event was being formatted
            if (!open_) {
                return false;
            }
            try {
                var out = response_.getOutputStream();
                out.write(payload);
                out.flush();
                if (response_ instanceof AbstractResponse abstract_response) {
                    abstract_response.sseEventSent(processedTemplate);
                }
                return true;
            } catch (IOException | EngineException e) {
                // close through the regular path, so that the connection is
                // also deregistered from its broadcaster
                close();
                return false;
            }
        }
    }

    /**
     * Sends an event with the provided text as its data.
     *
     * @param data the data of the event
     * @return {@code true} when the event was sent successfully; or
     * <p>{@code false} when the connection was closed
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public boolean send(CharSequence data) {
        return send(new ServerSentEvent().data(data));
    }

    /**
     * Sends an event with the content of the provided template as its data.
     *
     * @param template the template whose content will be used as event data
     * @return {@code true} when the event was sent successfully; or
     * <p>{@code false} when the connection was closed
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public boolean send(Template template) {
        return send(new ServerSentEvent().template(template));
    }

    /**
     * Sends an event with the content of a single template block as its
     * data.
     *
     * @param template the template that contains the block
     * @param blockId  the ID of the block whose content will be used as
     *                 event data
     * @return {@code true} when the event was sent successfully; or
     * <p>{@code false} when the connection was closed
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public boolean send(Template template, String blockId) {
        return send(new ServerSentEvent().templateBlock(template, blockId));
    }

    /**
     * Sends a comment line, typically used as a keep-alive heartbeat.
     *
     * @param comment the comment text
     * @return {@code true} when the comment was sent successfully; or
     * <p>{@code false} when the connection was closed
     * @see #send(ServerSentEvent)
     * @since 1.10
     */
    public boolean comment(String comment) {
        return send(new ServerSentEvent().comment(comment));
    }

    /**
     * Retrieves the ID of the last event that the client received.
     * <p>Browsers transmit this through the {@code Last-Event-ID} request
     * header when they reconnect. When that header is absent, the
     * {@code lastEventId} request parameter is used instead, which allows
     * pages to provide the ID of the last event that was rendered as part
     * of the stream URL for the initial connection.
     *
     * @return the last event ID; or {@code null} when the client didn't
     * provide one
     * @since 1.10
     */
    public String lastEventId() {
        var header = context_.request().getHeader("Last-Event-ID");
        if (header != null) {
            return header;
        }
        return context_.parameter("lastEventId");
    }

    /**
     * Indicates whether this connection is still open.
     * <p>A client disconnect is only detected when sending an event to it
     * fails, so a stale connection can still report being open until the
     * next event or {@link #comment(String) heartbeat} is sent.
     *
     * @return {@code true} when the connection is open; or
     * <p>{@code false} when it has been closed
     * @since 1.10
     */
    public boolean isOpen() {
        return open_;
    }

    /**
     * Retrieves the context of the request that established this
     * connection.
     *
     * @return this connection's context
     * @since 1.10
     */
    public Context context() {
        return context_;
    }

    /**
     * Closes this connection.
     * <p>For detached connections, this completes the underlying
     * asynchronous request and ends the response.
     *
     * @since 1.10
     */
    @Override
    public void close() {
        // serialize with in-flight event writes, so that no event can be
        // written to a response whose asynchronous context has completed
        synchronized (this) {
            open_ = false;
            completeAsyncContext();
        }
        var broadcaster = broadcaster_;
        if (broadcaster != null) {
            broadcaster.unregister(this);
        }
    }

    private void completeAsyncContext() {
        if (asyncContext_ != null) {
            try {
                asyncContext_.complete();
            } catch (IllegalStateException e) {
                // the async context has already been completed or recycled
            }
        }
    }

    private record FormattedEvent(String payload, Template processedTemplate) {
    }

    // formats an event whose payload doesn't depend on the receiving
    // connection, which allows broadcasters to format once for all
    // recipients
    static byte[] formatDataEventBytes(ServerSentEvent event, String idOverride) {
        var builder = new StringBuilder();
        appendHeaderFields(builder, event, idOverride);
        appendDataValues(builder, event);
        builder.append('\n');
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private FormattedEvent formatEvent(ServerSentEvent event, String idOverride) {
        var builder = new StringBuilder();

        appendHeaderFields(builder, event, idOverride);

        Template processed = null;
        var template = event.template();
        if (template != null) {
            // process a dedicated template instance for this connection, so
            // that the filtered tags resolve against this connection's own
            // context and concurrent sends don't mutate a shared template
            processed = (Template) template.clone();
            processed.setAttribute(Context.class.getName(), context_);
            new EngineTemplateProcessor(context_, processed).processTemplate();
            var block_id = event.templateBlockId();
            var content = block_id == null ? processed.getContent() : processed.getBlock(block_id);
            // a single trailing line break is a template file convention,
            // not meaningful event data, so it's not transmitted
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            } else if (content.endsWith("\n") || content.endsWith("\r")) {
                content = content.substring(0, content.length() - 1);
            }
            for (var line : splitLines(content)) {
                builder.append("data: ").append(line).append('\n');
            }
        }

        appendDataValues(builder, event);

        builder.append('\n');

        return new FormattedEvent(builder.toString(), processed);
    }

    private static void appendHeaderFields(StringBuilder builder, ServerSentEvent event, String idOverride) {
        for (var comment : event.comments()) {
            for (var line : splitLines(comment)) {
                builder.append(": ").append(line).append('\n');
            }
        }

        // the event name and ID are guaranteed to be single lines by the
        // ServerSentEvent setters, and ID overrides are broadcaster-generated
        if (event.name() != null) {
            builder.append("event: ").append(event.name()).append('\n');
        }

        var id = idOverride != null ? idOverride : event.id();
        if (id != null) {
            builder.append("id: ").append(id).append('\n');
        }

        if (event.retryValue() >= 0) {
            builder.append("retry: ").append(event.retryValue()).append('\n');
        }
    }

    private static void appendDataValues(StringBuilder builder, ServerSentEvent event) {
        for (var data : event.dataValues()) {
            for (var line : splitLines(data.toString())) {
                builder.append("data: ").append(line).append('\n');
            }
        }
    }

    private static String[] splitLines(String text) {
        return text.split("\r\n|\r|\n", -1);
    }
}
