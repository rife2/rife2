/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Select;
import rife.scheduler.Taskoption;
import rife.scheduler.exceptions.TaskoptionManagerException;
import rife.scheduler.taskoptionmanagers.exceptions.AddTaskoptionErrorException;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskoptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.scheduler.taskoptionmanagers.exceptions.UpdateTaskoptionErrorException;

public class com_mysql_cj_jdbc_Driver extends generic {
    protected Select taskIdExists_ = null;

    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        createTableTaskoption_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTaskoption())
            .column("task_id", Integer.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.scheduler().getTaskoptionNameMaximumLength(), CreateTable.NOTNULL)
            .column("val", String.class, RifeConfig.scheduler().getTaskoptionValueMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.scheduler().getTableTaskoption().toUpperCase() + "_PK", new String[]{"task_id", "name"});

        taskIdExists_ = new Select(getDatasource())
            .from(RifeConfig.scheduler().getTableTask())
            .whereParameter("id", "=");
    }

    public boolean addTaskoption(final Taskoption taskoption)
    throws TaskoptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        // simulate TaskID foreign key
        try {
            if (!executeHasResultRows(taskIdExists_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", taskoption.getTaskId());
                }
            })) {
                throw new InexistentTaskIdException(taskoption.getTaskId());
            }
        } catch (DatabaseException e) {
            throw new AddTaskoptionErrorException(taskoption, e);
        }

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
                if (-1 != e.getCause().getCause().getMessage().toLowerCase().indexOf("duplicate")) {
                    throw new DuplicateTaskoptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }

    public boolean updateTaskoption(final Taskoption taskoption)
    throws TaskoptionManagerException {
        if (null == taskoption) throw new IllegalArgumentException("taskoption can't be null.");

        // simulate TaskID foreign key
        try {
            if (!executeHasResultRows(taskIdExists_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("id", taskoption.getTaskId());
                }
            })) {
                throw new InexistentTaskIdException(taskoption.getTaskId());
            }
        } catch (DatabaseException e) {
            throw new UpdateTaskoptionErrorException(taskoption, e);
        }

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
                if (e.getCause().getCause().getMessage().toLowerCase().contains("duplicate")) {
                    throw new DuplicateTaskoptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }
}
