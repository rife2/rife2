/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler;

import rife.scheduler.exceptions.*;

import java.util.Collection;
import java.util.HashMap;

public class Scheduler extends Thread {
    private TaskManager taskManager_ = null;
    private TaskoptionManager taskoptionManager_ = null;
    private int sleepTime_ = 500;
    private final HashMap<Object, Executor> executors_;

    public Scheduler(TaskManager taskManager, TaskoptionManager taskoptionManager) {
        super("SCHEDULER_DAEMON");

        setDaemon(true);
        setTaskManager(taskManager);
        setTaskoptionManager(taskoptionManager);
        executors_ = new HashMap<Object, Executor>();
    }

    public void setTaskManager(TaskManager taskManager) {
        if (null == taskManager) throw new IllegalArgumentException("taskManager can't be null.");

        taskManager_ = taskManager;
        taskManager.setScheduler(this);
    }

    public TaskManager getTaskManager() {
        return taskManager_;
    }

    public void setTaskoptionManager(TaskoptionManager taskoptionManager) {
        if (null == taskoptionManager) throw new IllegalArgumentException("taskoptionManager can't be null.");

        taskoptionManager_ = taskoptionManager;
        taskoptionManager.setScheduler(this);
    }

    public TaskoptionManager getTaskoptionManager() {
        return taskoptionManager_;
    }

    public boolean addExecutor(Executor executor)
    throws SchedulerException {
        if (null == executor) throw new IllegalArgumentException("executor can't be null.");

        if (null == executor.getScheduler()) {
            executors_.put(executor.getHandledTasktype(), executor);
            executor.setScheduler(this);
        } else if (this == executor.getScheduler()) {
            return false;
        } else {
            throw new ExecutorAlreadyRegisteredException(executor);
        }

        assert executors_.containsKey(executor.getHandledTasktype());
        assert executor == executors_.get(executor.getHandledTasktype());
        assert this == executor.getScheduler();

        return true;
    }

    public boolean removeExecutor(Executor executor) {
        if (null == executor) throw new IllegalArgumentException("executor can't be null.");

        if (null == executors_.remove(executor.getHandledTasktype())) {
            return false;
        }

        executor.setScheduler(null);

        assert !executors_.containsKey(executor.getHandledTasktype());
        assert null == executor.getScheduler();

        return true;
    }

    public Executor getExecutor(String tasktype) {
        if (null == tasktype) throw new IllegalArgumentException("tasktype can't be null.");

        return executors_.get(tasktype);
    }

    public Collection<Executor> getExecutors() {
        return executors_.values();
    }

    public void setSleepTime(int sleeptime) {
        if (sleeptime <= 0) throw new IllegalArgumentException("sleeptime has to be bigger than 0.");

        sleepTime_ = sleeptime;
    }

    public void run() {
        while (true) {
            try {
                if (!isInterrupted()) {
                    scheduleStep();
                    // Ensure that the wakeup is always on an even multiplier of the
                    // sleep time, this to ensure that no drift occurs.
                    long now = System.currentTimeMillis();
                    long projected = ((System.currentTimeMillis() + sleepTime_) / sleepTime_) * sleepTime_;
                    long difference = projected - now;

                    Thread.sleep(difference);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        synchronized (this) {
            notifyAll();
        }
    }

    private void scheduleStep()
    throws SchedulerExecutionException {
        assert taskManager_ != null;

        try {
            Executor executor = null;
            for (Task task : taskManager_.getTasksToProcess()) {
                executor = executors_.get(task.getType());
                if (null != executor) {
                    executor.startTaskExecution(task);
                } else {
                    throw new NoExecutorForTasktypeException(task.getType());
                }
            }
        } catch (TaskManagerException e) {
            throw new UnableToRetrieveTasksToProcessException(e);
        }
    }
}
