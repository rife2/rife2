/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class WorkDep2 implements Work {
    @Override
    public void execute(Workflow workflow) {
        workflow.inform(TestEventTypes.BEGIN);

        int count;
        var sum = 0;
        for (count = 0; count < 10; ++count) {
            workflow.trigger(TestEventTypes.TYPE1, count);

            var event = pauseForEvent(TestEventTypes.TYPE2);

            sum += (Integer) event.getData();
        }

        workflow.inform(TestEventTypes.END, sum);
    }
}