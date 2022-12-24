/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStepBack {
    @Test
    public void testStepBackInWhile()
    throws Throwable {
        final var test_classes = new String[]{"TestStepBackCounter", "TestStepBackCounterInterface"};
        for (final var test_class : test_classes) {
            var runner = new ContinuableRunnerTest();

            var id1 = runner.start(TestStepBack.class.getPackage().getName() + "." + test_class);
            assertNotNull(id1);
            var context1 = runner.getManager().getContext(id1);

            var continuable1 = context1.getContinuable();
            var start_method = continuable1.getClass().getMethod("setStart", boolean.class);
            var total_method = continuable1.getClass().getMethod("getTotal");
            var answer_method = continuable1.getClass().getMethod("setAnswer", int.class);

            start_method.invoke(continuable1, Boolean.TRUE);
            var id2 = runner.resume(id1);
            assertNotNull(id2);
            assertNotEquals(id1, id2);
            var context2 = runner.getManager().getContext(id2);
            var continuable2 = context2.getContinuable();
            int total2 = (Integer) total_method.invoke(continuable2, new Object[0]);
            assertEquals(0, total2);

            answer_method.invoke(continuable2, 12);
            var id3 = runner.resume(id2);
            assertNotNull(id3);
            assertNotEquals(id1, id3);
            assertNotEquals(id2, id3);
            var context3 = runner.getManager().getContext(id3);
            var continuable3 = context3.getContinuable();
            int total3 = (Integer) total_method.invoke(continuable3, new Object[0]);
            assertEquals(12, total3);

            answer_method.invoke(continuable3, 32);
            var id4 = runner.resume(id3);
            assertNotNull(id4);
            assertNotEquals(id1, id4);
            assertNotEquals(id2, id4);
            assertNotEquals(id3, id4);
            var context4 = runner.getManager().getContext(id4);
            var continuable4 = context4.getContinuable();
            int total4 = (Integer) total_method.invoke(continuable4, new Object[0]);
            assertEquals(44, total4);

            answer_method.invoke(continuable4, 41);
            var id5 = runner.resume(id4);
            assertNull(id5);
        }
    }
}
