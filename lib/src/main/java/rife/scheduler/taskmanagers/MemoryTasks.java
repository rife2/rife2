/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import rife.scheduler.*;
import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.taskmanagers.exceptions.ConcludeTaskErrorException;
import rife.scheduler.taskmanagers.exceptions.RescheduleTaskErrorException;

public class MemoryTasks implements TaskManager {
    private final Map<Integer, Task> taskMapping_;
    private Scheduler scheduler_ = null;
    private int taskIdSequence_ = 0;

    public MemoryTasks() {
        taskMapping_ = new HashMap<>();
    }

    public void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler_;
    }

    public int addTask(Task task)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");

        synchronized (this) {
            var task_id = taskIdSequence_;
            // check for overflow and reset to 0
            if (++taskIdSequence_ < 0) {
                taskIdSequence_ = 0;
            }

            task.setId(task_id);
            taskMapping_.put(task_id, task);
            task.setTaskManager(this);

            return task_id;
        }
    }

    public boolean updateTask(Task task)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");
        if (task.getId() < 0) throw new IllegalArgumentException("the task id is required.");

        synchronized (this) {
            var task_id = task.getId();

            if (!taskMapping_.containsKey(task_id)) {
                return false;
            }

            taskMapping_.put(task_id, task);
            task.setTaskManager(this);

            return true;
        }
    }

    public Task getTask(int id)
    throws TaskManagerException {
        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        return taskMapping_.get(id);
    }

    public Collection<Task> getAllTasks()
    throws TaskManagerException {
        synchronized (this) {
            return new ArrayList<>(taskMapping_.values());
        }
    }

    public Collection<Task> getTasksToProcess()
    throws TaskManagerException {
        var tasks_to_process = new ArrayList<Task>();

        synchronized (this) {
            for (var task : taskMapping_.values()) {
                if (!task.isBusy() &&
                    task.getPlanned() < System.currentTimeMillis()) {
                    tasks_to_process.add(task);
                }
            }
        }

        return tasks_to_process;
    }

    public Collection<Task> getScheduledTasks()
    throws TaskManagerException {
        var scheduled_tasks = new ArrayList<Task>();

        synchronized (this) {
            for (var task : taskMapping_.values()) {
                if (!task.isBusy() &&
                    task.getPlanned() >= System.currentTimeMillis()) {
                    scheduled_tasks.add(task);
                }
            }
        }

        return scheduled_tasks;
    }

    public boolean removeTask(int id)
    throws TaskManagerException {
        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        synchronized (this) {
            return null != taskMapping_.remove(id);
        }
    }

    public boolean rescheduleTask(Task task, long newPlanned, Frequency frequency)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");
        if (newPlanned <= 0) throw new IllegalArgumentException("newPlanned has to be bigger than 0.");

        var result = false;

        Task task_tmp;
        try {
            task_tmp = task.clone();
            task_tmp.setPlanned(newPlanned);
            task_tmp.setFrequency(frequency);
        } catch (Throwable e) {
            if (null == frequency) {
                throw new RescheduleTaskErrorException(task.getId(), newPlanned, e);
            } else {
                throw new RescheduleTaskErrorException(task.getId(), newPlanned, frequency, e);
            }
        }
        result = updateTask(task_tmp);

        assert result;

        return result;
    }

    public boolean concludeTask(Task task)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");

        if (task.getPlanned() <= System.currentTimeMillis()) {
            if (null == task.getFrequency()) {
                return removeTask(task.getId());
            }

            try {
                var next_date = task.getNextDate();
                if (next_date >= 0 &&
                    rescheduleTask(task, next_date, task.getFrequency()) &&
                    deactivateTask(task.getId())) {
                    return true;
                }
            } catch (FrequencyException e) {
                throw new ConcludeTaskErrorException(task.getId(), e);
            }
        }

        return false;
    }

    public boolean activateTask(int id)
    throws TaskManagerException {
        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        synchronized (this) {
            var task = taskMapping_.get(id);
            if (null == task) {
                return false;
            }
            task.setBusy(true);
            return true;
        }
    }

    public boolean deactivateTask(int id)
    throws TaskManagerException {
        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        synchronized (this) {
            var task = taskMapping_.get(id);
            if (null == task) {
                return false;
            }
            task.setBusy(false);
            return true;
        }
    }
}
