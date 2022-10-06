/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceHsqldb extends TestDropSequence {
    @Test
    public void testInstantiationHsqldb() {
        DropSequence query = new DropSequence(mHsqldb);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    public void testClearHsqldb() {
        DropSequence query = new DropSequence(mHsqldb);
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
    public void testCreateHsqldb() {
        DropSequence query = new DropSequence(mHsqldb);
        query.name("sequencename");
        assertEquals(query.getSql(), "DROP SEQUENCE sequencename");
        execute(mHsqldb, query);
    }

    @Test
    public void testCloneHsqldb() {
        DropSequence query = new DropSequence(mHsqldb);
        query.name("sequencename");
        DropSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(mHsqldb, query_clone);
    }
}
