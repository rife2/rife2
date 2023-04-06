/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class InstallTasksErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -8972198627863614260L;

    public InstallTasksErrorException() {
        this(null);
    }

    public InstallTasksErrorException(DatabaseException cause) {
        super("Can't install the task database structure.", cause);
    }
}
