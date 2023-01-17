/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class GetAllTasksErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -270336179659576624L;

    public GetAllTasksErrorException() {
        this(null);
    }

    public GetAllTasksErrorException(DatabaseException cause) {
        super("Unable to get all the tasks.", cause);
    }
}
