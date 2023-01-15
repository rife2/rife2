/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTablePgsql extends TestDropTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testInstantiationPgsql() {
        var query = new DropTable(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testIncompleteQueryPgsql() {
        var query = new DropTable(PGSQL);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
        query.table("tablename");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClearPgsql() {
        var query = new DropTable(PGSQL);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testOneTablePgsql() {
        var query = new DropTable(PGSQL);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testMultipleTablesPgsql() {
        var query = new DropTable(PGSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop1, tabletodrop2, tabletodrop3");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testClonePgsql() {
        var query = new DropTable(PGSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(query_clone);
    }
}
