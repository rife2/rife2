/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import rife.scheduler.Executor;

import java.io.Serial;

public class ExecutorAlreadyRegisteredException extends SchedulerException {
    @Serial private static final long serialVersionUID = 7581141854929771532L;

    private final Executor executor_;

    public ExecutorAlreadyRegisteredException(Executor executor) {
        super("The executor has already been registered to a scheduler.");

        executor_ = executor;
    }

    public Executor getExecutor() {
        return executor_;
    }
}
