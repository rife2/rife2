/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class InstallTaskoptionsErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 3383700445767477585L;

    public InstallTaskoptionsErrorException() {
        this(null);
    }

    public InstallTaskoptionsErrorException(DatabaseException cause) {
        super("Can't install the taskoption database structure.", cause);
    }
}
