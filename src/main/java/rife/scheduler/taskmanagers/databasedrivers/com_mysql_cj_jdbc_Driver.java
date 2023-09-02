/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.DbConnectionUser;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Select;
import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.taskmanagers.exceptions.AddTaskErrorException;
import rife.scheduler.taskmanagers.exceptions.GetTaskIdErrorException;
import rife.scheduler.taskmanagers.exceptions.InstallTasksErrorException;
import rife.scheduler.taskmanagers.exceptions.RemoveTasksErrorException;

public class com_mysql_cj_jdbc_Driver extends generic {
    protected Select getInsertedTaskId_ = null;

    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        createTableTask_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTask())
            .column("id", int.class)
            .column("type", String.class, RifeConfig.scheduler().getTaskTypeMaximumLength(), CreateTable.NOTNULL)
            .column("planned", long.class, CreateTable.NOTNULL)
            .column("frequency", String.class, RifeConfig.scheduler().getTaskFrequencyMaximumLength(), CreateTable.NULL)
            .column("busy", boolean.class)
            .customAttribute("id", "AUTO_INCREMENT")
            .defaultValue("busy", false)
            .primaryKey(RifeConfig.scheduler().getTableTask().toUpperCase() + "_PK", "id");

        getInsertedTaskId_ = new Select(getDatasource())
            .field("LAST_INSERT_ID()");
    }


    @Override
    public boolean install()
    throws TaskManagerException {
        try {
            executeUpdate(createTableTask_);
        } catch (DatabaseException e) {
            throw new InstallTasksErrorException(e);
        }

        return true;
    }

    @Override
    public boolean remove()
    throws TaskManagerException {
        try {
            executeUpdate(dropTableTask_);
        } catch (DatabaseException e) {
            throw new RemoveTasksErrorException(e);
        }

        return true;
    }

    @Override
    public int addTask(final Task task)
    throws TaskManagerException {
        if (null == task) throw new IllegalArgumentException("task can't be null.");

        int result = 0;

        try {
            result = reserveConnection(new DbConnectionUser() {
                @Override
                public Integer useConnection(DbConnection connection) {
                    try {
                        if (0 == executeUpdate(addTask_, s -> s
                            .setBean(task)
                            .setNull("id", java.sql.Types.INTEGER))) {
                            throw new RuntimeException(new AddTaskErrorException(task));
                        }
                    } catch (DatabaseException e) {
                        throw new RuntimeException(new AddTaskErrorException(task, e));
                    }

                    try {
                        return executeGetFirstInt(getInsertedTaskId_);
                    } catch (DatabaseException e) {
                        throw new RuntimeException(new GetTaskIdErrorException(e));
                    }
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TaskManagerException) {
                throw (TaskManagerException) e.getCause();
            }
        }

        assert result >= 0;

        return result;
    }
}
