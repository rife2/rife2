/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.SequenceOperationRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestSequenceValueH2 extends TestSequenceValue {
    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInstantiationH2() {
        SequenceValue query = new SequenceValue(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInvalidH2() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testClearH2() {
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testNextH2() {
        SequenceValue query = new SequenceValue(H2);
        query
            .name("sequencename")
            .next();
        assertEquals(query.getSql(), "SELECT nextval('sequencename')");
        assertTrue(execute(H2, query) >= 0);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCurrentH2() {
        SequenceValue query = new SequenceValue(H2);
        query
            .name("sequencename")
            .current();
        assertEquals(query.getSql(), "SELECT currval('sequencename')");
        assertTrue(execute(H2, query) >= 0);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCloneH2() {
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
