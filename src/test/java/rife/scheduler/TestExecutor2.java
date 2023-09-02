/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

public class TestExecutor2 extends Executor {
    @Override
    public boolean executeTask(Task task) {
        return true;
    }

    @Override
    public String getHandledTaskType() {
        return "test_executor2";
    }
}
