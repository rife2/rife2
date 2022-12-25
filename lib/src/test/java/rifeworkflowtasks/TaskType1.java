/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtasks;

import rife.workflow.Event;
import rife.workflow.Task;
import rife.workflow.run.TaskRunner;

public class TaskType1 extends Task {
    public void execute(TaskRunner runner) {
        trigger(runner, TestEventTypes.BEGIN);

        int count;
        int sum = 0;
        for (count = 0; count < 20; count++) {
            trigger(runner, TestEventTypes.TYPE2, count);

            Event event = waitForEvent(TestEventTypes.TYPE1);

            sum += (Integer) event.getData();
        }

        trigger(runner, TestEventTypes.END, sum);
    }
}