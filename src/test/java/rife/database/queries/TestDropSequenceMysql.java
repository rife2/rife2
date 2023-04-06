/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DatasourceEnabledIf;
import rife.database.TestDatasourceIdentifier;
import rife.database.exceptions.SequenceNameRequiredException;
import rife.database.exceptions.UnsupportedSqlFeatureException;

import static org.junit.jupiter.api.Assertions.*;

public class TestDropSequenceMysql extends TestDropSequence {
    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testInstantiationMysql() {
        var query = new DropSequence(MYSQL);
        assertNotNull(query);
        try {
            query.getSql();
            fail();
        } catch (SequenceNameRequiredException e) {
            assertEquals(e.getQueryName(), "DropSequence");
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testClearMysql() {
        var query = new DropSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCreateMysql() {
        var query = new DropSequence(MYSQL);
        query.name("sequencename");
        try {
            query.getSql();
            fail();
        } catch (UnsupportedSqlFeatureException e) {
            assertTrue(true);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.MYSQL)
    void testCloneMysql() {
        // sequences are not supported on mysql
    }
}
