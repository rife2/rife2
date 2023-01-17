/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
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

        var executor_thread = new ExecutorThread(this, task);
        var thread = new Thread(executor_thread, getHandledTaskType());
        thread.start();
    }

    void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    protected long getRescheduleDelay() {
        return 1000;
    }

    public Task createTask() {
        return new Task().type(getHandledTaskType());
    }

    public String getHandledTaskType() {
        return getClass().getName();
    };

    public abstract boolean executeTask(Task task);
}
