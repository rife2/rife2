/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPause {
    @Test
    public void testPauseInWhile()
    throws Throwable {
        final var test_classes = new String[]{"TestPauseInWhile", "TestPauseInWhileInterface"};
        for (final var test_class : test_classes) {
            var runner = new ContinuableRunnerTest();

            var id1 = runner.start(TestPause.class.getPackage().getName() + "." + test_class);
            assertNotNull(id1);
            var context1 = runner.getManager().getContext(id1);
            assertEquals(5, context1.getLocalVars().getInt(1));

            var id2 = runner.resume(id1);
            assertNotNull(id2);
            assertNotEquals(id1, id2);
            var context2 = runner.getManager().getContext(id2);
            assertEquals(4, context2.getLocalVars().getInt(1));

            var id3 = runner.resume(id2);
            assertNotNull(id3);
            assertNotEquals(id1, id3);
            assertNotEquals(id2, id3);
            var context3 = runner.getManager().getContext(id3);
            assertEquals(3, context3.getLocalVars().getInt(1));

            var id4 = runner.resume(id3);
            assertNotNull(id4);
            assertNotEquals(id1, id4);
            assertNotEquals(id2, id4);
            assertNotEquals(id3, id4);
            var context4 = runner.getManager().getContext(id4);
            assertEquals(2, context4.getLocalVars().getInt(1));

            var id5 = runner.resume(id4);
            assertNotNull(id5);
            assertNotEquals(id1, id5);
            assertNotEquals(id2, id5);
            assertNotEquals(id3, id5);
            assertNotEquals(id4, id5);
            var context5 = runner.getManager().getContext(id5);
            assertEquals(1, context5.getLocalVars().getInt(1));

            var id6 = runner.resume(id5);
            assertNotNull(id6);
            assertNotEquals(id1, id6);
            assertNotEquals(id2, id6);
            assertNotEquals(id3, id6);
            assertNotEquals(id4, id6);
            assertNotEquals(id5, id6);
            var context6 = runner.getManager().getContext(id6);
            assertEquals(-1, context6.getLocalVars().getInt(1));

            var id7 = runner.resume(id6);
            assertNull(id7);
        }
    }

    @Test
    public void testPauseInWhileClones()
    throws Throwable {
        final var test_classes = new String[]{"TestPauseInWhile", "TestPauseInWhileInterface"};
        for (final String test_class : test_classes) {
            var runner = new ContinuableRunnerTest();

            var id1 = runner.start(TestPause.class.getPackage().getName() + "." + test_class);
            assertNotNull(id1);
            var context1 = runner.getManager().getContext(id1);
            assertEquals(5, context1.getLocalVars().getInt(1));

            var id2a = runner.resume(id1);
            assertNotNull(id2a);
            var context2 = runner.getManager().getContext(id2a);
            assertEquals(4, context2.getLocalVars().getInt(1));

            var id3aa = runner.resume(id2a);
            assertNotNull(id3aa);
            var context3aa = runner.getManager().getContext(id3aa);
            assertEquals(3, context3aa.getLocalVars().getInt(1));

            var id4aa = runner.resume(id3aa);
            assertNotNull(id4aa);
            var context4aa = runner.getManager().getContext(id4aa);
            assertEquals(2, context4aa.getLocalVars().getInt(1));

            var id2b = runner.resume(id1);
            assertNotNull(id2b);
            assertNotEquals(id2a, id2b);
            var context2b = runner.getManager().getContext(id2b);
            assertEquals(4, context2.getLocalVars().getInt(1));

            var id3ab = runner.resume(id2a);
            assertNotNull(id3ab);
            assertNotEquals(id3aa, id3ab);
            var context3ab = runner.getManager().getContext(id3ab);
            assertEquals(3, context3ab.getLocalVars().getInt(1));

            var id5aa = runner.resume(id4aa);
            assertNotNull(id5aa);
            var context5aa = runner.getManager().getContext(id5aa);
            assertEquals(1, context5aa.getLocalVars().getInt(1));

            var id3b = runner.resume(id2b);
            assertNotNull(id3b);
            assertNotEquals(id3aa, id3b);
            assertNotEquals(id3ab, id3b);
            var context3b = runner.getManager().getContext(id3b);
            assertEquals(3, context3b.getLocalVars().getInt(1));

            var id4ab = runner.resume(id3ab);
            assertNotNull(id4ab);
            assertNotEquals(id4aa, id4ab);
            var context4ab = runner.getManager().getContext(id4ab);
            assertEquals(2, context4ab.getLocalVars().getInt(1));

            var id4b = runner.resume(id3b);
            assertNotNull(id4b);
            assertNotEquals(id4aa, id4b);
            assertNotEquals(id4ab, id4b);
            var context4b = runner.getManager().getContext(id4b);
            assertEquals(2, context4b.getLocalVars().getInt(1));

            var id6aa = runner.resume(id5aa);
            assertNotNull(id6aa);
            var context6aa = runner.getManager().getContext(id6aa);
            assertEquals(-1, context6aa.getLocalVars().getInt(1));

            var id7aa = runner.resume(id6aa);
            assertNull(id7aa);

            var id5b = runner.resume(id4b);
            assertNotNull(id5b);
            assertNotEquals(id5aa, id5b);
            var context5b = runner.getManager().getContext(id5b);
            assertEquals(1, context5b.getLocalVars().getInt(1));

            var id5ab = runner.resume(id4ab);
            assertNotNull(id5ab);
            assertNotEquals(id5aa, id5ab);
            assertNotEquals(id5b, id5ab);
            var context5ab = runner.getManager().getContext(id5ab);
            assertEquals(1, context5ab.getLocalVars().getInt(1));

            var id6ab = runner.resume(id5ab);
            assertNotNull(id6ab);
            var context6ab = runner.getManager().getContext(id6ab);
            assertEquals(-1, context6ab.getLocalVars().getInt(1));

            var id7ab = runner.resume(id6ab);
            assertNull(id7ab);

            var id6b = runner.resume(id5b);
            assertNotNull(id6b);
            var context6b = runner.getManager().getContext(id6b);
            assertEquals(-1, context6b.getLocalVars().getInt(1));

            var id7b = runner.resume(id6b);
            assertNull(id7b);
        }
    }
}