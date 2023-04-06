/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class TaskManagerException extends SchedulerException {
    @Serial private static final long serialVersionUID = -1250407186493859593L;

    public TaskManagerException(String message) {
        super(message);
    }

    public TaskManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskManagerException(Throwable cause) {
        super(cause);
    }
}
