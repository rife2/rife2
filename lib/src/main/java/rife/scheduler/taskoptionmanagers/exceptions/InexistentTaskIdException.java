/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class InexistentTaskIdException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = -8781897352662853904L;

    private final int taskID_;

    public InexistentTaskIdException(int taskid) {
        super("The task id '" + taskid + "' doesn't exist.");
        taskID_ = taskid;
    }

    public int getTaskID() {
        return taskID_;
    }
}
