/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.SchedulerExecutionException;

/**
 * Executor is an abstract class that can be extended to implement the logic
 * that happen when tasks of a particular type are executing through the
 * scheduler.
 * <p>By default, the task type will be determined by the name of the
 * executor class, but it's possible to customize that by overriding the
 * `getHandledTaskType()` method.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class Executor {
    public static final int DEFAULT_RESCHEDULE_DELAY = 1000;

    private Scheduler scheduler_ = null;

    /**
     * Needs to be implemented with the logic that should happen when
     * the scheduler executes a task.
     * <p>
     * Sometimes tasks fail to be executed, or the right conditions can't be met.
     * In that case, returning {@code false} from this method will make the
     * scheduler reschedule the same task. The delay that is used for
     * rescheduling is determined by this class's {@link #getRescheduleDelay}
     * method.
     *
     * @param task the task that needs to be executed.
     * @return {@code true} if the task successfully executed, or
     * {@code false} if the task needs to be rescheduled
     * @see #getRescheduleDelay
     * @since 1.0
     */
    public abstract boolean executeTask(Task task);

    /**
     * Create a new task instance with this executor's task type.
     *
     * @return a new task
     * @since 1.0
     */
    public Task createTask() {
        return new Task().type(getHandledTaskType());
    }

    /**
     * Returns a string identifying the type of tasks that this executor will
     * handle.
     * <p>
     * By default, this is the fully qualified class name of this executor,
     * overriding this method allows for other type identifiers to be used.
     *
     * @return this executor's task type
     * @since 1.0
     */
    public String getHandledTaskType() {
        return getClass().getName();
    }

    /**
     * Provides the delay in milliseconds that should be used when tasks are
     * being rescheduled after non-successful execution.
     * <p>
     * By default, this is {@code 1000} ms (1 second), overriding this method
     * allows for any other delay to be used.
     *
     * @return the delay in milliseconds for task rescheduling
     * @since 1.0
     */
    protected long getRescheduleDelay() {
        return DEFAULT_RESCHEDULE_DELAY;
    }

    /**
     * Retrieves the scheduler that this executor has been registered with.
     *
     * @return this executor's scheduler; or
     * {@code null} if the executor has not been registered with a scheduler yet.
     *
     * @since 1.0
     */
    public Scheduler getScheduler() {
        return scheduler_;
    }

    void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    final void startTaskExecution(Task task)
    throws SchedulerExecutionException {
        assert task != null;

        var executor_thread = new ExecutorThread(this, task);
        var thread = new Thread(executor_thread, getHandledTaskType());
        thread.start();
    }
}
