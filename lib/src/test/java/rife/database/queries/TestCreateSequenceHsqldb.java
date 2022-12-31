/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateSequenceHsqldb extends TestCreateSequence {
    @Test
    void testInstantiationHsqldb() {
        CreateSequence query = new CreateSequence(HSQLDB);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    void testClearHsqldb() {
        CreateSequence query = new CreateSequence(HSQLDB);
        query.name("sequencename");
        assertNotNull(query.getSql());
        query.clear();
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    void testCreateHsqldb() {
        CreateSequence query = new CreateSequence(HSQLDB);
        query.name("sequencename");
        assertEquals(query.getSql(), "CREATE SEQUENCE sequencename");
        execute(HSQLDB, query);
    }

    @Test
    void testCloneHsqldb() {
        CreateSequence query = new CreateSequence(HSQLDB);
        query.name("sequencename");
        CreateSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(HSQLDB, query_clone);
    }
}
