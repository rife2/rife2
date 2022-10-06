/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers;

import rife.database.*;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.scheduler.Scheduler;
import rife.scheduler.Task;
import rife.scheduler.TaskManager;
import rife.scheduler.exceptions.FrequencyException;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.taskmanagers.exceptions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DatabaseTasks extends DbQueryManager implements TaskManager {
    private Scheduler scheduler_ = null;

    protected DatabaseTasks(Datasource datasource) {
        super(datasource);
    }

    public void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler_;
    }

    public abstract boolean install()
    throws TaskManagerException;

    public abstract boolean remove()
    throws TaskManagerException;

    protected boolean install_(final CreateSequence createSequenceTask, final CreateTable createTableTask)
    throws TaskManagerException {
        assert createSequenceTask != null;
        assert createTableTask != null;

        try {
            executeUpdate(createSequenceTask);
            executeUpdate(createTableTask);
        } catch (DatabaseException e) {
            throw new InstallTasksErrorException(e);
        }

        return true;
    }

    protected boolean remove_(final DropSequence dropSequenceTask, final DropTable dropTableTask)
    throws TaskManagerException {
        assert dropSequenceTask != null;
        assert dropTableTask != null;

        try {
            executeUpdate(dropTableTask);
            executeUpdate(dropSequenceTask);
        } catch (DatabaseException e) {
            throw new RemoveTasksErrorException(e);
        }

        return true;
    }

    protected int addTask_(SequenceValue getTaskId, Insert insertTask, DbPreparedStatementHandler handler, final Task task)
    throws TaskManagerException {
        assert getTaskId != null;
        assert insertTask != null;

        if (null == task) throw new IllegalArgumentException("task can't be null.");

        int result = -1;
        int task_id = -1;
        try {
            task_id = executeGetFirstInt(getTaskId);
            if (-1 == task_id) {
                throw new GetTaskIdErrorException();
            }
        } catch (DatabaseException e) {
            throw new GetTaskIdErrorException(e);
        }

        if (task_id >= 0) {
            task.setId(task_id);

            try {
                if (0 == executeUpdate(insertTask, handler)) {
                    throw new AddTaskErrorException(task);
                }

                result = task_id;
            } catch (DatabaseException e) {
                throw new AddTaskErrorException(task, e);
            }
        }

        assert result >= 0;

        return result;
    }

    protected boolean updateTask_(Update updateTask, DbPreparedStatementHandler handler, final Task task)
    throws TaskManagerException {
        assert updateTask != null;

        if (null == task) throw new IllegalArgumentException("task can't be null.");

        boolean result = false;

        try {
            if (0 == executeUpdate(updateTask, handler)) {
                throw new UpdateTaskErrorException(task);
            }

            result = true;
        } catch (DatabaseException e) {
            throw new UpdateTaskErrorException(task, e);
        }

        return result;
    }

    protected Task getTask_(Select getTask, ProcessTask processTask, final int id)
    throws TaskManagerException {
        assert getTask != null;

        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        Task task = null;

        try {
            executeFetchFirst(getTask, processTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", id);
                }
            });
            task = processTask.getTask();
        } catch (DatabaseException e) {
            throw new GetTaskErrorException(id, e);
        }

        return task;
    }

    protected Collection<Task> getTasksToProcess_(Select getTasksToProcess, ProcessTask processTask)
    throws TaskManagerException {
        assert getTasksToProcess != null;

        ArrayList<Task> tasks_to_process = new ArrayList<Task>();
        processTask.setCollection(tasks_to_process);

        try {
            executeFetchAll(getTasksToProcess, processTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("planned", System.currentTimeMillis());
                }
            });
        } catch (DatabaseException e) {
            throw new GetTasksToProcessErrorException(e);
        }

        assert tasks_to_process != null;

        return tasks_to_process;
    }

    protected Collection<Task> getScheduledTasks_(Select getScheduledTasks, ProcessTask processTask)
    throws TaskManagerException {
        ArrayList<Task> scheduled_tasks = new ArrayList<Task>();
        processTask.setCollection(scheduled_tasks);

        try {
            executeFetchAll(getScheduledTasks, processTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("planned", System.currentTimeMillis());
                }
            });
        } catch (DatabaseException e) {
            throw new GetScheduledTasksErrorException(e);
        }

        assert scheduled_tasks != null;

        return scheduled_tasks;
    }

    protected boolean removeTask_(Delete removeTask, final int id)
    throws TaskManagerException {
        assert removeTask != null;

        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        boolean result = false;

        try {
            if (0 != executeUpdate(removeTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", id);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveTaskErrorException(id, e);
        }

        return result;
    }

    protected boolean rescheduleTask_(Task task, long newPlanned, String frequency)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");
        if (newPlanned <= 0) throw new IllegalArgumentException("newPlanned has to be bigger than 0.");

        boolean result = false;

        Task task_tmp = null;
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

    protected boolean concludeTask_(Task task)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");

        if (task.getPlanned() <= System.currentTimeMillis()) {
            if (null == task.getFrequency()) {
                return removeTask(task.getId());
            }

            try {
                long next_date = task.getNextDate();
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

    protected boolean activateTask_(Update activateTask, final int id)
    throws TaskManagerException {
        assert activateTask != null;

        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        boolean result = false;

        try {
            if (0 != executeUpdate(activateTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", id);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new ActivateTaskErrorException(id);
        }

        return result;
    }

    protected boolean deactivateTask_(Update deactivateTask, final int id)
    throws TaskManagerException {
        assert deactivateTask != null;

        if (id < 0) throw new IllegalArgumentException("the task id can't be negative.");

        boolean result = false;

        try {
            if (0 != executeUpdate(deactivateTask, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", id);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new DeactivateTaskErrorException(id, e);
        }

        return result;
    }

    protected class ProcessTask extends DbRowProcessor {
        protected Collection<Task> collection_ = null;
        protected Task task_ = null;

        public ProcessTask() {
        }

        public void setCollection(Collection<Task> collection) {
            collection_ = collection;
        }

        public boolean processRow(ResultSet resultSet)
        throws SQLException {
            assert resultSet != null;

            task_ = new Task();

            task_.setId(resultSet.getInt("id"));
            task_.setType(resultSet.getString("type"));
            task_.setPlanned(resultSet.getLong("planned"));
            try {
                task_.setFrequency(resultSet.getString("frequency"));
            } catch (FrequencyException e) {
                throw new SQLException(e.getMessage());
            }
            task_.setBusy(resultSet.getBoolean("busy"));
            task_.setTaskManager(DatabaseTasks.this);

            if (collection_ != null) {
                collection_.add(task_);
            }

            return true;
        }

        Task getTask() {
            return task_;
        }
    }
}
