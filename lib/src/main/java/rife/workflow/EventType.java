/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

/**
 * The type of an event.
 * <p>The only basic requirement is that a unique textual type identifier is
 * present for each logically different type. Apart from that, classes can
 * extend this base class to create more elaborate constructs, if required.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class EventType {
    /**
     * Returns the textual type identifier that uniquely defines an event type.
     *
     * @return the textual type identifier
     * @since 1.0
     */
    public abstract String getType();

    public String toString() {
        return getType();
    }

    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (obj == this) return true;
        if (!(obj instanceof EventType)) return false;
        return ((EventType) obj).getType().equals(getType());
    }

    public int hashCode() {
        return getType().hashCode();
    }
}