/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rifeworkflowtests.FailingAfterResumeWork;
import rifeworkflowtests.FailingWork;
import rifeworkflowtests.TestEventTypes;
import rifeworkflowtests.WorkPauseType1;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(60)
public class TestWorkflowErrors {
    public static class NoPauseWork implements Work {
        public void execute(Workflow workflow) {
            // completes without ever pausing, this doesn't require
            // instrumentation since pauseForEvent is never called
        }
    }

    // this work class deliberately lives inside the rife.workflow package,
    // which is excluded from bytecode instrumentation by the continuations
    // agent, so that pauseForEvent is guaranteed to throw
    // ContinuationsNotActiveException
    public static class NotInstrumentedWork implements Work {
        public void execute(Workflow workflow) {
            pauseForEvent(TestEventTypes.TYPE1);
        }
    }

    @Test
    void testFailingWorkDoesntHangWaits()
    throws Throwable {
        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(errors::add);

        workflow.start(new FailingWork());
        workflow.waitForNoWork();

        assertEquals(1, errors.size());
        var error = errors.get(0);
        assertTrue(error.getMessage().contains(FailingWork.class.getName()));
        assertFalse(error.getMessage().contains("instrumented for continuations"));
        assertInstanceOf(IllegalStateException.class, getRootCause(error));
        assertEquals("failing work", getRootCause(error).getMessage());
    }

    @Test
    void testFailingWorkStartedByClass()
    throws Throwable {
        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(errors::add);

        workflow.start(FailingWork.class);
        workflow.waitForNoWork();

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains(FailingWork.class.getName()));
    }

    @Test
    void testWaitForPausedWorkReturnsWhenWorkFails()
    throws Throwable {
        var workflow = new Workflow();
        workflow.addErrorListener(error -> {
            // silence the default logging
        });

        workflow.start(new FailingWork());

        assertFalse(workflow.waitForPausedWork());
    }

    @Test
    void testWaitForPausedWorkReturnsWhenWorkCompletesWithoutPausing()
    throws Throwable {
        var workflow = new Workflow();

        workflow.start(new NoPauseWork());

        assertFalse(workflow.waitForPausedWork());
        workflow.waitForNoWork();
    }

    @Test
    void testWaitForPausedWorkReturnsTrueForPausedWork()
    throws Throwable {
        var workflow = new Workflow();

        workflow.start(new WorkPauseType1());

        assertTrue(workflow.waitForPausedWork());

        workflow.trigger(TestEventTypes.TYPE1, null);
        workflow.waitForNoWork();
    }

    @Test
    void testFailureAfterResume()
    throws Throwable {
        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(errors::add);

        workflow.start(new FailingAfterResumeWork());
        assertTrue(workflow.waitForPausedWork());

        workflow.trigger(TestEventTypes.TYPE1, "payment");
        workflow.waitForNoWork();

        assertEquals(1, errors.size());
        var error = errors.get(0);
        assertTrue(error.getMessage().contains("resumed"));
        assertEquals("failing after resume for payment", getRootCause(error).getMessage());
    }

    @Test
    void testNotInstrumentedDiagnostic()
    throws Throwable {
        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(errors::add);

        workflow.start(new NotInstrumentedWork());
        workflow.waitForNoWork();

        assertEquals(1, errors.size());
        var error = errors.get(0);
        assertTrue(error.getMessage().contains(NotInstrumentedWork.class.getName()));
        assertTrue(error.getMessage().contains("instrumented for continuations"));
        assertTrue(error.getMessage().contains("-javaagent"));
        assertTrue(hasCauseOfType(error, rife.continuations.exceptions.ContinuationsNotActiveException.class));
    }

    @Test
    void testDefaultLoggingWithoutListeners()
    throws Throwable {
        var records = new CopyOnWriteArrayList<LogRecord>();
        var handler = new Handler() {
            public void publish(LogRecord record) {
                records.add(record);
            }

            public void flush() {
            }

            public void close() {
            }
        };
        var logger = Logger.getLogger("rife.workflow");
        logger.addHandler(handler);
        try {
            var workflow = new Workflow();
            workflow.start(new FailingWork());
            workflow.waitForNoWork();
        } finally {
            logger.removeHandler(handler);
        }

        assertEquals(1, records.size());
        assertTrue(records.get(0).getMessage().contains(FailingWork.class.getName()));
        assertTrue(records.get(0).getMessage().contains("failing work"));
    }

    @Test
    void testAddRemoveErrorListener()
    throws Throwable {
        var counter = new AtomicInteger();
        ErrorListener listener = error -> counter.incrementAndGet();

        var workflow = new Workflow();
        workflow.addErrorListener(listener);
        workflow.addErrorListener(null);
        workflow.removeErrorListener(null);

        workflow.start(new FailingWork());
        workflow.waitForNoWork();
        assertEquals(1, counter.get());

        workflow.removeErrorListener(listener);
        var records = new CopyOnWriteArrayList<LogRecord>();
        var handler = new Handler() {
            public void publish(LogRecord record) {
                records.add(record);
            }

            public void flush() {
            }

            public void close() {
            }
        };
        var logger = Logger.getLogger("rife.workflow");
        logger.addHandler(handler);
        try {
            workflow.start(new FailingWork());
            workflow.waitForNoWork();
        } finally {
            logger.removeHandler(handler);
        }

        assertEquals(1, counter.get());
        assertEquals(1, records.size());
    }

    @Test
    void testThrowingErrorListenerDoesntPreventOthers()
    throws Throwable {
        var errors = new CopyOnWriteArrayList<WorkErrorException>();
        var workflow = new Workflow();
        workflow.addErrorListener(error -> {
            throw new RuntimeException("listener failure");
        });
        workflow.addErrorListener(errors::add);

        workflow.start(new FailingWork());
        workflow.waitForNoWork();

        assertEquals(1, errors.size());
    }

    private static Throwable getRootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }

    private static boolean hasCauseOfType(Throwable e, Class<? extends Throwable> type) {
        while (e != null) {
            if (type.isInstance(e)) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
}
