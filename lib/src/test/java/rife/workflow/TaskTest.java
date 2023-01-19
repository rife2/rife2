/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import rife.workflow.run.TaskRunner;
import rifeworkflowtasks.TestEventTypes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    void simple()
    throws Throwable {
        final var one_ended = new CountDownLatch(1);
        final var all_ended = new CountDownLatch(3);
        final var sum = new LongAdder();

        var runner = new TaskRunner();
        runner.addListener(event -> {
            if (TestEventTypes.END == event.getType()) {
                one_ended.countDown();
                all_ended.countDown();
                sum.add((Integer) event.getData());
            }
        });

        runner.start(rifeworkflowtasks.TaskType1.class);
        runner.start(rifeworkflowtasks.TaskType2.class);
        one_ended.await();

        runner.start(rifeworkflowtasks.TaskType2.class);
        all_ended.await();

        assertEquals(45 + 90 + 145, sum.sum());
    }
}