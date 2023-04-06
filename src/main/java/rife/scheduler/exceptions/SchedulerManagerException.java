/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class SchedulerManagerException extends SchedulerException {
    @Serial private static final long serialVersionUID = -4381666648963934294L;

    public SchedulerManagerException(String message) {
        super(message);
    }

    public SchedulerManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerManagerException(Throwable cause) {
        super(cause);
    }
}
