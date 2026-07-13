/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

/**
 * This interface allows classes to register themselves to receive
 * notifications when work fails with an exception in a {@link Workflow}.
 * <p>Error listeners have to be registered through
 * {@link Workflow#addErrorListener}. When no error listeners are registered,
 * work failures are logged to the {@code rife.workflow} logger instead, so
 * that they never pass silently.
 * <p>Failed work counts as finished work: {@link Workflow#waitForNoWork}
 * and {@link Workflow#waitForPausedWork} return normally when work fails,
 * and these listeners are the way to detect that failures happened.
 *
 * @rife.apiNote The workflow engine is still in an ALPHA EXPERIMENTAL STAGE and might change.
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see WorkErrorException
 * @see Workflow#addErrorListener
 * @since 1.10
 */
public interface ErrorListener {
    /**
     * Called when work failed with an exception.
     * <p>This is called on the thread that was executing the work.
     *
     * @param error describes the work that failed, with the original
     *              exception available as its cause
     * @since 1.10
     */
    void errorOccurred(WorkErrorException error);
}
