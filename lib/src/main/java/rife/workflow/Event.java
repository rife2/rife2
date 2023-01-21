/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

/**
 * Events cause work to be resumed when they are waiting for the event type.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 * @apiNote The workflow engine is still in an ALPHA EXPERIMENTAL STAGE and might change.
 */
public class Event {
    private final Object type_;
    private final Object data_;

    /**
     * Creates a new event.
     *
     * @param type the type of the event
     * @param data the data that has to be sent along with the event
     * @since 1.0
     */
    public Event(final Object type, final Object data) {
        type_ = type;
        data_ = data;
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