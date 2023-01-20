/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class Work2 implements Work {
    public void execute(Workflow workflow) {
        workflow.trigger(TestEventTypes.BEGIN);

        int count;
        var sum = 0;
        for (count = 0; count < 10; ++count) {
            workflow.trigger(TestEventTypes.TYPE1, count);

            var event = waitForEvent(TestEventTypes.TYPE2);

            sum += (Integer) event.getData();
        }

        workflow.trigger(TestEventTypes.END, sum);
    }
}