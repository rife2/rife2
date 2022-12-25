/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import rife.workflow.run.EventListener;
import rife.workflow.run.TaskRunner;
import rifeworkflowtasks.TestEventTypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    public void simple()
    throws Throwable {
        final var endings = new int[]{0};
        final var sum = new int[]{0};
        final EventListener listener = event -> {
            if (TestEventTypes.END == event.getType()) {
                synchronized (sum) {
                    endings[0]++;

                    sum[0] += (Integer) event.getData();
                    sum.notifyAll();
                }
            }
        };
        var runner = new TaskRunner();
        runner.addListener(listener);

        runner.start("rifeworkflowtasks.TaskType1");
        runner.start("rifeworkflowtasks.TaskType2");
        while (endings[0] < 1) {
            synchronized (sum) {
                sum.wait();
            }
        }

        runner.start("rifeworkflowtasks.TaskType2");
        while (endings[0] < 3) {
            synchronized (sum) {
                sum.wait();
            }
        }

        assertEquals(45 + 90 + 145, sum[0]);
    }
}