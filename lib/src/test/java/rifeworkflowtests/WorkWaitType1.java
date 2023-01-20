/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.*;

public class WorkWaitType1 implements Work {
    private Event event_;
    public void execute(Workflow workflow) {
        event_ = pauseForEvent(TestEventTypes.TYPE1);
        System.out.println(this + " " + event_);
    }

    public Event getEvent() {
        System.out.println(this + " " + event_);
        return event_;
    }
}