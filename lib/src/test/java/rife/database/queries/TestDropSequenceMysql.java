/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceMysql extends TestDropSequence {
    @Test
    public void testInstantiationMysql() {
        DropSequence query = new DropSequence(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @Test
    public void testClearMysql() {
        DropSequence query = new DropSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateMysql() {
        DropSequence query = new DropSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCloneMysql() {
        // sequences are not supported on mysql
    }
}
