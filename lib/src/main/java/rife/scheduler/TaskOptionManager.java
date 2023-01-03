/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.TaskOptionManagerException;

import java.util.Collection;

public interface TaskOptionManager {
    void setScheduler(Scheduler scheduler);

    Scheduler getScheduler();

    boolean addTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException;

    boolean updateTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException;

    TaskOption getTaskOption(int taskId, String name)
    throws TaskOptionManagerException;

    Collection<TaskOption> getTaskOptions(int taskId)
    throws TaskOptionManagerException;

    boolean removeTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException;

    boolean removeTaskOption(int taskid, String name)
    throws TaskOptionManagerException;
}
