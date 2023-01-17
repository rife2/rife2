/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
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
import rife.scheduler.TaskOption;
import rife.scheduler.exceptions.TaskOptionManagerException;
import rife.scheduler.taskoptionmanagers.exceptions.AddTaskOptionErrorException;
import rife.scheduler.taskoptionmanagers.exceptions.DuplicateTaskOptionException;
import rife.scheduler.taskoptionmanagers.exceptions.InexistentTaskIdException;
import rife.scheduler.taskoptionmanagers.exceptions.UpdateTaskOptionErrorException;

public class com_mysql_cj_jdbc_Driver extends generic {
    protected Select taskIdExists_ = null;

    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        createTableTaskOption_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTaskOption())
            .column("task_id", Integer.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.scheduler().getTaskOptionNameMaximumLength(), CreateTable.NOTNULL)
            .column("val", String.class, RifeConfig.scheduler().getTaskOptionValueMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.scheduler().getTableTaskOption().toUpperCase() + "_PK", new String[]{"task_id", "name"});

        taskIdExists_ = new Select(getDatasource())
            .from(RifeConfig.scheduler().getTableTask())
            .whereParameter("id", "=");
    }

    public boolean addTaskOption(final TaskOption taskoption)
    throws TaskOptionManagerException {
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
            throw new AddTaskOptionErrorException(taskoption, e);
        }

        try {
            return _addTaskOption(addTaskOption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskoption.getTaskId())
                        .setString("name", taskoption.getName())
                        .setString("val", taskoption.getValue());
                }
            }, taskoption);
        } catch (TaskOptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                if (-1 != e.getCause().getCause().getMessage().toLowerCase().indexOf("duplicate")) {
                    throw new DuplicateTaskOptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }

    public boolean updateTaskOption(final TaskOption taskoption)
    throws TaskOptionManagerException {
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
            throw new UpdateTaskOptionErrorException(taskoption, e);
        }

        try {
            return _updateTaskOption(updateTaskOption_, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("task_id", taskoption.getTaskId())
                        .setString("name", taskoption.getName())
                        .setString("val", taskoption.getValue());
                }
            }, taskoption);
        } catch (TaskOptionManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                if (e.getCause().getCause().getMessage().toLowerCase().contains("duplicate")) {
                    throw new DuplicateTaskOptionException(taskoption.getTaskId(), taskoption.getName());
                }
            }

            throw e;
        }
    }
}
