/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.json.Json;
import rife.json.JsonArray;
import rife.json.JsonObject;
import rife.template.Template;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A single server-sent event that can be sent through an {@link SseConnection}
 * or broadcast to all the connections of an {@link SseBroadcaster}.
 * <p>All the fields are optional, they will only be part of the event on the
 * wire when they have been set. Events are built fluently:
 * <pre>sse.send(new ServerSentEvent().name("update").id("42").data("changed"));</pre>
 * <p>Data can be provided as text with {@code data()}, or as a
 * {@link Template} or a single template block with {@code template()} and
 * {@code templateBlock()}. Templates are processed exactly like
 * {@link Context#print(Template)} processes them, with
 * all the filtered tags resolved against the context of each receiving
 * connection, making them convenient for sending HTML fragments, for
 * instance for the htmx {@code sse} extension.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseConnection
 * @see SseBroadcaster
 * @since 1.10
 */
public class ServerSentEvent {
    private String name_ = null;
    private String id_ = null;
    private int retry_ = -1;
    private final List<String> comments_ = new ArrayList<>();
    private final List<CharSequence> data_ = new ArrayList<>();
    private Template template_ = null;
    private String templateBlockId_ = null;

    /**
     * Sets the event name, transmitted as the {@code event} field.
     * <p>Browsers dispatch named events to the listener that was registered
     * for that particular name.
     *
     * @param name the name of the event
     * @return this event instance
     * @throws IllegalArgumentException when the name contains line breaks
     * @since 1.10
     */
    public ServerSentEvent name(String name) {
        requireSingleLine("name", name);
        name_ = name;
        return this;
    }

    /**
     * Sets the event ID, transmitted as the {@code id} field.
     * <p>Browsers echo the last received ID back in the
     * {@code Last-Event-ID} header when they reconnect, which can be
     * retrieved with {@link SseConnection#lastEventId()}.
     * <p>Event IDs are the application's reconnection strategy. Broadcasters
     * with {@link SseBroadcaster#history(int) history} enabled assign their
     * own IDs and refuse events that carry one.
     *
     * @param id the ID of the event
     * @return this event instance
     * @throws IllegalArgumentException when the ID contains line breaks or
     *                                  NUL characters, browsers ignore IDs
     *                                  with NUL characters when tracking
     *                                  their last event ID
     * @since 1.10
     */
    public ServerSentEvent id(String id) {
        requireSingleLine("id", id);
        if (id != null && id.indexOf('\0') != -1) {
            throw new IllegalArgumentException("the event id can't contain NUL characters");
        }
        id_ = id;
        return this;
    }

    private static void requireSingleLine(String field, String value) {
        if (value != null &&
            (value.indexOf('\n') != -1 || value.indexOf('\r') != -1)) {
            throw new IllegalArgumentException("the event " + field + " can't contain line breaks");
        }
    }

    /**
     * Sets the reconnection time in milliseconds, transmitted as the
     * {@code retry} field.
     *
     * @param milliseconds the reconnection time in milliseconds
     * @return this event instance
     * @throws IllegalArgumentException when the reconnection time is
     *                                  negative
     * @since 1.10
     */
    public ServerSentEvent retry(int milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("the retry value can't be negative");
        }
        retry_ = milliseconds;
        return this;
    }

    /**
     * Adds a comment line to the event.
     * <p>Comments are ignored by browsers and are typically used as
     * keep-alive heartbeats to prevent proxies from closing idle
     * connections.
     *
     * @param comment the comment text
     * @return this event instance
     * @since 1.10
     */
    public ServerSentEvent comment(String comment) {
        if (comment != null) {
            comments_.add(comment);
        }
        return this;
    }

    /**
     * Adds data to the event, transmitted as {@code data} fields.
     * <p>This method can be called multiple times, each call adds another
     * line of data. Data that itself contains line breaks will automatically
     * be transmitted as multiple {@code data} fields.
     *
     * @param data the data to add
     * @return this event instance
     * @since 1.10
     */
    public ServerSentEvent data(CharSequence data) {
        if (data != null) {
            data_.add(data);
        }
        return this;
    }

    /**
     * Adds the compact JSON representation of a value as the data of the
     * event, transmitted as a {@code data} field.
     * <p>This accepts the same values as {@link Json#toString(Object)} —
     * strings, numbers, booleans, {@code null}, maps, collections, arrays,
     * dates, temporals, enums, {@link JsonObject} and {@link JsonArray} —
     * and additionally converts beans and records, as well as the elements
     * of collections, through {@link Json#from}.
     * <p>Since compact JSON contains no line breaks, the value is always
     * transmitted as a single {@code data} field, which clients can parse
     * directly with {@code JSON.parse(event.data)}.
     *
     * @param value the value whose JSON representation will be added as
     *              event data
     * @return this event instance
     * @see #data
     * @since 1.10
     */
    public ServerSentEvent json(Object value) {
        data_.add(Json.toString(jsonValue(value)));
        return this;
    }

    private static Object jsonValue(Object value) {
        if (value instanceof Collection<?> collection) {
            // also converts bean and record elements
            return Json.from(collection);
        }
        if (value == null ||
            value instanceof CharSequence ||
            value instanceof Character ||
            value instanceof Boolean ||
            value instanceof Number ||
            value instanceof Enum<?> ||
            value instanceof Date ||
            value instanceof TemporalAccessor ||
            value instanceof Map<?, ?> ||
            value instanceof JsonObject ||
            value instanceof JsonArray ||
            value.getClass().isArray()) {
            return value;
        }
        return Json.from(value);
    }

    /**
     * Uses the content of a template as the data of the event.
     * <p>The template will be processed exactly like
     * {@link Context#print(Template)} processes it, resolving all the
     * filtered tags against the context of each connection that receives
     * the event.
     * <p>A single trailing line break of the template content is not
     * transmitted, since it's a template file convention rather than
     * meaningful event data.
     *
     * @param template the template whose content will be used as event data
     * @return this event instance
     * @see #templateBlock
     * @since 1.10
     */
    public ServerSentEvent template(Template template) {
        template_ = template;
        templateBlockId_ = null;
        return this;
    }

    /**
     * Uses the content of a single template block as the data of the event.
     * <p>The template will be processed exactly like
     * {@link Context#print(Template)} processes it, after which only the
     * content of the provided block is used, with the value assignments
     * that are active at that moment.
     * <p>This allows a page template and the event fragments that update it
     * to be maintained in the same template file.
     *
     * @param template the template that contains the block
     * @param blockId  the ID of the block whose content will be used as
     *                 event data
     * @return this event instance
     * @see #template
     * @since 1.10
     */
    public ServerSentEvent templateBlock(Template template, String blockId) {
        template_ = template;
        templateBlockId_ = blockId;
        return this;
    }

    String name() {
        return name_;
    }

    String id() {
        return id_;
    }

    int retryValue() {
        return retry_;
    }

    List<String> comments() {
        return comments_;
    }

    List<CharSequence> dataValues() {
        return data_;
    }

    Template template() {
        return template_;
    }

    String templateBlockId() {
        return templateBlockId_;
    }

    boolean isEmpty() {
        return name_ == null &&
            id_ == null &&
            retry_ < 0 &&
            comments_.isEmpty() &&
            data_.isEmpty() &&
            template_ == null;
    }
}
