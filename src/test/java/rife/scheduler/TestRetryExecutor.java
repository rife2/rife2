/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import java.util.concurrent.atomic.AtomicInteger;

public class TestRetryExecutor extends Executor {
    private static final long TIMEOUT = 3000;

    private final AtomicInteger executions_ = new AtomicInteger();

    public boolean executeTask(Task task) {
        // only the first execution fails, which has the task retried
        return executions_.incrementAndGet() > 1;
    }

    public String getHandledTaskType() {
        return "test_retry_executor";
    }

    protected long getRescheduleDelay() {
        return 100;
    }

    public int getExecutions() {
        return executions_.get();
    }

    public void waitForExecutions(int count)
    throws InterruptedException {
        var deadline = System.currentTimeMillis() + TIMEOUT;
        while (executions_.get() < count &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
    }

    public static Task waitForConclusion(TaskManager manager, int id)
    throws Exception {
        var deadline = System.currentTimeMillis() + TIMEOUT;
        var task = manager.getTask(id);
        while (task != null &&
               task.isBusy() &&
               System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
            task = manager.getTask(id);
        }
        return task;
    }
}
