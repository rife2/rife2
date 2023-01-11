/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class GetScheduledTasksErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -4854656738595719247L;

    public GetScheduledTasksErrorException() {
        this(null);
    }

    public GetScheduledTasksErrorException(DatabaseException cause) {
        super("Unable to get the scheduled tasks.", cause);
    }
}
