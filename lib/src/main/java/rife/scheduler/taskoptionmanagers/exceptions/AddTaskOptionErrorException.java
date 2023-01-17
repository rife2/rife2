/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.TaskOption;
import rife.scheduler.exceptions.TaskOptionManagerException;

import java.io.Serial;

public class AddTaskOptionErrorException extends TaskOptionManagerException {
    @Serial private static final long serialVersionUID = 4905743175691118664L;

    private final TaskOption taskoption_;

    public AddTaskOptionErrorException(TaskOption taskoption) {
        this(taskoption, null);
    }

    public AddTaskOptionErrorException(TaskOption taskoption, Throwable cause) {
        super("Error while adding taskoption with task id '" + taskoption.getTaskId() + "', name '" + taskoption.getName() + "' and value '" + taskoption.getValue() + "'.", cause);

        taskoption_ = taskoption;
    }

    public TaskOption getTaskOption() {
        return taskoption_;
    }
}
