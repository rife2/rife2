/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Event;
import rife.workflow.Work;
import rife.workflow.Workflow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test fixture ensuring continuation instrumentation preserves the nestmate
 * relationship needed to access private members of an enclosing class.
 */
public class NestedPrivateFieldWork {
    private static final Object EVENT_TYPE = new Object();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface ParameterMarker {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    public @interface TypeMarker {
    }

    public static Object eventType() {
        return EVENT_TYPE;
    }

    public static class NestedWork implements Work {
        private final AtomicReference<Event> event_ = new AtomicReference<>();

        public void execute(@ParameterMarker @TypeMarker Workflow workflow) {
            event_.set(pauseForEvent(EVENT_TYPE));
            if (event_.get().getType() != EVENT_TYPE) {
                throw new IllegalStateException("Unexpected event type");
            }
        }

        public Event getEvent() {
            return event_.get();
        }
    }
}
