/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers.exceptions;

import rife.scheduler.exceptions.SchedulerManagerException;

import java.io.Serial;

public class RemoveSchedulerErrorException extends SchedulerManagerException {
    @Serial private static final long serialVersionUID = -3277083283936069418L;

    public RemoveSchedulerErrorException() {
        this(null);
    }

    public RemoveSchedulerErrorException(Throwable cause) {
        super("Can't remove the scheduler database structure.", cause);
    }
}
