/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.exceptions;

import rife.scheduler.Frequency;
import rife.scheduler.exceptions.TaskManagerException;

import java.io.Serial;
import java.util.Date;

public class RescheduleTaskErrorException extends TaskManagerException {
    @Serial private static final long serialVersionUID = -5612314711614259305L;

    private final int id_;
    private final long newPlanned_;
    private final Frequency frequency_;

    public RescheduleTaskErrorException(int id, long newPlanned, Frequency frequency, Throwable cause) {
        super("Error while trying to reschedule the task with id '" + id + "', planned at '" + new Date(newPlanned) + "' with frequency '" + frequency + "'.", cause);

        id_ = id;
        newPlanned_ = newPlanned;
        frequency_ = frequency;
    }

    public RescheduleTaskErrorException(int id, long newPlanned, Throwable cause) {
        super("Error while trying to reschedule the task with id '" + id + "', planned at '" + new Date(newPlanned) + "' with no frequency.", cause);

        id_ = id;
        newPlanned_ = newPlanned;
        frequency_ = null;
    }

    public int getTaskId() {
        return id_;
    }

    public long getNewPlanned() {
        return newPlanned_;
    }

    public Frequency getFrequency() {
        return frequency_;
    }
}
