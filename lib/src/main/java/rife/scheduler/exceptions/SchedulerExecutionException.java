/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class SchedulerExecutionException extends RuntimeException {
    @Serial private static final long serialVersionUID = 779737627952928528L;

    public SchedulerExecutionException(String message) {
        super(message);
    }

    public SchedulerExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerExecutionException(Throwable cause) {
        super(cause);
    }
}
