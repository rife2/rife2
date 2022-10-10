/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.SequenceOperationRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestSequenceValuePgsql extends TestSequenceValue {
    @Test
    public void testInstantiationPgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @Test
    public void testInvalidPgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (SequenceOperationRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
        query.clear();
        query.next();
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
        query.clear();
    }

    @Test
    public void testClearPgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        query
            .name("sequencename")
            .next();
        assertNotNull(query.getSql());
        query
            .clear();
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @Test
    public void testNextPgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        query
            .name("sequencename")
            .next();
        assertEquals(query.getSql(), "SELECT nextval('sequencename')");
        assertTrue(execute(PGSQL, query) >= 0);
    }

    @Test
    public void testCurrentPgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        query
            .name("sequencename")
            .current();
        assertEquals(query.getSql(), "SELECT currval('sequencename')");
        assertTrue(execute(PGSQL, query) >= 0);
    }

    @Test
    public void testClonePgsql() {
        SequenceValue query = new SequenceValue(PGSQL);
        query
            .name("sequencename")
            .next();
        SequenceValue query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        assertTrue(execute(PGSQL, query_clone) >= 0);
    }
}
