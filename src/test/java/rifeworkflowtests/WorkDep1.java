/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class WorkDep1 implements Work {
    @Override
    public void execute(Workflow workflow) {
        workflow.inform(TestEventTypes.BEGIN);

        int count;
        var sum = 0;
        for (count = 0; count < 20; ++count) {
            workflow.trigger(TestEventTypes.TYPE2, count);

            var event = pauseForEvent(TestEventTypes.TYPE1);

            sum += (Integer) event.getData();
        }

        workflow.inform(TestEventTypes.END, sum);
    }
}