/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class TaskOptionManagerException extends SchedulerException {
    @Serial private static final long serialVersionUID = 4109184135680666647L;

    public TaskOptionManagerException(String message) {
        super(message);
    }

    public TaskOptionManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskOptionManagerException(Throwable cause) {
        super(cause);
    }
}
