/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.database.exceptions.DatabaseException;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class RemoveTaskoptionsErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 2096333126507604963L;

    public RemoveTaskoptionsErrorException() {
        this(null);
    }

    public RemoveTaskoptionsErrorException(DatabaseException cause) {
        super("Can't remove the taskoption database structure.", cause);
    }
}
