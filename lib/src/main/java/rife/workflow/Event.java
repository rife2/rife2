/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

/**
 * Events cause tasks to be resumed when they are waiting for the event type.
 * <p>You shouldn't instantiate events yourself, but rather rely on the
 * {@link rife.workflow.run.TaskRunner} to do so when an event is triggered.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Event {
    private final Task source_;
    private final Object type_;
    private final Object data_;

    /**
     * Creates a new event.
     *
     * @param source the task instance that triggered the event
     * @param type   the type of the event
     * @param data   the data that has to be sent along with the event
     * @since 1.0
     */
    public Event(final Task source, final Object type, final Object data) {
        source_ = source;
        type_ = type;
        data_ = data;
    }

    /**
     * Returns the task instance that triggered the event.
     *
     * @return this event's task instance
     * @since 1.0
     */
    public Task getSource() {
        return source_;
    }

    /**
     * Returns the type of this event.
     *
     * @return this event's type
     * @since 1.0
     */
    public Object getType() {
        return type_;
    }

    /**
     * Returns the data that was sent along with this event.
     *
     * @return this event's data
     * @since 1.0
     */
    public Object getData() {
        return data_;
    }
}