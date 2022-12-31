/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import org.junit.jupiter.api.Test;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCreateSequenceMysql extends TestCreateSequence {
    @Test
    void testInstantiationMysql() {
        CreateSequence query = new CreateSequence(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "CreateSequence");
        }
    }

    @Test
    void testClearMysql() {
        CreateSequence query = new CreateSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    void testCreateMysql() {
        CreateSequence query = new CreateSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @Test
    void testCloneMysql() {
        // sequences are not supported with mysql
    }
}
