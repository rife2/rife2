/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers.exceptions;

import rife.scheduler.exceptions.SchedulerManagerException;

import java.io.Serial;

public class InstallSchedulerErrorException extends SchedulerManagerException {
    @Serial private static final long serialVersionUID = -3910470092652351542L;

    public InstallSchedulerErrorException() {
        this(null);
    }

    public InstallSchedulerErrorException(Throwable cause) {
        super("Can't install the scheduler database structure.", cause);
    }
}
