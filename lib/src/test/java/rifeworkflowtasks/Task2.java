/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtasks;

import rife.workflow.Task;
import rife.workflow.run.TaskRunner;

public class Task2 implements Task {
    public void execute(TaskRunner runner) {
        runner.trigger(TestEventTypes.BEGIN);

        int count;
        var sum = 0;
        for (count = 0; count < 10; ++count) {
            runner.trigger(TestEventTypes.TYPE1, count);

            var event = waitForEvent(TestEventTypes.TYPE2);

            sum += (Integer) event.getData();
        }

        runner.trigger(TestEventTypes.END, sum);
    }
}