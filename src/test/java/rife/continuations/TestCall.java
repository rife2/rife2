/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import org.junit.jupiter.api.Test;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestCall {
    @Test
    void testSimpleCall()
    throws Throwable {
        final var test_classes = new String[]{"TestCallSimpleCallSource", "TestCallSimpleCallInterfaceSource"};
        for (final var test_class : test_classes) {
            var runner = new ContinuableRunnerTest();

            var id1 = runner.start(TestCall.class.getPackage().getName() + "." + test_class);
            assertNull(id1);
            var continuable = runner.getCurrentContinuable();
            assertNotNull(continuable);
            var method_get_result = continuable.getClass().getMethod("getResult");
            assertEquals("before call\nduring call target 1\nduring call target 2\nafter call", method_get_result.invoke(continuable));
        }
    }

    @Test
    void testAnswerInOtherThread()
    throws Throwable {
        final var test_classes = new String[]{"TestCallAnswerInOtherThreadCallSource", "TestCallAnswerInOtherThreadCallInterfaceSource"};
        for (final var test_class : test_classes) {
            final var runner = new ContinuableRunnerTest();
            final var continuables = new Object[2];
            final var ids = new String[2];

            var thread1 = new Thread() {
                @Override
                public void run() {
                    try {
                        var id = runner.start(TestCall.class.getPackage().getName() + "." + test_class);
                        assertNull(id);
                        ids[0] = ContinuationContext.getLastContext().getId();
                        continuables[0] = ContinuationContext.getLastContext().getContinuable();
                        assertNotNull(continuables[0]);
                        var method_setdoanswer = continuables[0].getClass().getMethod("setDoAnswer", boolean.class);
                        method_setdoanswer.invoke(continuables[0], Boolean.TRUE);
                    } catch (Throwable e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    } finally {
                        synchronized (this) {
                            this.notifyAll();
                        }
                    }
                }
            };

            var thread2 = new Thread() {
                @Override
                public void run() {
                    try {
                        var id = runner.run(ids[0]);
                        assertNull(id);
                        continuables[1] = runner.getCurrentContinuable();
                        assertNotNull(continuables[1]);
                        var method_getresult = continuables[1].getClass().getMethod("getResult");
                        assertEquals("before call\ntrue\nafter call", method_getresult.invoke(continuables[1]));
                    } catch (Throwable e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    } finally {
                        synchronized (this) {
                            this.notifyAll();
                        }
                    }
                }
            };

            synchronized (thread1) {
                thread1.start();
                while (thread1.isAlive()) {
                    thread1.wait();
                }
            }

            synchronized (thread2) {
                thread2.start();
                while (thread2.isAlive()) {
                    thread2.wait();
                }
            }
        }
    }

    @Test
    void testMultipleAnswersWithTryCatch()
    throws Throwable {
        final var test_classes = new String[]{"TestCallMultipleAnswersWithTryCatchSource", "TestCallSimpleCallInterfaceSource"};
        for (final String test_class : test_classes) {
            var runner = new ContinuableRunnerTest();

            var id1 = runner.start(TestCall.class.getPackage().getName() + "." + test_class);
            assertNull(id1);
            var continuable = runner.getCurrentContinuable();
            assertNotNull(continuable);
            var method_get_result = continuable.getClass().getMethod("getResult");
            assertEquals("before call\nduring call target 1\nduring call target 2\nafter call", method_get_result.invoke(continuable));
        }
    }
}
