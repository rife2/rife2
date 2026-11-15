/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SchedulerTesting {
    private static final long STOP_TIMEOUT = 10000;

    // a scheduler that doesn't stop fails the test instead of hanging it
    public static void stopAndAwait(Scheduler scheduler)
    throws InterruptedException {
        synchronized (scheduler) {
            scheduler.stop();
            var start = System.currentTimeMillis();
            scheduler.wait(STOP_TIMEOUT);
            assertTrue(System.currentTimeMillis() - start < STOP_TIMEOUT, "the scheduler didn't stop");
        }
    }

    private SchedulerTesting() {
    }
}
