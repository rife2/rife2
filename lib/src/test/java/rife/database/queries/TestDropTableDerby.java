/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.TableNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTableDerby extends TestDropTable {
    @Test
    public void testInstantiationDerby() {
        DropTable query = new DropTable(mDerby);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @Test
    public void testIncompleteQueryDerby() {
        DropTable query = new DropTable(mDerby);
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
    public void testClearDerby() {
        DropTable query = new DropTable(mDerby);
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
    public void testOneTableDerby() {
        DropTable query = new DropTable(mDerby);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @Test
    public void testMultipleTablesDerby() {
        DropTable query = new DropTable(mDerby);
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
    public void testCloneDerby() {
        DropTable query = new DropTable(mDerby);
        query.table("tabletodrop1");
//			.table("tabletodrop2")
//			.table("tabletodrop3");
        DropTable query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(query_clone);
    }
}
