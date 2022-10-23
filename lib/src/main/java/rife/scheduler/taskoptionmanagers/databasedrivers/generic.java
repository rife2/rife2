/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.databasedrivers;

import rife.database.queries.*;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.scheduler.Taskoption;
import rife.scheduler.exceptions.TaskoptionManagerException;
import rife.scheduler.taskoptionmanagers.DatabaseTaskoptions;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskoptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;

import java.util.Collection;

public class generic extends DatabaseTaskoptions {
    protected CreateTable createTableTaskoption_ = null;
    protected DropTable dropTableTaskoption_ = null;
    protected Insert addTaskoption_ = null;
    protected Select getTaskoption_ = null;
    protected Select getTaskoptions_ = null;
    protected Update updateTaskoption_ = null;
    protected Delete removeTaskoption_ = null;

    public generic(Datasource datasource) {
        super(datasource);

        createTableTaskoption_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTaskoption())
            .column("task_id", Integer.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.scheduler().getTaskoptionNameMaximumLength(), CreateTable.NOTNULL)
            .column("val", String.class, RifeConfig.scheduler().getTaskoptionValueMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.scheduler().getTableTaskoption().toUpperCase() + "_PK", new String[]{"task_id", "name"})
            .foreignKey(RifeConfig.scheduler().getTableTaskoption().toUpperCase() + "_TASKID_FK", RifeConfig.scheduler().getTableTask(), "task_id", "id", null, CreateTable.CASCADE);

        dropTableTaskoption_ = new DropTable(getDatasource())
            .table(createTableTaskoption_.getTable());

        addTaskoption_ = new Insert(getDatasource())
            .into(createTableTaskoption_.getTable())
            .fieldParameter("task_id")
            .fieldParameter("name")
            .fieldParameter("val");

        getTaskoption_ = new Select(getDatasource())
            .from(createTableTaskoption_.getTable())
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");

        getTaskoptions_ = new Select(getDatasource())
            .from(createTableTaskoption_.getTable())
            .whereParameter("task_id", "=");

        updateTaskoption_ = new Update(getDatasource())
            .table(createTableTaskoption_.getTable())
            .fieldParameter("val")
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");

        removeTaskoption_ = new Delete(getDatasource())
            .from(createTableTaskoption_.getTable())
            .whereParameter("task_id", "=")
            .whereParameterAnd("name", "=");
    }

    public boolean install()
    throws TaskoptionManagerException {
        return install_(createTableTaskoption_);
    }

    public boolean remove()
    throws TaskoptionManagerException {
        return remove_(dropTableTaskoption_);
    }

    public boolean addTaskoption(final Taskoption taskoption)
    throws TaskoptionManagerException {
        try {
            return _addTaskoption(addTaskoption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskoption.getTaskId())
                        .setString("name", taskoption.getName())
                        .setString("val", taskoption.getValue());
                }
            }, taskoption);
        } catch (TaskoptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableTaskoption_.getForeignKeys().get(0).getName())) {
                    throw new InexistentTaskIdException(taskoption.getTaskId());
                } else if (message.contains(createTableTaskoption_.getPrimaryKeys().get(0).getName())) {
                    throw new DuplicateTaskoptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }

    public boolean updateTaskoption(final Taskoption taskoption)
    throws TaskoptionManagerException {
        try {
            return _updateTaskoption(updateTaskoption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskoption.getTaskId())
                        .setString("name", taskoption.getName())
                        .setString("val", taskoption.getValue());
                }
            }, taskoption);
        } catch (TaskoptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableTaskoption_.getForeignKeys().get(0).getName())) {
                    throw new InexistentTaskIdException(taskoption.getTaskId());
                } else if (message.contains(createTableTaskoption_.getPrimaryKeys().get(0).getName())) {
                    throw new DuplicateTaskoptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }

    public Taskoption getTaskoption(int taskId, String name)
    throws TaskoptionManagerException {
        return _getTaskoption(getTaskoption_, new ProcessTaskoption(), taskId, name);
    }

    public Collection<Taskoption> getTaskoptions(int taskId)
    throws TaskoptionManagerException {
        return _getTaskoptions(getTaskoptions_, new ProcessTaskoption(), taskId);
    }

    public boolean removeTaskoption(Taskoption taskoption)
    throws TaskoptionManagerException {
        return _removeTaskoption(removeTaskoption_, taskoption);
    }

    public boolean removeTaskoption(int taskId, String name)
    throws TaskoptionManagerException {
        return _removeTaskoption(removeTaskoption_, taskId, name);
    }
}
