/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class GetTasksToProcessErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -4883619369685381990L;

    public GetTasksToProcessErrorException() {
        this(null);
    }

    public GetTasksToProcessErrorException(DatabaseException cause) {
        super("Unable to get the tasks to process.", cause);
    }
}
