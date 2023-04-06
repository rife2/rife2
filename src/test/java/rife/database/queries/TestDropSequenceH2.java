/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceH2 extends TestDropSequence {
    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testInstantiationH2() {
        var query = new DropSequence(H2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testClearH2() {
        var query = new DropSequence(H2);
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

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCreateH2() {
        var query = new DropSequence(H2);
        query.name("sequencename");
        assertEquals(query.getSql(), "DROP SEQUENCE sequencename");
        execute(H2, query);
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.H2)
    void testCloneH2() {
        var query = new DropSequence(H2);
        query.name("sequencename");
        var query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(H2, query_clone);
    }
}
