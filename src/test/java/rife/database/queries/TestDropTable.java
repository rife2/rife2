/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;

public abstract class TestDropTable extends TestQuery {
    public void execute(DropTable query) {
        try {
            var manager = new DbQueryManager(query.getDatasource());
            var create_table = new CreateTable(query.getDatasource());
            create_table.column("firstcolumn", int.class);

            for (var table : query.getTables()) {
                create_table.table(table);
                manager.executeUpdate(create_table);
            }
            manager.executeUpdate(query);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
}
