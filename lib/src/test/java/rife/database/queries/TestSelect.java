/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.*;
import rife.database.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class TestSelect extends TestQuery {
    public DbQueryManager setupQuery(Datasource datasource) {
        var manager = new DbQueryManager(datasource);

        var createtable = new CreateTable(datasource);
        createtable.table("tablename")
            .columns(BeanImpl.class)
            .precision("propertyBigDecimal", 18, 9)
            .precision("propertyChar", 1)
            .precision("propertyDouble", 12, 3)
            .precision("propertyDoubleObject", 12, 3)
            .precision("propertyFloat", 13, 2)
            .precision("propertyFloatObject", 13, 2)
            .precision("propertyString", 255)
            .precision("propertyStringBuffer", 100);

        try {
            // prepare table and data
            manager.executeUpdate(createtable);

            createtable.table("table2");
            manager.executeUpdate(createtable);

            createtable.table("table3");
            manager.executeUpdate(createtable);

            var insert = new Insert(datasource);
            insert.into("tablename")
                .fields(BeanImpl.getPopulatedBean());
            manager.executeUpdate(insert);

            insert.into("table2");
            manager.executeUpdate(insert);

            insert.into("table3");
            manager.executeUpdate(insert);

            insert.clear();
            insert.into("tablename")
                .fields(BeanImpl.getNullBean());
            manager.executeUpdate(insert);

            var impl = BeanImpl.getPopulatedBean();
            insert.clear();
            impl.setPropertyInt(3);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);
            insert.clear();
            impl.setPropertyInt(4);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);
            insert.clear();
            impl.setPropertyInt(5);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);

            insert.into("table2");
            manager.executeUpdate(insert);

            insert.into("table3");
            manager.executeUpdate(insert);
        } catch (DatabaseException e) {
            cleanupQuery(manager);
            throw new RuntimeException(e);
        }

        return manager;
    }

    public void cleanupQuery(DbQueryManager manager) {
        // clean up nicely
        var drop_table = new DropTable(manager.getDatasource());
        try {
            drop_table.table("tablename");
            manager.executeUpdate(drop_table);

            drop_table.clear();
            drop_table.table("table2");
            manager.executeUpdate(drop_table);

            drop_table.clear();
            drop_table.table("table3");
            manager.executeUpdate(drop_table);
        } catch (DatabaseException e) {
            System.out.println(e.toString());
        }
    }

    public boolean execute(Select query) {
        var success = false;
        var manager = setupQuery(query.getDatasource());

        try {
            success = manager.executeHasResultRows(query);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        } finally {
            cleanupQuery(manager);
        }

        return success;
    }

    public boolean execute(Select query, DbPreparedStatementHandler handler) {
        var success = false;
        var manager = setupQuery(query.getDatasource());

        try {
            success = manager.executeHasResultRows(query, handler);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        } finally {
            cleanupQuery(manager);
        }

        return success;
    }
}

