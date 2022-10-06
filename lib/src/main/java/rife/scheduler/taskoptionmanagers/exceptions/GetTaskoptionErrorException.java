/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class GetTaskoptionErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 3920950726058002527L;

    private final int taskId_;
    private String name_;

    public GetTaskoptionErrorException(int taskId, String name) {
        this(taskId, name, null);
    }

    public GetTaskoptionErrorException(int taskId, String name, DatabaseException cause) {
        super("Error while getting taskoption with task id '" + taskId + "' and name '" + name + "'.", cause);

        taskId_ = taskId;
        name_ = name;
    }

    public int getTaskId() {
        return taskId_;
    }

    public String getName() {
        return name_;
    }
}
