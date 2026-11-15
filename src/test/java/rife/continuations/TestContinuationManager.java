/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestContinuationManager {
    @Test
    void testPurgeWaitsForTheLock()
    throws Exception {
        var test_thread = Thread.currentThread();
        var purge_checked = new CountDownLatch(1);
        var manager = new ContinuationManager[1];
        var config = new ContinuationConfigRuntime() {
            public ContinuationManager getContinuationManager(Object executingInstance) {
                return manager[0];
            }

            public boolean cloneContinuations(Object executingContinuable) {
                return false;
            }

            public long getContinuationDuration() {
                if (Thread.currentThread() != test_thread) {
                    purge_checked.countDown();
                }
                return 0;
            }

            public int getContinuationPurgeFrequency() {
                return 1;
            }

            public int getContinuationPurgeScale() {
                return 1;
            }
        };
        manager[0] = new ContinuationManager(config);

        ContinuationConfigRuntime.setActiveConfigRuntime(config);
        ContinuationContext.clearActiveContext();
        try {
            manager[0].addContext(ContinuationContext.createOrResetContext(new Object()));

            manager[0].writeLock_.lock();
            try {
                assertNull(manager[0].resumeContext("unknown"));
                assertFalse(purge_checked.await(200, TimeUnit.MILLISECONDS));
            } finally {
                manager[0].writeLock_.unlock();
            }
            assertTrue(purge_checked.await(5, TimeUnit.SECONDS));
        } finally {
            ContinuationContext.clearActiveContext();
            ContinuationConfigRuntime.clearActiveConfigRuntime();
        }
    }
}
