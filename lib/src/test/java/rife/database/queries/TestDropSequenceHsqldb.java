/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceHsqldb extends TestDropSequence {
    @Test
    void testInstantiationHsqldb() {
        DropSequence query = new DropSequence(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    void testClearHsqldb() {
        DropSequence query = new DropSequence(HSQLDB);
        query.name("sequencename");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    void testCreateHsqldb() {
        DropSequence query = new DropSequence(HSQLDB);
        query.name("sequencename");
        assertEquals(query.getSql(), "DROP SEQUENCE sequencename");
        execute(HSQLDB, query);
    }

    @Test
    void testCloneHsqldb() {
        DropSequence query = new DropSequence(HSQLDB);
        query.name("sequencename");
        DropSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(HSQLDB, query_clone);
    }
}
