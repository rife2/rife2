/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceH2 extends TestDropSequence {
    @Test
    public void testInstantiationH2() {
        DropSequence query = new DropSequence(mH2);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    public void testClearH2() {
        DropSequence query = new DropSequence(mH2);
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
    public void testCreateH2() {
        DropSequence query = new DropSequence(mH2);
        query.name("sequencename");
        assertEquals(query.getSql(), "DROP SEQUENCE sequencename");
        execute(mH2, query);
    }

    @Test
    public void testCloneH2() {
        DropSequence query = new DropSequence(mH2);
        query.name("sequencename");
        DropSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(mH2, query_clone);
    }
}
