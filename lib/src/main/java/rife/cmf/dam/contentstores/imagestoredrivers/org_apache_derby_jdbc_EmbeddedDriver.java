/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.imagestoredrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;

import java.sql.Blob;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableContent_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreImage())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .column("content", Blob.class)
            .primaryKey("PK_CONTENTIMAGE", "contentId")
            .foreignKey("FK_CONTENTIMAGE", RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");
    }
}
