/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifeworkflowtests;

import rife.workflow.Work;
import rife.workflow.Workflow;

public class CountdownWork implements Work {
    public enum Types {
        START, TICK, DONE
    }

    public void execute(Workflow workflow) {
        var event = pauseForEvent(Types.START);
        var count = (Integer) event.getData();
        while (count > 0) {
            workflow.trigger(Types.TICK, count);
            count -= 1;
        }
        workflow.trigger(Types.DONE, null);
    }
}
