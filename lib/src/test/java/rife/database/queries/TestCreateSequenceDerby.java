/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateSequenceDerby extends TestCreateSequence {
    @Test
    public void testInstantiationDerby() {
        CreateSequence query = new CreateSequence(mDerby);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    public void testClearDerby() {
        CreateSequence query = new CreateSequence(mDerby);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateDerby() {
        CreateSequence query = new CreateSequence(mDerby);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCloneDerby() {
        // sequences are not supported with mysql
    }
}
