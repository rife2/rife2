/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.SequenceOperationRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestSequenceValueH2 extends TestSequenceValue {
    @Test
    public void testInstantiationH2() {
        SequenceValue query = new SequenceValue(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @Test
    public void testInvalidH2() {
        SequenceValue query = new SequenceValue(H2);
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
    public void testClearH2() {
        SequenceValue query = new SequenceValue(H2);
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
    public void testNextH2() {
        SequenceValue query = new SequenceValue(H2);
        query
            .name("sequencename")
            .next();
        assertEquals(query.getSql(), "SELECT nextval('sequencename')");
        assertTrue(execute(H2, query) >= 0);
    }

    @Test
    public void testCurrentH2() {
        SequenceValue query = new SequenceValue(H2);
        query
            .name("sequencename")
            .current();
        assertEquals(query.getSql(), "SELECT currval('sequencename')");
        assertTrue(execute(H2, query) >= 0);
    }

    @Test
    public void testCloneH2() {
        SequenceValue query = new SequenceValue(H2);
        query
            .name("sequencename")
            .next();
        SequenceValue query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        assertTrue(execute(H2, query_clone) >= 0);
    }
}
