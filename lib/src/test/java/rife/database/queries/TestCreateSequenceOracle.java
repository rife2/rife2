/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateSequenceOracle extends TestCreateSequence {
    @Test
    void testInstantiationOracle() {
        System.out.println("TESTING");
        CreateSequence query = new CreateSequence(ORACLE);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    void testClearOracle() {
        CreateSequence query = new CreateSequence(ORACLE);
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
    void testCreateOracle() {
        CreateSequence query = new CreateSequence(ORACLE);
        query.name("sequencename");
        assertEquals(query.getSql(), "CREATE SEQUENCE sequencename");
        execute(ORACLE, query);
    }

    @Test
    void testCloneOracle() {
        CreateSequence query = new CreateSequence(ORACLE);
        query.name("sequencename");
        CreateSequence query_clone = query.clone();
        assertEquals(query.getSql(), query_clone.getSql());
        assertNotSame(query, query_clone);
        execute(ORACLE, query_clone);
    }
}
