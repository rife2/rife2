/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;
import rife.database.queries.Query;
import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.taskmanagers.exceptions.AddTaskErrorException;
import rife.scheduler.taskmanagers.exceptions.GetTaskIdErrorException;
import rife.scheduler.taskmanagers.exceptions.InstallTasksErrorException;
import rife.scheduler.taskmanagers.exceptions.RemoveTasksErrorException;

import java.sql.Statement;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableTask_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTask())
            .column("id", int.class)
            .column("type", String.class, RifeConfig.scheduler().getTaskTypeMaximumLength(), CreateTable.NOTNULL)
            .column("planned", long.class, CreateTable.NOTNULL)
            .column("frequency", String.class, RifeConfig.scheduler().getTaskFrequencyMaximumLength(), CreateTable.NULL)
            .column("busy", boolean.class)
            .customAttribute("id", "GENERATED ALWAYS AS IDENTITY")
            .defaultValue("busy", false)
            .primaryKey(RifeConfig.scheduler().getTableTask().toUpperCase() + "_PK", "id");

        addTask_ = new Insert(getDatasource())
            .into(createTableTask_.getTable())
            .fieldParameter("type")
            .fieldParameter("planned")
            .fieldParameter("frequency", "frequencySpecification")
            .fieldParameter("busy");
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

        final int[] result = new int[]{-1};

        try {
            if (0 == executeUpdate(addTask_, new DbPreparedStatementHandler() {
                @Override
                public DbPreparedStatement getPreparedStatement(Query query, DbConnection connection) {
                    return connection.getPreparedStatement(query, Statement.RETURN_GENERATED_KEYS);
                }

                @Override
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setBean(task);
                }

                @Override
                public int performUpdate(DbPreparedStatement statement) {
                    setParameters(statement);
                    int query_result = statement.executeUpdate();
                    try {
                        result[0] = statement.getFirstGeneratedIntKey();
                    } catch (DatabaseException e) {
                        throw new RuntimeException(new GetTaskIdErrorException(e));
                    }
                    return query_result;
                }
            })) {
                throw new AddTaskErrorException(task);
            }
        } catch (DatabaseException e) {
            throw new AddTaskErrorException(task, e);
        }

        assert result[0] >= 0;

        return result[0];
    }
}
