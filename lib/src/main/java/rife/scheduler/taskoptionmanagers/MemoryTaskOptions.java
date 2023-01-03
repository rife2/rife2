/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import rife.scheduler.Scheduler;
import rife.scheduler.TaskOption;
import rife.scheduler.TaskOptionManager;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.exceptions.TaskOptionManagerException;
import rife.scheduler.taskoptionmanagers.exceptions.AddTaskOptionErrorException;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskOptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.scheduler.taskoptionmanagers.exceptions.UpdateTaskOptionErrorException;

public class MemoryTaskOptions implements TaskOptionManager {
    private Scheduler scheduler_ = null;
    private Map<Integer, ArrayList<TaskOption>> taskOptionsMapping_ = null;

    public MemoryTaskOptions() {
        taskOptionsMapping_ = new HashMap<>();
    }

    public void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler_;
    }

    public boolean addTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");
        if (taskoption.getTaskId() < 0) throw new IllegalArgumentException("the task id is required.");

        synchronized (this) {
            TaskOption cloned_taskoption = null;
            try {
                cloned_taskoption = taskoption.clone();
            } catch (CloneNotSupportedException e) {
                throw new AddTaskOptionErrorException(cloned_taskoption, e);
            }

            // check if the task id exists
            try {
                if (null == getScheduler().getTaskManager().getTask(cloned_taskoption.getTaskId())) {
                    throw new InexistentTaskIdException(cloned_taskoption.getTaskId());
                }
            } catch (TaskManagerException e) {
                throw new AddTaskOptionErrorException(cloned_taskoption, e);
            }

            // get the task options list for the same task id
            var task_id = cloned_taskoption.getTaskId();
            var task_options = taskOptionsMapping_.get(task_id);
            if (null == task_options) {
                // no list exists, create one
                task_options = new ArrayList<>();
                taskOptionsMapping_.put(cloned_taskoption.getTaskId(), task_options);
            } else {
                // list exists, check if the same task option isn't already present
                // and throw an exception if that is the case
                for (var task_option_to_check : task_options) {
                    if (task_option_to_check.getName().equals(cloned_taskoption.getName())) {
                        throw new DuplicateTaskOptionException(cloned_taskoption.getTaskId(), cloned_taskoption.getName());
                    }
                }
            }

            task_options.add(cloned_taskoption);

            return true;
        }
    }

    public boolean updateTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");
        if (taskoption.getTaskId() < 0) throw new IllegalArgumentException("the task id is required.");

        synchronized (this) {
            TaskOption cloned_taskoption = null;
            try {
                cloned_taskoption = taskoption.clone();
            } catch (CloneNotSupportedException e) {
                throw new AddTaskOptionErrorException(cloned_taskoption, e);
            }

            // check if the task id exists
            try {
                if (null == getScheduler().getTaskManager().getTask(cloned_taskoption.getTaskId())) {
                    throw new InexistentTaskIdException(cloned_taskoption.getTaskId());
                }
            } catch (TaskManagerException e) {
                throw new UpdateTaskOptionErrorException(cloned_taskoption, e);
            }

            // get the task options for the same task id
            var task_id = cloned_taskoption.getTaskId();
            var taskoptions = taskOptionsMapping_.get(task_id);
            if (null == taskoptions) {
                return false;
            }
            // obtain the task option with same name
            TaskOption task_option_to_remove = null;
            for (var taskoption_to_check : taskoptions) {
                if (taskoption_to_check.getName().equals(cloned_taskoption.getName())) {
                    task_option_to_remove = taskoption_to_check;
                    break;
                }
            }

            // no match was found
            if (null == task_option_to_remove) {
                return false;
            }

            // remove the old taskoption and store the new one
            taskoptions.remove(task_option_to_remove);
            taskoptions.add(cloned_taskoption);

            return true;
        }
    }

    public TaskOption getTaskOption(int taskId, String name)
    throws TaskOptionManagerException {
        if (taskId < 0) throw new IllegalArgumentException("taskId can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        synchronized (this) {
            // get the task options for the same task id
            var task_options = taskOptionsMapping_.get(taskId);
            if (null == task_options) {
                return null;
            }
            // obtain the task option with same name
            for (var taskoption : task_options) {
                if (taskoption.getName().equals(name)) {
                    return taskoption;
                }
            }
        }

        return null;
    }

    public Collection<TaskOption> getTaskOptions(int taskId)
    throws TaskOptionManagerException {
        if (taskId < 0) throw new IllegalArgumentException("taskId can't be negative.");

        return taskOptionsMapping_.get(taskId);
    }

    public boolean removeTaskOption(TaskOption taskoption)
    throws TaskOptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        return removeTaskOption(taskoption.getTaskId(), taskoption.getName());
    }

    public boolean removeTaskOption(int taskId, String name)
    throws TaskOptionManagerException {
        if (taskId < 0) throw new IllegalArgumentException("taskId can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        synchronized (this) {
            // get the task options for the same task id
            var task_options = taskOptionsMapping_.get(taskId);
            if (null == task_options) {
                return false;
            }
            // obtain the task option with same name
            TaskOption taskoption_to_remove = null;
            for (var taskoption : task_options) {
                if (taskoption.getName().equals(name)) {
                    taskoption_to_remove = taskoption;
                    break;
                }
            }

            if (null == taskoption_to_remove) {
                return false;
            }

            task_options.remove(taskoption_to_remove);
        }

        return true;
    }
}
