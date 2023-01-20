/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import rife.continuations.exceptions.ContinuationsNotActiveException;

/**
 * Work can be executed in a {@link Workflow}.
 * <p>Their execution will be done in a thread by invoking the
 * {@link #execute} method on a new instance of the work class.
 * <p>Afterwards, work can suspend its execution by pausing for particular
 * event types. The thread will stop executing this work and no system resources
 * will be used except for the memory required to maintain the state of the
 * suspended work instance.
 * <p>When a suitable event is triggered in the {@code Workflow}, a thread
 * will resume the execution of the suspended work where it left off.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Workflow
 * @since 1.0
 */
public interface Work {
    /**
     * The entry method of this work's execution.
     *
     * @param workflow the workflow that is executing the work
     * @since 1.0
     */
    void execute(Workflow workflow);

    /**
     * Pause until a particular event type is triggered in the workflow.
     * <p>When an event is triggered with a suitable type, is will be returned
     * through this method call.
     *
     * @param type the event type to wait for.
     * @return the event that woke up the work
     * @since 1.0
     */
    default Event pauseForEvent(Object type) {
        // this should not be triggered, since bytecode rewriting will replace this
        // method call with the appropriate logic
        throw new ContinuationsNotActiveException();
    }
}