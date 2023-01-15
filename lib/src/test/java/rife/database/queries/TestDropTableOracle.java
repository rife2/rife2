/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTableOracle extends TestDropTable {
    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testInstantiationOracle() {
        var query = new DropTable(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testIncompleteQueryOracle() {
        var query = new DropTable(ORACLE);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
        query.table("tablename");
        assertNotNull(query.getSql());
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testClearOracle() {
        var query = new DropTable(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testOneTableOracle() {
        var query = new DropTable(ORACLE);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testMultipleTablesOracle() {
        var query = new DropTable(ORACLE);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.ORACLE)
    void testCloneOracle() {
        var query = new DropTable(ORACLE);
        query.table("tabletodrop");
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(query_clone);
    }
}
