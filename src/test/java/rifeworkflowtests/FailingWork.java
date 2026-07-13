/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class FailingWork implements Work {
    public void execute(Workflow workflow) {
        throw new IllegalStateException("failing work");
    }
}
