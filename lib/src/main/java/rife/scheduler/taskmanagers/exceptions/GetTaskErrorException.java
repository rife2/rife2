/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class GetTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = 2834724601081802023L;

    private final int id_;

    public GetTaskErrorException(int id) {
        this(id, null);
    }

    public GetTaskErrorException(int id, DatabaseException cause) {
        super("Error while trying to obtain the task with id '" + id + "'.", cause);

        id_ = id;
    }

    public int getTaskId() {
        return id_;
    }
}
