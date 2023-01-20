/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import rife.workflow.run.TaskRunner;
import rifeworkflowtasks.*;

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
                sum.add((Integer) event.getData());

                one_ended.countDown();
                all_ended.countDown();
            }
        });

        runner.start(new Task1());
        runner.start(rifeworkflowtasks.Task2.class);
        one_ended.await();

        runner.start(new Task2());
        all_ended.await();

        assertEquals(45 + 90 + 145, sum.sum());
    }
}