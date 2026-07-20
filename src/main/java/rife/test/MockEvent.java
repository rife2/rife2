/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.test;

import rife.json.Json;
import rife.json.JsonObject;
import rife.json.JsonParseException;
import rife.template.Template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Corresponds to a single event that was parsed from a server-sent events
 * (SSE) response, as a result of {@link MockResponse#getEvents()}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see MockResponse#getEvents()
 * @since 1.10
 */
public class MockEvent {
    private String name_ = null;
    private String id_ = null;
    private int retry_ = -1;
    private final List<String> comments_ = new ArrayList<>();
    private final List<String> dataLines_ = new ArrayList<>();
    private Template template_ = null;

    MockEvent() {
    }

    void setTemplate(Template template) {
        template_ = template;
    }

    void setName(String name) {
        name_ = name;
    }

    void setId(String id) {
        id_ = id;
    }

    void setRetry(int retry) {
        retry_ = retry;
    }

    void addComment(String comment) {
        comments_.add(comment);
    }

    void addDataLine(String dataLine) {
        dataLines_.add(dataLine);
    }

    boolean isEmpty() {
        return name_ == null &&
            id_ == null &&
            retry_ < 0 &&
            comments_.isEmpty() &&
            dataLines_.isEmpty();
    }

    /**
     * Retrieves the name of this event, transmitted as the {@code event}
     * field.
     *
     * @return this event's name; or {@code null} when it was an unnamed
     * event
     * @since 1.10
     */
    public String getName() {
        return name_;
    }

    /**
     * Retrieves the ID of this event, transmitted as the {@code id} field.
     *
     * @return this event's ID; or {@code null} when no ID was provided
     * @since 1.10
     */
    public String getId() {
        return id_;
    }

    /**
     * Retrieves the reconnection time in milliseconds, transmitted as the
     * {@code retry} field.
     *
     * @return this event's reconnection time; or {@code -1} when none was
     * provided
     * @since 1.10
     */
    public int getRetry() {
        return retry_;
    }

    /**
     * Retrieves the comments of this event.
     *
     * @return this event's comments; or an empty list when there were none
     * @since 1.10
     */
    public List<String> getComments() {
        return Collections.unmodifiableList(comments_);
    }

    /**
     * Retrieves the data of this event, with multiple {@code data} fields
     * joined by line feeds, the same way browsers assemble the event data.
     *
     * @return this event's data; or {@code null} when the event carried no
     * data
     * @since 1.10
     */
    public String getData() {
        if (dataLines_.isEmpty()) {
            return null;
        }
        return String.join("\n", dataLines_);
    }

    /**
     * Parses the data of this event as a JSON object.
     *
     * @return this event's data as a JSON object
     * @throws JsonParseException when the data isn't a valid JSON object
     * @see #getData
     * @since 1.10
     */
    public JsonObject getDataAsJsonObject() {
        return Json.parseObject(getData());
    }

    /**
     * Parses the data of this event as a JSON object and fills a new bean
     * instance with its members, like {@link Json#toBean} does.
     *
     * @param <T>       the type of the bean
     * @param beanClass the class of the bean to instantiate
     * @return the new bean instance with this event's data filled in
     * @throws JsonParseException when the data isn't a valid JSON object
     * @see #getDataAsJsonObject
     * @since 1.10
     */
    public <T> T getDataAsBean(Class<T> beanClass) {
        return Json.toBean(getDataAsJsonObject(), beanClass);
    }

    /**
     * Retrieves the template instance that provided the data of this event.
     * <p>The template is captured with the value assignments that were
     * active at the moment the event was sent, which makes it possible to
     * assert on the values of streamed fragments without parsing the event
     * data.
     *
     * @return the template that provided this event's data; or
     * <p>{@code null} when the event's data didn't come from a template
     * @since 1.10
     */
    public Template getTemplate() {
        return template_;
    }
}
