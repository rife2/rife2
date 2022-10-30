/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class ActivateTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -3826831322534555616L;

    private final int id_;

    public ActivateTaskErrorException(int id) {
        this(id, null);
    }

    public ActivateTaskErrorException(int id, DatabaseException cause) {
        super("Error while trying to activate the task with id '" + id + "'.", cause);

        id_ = id;
    }

    public int getTaskId() {
        return id_;
    }
}
