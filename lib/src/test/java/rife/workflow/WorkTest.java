/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import rifeworkflowtests.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkTest {
    @Test
    void simple()
    throws Throwable {
        final var one_ended = new CountDownLatch(1);
        final var all_ended = new CountDownLatch(3);
        final var sum = new LongAdder();

        var workflow = new Workflow();
        workflow.addListener(event -> {
            if (TestEventTypes.END == event.getType()) {
                sum.add((Integer) event.getData());

                one_ended.countDown();
                all_ended.countDown();
            }
        });

        workflow.start(new Work1());
        workflow.start(Work2.class);
        one_ended.await();

        workflow.start(new Work2());
        all_ended.await();

        assertEquals(45 + 90 + 145, sum.sum());
    }
}