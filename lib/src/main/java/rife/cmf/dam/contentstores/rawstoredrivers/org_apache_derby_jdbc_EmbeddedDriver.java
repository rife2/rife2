/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.rawstoredrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableContentInfo_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawInfo())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .primaryKey("PK_CONTENTRAW", "contentId")
            .foreignKey("FK_CONTENTRAW", RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");

        createTableContentChunk_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreRawChunk())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("ordinal", int.class, CreateTable.NOTNULL)
            .column("chunk", Blob.class)
            .primaryKey("PK_CONTENTCHUNK", new String[]{"contentId", "ordinal"})
            .foreignKey("FK_CONTENTCHUNK", RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");
    }

    protected int storeChunks(Insert storeContentChunk, final int id, InputStream data)
    throws IOException {
        return storeChunksNoStream(storeContentChunk, id, data);
    }
}
