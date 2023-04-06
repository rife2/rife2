/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.testdatabasedrivers;

import rife.database.Datasource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;

public class org_postgresql_Driver extends generic {
    public org_postgresql_Driver(Datasource datasource) {
        super(datasource);

        createStructure_ = new CreateTable(getDatasource())
            .table("TestTable")
            .column("id", int.class, CreateTable.NOTNULL)
            .column("valuecol", String.class, 32, CreateTable.NOTNULL)
            .primaryKey("ID_PK", "id");

        removeStructure_ = new DropTable(getDatasource())
            .table(createStructure_.getTable());
    }

    public boolean install()
    throws DatabaseException {
        return _install(createStructure_);
    }

    public boolean remove()
    throws DatabaseException {
        return _remove(removeStructure_);
    }
}
