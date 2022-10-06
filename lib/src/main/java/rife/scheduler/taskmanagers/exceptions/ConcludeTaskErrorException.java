/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;

public class ConcludeTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -7412671423693693166L;

    private final int id_;

    public ConcludeTaskErrorException(int id) {
        this(id, null);
    }

    public ConcludeTaskErrorException(int id, FrequencyException cause) {
        super("Error while trying to conclude the task with id '" + id + "'.", cause);

        id_ = id;
    }

    public int getTaskId() {
        return id_;
    }
}
