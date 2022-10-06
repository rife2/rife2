/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class UpdateTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = 7613535991016387602L;

    private final Task task_;

    public UpdateTaskErrorException(Task task) {
        this(task, null);
    }

    public UpdateTaskErrorException(Task task, DatabaseException cause) {
        super("Error while updating task with id '" + task.getId() + "'.", cause);

        task_ = task;
    }

    public Task getTask() {
        return task_;
    }
}
