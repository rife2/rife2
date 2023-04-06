/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.exceptions;

import java.io.Serial;

public class TaskTypeAlreadyRegisteredException extends SchedulerException {
    @Serial private static final long serialVersionUID = 1894374458175141334L;

    private final String taskType_;

    public TaskTypeAlreadyRegisteredException(String taskType) {
        super("An executor has already been registered with task type '" + taskType + "'.");

        taskType_ = taskType;
    }

    public String getTaskType() {
        return taskType_;
    }
}
