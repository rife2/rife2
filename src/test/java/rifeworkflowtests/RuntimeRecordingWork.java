/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.continuations.ContinuationConfigRuntime;
import rife.workflow.Work;
import rife.workflow.Workflow;

public class RuntimeRecordingWork implements Work {
    private volatile boolean daemon_;
    private volatile long continuationDuration_;

    public void execute(Workflow workflow) {
        daemon_ = Thread.currentThread().isDaemon();
        continuationDuration_ = ContinuationConfigRuntime.getActiveConfigRuntime().getContinuationDuration();
    }

    public boolean isDaemon() {
        return daemon_;
    }

    public long getContinuationDuration() {
        return continuationDuration_;
    }
}
