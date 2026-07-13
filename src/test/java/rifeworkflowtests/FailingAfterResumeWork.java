/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class FailingAfterResumeWork implements Work {
    public void execute(Workflow workflow) {
        var event = pauseForEvent(TestEventTypes.TYPE1);
        throw new IllegalStateException("failing after resume for " + event.getData());
    }
}
