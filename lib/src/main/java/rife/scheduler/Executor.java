/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.SchedulerExecutionException;

public abstract class Executor {
    private Scheduler scheduler_ = null;

    public Scheduler getScheduler() {
        return scheduler_;
    }

    final void startTaskExecution(Task task)
    throws SchedulerExecutionException {
        assert task != null;

        ExecutorThread executor_thread = new ExecutorThread(this, task);
        Thread thread = new Thread(executor_thread, getHandledTasktype());
        thread.start();
    }

    void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    protected long getRescheduleDelay() {
        return 1000;
    }

    public abstract boolean executeTask(Task task);

    public abstract String getHandledTasktype();
}
