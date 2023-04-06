/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.exceptions.TaskOptionManagerException;

import java.util.Collection;

/**
 * This interface defines the methods that classes with
 * {@code TaskOptionManager} functionalities have to implement.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface TaskOptionManager {
    /**
     * Sets the scheduler that uses this task option manager.
     *
     * @param scheduler this task option manager's scheduler
     * @since 1.0
     */
    void setScheduler(Scheduler scheduler);

    /**
     * Retrieves the scheduler of this task option manager.
     *
     * @return this task option manager's scheduler; or
     * {@code null} if the scheduler hasn't been set
     * @since 1.0
     */
    Scheduler getScheduler();

    /**
     * Adds a new task option to a task.
     * <p>
     * The provided task option needs to have the appropriate
     * task ID set, otherwise the addition will fail.
     *
     * @param taskOption the task option to add
     * @return {@code true} when the task option was successfully added; or
     * {@code false} otherwise
     * @throws TaskOptionManagerException when an error occurred during the task option addition
     * @since 1.0
     */
    boolean addTaskOption(TaskOption taskOption)
    throws TaskOptionManagerException;

    /**
     * Update an existing task option.
     * <p>
     * The provided task option needs to have the appropriate
     * task ID set, otherwise the update will fail.
     *
     * @param taskOption the updated task option
     * @return {@code true} if the task option was successfully updated; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the task option update
     * @since 1.0
     */
    boolean updateTaskOption(TaskOption taskOption)
    throws TaskOptionManagerException;

    /**
     * Retrieves a named task options for a particular task.
     *
     * @param taskId the unique ID of the task to retrieve the task option for
     * @param name the name of the task option
     * @return the retrieved task option instance; or
     * {@code null} when the task option couldn't be found
     * @throws TaskOptionManagerException when an error occurred during the task option retrieval
     * @since 1.0
     */
    TaskOption getTaskOption(int taskId, String name)
    throws TaskOptionManagerException;

    /**
     * Retrieves all the task options for a particular task.
     *
     * @param taskId the unique ID of the task to retrieve the task options for
     * @return the retrieved task options collection
     * @throws TaskOptionManagerException when an error occurred during the task option retrieval
     * @since 1.0
     */
    Collection<TaskOption> getTaskOptions(int taskId)
    throws TaskOptionManagerException;

    /**
     * Remove a particular task option.
     * <p>
     * The provided task option needs to have the appropriate
     * task ID set, otherwise the removal will fail.
     *
     * @param taskOption the task option to remove
     * @return {@code true} when the task option was successfully removed; or
     * {@code false} otherwise
     * @throws TaskOptionManagerException when an error occurred during the task option removal
     * @since 1.0
     */
    boolean removeTaskOption(TaskOption taskOption)
    throws TaskOptionManagerException;

    /**
     * Remove a particular task option.
     *
     * @param taskId the unique ID of the task to retrieve the task option for
     * @param name the name of the task option
     * @return {@code true} when the task option was successfully removed; or
     * {@code false} otherwise
     * @throws TaskOptionManagerException when an error occurred during the task option removal
     * @since 1.0
     */
    boolean removeTaskOption(int taskId, String name)
    throws TaskOptionManagerException;
}
