/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.exceptions.TaskOptionManagerException;

import java.io.Serial;

public class InexistentTaskIdException extends TaskOptionManagerException {
    @Serial private static final long serialVersionUID = -8781897352662853904L;

    private final int taskID_;

    public InexistentTaskIdException(int taskid) {
        this(taskid, null);
    }

    public InexistentTaskIdException(int taskid, Throwable e) {
        super("The task id '" + taskid + "' doesn't exist.", e);
        taskID_ = taskid;
    }

    public int getTaskID() {
        return taskID_;
    }
}
