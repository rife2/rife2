/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;

import java.util.Collection;

public interface TaskManager {
    void setScheduler(Scheduler scheduler);

    Scheduler getScheduler();

    int addTask(Task task)
    throws TaskManagerException;

    boolean updateTask(Task task)
    throws TaskManagerException;

    Task getTask(int id)
    throws TaskManagerException;

    Collection<Task> getTasksToProcess()
    throws TaskManagerException;

    Collection<Task> getScheduledTasks()
    throws TaskManagerException;

    boolean removeTask(int id)
    throws TaskManagerException;

    boolean rescheduleTask(Task task, long interval, String frequency)
    throws TaskManagerException;

    boolean concludeTask(Task task)
    throws TaskManagerException;

    boolean activateTask(int id)
    throws TaskManagerException;

    boolean deactivateTask(int id)
    throws TaskManagerException;
}
