/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.*;

import java.util.concurrent.atomic.AtomicReference;

public class WorkWaitType1 implements Work {
    private AtomicReference<Event> event_ = new AtomicReference<>();
    public void execute(Workflow workflow) {
        event_.set(pauseForEvent(TestEventTypes.TYPE1));
    }

    public Event getEvent() {
        return event_.get();
    }
}