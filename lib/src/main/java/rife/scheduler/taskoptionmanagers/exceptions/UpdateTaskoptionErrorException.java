/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.Taskoption;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class UpdateTaskoptionErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 4032049075661263762L;

    private final Taskoption taskoption_;

    public UpdateTaskoptionErrorException(Taskoption taskoption) {
        this(taskoption, null);
    }

    public UpdateTaskoptionErrorException(Taskoption taskoption, Throwable cause) {
        super("Error while updating taskoption with task id '" + taskoption.getTaskId() + "', name '" + taskoption.getName() + "' and value '" + taskoption.getValue() + "'.", cause);

        taskoption_ = taskoption;
    }

    public Taskoption getTaskoption() {
        return taskoption_;
    }
}
