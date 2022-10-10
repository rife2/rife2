/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequencePgsql extends TestDropSequence {
    @Test
    public void testInstantiationPgsql() {
        DropSequence query = new DropSequence(PGSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    public void testClearPgsql() {
        DropSequence query = new DropSequence(PGSQL);
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
    public void testCreatePgsql() {
        DropSequence query = new DropSequence(PGSQL);
        query.name("sequencename");
        assertEquals(query.getSql(), "DROP SEQUENCE sequencename");
        execute(PGSQL, query);
    }

    @Test
    public void testClonePgsql() {
        DropSequence query = new DropSequence(PGSQL);
        query.name("sequencename");
        DropSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(PGSQL, query_clone);
    }
}
