/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class DeactivateTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = 4625084266066863923L;

    private final int id_;

    public DeactivateTaskErrorException(int id) {
        this(id, null);
    }

    public DeactivateTaskErrorException(int id, DatabaseException cause) {
        super("Error while trying to desactivate the task with id '" + id + "'.", cause);

        id_ = id;
    }

    public int getTaskId() {
        return id_;
    }
}
