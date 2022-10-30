/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class RemoveTasksErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = 8498777641623518926L;

    public RemoveTasksErrorException() {
        this(null);
    }

    public RemoveTasksErrorException(DatabaseException cause) {
        super("Can't remove the task database structure.", cause);
    }
}
