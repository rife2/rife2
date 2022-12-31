/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.TableNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropTablePgsql extends TestDropTable {
    @Test
    void testInstantiationPgsql() {
        DropTable query = new DropTable(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (TableNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropTable");
        }
    }

    @Test
    void testIncompleteQueryPgsql() {
        DropTable query = new DropTable(PGSQL);
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
    void testClearPgsql() {
        DropTable query = new DropTable(PGSQL);
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
    void testOneTablePgsql() {
        DropTable query = new DropTable(PGSQL);
        query.table("tabletodrop");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop");
        execute(query);
    }

    @Test
    void testMultipleTablesPgsql() {
        DropTable query = new DropTable(PGSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        assertEquals(query.getSql(), "DROP TABLE tabletodrop1, tabletodrop2, tabletodrop3");
        execute(query);
    }

    @Test
    void testClonePgsql() {
        DropTable query = new DropTable(PGSQL);
        query.table("tabletodrop1")
            .table("tabletodrop2")
            .table("tabletodrop3");
        DropTable query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(query_clone);
    }
}
