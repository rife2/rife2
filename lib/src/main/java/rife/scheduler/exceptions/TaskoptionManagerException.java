/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class TaskoptionManagerException extends SchedulerException {
    @Serial private static final long serialVersionUID = 4109184135680666647L;

    public TaskoptionManagerException(String message) {
        super(message);
    }

    public TaskoptionManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskoptionManagerException(Throwable cause) {
        super(cause);
    }
}
