/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateSequencePgsql extends TestCreateSequence {
    @Test
    public void testInstantiationPgsql() {
        CreateSequence query = new CreateSequence(mPgsql);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    public void testClearPgsql() {
        CreateSequence query = new CreateSequence(mPgsql);
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
    public void testCreatePgsql() {
        CreateSequence query = new CreateSequence(mPgsql);
        query.name("sequencename");
        assertEquals(query.getSql(), "CREATE SEQUENCE sequencename");
        execute(mPgsql, query);
    }

    @Test
    public void testClonePgsql() {
        CreateSequence query = new CreateSequence(mPgsql);
        query.name("sequencename");
        CreateSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertTrue(query != query_clone);
        execute(mPgsql, query_clone);
    }
}
