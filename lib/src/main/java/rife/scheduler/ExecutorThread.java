/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.*;

class ExecutorThread implements Runnable {
    private final Executor executor_;
    private final Task task_;

    public ExecutorThread(Executor executor, Task task) {
        executor_ = executor;
        task_ = task;
    }

    public void run() {
        assert task_ != null;

        var successful_execution = false;
        var manager = executor_.getScheduler().getTaskManager();

        try {
            manager.activateTask(task_.getId());
            successful_execution = executor_.executeTask(task_);
        } catch (TaskManagerException ignored) {
        } finally {
            try {
                if (!successful_execution) {
                    manager.rescheduleTask(task_, executor_.getRescheduleDelay(), null);
                }
                manager.concludeTask(task_);
            } catch (TaskManagerException e) {
                throw new FatalTaskExecutionException(task_, e);
            }
        }
    }
}
