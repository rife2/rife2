/*
 * Copyright 2001-202 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTableHsqldb extends TestDropTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new DropTable(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testIncompleteQueryHsqldb() {
        var query = new DropTable(HSQLDB);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
        query.table("tablename");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new DropTable(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testOneTableHsqldb() {
        var query = new DropTable(HSQLDB);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testMultipleTablesHsqldb() {
        var query = new DropTable(HSQLDB);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new DropTable(HSQLDB);
        query.table("tabletodrop");
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
