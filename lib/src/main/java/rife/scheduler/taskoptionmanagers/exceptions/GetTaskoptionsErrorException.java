/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class GetTaskoptionsErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = -4348602870066135917L;

    private final int taskId_;

    public GetTaskoptionsErrorException(int taskId) {
        this(taskId, null);
    }

    public GetTaskoptionsErrorException(int taskId, DatabaseException cause) {
        super("Error while getting the taskoptions for task id '" + taskId + "'.", cause);

        taskId_ = taskId;
    }

    public int getTaskId() {
        return taskId_;
    }
}
