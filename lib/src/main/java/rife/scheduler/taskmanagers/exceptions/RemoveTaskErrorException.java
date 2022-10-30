/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class RemoveTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = 7546246390590430739L;

    private final int id_;

    public RemoveTaskErrorException(int id) {
        this(id, null);
    }

    public RemoveTaskErrorException(int id, DatabaseException cause) {
        super("Error while trying to remove the task with id '" + id + "'.", cause);

        id_ = id;
    }

    public int getTaskId() {
        return id_;
    }
}
