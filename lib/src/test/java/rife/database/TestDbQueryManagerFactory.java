/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDbQueryManagerFactory {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetDefault(Datasource datasource) {
        TestDbQueryManagerImpl dbquerymanager1 = TestDbQueryManagerFactoryImpl.getInstance(datasource);
        TestDbQueryManagerImpl dbquerymanager2 = TestDbQueryManagerFactoryImpl.getInstance(datasource);

        assertSame(dbquerymanager1.getDatasource(), datasource);
        assertSame(dbquerymanager2.getDatasource(), datasource);

        assertSame(dbquerymanager1, dbquerymanager2);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetIdentifier(Datasource datasource) {
        TestDbQueryManagerImpl dbquerymanager1 = TestDbQueryManagerFactoryImpl.getInstance(datasource, "id1");
        TestDbQueryManagerImpl dbquerymanager2 = TestDbQueryManagerFactoryImpl.getInstance(datasource, "id1");
        TestDbQueryManagerImpl dbquerymanager3 = TestDbQueryManagerFactoryImpl.getInstance(datasource, "id2");
        TestDbQueryManagerImpl dbquerymanager4 = TestDbQueryManagerFactoryImpl.getInstance(datasource, "id3");

        assertSame(dbquerymanager1.getDatasource(), datasource);
        assertSame(dbquerymanager2.getDatasource(), datasource);
        assertSame(dbquerymanager3.getDatasource(), datasource);
        assertSame(dbquerymanager4.getDatasource(), datasource);

        assertSame(dbquerymanager1, dbquerymanager2);
        assertNotSame(dbquerymanager1, dbquerymanager3);
        assertNotSame(dbquerymanager1, dbquerymanager4);
        assertNotSame(dbquerymanager2, dbquerymanager3);
        assertNotSame(dbquerymanager2, dbquerymanager4);
        assertNotSame(dbquerymanager3, dbquerymanager4);

        dbquerymanager1.setSetting("setting1");
        dbquerymanager3.setSetting("setting2");
        dbquerymanager4.setSetting("setting3");

        assertEquals(dbquerymanager1.getSetting(), dbquerymanager2.getSetting());
        assertFalse(dbquerymanager1.getSetting().equals(dbquerymanager3.getSetting()));
        assertFalse(dbquerymanager1.getSetting().equals(dbquerymanager4.getSetting()));
        assertFalse(dbquerymanager2.getSetting().equals(dbquerymanager3.getSetting()));
        assertFalse(dbquerymanager2.getSetting().equals(dbquerymanager4.getSetting()));
        assertFalse(dbquerymanager3.getSetting().equals(dbquerymanager4.getSetting()));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testQuery(Datasource datasource) {
        TestDbQueryManagerImpl dbquerymanager = TestDbQueryManagerFactoryImpl.getInstance(datasource);
        try {
            dbquerymanager.install();
            assertEquals(0, dbquerymanager.count());
            dbquerymanager.store(1, "one");
            assertEquals(1, dbquerymanager.count());
            dbquerymanager.store(2, "two");
            assertEquals(2, dbquerymanager.count());
            dbquerymanager.store(3, "three");
            assertEquals(3, dbquerymanager.count());
            dbquerymanager.store(4, "four");
            assertEquals(4, dbquerymanager.count());
            try {
                dbquerymanager.store(4, "fourb");
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
            assertEquals(4, dbquerymanager.count());
            dbquerymanager.remove();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
