/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.exceptions;

import rife.scheduler.exceptions.TaskoptionManagerException;

import java.io.Serial;

public class DuplicateTaskoptionException extends TaskoptionManagerException {
    @Serial private static final long serialVersionUID = 1952213015727655475L;

    private final int taskID_;
    private final String taskoptionName_;

    public DuplicateTaskoptionException(int taskid, String taskoptionName) {
        super("The task option with task id '" + taskid + "' and name '" + taskoptionName + "' already exists.");

        taskID_ = taskid;
        taskoptionName_ = taskoptionName;
    }

    public int getTaskID() {
        return taskID_;
    }

    public String getTaskoptionName() {
        return taskoptionName_;
    }
}
