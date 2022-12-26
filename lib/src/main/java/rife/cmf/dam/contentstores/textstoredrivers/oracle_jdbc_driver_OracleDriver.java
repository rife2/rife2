/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.textstoredrivers;

import rife.database.*;

import rife.config.RifeConfig;
import rife.database.queries.CreateTable;
import java.sql.Clob;

public class oracle_jdbc_driver_OracleDriver extends generic {
    public oracle_jdbc_driver_OracleDriver(Datasource datasource) {
        super(datasource);

        createTableContent_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreText())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .column("content", Clob.class)
            .primaryKey(("PK_" + RifeConfig.cmf().getTableContentStoreText()).toUpperCase(), "contentId")
            .foreignKey(("FK_" + RifeConfig.cmf().getTableContentStoreText()).toUpperCase(), RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");
    }
}
