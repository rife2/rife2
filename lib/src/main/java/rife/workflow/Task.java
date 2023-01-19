/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import rife.continuations.CloneableContinuable;
import rife.workflow.run.TaskRunner;

/**
 * Tasks can be executed in a {@link TaskRunner}.
 * <p>Their execution will be done in a dedicated thread by invoking the
 * {@link #execute} method on a new instance of the task class.
 * <p>Afterwards, tasks can suspend their execution by waiting for particular
 * event types. The thread will stop executing and no system resources will be
 * used except for the memory required to maintain the state of the suspended
 * task instance.
 * <p>When a suitable event is triggered in the {@code TaskRunner}, a new
 * thread will be created and the execution of the suspended task will be
 * resumed in it where it left off.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see TaskRunner
 * @since 1.0
 */
public abstract class Task implements CloneableContinuable {
    /**
     * The entry method of this task's execution.
     *
     * @param runner the task runner that is executing the task
     * @since 1.0
     */
    public abstract void execute(TaskRunner runner);

    /**
     * Convenience method that triggers an event in a task runner.
     *
     * @param runner the task runner where the even should be triggered
     * @param type   the type of the event
     * @see #trigger(rife.workflow.run.TaskRunner, Object, Object)
     * @see TaskRunner#trigger
     * @since 1.0
     */
    protected void trigger(TaskRunner runner, Object type) {
        trigger(runner, type, null);
    }

    /**
     * Convenience method that triggers an event in a task runner with
     * associated data.
     *
     * @param runner the task runner where the even should be triggered
     * @param type   the type of the event
     * @param data   the data that will be sent with the event
     * @see #trigger(rife.workflow.run.TaskRunner, Object)
     * @see TaskRunner#trigger
     * @since 1.0
     */
    protected void trigger(TaskRunner runner, Object type, Object data) {
        runner.trigger(new Event(this, type, data));
    }

    /**
     * Wait for a particular event type to be triggered in the task runner.
     * <p>When an event is triggered with a suitable type, is will be returned
     * through this method call.
     *
     * @param type the event type to wait for.
     * @return the event that woke up the task
     * @since 1.0
     */
    public final Event waitForEvent(Object type) {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new UnsupportedOperationException();
    }

    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }
}