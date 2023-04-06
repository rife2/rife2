/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.TaskManagerException;

import java.util.Collection;

/**
 * This interface defines the methods that classes with
 * {@code TaskManager} functionalities have to implement.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface TaskManager {
    /**
     * Sets the scheduler that uses this task manager.
     *
     * @param scheduler this task manager's scheduler
     * @since 1.0
     */
    void setScheduler(Scheduler scheduler);

    /**
     * Retrieves the scheduler of this task manager.
     *
     * @return this task manager's scheduler; or
     * {@code null} if the scheduler hasn't been set
     * @since 1.0
     */
    Scheduler getScheduler();

    /**
     * Adds a new task.
     * <p>
     * After the task addition, the unique ID of the task should
     * be stored in the provided task instance.
     *
     * @param task the task to add
     * @return this unique ID of the added task
     * @throws TaskManagerException when an error occurred during the task addition
     * @since 1.0
     */
    int addTask(Task task)
    throws TaskManagerException;

    /**
     * Update an existing task.
     * <p>
     * The task instance should have the unique ID set to be able to
     * update the existing one.
     *
     * @param task the updated task
     * @return {@code true} if the task was successfully updated; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the task update
     * @since 1.0
     */
    boolean updateTask(Task task)
    throws TaskManagerException;

    /**
     * Retrieves a task from it's unique ID.
     *
     * @param id the unique ID of the task to retrieve
     * @return the retrieved task; or
     * {@code null} if no such task could be found
     * @throws TaskManagerException when an error occurred during the task retrieval
     * @since 1.0
     */
    Task getTask(int id)
    throws TaskManagerException;

    /**
     * Remove a task.
     *
     * @param id the unique ID of the task to remove
     * @return {@code true} if the task was successfully removed; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the task removal
     * @since 1.0
     */
    boolean removeTask(int id)
    throws TaskManagerException;

    /**
     * Retrieves all the tasks that are registered with this task manager.
     *
     * @return a collection of all the task manager tasks
     * @throws TaskManagerException when an error occurred during the collection of the tasks
     * @since 1.0
     */
    Collection<Task> getAllTasks()
    throws TaskManagerException;

    /**
     * Retrieves the tasks that are not busy and that should be processed
     * next by the scheduled based on the current time.
     *
     * @return a collection of all the tasks that need to be processed
     * @throws TaskManagerException when an error occurred during the collection of the tasks
     * @since 1.0
     */
    Collection<Task> getTasksToProcess()
    throws TaskManagerException;

    /**
     * Retrieves the tasks that are not busy and that are schedule to
     * execute some time in the future.
     *
     * @return a collection of all the tasks that are scheduled
     * @throws TaskManagerException when an error occurred during the collection of the tasks
     * @since 1.0
     */
    Collection<Task> getScheduledTasks()
    throws TaskManagerException;

    /**
     * Reschedule an existing task at a particular timestamp.
     * <p>
     * This method will also change the task instance that's provided.
     * <p>
     * The task instance should have the unique ID set to be able to
     * update the existing one.
     *
     * @param task the task to reschedule
     * @param planned the planned timestamp in milliseconds since epoch
     * @param frequency the new frequency of the task
     * @return {@code true} if the task was successfully rescheduled; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the rescheduling of the task
     * @since 1.0
     */
    boolean rescheduleTask(Task task, long planned, Frequency frequency)
    throws TaskManagerException;

    /**
     * This method will be called when the scheduler processes a
     * particular task. While being processed, the task should be set as {@code busy}.
     *
     * @param id the unique ID of the task to activate
     * @return {@code true} if the task was successfully activated; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the activation of the task
     * @since 1.0
     */
    boolean activateTask(int id)
    throws TaskManagerException;

    /**
     * This method will be called when the task is fully done being executed.
     * The task should be set as not busy.
     *
     * @param id the unique ID of the task to activate
     * @return {@code true} if the task was successfully deactivated; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the deactivation of the task
     * @since 1.0
     */
    boolean deactivateTask(int id)
    throws TaskManagerException;

    /**
     * This method will be called by the scheduler when the task has done
     * processing, any resources should be cleaned up and any task rescheduling, removal
     * or deactivation should be done by the manager here.
     *
     * @param task the task to conclude
     * @return @{true} when the task was successfully concluded; or
     * {@code false} otherwise
     * @throws TaskManagerException when an error occurred during the conclusion of the task
     * @since 1.0
     */
    boolean concludeTask(Task task)
    throws TaskManagerException;
}
