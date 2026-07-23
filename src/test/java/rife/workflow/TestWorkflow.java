/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rifeworkflowtests.CountdownWork;
import rifeworkflowtests.DurableHandoffWork;
import rifeworkflowtests.NestedPrivateFieldWork;
import rifeworkflowtests.TestEventTypes;
import rifeworkflowtests.WorkDep1;
import rifeworkflowtests.WorkDep2;
import rifeworkflowtests.WorkPauseType1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWorkflow {
    @Test
    void testCodependency()
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

        workflow.start(new WorkDep1());
        workflow.start(WorkDep2.class);
        one_ended.await();

        workflow.start(new WorkDep2());
        all_ended.await();

        assertEquals(45 + 90 + 145, sum.sum());
    }

    @Test
    void testTrigger()
    throws Throwable {
        var wf = new Workflow();
        wf.trigger(TestEventTypes.TYPE1, 1);
        var work = new WorkPauseType1();
        wf.start(work);
        wf.waitForNoWork();

        assertEquals(1, work.getEvent().getData());
    }

    @Test
    void testInform()
    throws Throwable {
        var wf = new Workflow();
        wf.inform(TestEventTypes.TYPE1, 1);
        var work = new WorkPauseType1();
        wf.start(work);
        wf.waitForPausedWork();

        wf.inform(TestEventTypes.TYPE1, 2);
        wf.waitForNoWork();

        assertEquals(2, work.getEvent().getData());
    }

    @Test
    void testNestedWorkCanAccessPrivateEnclosingField()
    throws Throwable {
        var execute = NestedPrivateFieldWork.NestedWork.class.getMethod("execute", Workflow.class);
        assertTrue(execute.getParameters()[0].isAnnotationPresent(NestedPrivateFieldWork.ParameterMarker.class));
        assertTrue(execute.getAnnotatedParameterTypes()[0].isAnnotationPresent(NestedPrivateFieldWork.TypeMarker.class));

        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(errors::add);
        var work = new NestedPrivateFieldWork.NestedWork();

        workflow.start(work);
        assertTrue(workflow.waitForPausedWork());
        workflow.trigger(NestedPrivateFieldWork.eventType(), 42);
        workflow.waitForNoWork();

        assertTrue(errors.isEmpty());
        assertEquals(42, work.getEvent().getData());
    }

    @Test
    @Timeout(60)
    void testDurableHandoffsOnCachedThreadPool()
    throws Throwable {
        final var runs = 25;
        final var rounds = 20;
        var expected = new ArrayList<Integer>();
        for (var i = 1; i <= rounds; ++i) {
            expected.add(i);
        }

        for (var run = 0; run < runs; ++run) {
            var executor = Executors.newCachedThreadPool();
            try {
                var errors = new CopyOnWriteArrayList<WorkErrorException>();
                var received = new CopyOnWriteArrayList<Integer>();
                var workflow = new Workflow(executor);
                workflow.addErrorListener(errors::add);

                workflow.start(new DurableHandoffWork.Consumer(rounds, received));
                workflow.start(new DurableHandoffWork.Producer(rounds));
                workflow.waitForNoWork();

                assertTrue(errors.isEmpty());
                assertEquals(expected, received);
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test
    @Timeout(60)
    void testThrowingListenerStillResumesWork()
    throws Throwable {
        var workflow = new Workflow();
        workflow.addListener(event -> {
            throw new RuntimeException("listener failure");
        });

        var work = new WorkPauseType1();
        workflow.start(work);
        assertTrue(workflow.waitForPausedWork());

        // the listener failure propagates to the caller of trigger, but the
        // paused work is still resumed with the event
        assertThrows(RuntimeException.class, () -> workflow.trigger(TestEventTypes.TYPE1, 42));
        workflow.waitForNoWork();

        assertEquals(42, work.getEvent().getData());
    }

    @Test
    @Timeout(60)
    void testBlockedListenerDoesNotSignalCompletion()
    throws Throwable {
        var workflow = new Workflow();
        var listener_entered = new CountDownLatch(1);
        var release_listener = new CountDownLatch(1);
        workflow.addListener(event -> {
            listener_entered.countDown();
            try {
                release_listener.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        var work = new WorkPauseType1();
        workflow.start(work);
        assertTrue(workflow.waitForPausedWork());

        var no_work_reported = new java.util.concurrent.atomic.AtomicBoolean(false);
        var waiter = new Thread(() -> {
            try {
                workflow.waitForNoWork();
                no_work_reported.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        var trigger = new Thread(() -> workflow.trigger(TestEventTypes.TYPE1, 42));
        trigger.start();
        listener_entered.await();

        // the captured continuation is guaranteed to resume after the
        // listeners, so the workflow doesn't report completion while a
        // listener is still running
        waiter.start();
        Thread.sleep(300);
        assertFalse(no_work_reported.get());

        release_listener.countDown();
        trigger.join();
        waiter.join();
        assertTrue(no_work_reported.get());
        assertEquals(42, work.getEvent().getData());
    }

    @Test
    @Timeout(60)
    void testListenerCausalOrdering()
    throws Throwable {
        // events are delivered to listeners before work is resumed, so the
        // event that wakes up work is always seen before the events that
        // the resumed work triggers
        for (var run = 0; run < 50; run++) {
            var types = new CopyOnWriteArrayList<Object>();
            var workflow = new Workflow();
            workflow.addListener(event -> types.add(event.getType()));

            workflow.start(new CountdownWork());
            assertTrue(workflow.waitForPausedWork());
            workflow.trigger(CountdownWork.Types.START, 1);
            workflow.waitForNoWork();

            assertEquals(List.of(CountdownWork.Types.START, CountdownWork.Types.TICK, CountdownWork.Types.DONE), types);
        }
    }
}
