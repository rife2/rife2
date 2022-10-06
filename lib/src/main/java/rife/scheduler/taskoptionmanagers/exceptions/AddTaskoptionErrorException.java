/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.Taskoption;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class AddTaskoptionErrorException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 4905743175691118664L;

    private final Taskoption taskoption_;

    public AddTaskoptionErrorException(Taskoption taskoption) {
        this(taskoption, null);
    }

    public AddTaskoptionErrorException(Taskoption taskoption, Throwable cause) {
        super("Error while adding taskoption with task id '" + taskoption.getTaskId() + "', name '" + taskoption.getName() + "' and value '" + taskoption.getValue() + "'.", cause);

        taskoption_ = taskoption;
    }

    public Taskoption getTaskoption() {
        return taskoption_;
    }
}
