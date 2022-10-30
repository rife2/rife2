/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class AddTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -7352228624354115586L;

    private final Task task_;

    public AddTaskErrorException(Task task) {
        this(task, null);
    }

    public AddTaskErrorException(Task task, DatabaseException cause) {
        super("Error while adding task with id '" + task.getId() + "', type '" + task.getType() + "', planned '" + task.getPlanned() + "' and frequency '" + task.getFrequency() + "'.", cause);

        task_ = task;
    }

    public Task getTask() {
        return task_;
    }
}
