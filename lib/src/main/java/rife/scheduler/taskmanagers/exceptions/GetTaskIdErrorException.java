/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class GetTaskIdErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -1558135834952131030L;

    public GetTaskIdErrorException() {
        this(null);
    }

    public GetTaskIdErrorException(DatabaseException cause) {
        super("Unable to get a task id.", cause);
    }
}
