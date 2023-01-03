/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

public class TestExecutor extends Executor {
    public boolean executeTask(Task task) {
        return true;
    }

    public String getHandledTaskType() {
        return "test_executor";
    }
}
