/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.Taskoption;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.util.Collection;

public interface TaskoptionManager {
    public void setScheduler(Scheduler scheduler);

    public Scheduler getScheduler();

    public boolean addTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    public boolean updateTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    public Taskoption getTaskoption(int taskid, String name)
    throws TaskoptionManagerException;

    public Collection<Taskoption> getTaskoptions(int taskid)
    throws TaskoptionManagerException;

    public boolean removeTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException;

    public boolean removeTaskoption(int taskid, String name)
    throws TaskoptionManagerException;
}
