/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskOptionManagerException;

import java.io.Serial;

public class GetTaskOptionsErrorException extends TaskOptionManagerException {
    @Serial private static final long serialVersionUID = -4348602870066135917L;

    private final int taskId_;

    public GetTaskOptionsErrorException(int taskId) {
        this(taskId, null);
    }

    public GetTaskOptionsErrorException(int taskId, DatabaseException cause) {
        super("Error while getting the taskoptions for task id '" + taskId + "'.", cause);

        taskId_ = taskId;
    }

    public int getTaskId() {
        return taskId_;
    }
}
