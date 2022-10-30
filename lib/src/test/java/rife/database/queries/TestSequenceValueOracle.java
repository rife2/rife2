/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.SequenceOperationRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestSequenceValueOracle extends TestSequenceValue {
    @Test
    public void testInstantiationOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @Test
    public void testInvalidOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
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
    public void testClearOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
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
    public void testNextOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
        query
            .name("sequencename")
            .next();
        assertEquals(query.getSql(), "SELECT sequencename.nextval FROM DUAL");
        assertTrue(execute(ORACLE, query) >= 0);
    }

    @Test
    public void testCurrentOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
        query
            .name("sequencename")
            .current();
        assertEquals(query.getSql(), "SELECT sequencename.currval FROM DUAL");
        assertTrue(execute(ORACLE, query) >= 0);
    }

    @Test
    public void testCloneOracle() {
        SequenceValue query = new SequenceValue(ORACLE);
        query
            .name("sequencename")
            .next();
        SequenceValue query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        assertTrue(execute(ORACLE, query_clone) >= 0);
    }
}
