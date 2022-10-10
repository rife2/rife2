/*
 * Copyright 2001-202 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTableHsqldb extends TestDropTable {
    @Test
    public void testInstantiationHsqldb() {
        DropTable query = new DropTable(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @Test
    public void testIncompleteQueryHsqldb() {
        DropTable query = new DropTable(HSQLDB);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
        query.table("tablename");
        assertNotNull(query.getSql());
    }

    @Test
    public void testClearHsqldb() {
        DropTable query = new DropTable(HSQLDB);
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

    @Test
    public void testOneTableHsqldb() {
        DropTable query = new DropTable(HSQLDB);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @Test
    public void testMultipleTablesHsqldb() {
        DropTable query = new DropTable(HSQLDB);
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

    @Test
    public void testCloneHsqldb() {
        DropTable query = new DropTable(HSQLDB);
        query.table("tabletodrop");
        DropTable query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
