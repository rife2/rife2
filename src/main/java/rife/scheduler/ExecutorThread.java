/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.FatalTaskExecutionException;
import rife.scheduler.exceptions.TaskManagerException;

class ExecutorThread implements Runnable {
    private final Executor executor_;
    private final Task task_;
    private boolean activated_ = false;

    public ExecutorThread(Executor executor, Task task) {
        executor_ = executor;
        task_ = task;
    }

    // called on the scheduler thread, so that its next step can't start the task again
    void activate() {
        try {
            executor_.getScheduler().getTaskManager().activateTask(task_.getId());
            activated_ = true;
        } catch (TaskManagerException ignored) {
        }
    }

    public void run() {
        assert task_ != null;

        var successful_execution = false;
        var manager = executor_.getScheduler().getTaskManager();

        try {
            if (activated_) {
                successful_execution = executor_.executeTask(task_);
            }
        } catch (TaskManagerException ignored) {
        } finally {
            try {
                if (successful_execution) {
                    manager.concludeTask(task_);
                } else {
                    manager.rescheduleTask(task_, System.currentTimeMillis() + executor_.getRescheduleDelay(), task_.getFrequency());
                    manager.deactivateTask(task_.getId());
                }
            } catch (TaskManagerException e) {
                throw new FatalTaskExecutionException(task_, e);
            }
        }
    }
}
