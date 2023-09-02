/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.textstoredrivers;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.queries.CreateTable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableContent_ = new CreateTable(getDatasource())
            .table(RifeConfig.cmf().getTableContentStoreText())
            .column("contentId", int.class, CreateTable.NOTNULL)
            .column("contentSize", int.class, CreateTable.NOTNULL)
            .column("content", Clob.class)
            .primaryKey("PK_CONTENTTEXT", "contentId")
            .foreignKey("FK_CONTENTTEXT", RifeConfig.cmf().getTableContentInfo(), "contentId", "contentId");
    }

    @Override
    protected void outputContentColumn(ResultSet resultSet, OutputStream os)
    throws SQLException {
        var clob = resultSet.getClob("content");
        var text_reader = clob.getCharacterStream();
        var buffer = new char[512];
        var size = 0;
        try {
            while ((size = text_reader.read(buffer)) != -1) {
                os.write(new String(buffer).getBytes(StandardCharsets.UTF_8), 0, size);
            }

            os.flush();
        } catch (IOException e) {
            // don't do anything, the client has probably disconnected
        }
    }
}
