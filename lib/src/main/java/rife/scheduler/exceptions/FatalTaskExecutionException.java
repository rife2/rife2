/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import rife.scheduler.Task;

import java.io.Serial;
import java.util.Date;

public class FatalTaskExecutionException extends SchedulerExecutionException {
    @Serial private static final long serialVersionUID = 8346061648025924402L;

    private final Task task_;

    public FatalTaskExecutionException(Task task, Throwable cause) {
        super("The task with id '" + task.getId() + "' and type '" + task.getType() + "' which had to execute at '" + new Date(task.getPlanned()) + "' couldn't be executed.", cause);
        task_ = task;
    }

    public Task getTask() {
        return task_;
    }
}
