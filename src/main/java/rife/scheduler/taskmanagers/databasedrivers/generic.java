/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.queries.*;
import rife.scheduler.Frequency;
import rife.scheduler.Task;
import rife.scheduler.exceptions.TaskManagerException;
import rife.scheduler.taskmanagers.DatabaseTasks;

import java.util.Collection;

public class generic extends DatabaseTasks {
    protected CreateSequence createSequenceTask_ = null;
    protected CreateTable createTableTask_ = null;
    protected DropSequence dropSequenceTask_ = null;
    protected DropTable dropTableTask_ = null;
    protected SequenceValue getTaskId_ = null;
    protected Insert addTask_ = null;
    protected Select getTask_ = null;
    protected Select getAllTasks_ = null;
    protected Select getTasksToProcess_ = null;
    protected Select getScheduledTasks_ = null;
    protected Update updateTask_ = null;
    protected Delete removeTask_ = null;
    protected Update activateTask_ = null;
    protected Update deactivateTask_ = null;

    public generic(Datasource datasource) {
        super(datasource);

        createSequenceTask_ = new CreateSequence(getDatasource())
            .name(RifeConfig.scheduler().getSequenceTask());

        createTableTask_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTask())
            .column("id", int.class, CreateTable.NOTNULL)
            .column("type", String.class, RifeConfig.scheduler().getTaskTypeMaximumLength(), CreateTable.NOTNULL)
            .column("planned", long.class, CreateTable.NOTNULL)
            .column("frequency", String.class, RifeConfig.scheduler().getTaskFrequencyMaximumLength(), CreateTable.NULL)
            .column("busy", boolean.class)
            .defaultValue("busy", false)
            .primaryKey(RifeConfig.scheduler().getTableTask().toUpperCase() + "_PK", "id");

        dropSequenceTask_ = new DropSequence(getDatasource())
            .name(createSequenceTask_.getName());

        dropTableTask_ = new DropTable(getDatasource())
            .table(createTableTask_.getTable());

        getTaskId_ = new SequenceValue(getDatasource())
            .name(createSequenceTask_.getName())
            .next();

        addTask_ = new Insert(getDatasource())
            .into(createTableTask_.getTable())
            .fieldParameter("id")
            .fieldParameter("type")
            .fieldParameter("planned")
            .fieldParameter("frequency", "frequencySpecification")
            .fieldParameter("busy");

        getTask_ = new Select(getDatasource())
            .from(createTableTask_.getTable())
            .whereParameter("id", "=");

        getAllTasks_ = new Select(getDatasource())
            .from(createTableTask_.getTable());

        getTasksToProcess_ = new Select(getDatasource())
            .from(createTableTask_.getTable())
            .whereParameter("planned", "<")
            .whereAnd("busy", "=", false);

        getScheduledTasks_ = new Select(getDatasource())
            .from(createTableTask_.getTable())
            .whereParameter("planned", ">=")
            .whereAnd("busy", "=", false);

        updateTask_ = new Update(getDatasource())
            .table(createTableTask_.getTable())
            .fieldParameter("type")
            .fieldParameter("planned")
            .fieldParameter("frequency", "frequencySpecification")
            .fieldParameter("busy")
            .whereParameter("id", "=");

        removeTask_ = new Delete(getDatasource())
            .from(createTableTask_.getTable())
            .whereParameter("id", "=");

        activateTask_ = new Update(getDatasource())
            .table(createTableTask_.getTable())
            .field("busy", true)
            .whereParameter("id", "=");

        deactivateTask_ = new Update(getDatasource())
            .table(createTableTask_.getTable())
            .field("busy", false)
            .whereParameter("id", "=");
    }

    @Override
    public boolean install()
    throws TaskManagerException {
        return install_(createSequenceTask_, createTableTask_);
    }

    @Override
    public boolean remove()
    throws TaskManagerException {
        return remove_(dropSequenceTask_, dropTableTask_);
    }

    @Override
    public int addTask(final Task task)
    throws TaskManagerException {
        return addTask_(getTaskId_, addTask_, new DbPreparedStatementHandler() {
            @Override
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setBean(task);
            }
        }, task);
    }

    @Override
    public boolean updateTask(final Task task)
    throws TaskManagerException {
        return updateTask_(updateTask_, new DbPreparedStatementHandler() {
            @Override
            public void setParameters(DbPreparedStatement statement) {
                statement
                    .setBean(task);
            }
        }, task);
    }

    @Override
    public Task getTask(int id)
    throws TaskManagerException {
        return getTask_(getTask_, new ProcessTask(), id);
    }

    @Override
    public Collection<Task> getAllTasks()
    throws TaskManagerException {
        return getAllTasks_(getAllTasks_, new ProcessTask());
    }

    @Override
    public Collection<Task> getTasksToProcess()
    throws TaskManagerException {
        return getTasksToProcess_(getTasksToProcess_, new ProcessTask());
    }

    @Override
    public Collection<Task> getScheduledTasks()
    throws TaskManagerException {
        return getScheduledTasks_(getScheduledTasks_, new ProcessTask());
    }

    @Override
    public boolean removeTask(int id)
    throws TaskManagerException {
        return removeTask_(removeTask_, id);
    }

    @Override
    public boolean rescheduleTask(Task task, long planned, Frequency frequency)
    throws TaskManagerException {
        return rescheduleTask_(task, planned, frequency);
    }

    @Override
    public boolean concludeTask(Task task)
    throws TaskManagerException {
        return concludeTask_(task);
    }

    @Override
    public boolean activateTask(int id)
    throws TaskManagerException {
        return activateTask_(activateTask_, id);
    }

    @Override
    public boolean deactivateTask(int id)
    throws TaskManagerException {
        return deactivateTask_(deactivateTask_, id);
    }
}
