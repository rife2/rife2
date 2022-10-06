/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.testdatabasedrivers;

import rife.database.Datasource;
import rife.database.TestDbQueryManagerImpl;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;

public class generic extends TestDbQueryManagerImpl {
    protected CreateTable createStructure_;
    protected Insert store_;
    protected Select count_;
    protected DropTable removeStructure_;

    public generic(Datasource datasource) {
        super(datasource);

        createStructure_ = new CreateTable(getDatasource())
            .table("TestTable")
            .column("id", int.class, CreateTable.NOTNULL)
            .column("valuecol", String.class, 32, CreateTable.NOTNULL)
            .primaryKey("ID_PK", "id");

        store_ = new Insert(getDatasource())
            .into(createStructure_.getTable())
            .fieldParameter("id")
            .fieldParameter("valuecol");

        count_ = new Select(getDatasource())
            .from(createStructure_.getTable())
            .field("count(*)");

        removeStructure_ = new DropTable(getDatasource())
            .table(createStructure_.getTable());
    }

    public boolean install()
    throws DatabaseException {
        return _install(createStructure_);
    }

    public void store(int id, String value)
    throws DatabaseException {
        _store(store_, id, value);
    }

    public int count()
    throws DatabaseException {
        return _count(count_);
    }

    public boolean remove()
    throws DatabaseException {
        return _remove(removeStructure_);
    }
}
