/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskOptionManagerException;

import java.io.Serial;

public class InstallTaskOptionsErrorException extends TaskOptionManagerException {
    @Serial private static final long serialVersionUID = 3383700445767477585L;

    public InstallTaskOptionsErrorException() {
        this(null);
    }

    public InstallTaskOptionsErrorException(DatabaseException cause) {
        super("Can't install the taskoption database structure.", cause);
    }
}
