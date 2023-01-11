/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTableMysql extends TestDropTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        DropTable query = new DropTable(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL) void testIncompleteQueryMysql() {
        DropTable query = new DropTable(MYSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
        query.table("tablename");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL) void testClearMysql() {
        DropTable query = new DropTable(MYSQL);
        query.table("tablename");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL) void testOneTableMysql() {
        DropTable query = new DropTable(MYSQL);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL) void testMultipleTablesMysql() {
        DropTable query = new DropTable(MYSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop1, tabletodrop2, tabletodrop3");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL) void testCloneMysql() {
        DropTable query = new DropTable(MYSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        DropTable query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
