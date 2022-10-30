/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.TaskoptionManagerException;

import java.util.Collection;

public interface TaskoptionManager {
    void setScheduler(Scheduler scheduler);

    Scheduler getScheduler();

    boolean addTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    boolean updateTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    Taskoption getTaskoption(int taskid, String name)
    throws TaskoptionManagerException;

    Collection<Taskoption> getTaskoptions(int taskid)
    throws TaskoptionManagerException;

    boolean removeTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    boolean removeTaskoption(int taskid, String name)
    throws TaskoptionManagerException;
}
