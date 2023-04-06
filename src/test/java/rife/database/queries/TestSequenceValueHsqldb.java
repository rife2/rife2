/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestSequenceValueHsqldb extends TestSequenceValue {
    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInstantiationHsqldb() {
        var query = new SequenceValue(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "SequenceValue");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testInvalidHsqldb() {
        var query = new SequenceValue(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testClearHsqldb() {
        var query = new SequenceValue(HSQLDB);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testNextHsqldb() {
        var query = new SequenceValue(HSQLDB);
        query
            .name("sequencename")
            .next();
        assertEquals(query.getSql(), "CALL NEXT VALUE FOR sequencename");
        assertTrue(execute(HSQLDB, query) >= 0);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCurrentHsqldb() {
        var query = new SequenceValue(HSQLDB);
        query
            .name("sequencename")
            .current();
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.HSQLDB)
    void testCloneHsqldb() {
        var query = new SequenceValue(HSQLDB);
        query
            .name("sequencename")
            .next();
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        assertTrue(execute(HSQLDB, query_clone) >= 0);
    }
}
