/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.databasedrivers;

import rife.database.queries.*;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.scheduler.TaskOption;
import rife.scheduler.exceptions.TaskOptionManagerException;
import rife.scheduler.taskoptionmanagers.DatabaseTaskOptions;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskOptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;

import java.util.Collection;

public class generic extends DatabaseTaskOptions {
    protected CreateTable createTableTaskOption_ = null;
    protected DropTable dropTableTaskOption_ = null;
    protected Insert addTaskOption_ = null;
    protected Select getTaskOption_ = null;
    protected Select getTaskOptions_ = null;
    protected Update updateTaskOption_ = null;
    protected Delete removeTaskOption_ = null;

    public generic(Datasource datasource) {
        super(datasource);

        createTableTaskOption_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTaskOption())
            .column("task_id", Integer.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.scheduler().getTaskOptionNameMaximumLength(), CreateTable.NOTNULL)
            .column("val", String.class, RifeConfig.scheduler().getTaskOptionValueMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.scheduler().getTableTaskOption().toUpperCase() + "_PK", new String[]{"task_id", "name"})
            .foreignKey(RifeConfig.scheduler().getTableTaskOption().toUpperCase() + "_TASKID_FK", RifeConfig.scheduler().getTableTask(), "task_id", "id", null, CreateTable.CASCADE);

        dropTableTaskOption_ = new DropTable(getDatasource())
            .table(createTableTaskOption_.getTable());

        addTaskOption_ = new Insert(getDatasource())
            .into(createTableTaskOption_.getTable())
            .fieldParameter("task_id")
            .fieldParameter("name")
            .fieldParameter("val");

        getTaskOption_ = new Select(getDatasource())
            .from(createTableTaskOption_.getTable())
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");

        getTaskOptions_ = new Select(getDatasource())
            .from(createTableTaskOption_.getTable())
            .whereParameter("task_id", "=");

        updateTaskOption_ = new Update(getDatasource())
            .table(createTableTaskOption_.getTable())
            .fieldParameter("val")
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");

        removeTaskOption_ = new Delete(getDatasource())
            .from(createTableTaskOption_.getTable())
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");
    }

    public boolean install()
    throws TaskOptionManagerException {
        return install_(createTableTaskOption_);
    }

    public boolean remove()
    throws TaskOptionManagerException {
        return remove_(dropTableTaskOption_);
    }

    public boolean addTaskOption(final TaskOption taskOption)
    throws TaskOptionManagerException {
        try {
            return _addTaskOption(addTaskOption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskOption.getTaskId())
                        .setString("name", taskOption.getName())
                        .setString("val", taskOption.getValue());
                }
            }, taskOption);
        } catch (TaskOptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableTaskOption_.getForeignKeys().get(0).getName())) {
                    throw new InexistentTaskIdException(taskOption.getTaskId());
                } else if (message.contains(createTableTaskOption_.getPrimaryKeys().get(0).getName())) {
                    throw new DuplicateTaskOptionException(taskOption.getTaskId(), taskOption.getName());
                }
            }

            throw e;
        }
    }

    public boolean updateTaskOption(final TaskOption taskOption)
    throws TaskOptionManagerException {
        try {
            return _updateTaskOption(updateTaskOption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskOption.getTaskId())
                        .setString("name", taskOption.getName())
                        .setString("val", taskOption.getValue());
                }
            }, taskOption);
        } catch (TaskOptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableTaskOption_.getForeignKeys().get(0).getName())) {
                    throw new InexistentTaskIdException(taskOption.getTaskId());
                } else if (message.contains(createTableTaskOption_.getPrimaryKeys().get(0).getName())) {
                    throw new DuplicateTaskOptionException(taskOption.getTaskId(), taskOption.getName());
                }
            }

            throw e;
        }
    }

    public TaskOption getTaskOption(int taskId, String name)
    throws TaskOptionManagerException {
        return _getTaskOption(getTaskOption_, new ProcessTaskOption(), taskId, name);
    }

    public Collection<TaskOption> getTaskOptions(int taskId)
    throws TaskOptionManagerException {
        return _getTaskOptions(getTaskOptions_, new ProcessTaskOption(), taskId);
    }

    public boolean removeTaskOption(TaskOption taskOption)
    throws TaskOptionManagerException {
        return _removeTaskOption(removeTaskOption_, taskOption);
    }

    public boolean removeTaskOption(int taskId, String name)
    throws TaskOptionManagerException {
        return _removeTaskOption(removeTaskOption_, taskId, name);
    }
}
