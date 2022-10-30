/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class UnableToRetrieveTasksToProcessException extends SchedulerExecutionException {
    @Serial private static final long serialVersionUID = -2233605418085075672L;

    public UnableToRetrieveTasksToProcessException(TaskManagerException e) {
        super("Unable to retrieve the tasks to process.", e);
    }
}
