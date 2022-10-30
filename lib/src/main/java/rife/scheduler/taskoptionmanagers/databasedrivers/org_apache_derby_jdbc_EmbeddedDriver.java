/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.taskoptionmanagers.databasedrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableTaskoption_ = new CreateTable(getDatasource())
            .table(RifeConfig.scheduler().getTableTaskoption())
            .column("task_id", Integer.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.scheduler().getTaskoptionNameMaximumLength(), CreateTable.NOTNULL)
            .column("val", String.class, RifeConfig.scheduler().getTaskoptionValueMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.scheduler().getTableTaskoption().toUpperCase() + "_PK", new String[]{"task_id", "name"})
            .foreignKey(RifeConfig.scheduler().getTableTaskoption().toUpperCase() + "_FK", RifeConfig.scheduler().getTableTask(), "task_id", "id", null, CreateTable.CASCADE);
    }
}
