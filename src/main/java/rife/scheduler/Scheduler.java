/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.config.RifeConfig;
import rife.scheduler.exceptions.*;
import rife.scheduler.schedulermanagers.*;

import java.util.*;

/**
 * The {@code Scheduler} class performs the actual task scheduling and dispatch to
 * the appropriate executors.
 * <p>
 * You would typically not create an instance of {@code Scheduler} yourself, but
 * instead obtain the one that is provided by {@link MemoryScheduling}
 * or {@link DatabaseScheduling}
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Scheduler implements Runnable {
    public static final int DEFAULT_SLEEP_TIME = 30000; // 30 seconds

    private static final Set<Scheduler> activeSchedulers = Collections.newSetFromMap(new WeakHashMap<>());

    private Thread thread_ = null;
    private TaskManager taskManager_ = null;
    private TaskOptionManager taskOptionManager_ = null;
    private int sleepTime_ = DEFAULT_SLEEP_TIME;
    private final HashMap<Object, Executor> executors_;

    /**
     * Creates a new scheduler instance for the provided task manager and task option manager.
     *
     * @param taskManager       the task manager to use for this scheduler
     * @param taskOptionManager the task option manager to use for this scheduler
     * @since 1.0
     */
    public Scheduler(TaskManager taskManager, TaskOptionManager taskOptionManager) {
        setTaskManager(taskManager);
        setTaskOptionManager(taskOptionManager);
        executors_ = new HashMap<>();
    }

    /**
     * Sets the task manager of this scheduler.
     *
     * @param taskManager the task manager to use
     * @since 1.0
     */
    public void setTaskManager(TaskManager taskManager) {
        if (null == taskManager) throw new IllegalArgumentException("taskManager can't be null.");

        taskManager_ = taskManager;
        taskManager.setScheduler(this);
    }

    /**
     * Retrieves this scheduler's task manager.
     *
     * @return this scheduler's task manager; or
     * {@code null} if no task manager has been assigned yet
     * @since 1.0
     */
    public TaskManager getTaskManager() {
        return taskManager_;
    }

    /**
     * Sets the task option manager of this scheduler.
     *
     * @param taskOptionManager the task option manager to use
     * @since 1.0
     */
    public void setTaskOptionManager(TaskOptionManager taskOptionManager) {
        if (null == taskOptionManager) throw new IllegalArgumentException("taskOptionManager can't be null.");

        taskOptionManager_ = taskOptionManager;
        taskOptionManager.setScheduler(this);
    }

    /**
     * Retrieves this scheduler's task option manager.
     *
     * @return this scheduler's task option manager; or
     * {@code null} if no task option manager has been assigned yet
     * @since 1.0
     */
    public TaskOptionManager getTaskOptionManager() {
        return taskOptionManager_;
    }

    /**
     * Convenience method to add a task to the task manager that's
     * registered with this scheduler.
     *
     * @param task the task instance to add
     * @return the unique ID of the task
     * @since 1.0
     */
    public int addTask(Task task) {
        return taskManager_.addTask(task);
    }

    /**
     * Convenience method to add a task option to the task option manager that's
     * registered with this scheduler.
     *
     * @param taskOption the task option instance to add
     * @return {@code true} if the task option was added successfully; or
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean addTaskOption(TaskOption taskOption) {
        return taskOptionManager_.addTaskOption(taskOption);
    }

    /**
     * Adds an executor to this scheduler.
     *
     * @param executor the executor to add to this scheduler
     * @throws SchedulerException when this executor is already registered with another
     *                            scheduler; or when another executor is already registered for this task type
     * @since 1.0
     */
    public void addExecutor(Executor executor)
    throws SchedulerException {
        if (null == executor) throw new IllegalArgumentException("executor can't be null.");

        if (null == executor.getScheduler()) {
            if (executors_.containsKey(executor.getHandledTaskType())) {
                throw new TaskTypeAlreadyRegisteredException(executor.getHandledTaskType());
            }
            executors_.put(executor.getHandledTaskType(), executor);
            executor.setScheduler(this);
        } else if (this != executor.getScheduler()) {
            throw new ExecutorAlreadyRegisteredException(executor);
        }

        assert executors_.containsKey(executor.getHandledTaskType());
        assert executor == executors_.get(executor.getHandledTaskType());
        assert this == executor.getScheduler();
    }

    /**
     * Removes an executor from this scheduler.
     *
     * @param executor the executor to remove
     * @return {@code true} if the scheduler was successfully remove; or
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean removeExecutor(Executor executor) {
        if (null == executor) throw new IllegalArgumentException("executor can't be null.");

        if (null == executors_.remove(executor.getHandledTaskType())) {
            return false;
        }

        executor.setScheduler(null);

        assert !executors_.containsKey(executor.getHandledTaskType());
        assert null == executor.getScheduler();

        return true;
    }

    /**
     * Retrieves the executor that was registered for a particular task type.
     *
     * @param taskType the task type to find the executor for
     * @return the executor handling the provided task type; or
     * {@code null} if no executor is registered for that task type
     * @since 1.0
     */
    public Executor getExecutor(String taskType) {
        if (null == taskType) throw new IllegalArgumentException("task type can't be null.");

        return executors_.get(taskType);
    }

    /**
     * Retrieves the executors that have been registered with this scheduler.
     *
     * @return the collection of this scheduler's executors
     * @since 1.0
     */
    public Collection<Executor> getExecutors() {
        return Collections.unmodifiableCollection(executors_.values());
    }

    /**
     * Set the time the schedule should sleep in between evaluating which tasks
     * to execute.
     * <p>
     * This defaults to {@code 500} milliseconds.
     *
     * @param sleepTime the time to sleep between task evaluations in milliseconds
     * @since 1.0
     */
    public void setSleepTime(int sleepTime) {
        if (sleepTime <= 0) throw new IllegalArgumentException("sleep time has to be bigger than 0.");

        sleepTime_ = sleepTime;
    }

    /**
     * Starts this scheduler.
     *
     * @since 1.0
     */
    public void start() {
        synchronized (this) {
            if (thread_ != null) {
                return;
            }

            thread_ = new Thread(this, "SCHEDULER_DAEMON");
            thread_.setDaemon(true);
            thread_.start();

            synchronized (activeSchedulers) {
                activeSchedulers.add(this);
            }
        }
    }

    /**
     * Indicates whether this scheduler is running or not.
     *
     * @return {@code true} if this scheduler is running; ro
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean isRunning() {
        synchronized (this) {
            if (thread_ == null) {
                return false;
            }

            return thread_.isAlive();
        }
    }

    /**
     * Stops this scheduler.
     *
     * @since 1.0
     */
    public void stop() {
        synchronized (this) {
            if (thread_ != null) {
                thread_.interrupt();
                notifyAll();
                thread_ = null;
            }
        }
    }

    public void run() {
        try {
            while (true) {
                try {
                    if (!Thread.interrupted()) {
                        scheduleStep();
                        // Ensure that the wakeup is always on an even multiplier of the
                        // sleep time, this to ensure that no drift occurs.
                        var now = RifeConfig.tools().getCalendarInstance().getTimeInMillis();
                        var projected = ((now + sleepTime_) / sleepTime_) * sleepTime_;
                        var difference = projected - now;

                        synchronized (this) {
                            wait(difference);
                        }
                    } else {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        } finally {
            synchronized (this) {
                thread_ = null;
                notifyAll();

                synchronized (activeSchedulers) {
                    activeSchedulers.remove(this);
                }
            }
        }
    }

    private void scheduleStep()
    throws SchedulerExecutionException {
        assert taskManager_ != null;

        try {
            Executor executor = null;
            for (var task : taskManager_.getTasksToProcess()) {
                if (task.getPlanned() == 0 && task.getFrequency() != null) {
                    // if no explicit planned date was set and a task frequency was set,
                    // only process the task the first time the frequency determines that it should
                    taskManager_.concludeTask(task);
                }
                else {
                    executor = executors_.get(task.getType());
                    if (null != executor) {
                        executor.startTaskExecution(task);
                    } else {
                        throw new NoExecutorForTasktypeException(task.getType());
                    }
                }
            }
        } catch (TaskManagerException e) {
            throw new UnableToRetrieveTasksToProcessException(e);
        }
    }

    /**
     * Stops all the active schedulers.
     * <p>
     * This can be used to ensure that no schedulers keeps running
     * when an application shuts down. It's already used by the destroy method
     * of the {@code RifeFilter}.
     *
     * @since 1.6.1
     */
    public static void stopAllActiveSchedulers() {
        Set<Scheduler> active_schedulers;
        synchronized (activeSchedulers) {
            active_schedulers = new HashSet<>(activeSchedulers);
            activeSchedulers.clear();
        }

        for (var scheduler : active_schedulers) {
            scheduler.stop();
        }
    }
}
