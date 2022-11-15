/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers;

import rife.database.queries.*;
import rife.scheduler.taskoptionmanagers.exceptions.*;

import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.DbQueryManager;
import rife.database.DbRowProcessor;
import rife.database.exceptions.DatabaseException;
import rife.scheduler.Scheduler;
import rife.scheduler.TaskOption;
import rife.scheduler.TaskOptionManager;
import rife.scheduler.exceptions.TaskOptionManagerException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DatabaseTaskOptions extends DbQueryManager implements TaskOptionManager {
    private Scheduler scheduler_ = null;

    protected DatabaseTaskOptions(Datasource datasource) {
        super(datasource);
    }

    public void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler_;
    }

    public abstract boolean install()
    throws TaskOptionManagerException;

    public abstract boolean remove()
    throws TaskOptionManagerException;

    protected boolean install_(CreateTable createTableTaskOption)
    throws TaskOptionManagerException {
        assert createTableTaskOption != null;

        try {
            executeUpdate(createTableTaskOption);
        } catch (DatabaseException e) {
            throw new InstallTaskOptionsErrorException(e);
        }

        return true;
    }

    protected boolean remove_(DropTable dropTableTaskOption)
    throws TaskOptionManagerException {
        assert dropTableTaskOption != null;

        try {
            executeUpdate(dropTableTaskOption);
        } catch (DatabaseException e) {
            throw new RemoveTaskOptionsErrorException(e);
        }

        return true;
    }

    protected boolean _addTaskOption(Insert addTaskOption, DbPreparedStatementHandler handler, final TaskOption taskoption)
    throws TaskOptionManagerException {
        assert addTaskOption != null;

        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        boolean result = false;

        try {
            if (0 == executeUpdate(addTaskOption, handler)) {
                throw new AddTaskOptionErrorException(taskoption);
            }
            result = true;
        } catch (DatabaseException e) {
            throw new AddTaskOptionErrorException(taskoption, e);
        }

        return result;
    }

    protected boolean _updateTaskOption(Update updateTaskOption, DbPreparedStatementHandler handler, final TaskOption taskoption)
    throws TaskOptionManagerException {
        assert updateTaskOption != null;

        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        boolean result = false;
        try {
            if (0 == executeUpdate(updateTaskOption, handler)) {
                throw new UpdateTaskOptionErrorException(taskoption);
            }
            result = true;
        } catch (DatabaseException e) {
            throw new UpdateTaskOptionErrorException(taskoption, e);
        }

        return result;
    }

    protected TaskOption _getTaskOption(Select getTaskOption, ProcessTaskOption processTaskOption, final int taskId, final String name)
    throws TaskOptionManagerException {
        assert getTaskOption != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        TaskOption taskoption = null;

        try {
            executeFetchFirst(getTaskOption, processTaskOption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId)
                        .setString("name", name);
                }
            });
            taskoption = processTaskOption.getTaskOption();
        } catch (DatabaseException e) {
            throw new GetTaskOptionErrorException(taskId, name, e);
        }

        return taskoption;
    }

    protected Collection<TaskOption> _getTaskOptions(Select getTaskOptions, ProcessTaskOption processTaskOption, final int taskId)
    throws TaskOptionManagerException {
        assert getTaskOptions != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");

        ArrayList<TaskOption> taskoptions = new ArrayList<TaskOption>();
        processTaskOption.setCollection(taskoptions);

        try {
            executeFetchAll(getTaskOptions, processTaskOption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId);
                }
            });
        } catch (DatabaseException e) {
            throw new GetTaskOptionsErrorException(taskId, e);
        }

        assert taskoptions != null;

        return taskoptions;
    }

    protected boolean _removeTaskOption(Delete removeTaskOption, TaskOption taskoption)
    throws TaskOptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        return _removeTaskOption(removeTaskOption, taskoption.getTaskId(), taskoption.getName());
    }

    protected boolean _removeTaskOption(Delete removeTaskOption, final int taskId, final String name)
    throws TaskOptionManagerException {
        assert removeTaskOption != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        boolean result = false;

        try {
            if (0 != executeUpdate(removeTaskOption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId)
                        .setString("name", name);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveTaskOptionErrorException(taskId, name, e);
        }

        return result;
    }

    protected class ProcessTaskOption extends DbRowProcessor {
        protected Collection<TaskOption> mCollection = null;
        protected TaskOption mTaskOption = null;

        public ProcessTaskOption() {
        }

        public void setCollection(Collection<TaskOption> collection) {
            mCollection = collection;
        }

        public boolean processRow(ResultSet resultSet)
        throws SQLException {
            assert resultSet != null;

            mTaskOption = new TaskOption();

            mTaskOption.setTaskId(resultSet.getInt("task_id"));
            mTaskOption.setName(resultSet.getString("name"));
            mTaskOption.setValue(resultSet.getString("val"));

            if (mCollection != null) {
                mCollection.add(mTaskOption);
            }

            return true;
        }

        public TaskOption getTaskOption() {
            return mTaskOption;
        }
    }
}

