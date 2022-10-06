/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
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
import rife.scheduler.Taskoption;
import rife.scheduler.TaskoptionManager;
import rife.scheduler.exceptions.TaskoptionManagerException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DatabaseTaskoptions extends DbQueryManager implements TaskoptionManager {
    private Scheduler scheduler_ = null;

    protected DatabaseTaskoptions(Datasource datasource) {
        super(datasource);
    }

    public void setScheduler(Scheduler scheduler) {
        scheduler_ = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler_;
    }

    public abstract boolean install()
    throws TaskoptionManagerException;

    public abstract boolean remove()
    throws TaskoptionManagerException;

    protected boolean install_(CreateTable createTableTaskoption)
    throws TaskoptionManagerException {
        assert createTableTaskoption != null;

        try {
            executeUpdate(createTableTaskoption);
        } catch (DatabaseException e) {
            throw new InstallTaskoptionsErrorException(e);
        }

        return true;
    }

    protected boolean remove_(DropTable dropTableTaskoption)
    throws TaskoptionManagerException {
        assert dropTableTaskoption != null;

        try {
            executeUpdate(dropTableTaskoption);
        } catch (DatabaseException e) {
            throw new RemoveTaskoptionsErrorException(e);
        }

        return true;
    }

    protected boolean _addTaskoption(Insert addTaskoption, DbPreparedStatementHandler handler, final Taskoption taskoption)
    throws TaskoptionManagerException {
        assert addTaskoption != null;

        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        boolean result = false;

        try {
            if (0 == executeUpdate(addTaskoption, handler)) {
                throw new AddTaskoptionErrorException(taskoption);
            }
            result = true;
        } catch (DatabaseException e) {
            throw new AddTaskoptionErrorException(taskoption, e);
        }

        return result;
    }

    protected boolean _updateTaskoption(Update updateTaskoption, DbPreparedStatementHandler handler, final Taskoption taskoption)
    throws TaskoptionManagerException {
        assert updateTaskoption != null;

        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        boolean result = false;
        try {
            if (0 == executeUpdate(updateTaskoption, handler)) {
                throw new UpdateTaskoptionErrorException(taskoption);
            }
            result = true;
        } catch (DatabaseException e) {
            throw new UpdateTaskoptionErrorException(taskoption, e);
        }

        return result;
    }

    protected Taskoption _getTaskoption(Select getTaskoption, ProcessTaskoption processTaskoption, final int taskId, final String name)
    throws TaskoptionManagerException {
        assert getTaskoption != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        Taskoption taskoption = null;

        try {
            executeFetchFirst(getTaskoption, processTaskoption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId)
                        .setString("name", name);
                }
            });
            taskoption = processTaskoption.getTaskoption();
        } catch (DatabaseException e) {
            throw new GetTaskoptionErrorException(taskId, name, e);
        }

        return taskoption;
    }

    protected Collection<Taskoption> _getTaskoptions(Select getTaskoptions, ProcessTaskoption processTaskoption, final int taskId)
    throws TaskoptionManagerException {
        assert getTaskoptions != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");

        ArrayList<Taskoption> taskoptions = new ArrayList<Taskoption>();
        processTaskoption.setCollection(taskoptions);

        try {
            executeFetchAll(getTaskoptions, processTaskoption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId);
                }
            });
        } catch (DatabaseException e) {
            throw new GetTaskoptionsErrorException(taskId, e);
        }

        assert taskoptions != null;

        return taskoptions;
    }

    protected boolean _removeTaskoption(Delete removeTaskoption, Taskoption taskoption)
    throws TaskoptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        return _removeTaskoption(removeTaskoption, taskoption.getTaskId(), taskoption.getName());
    }

    protected boolean _removeTaskoption(Delete removeTaskoption, final int taskId, final String name)
    throws TaskoptionManagerException {
        assert removeTaskoption != null;

        if (taskId < 0) throw new IllegalArgumentException("taskid can't be negative.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        boolean result = false;

        try {
            if (0 != executeUpdate(removeTaskoption, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskId)
                        .setString("name", name);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveTaskoptionErrorException(taskId, name, e);
        }

        return result;
    }

    protected class ProcessTaskoption extends DbRowProcessor {
        protected Collection<Taskoption> mCollection = null;
        protected Taskoption mTaskoption = null;

        public ProcessTaskoption() {
        }

        public void setCollection(Collection<Taskoption> collection) {
            mCollection = collection;
        }

        public boolean processRow(ResultSet resultSet)
        throws SQLException {
            assert resultSet != null;

            mTaskoption = new Taskoption();

            mTaskoption.setTaskId(resultSet.getInt("task_id"));
            mTaskoption.setName(resultSet.getString("name"));
            mTaskoption.setValue(resultSet.getString("value"));

            if (mCollection != null) {
                mCollection.add(mTaskoption);
            }

            return true;
        }

        public Taskoption getTaskoption() {
            return mTaskoption;
        }
    }
}

