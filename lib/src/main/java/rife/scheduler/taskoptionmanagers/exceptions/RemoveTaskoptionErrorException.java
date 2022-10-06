/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class RemoveTaskoptionErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = -1476382542192225439L;

    private final int taskID_;
    private final String name_;

    public RemoveTaskoptionErrorException(int taskId, String name) {
        this(taskId, name, null);
    }

    public RemoveTaskoptionErrorException(int taskId, String name, DatabaseException cause) {
        super("Error while getting taskoption with task id '" + taskId + "' and name '" + name + "'.", cause);

        taskID_ = taskId;
        name_ = name;
    }

    public int getTaskId() {
        return taskID_;
    }

    public String getName() {
        return name_;
    }
}
