/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import rife.database.exceptions.DatabaseException;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatasource {
    @Test
    void testInstantiation() {
        Datasource datasource1 = new Datasource();
        assertNotNull(datasource1);
        assertNull(datasource1.getDriver());
        assertNull(datasource1.getDataSource());
        assertNull(datasource1.getUrl());
        assertNull(datasource1.getUser());
        assertNull(datasource1.getPassword());
        assertEquals(datasource1.getPoolSize(), 0);
        assertFalse(datasource1.isPooled());

        String driver = "driver";
        String url = "url";
        String user = "user";
        String password = "password";
        int poolSize = 5;

        Datasource datasource2 = new Datasource(driver, url, user, password, poolSize);
        assertNotNull(datasource2);
        assertEquals(datasource2.getDriver(), driver);
        assertEquals(datasource2.getUrl(), url);
        assertEquals(datasource2.getUser(), user);
        assertEquals(datasource2.getPassword(), password);
        assertEquals(datasource2.getPoolSize(), poolSize);
        assertTrue(datasource2.isPooled());

        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();
        Datasource datasource3 = new Datasource(pgdatasource, driver, user, password, poolSize);
        assertNotNull(datasource3);
        assertEquals(datasource3.getDriver(), driver);
        assertSame(datasource3.getDataSource(), pgdatasource);
        assertEquals(datasource3.getUser(), user);
        assertEquals(datasource3.getPassword(), password);
        assertEquals(datasource3.getPoolSize(), poolSize);
        assertTrue(datasource3.isPooled());
    }

    @Test
    void testPopulation() {
        String driver = "driver";
        String url = "url";
        String user = "user";
        String password = "password";
        int poolSize = 5;
        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();

        Datasource datasource = new Datasource();
        datasource.setDriver(driver);
        datasource.setDataSource(pgdatasource);
        datasource.setUrl(url);
        datasource.setUser(user);
        datasource.setPassword(password);
        datasource.setPoolSize(poolSize);
        assertEquals(datasource.getDriver(), driver);
        assertSame(datasource.getDataSource(), pgdatasource);
        assertEquals(datasource.getUrl(), url);
        assertEquals(datasource.getUser(), user);
        assertEquals(datasource.getPassword(), password);
        assertEquals(datasource.getPoolSize(), poolSize);
        assertTrue(datasource.isPooled());
    }

    @Test
    void testDriverAlias() {
        String driver_aliased = "org.gjt.mm.mysql.Driver";
        String driver_unaliased = "com.mysql.cj.jdbc.Driver";

        Datasource datasource = new Datasource();

        datasource.setDriver(driver_aliased);
        assertEquals(datasource.getDriver(), driver_aliased);
        assertEquals(datasource.getAliasedDriver(), driver_unaliased);

        datasource.setDriver(driver_unaliased);
        assertEquals(datasource.getDriver(), driver_unaliased);
        assertEquals(datasource.getAliasedDriver(), driver_unaliased);
    }

    @Test
    void testEquality() {
        String driver = "driver";
        String url = "url";
        String user = "user";
        String password = "password";
        int poolSize = 5;

        Datasource datasource1 = new Datasource();
        datasource1.setDriver(driver);
        datasource1.setUrl(url);
        datasource1.setUser(user);
        datasource1.setPassword(password);
        datasource1.setPoolSize(poolSize);

        Datasource datasource2 = new Datasource(driver, url, user, password, poolSize);

        assertNotSame(datasource1, datasource2);

        assertEquals(datasource1.getDriver(), datasource2.getDriver());
        assertEquals(datasource1.getUrl(), datasource2.getUrl());
        assertEquals(datasource1.getUser(), datasource2.getUser());
        assertEquals(datasource1.getPassword(), datasource2.getPassword());
        assertEquals(datasource1.getPoolSize(), datasource2.getPoolSize());
        assertEquals(datasource1.isPooled(), datasource2.isPooled());
        assertEquals(datasource1, datasource2);

        datasource2.setDriver("otherdriver");
        assertTrue(!datasource1.equals(datasource2));
        datasource2.setDriver(driver);
        assertTrue(datasource1.equals(datasource2));

        datasource2.setUrl("otherurl");
        assertTrue(!datasource1.equals(datasource2));
        datasource2.setUrl(url);
        assertTrue(datasource1.equals(datasource2));

        datasource2.setUser("otheruser");
        assertTrue(!datasource1.equals(datasource2));
        datasource2.setUser(user);
        assertTrue(datasource1.equals(datasource2));

        datasource2.setPassword("otherpassword");
        assertTrue(!datasource1.equals(datasource2));
        datasource2.setPassword(password);
        assertTrue(datasource1.equals(datasource2));

        datasource2.setPoolSize(poolSize + 1);
        assertTrue(datasource1.equals(datasource2));

        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();

        Datasource datasource3 = new Datasource();
        datasource3.setDriver(driver);
        datasource3.setDataSource(pgdatasource);
        datasource3.setUser(user);
        datasource3.setPassword(password);
        datasource3.setPoolSize(poolSize);

        Datasource datasource4 = new Datasource(pgdatasource, driver, user, password, poolSize);

        assertNotSame(datasource3, datasource4);

        assertEquals(datasource3.getDriver(), datasource4.getDriver());
        assertEquals(datasource3.getUrl(), datasource4.getUrl());
        assertEquals(datasource3.getUser(), datasource4.getUser());
        assertEquals(datasource3.getPassword(), datasource4.getPassword());
        assertEquals(datasource3.getPoolSize(), datasource4.getPoolSize());
        assertEquals(datasource3.isPooled(), datasource4.isPooled());
        assertEquals(datasource3, datasource4);

        datasource4.setDriver("otherdriver");
        assertTrue(!datasource3.equals(datasource4));
        datasource4.setDriver(driver);
        assertTrue(datasource3.equals(datasource4));

        datasource4.setDataSource(new PGSimpleDataSource());
        assertTrue(!datasource3.equals(datasource4));
        datasource4.setDataSource(pgdatasource);
        assertTrue(datasource3.equals(datasource4));

        datasource4.setUser("otheruser");
        assertTrue(!datasource3.equals(datasource4));
        datasource4.setUser(user);
        assertTrue(datasource3.equals(datasource4));

        datasource4.setPassword("otherpassword");
        assertTrue(!datasource3.equals(datasource4));
        datasource4.setPassword(password);
        assertTrue(datasource3.equals(datasource4));

        datasource4.setPoolSize(poolSize + 1);
        assertTrue(datasource3.equals(datasource4));
    }

    @Test
    void testConnection() {
        Datasource datasource = TestDatasources.PGSQL;
        if (null == datasource) {
            System.out.println("WARNING : Datasource testConnection test not executed (postgresql missing)");
            return;
        }

        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        assertNotNull(connection);
        try {
            connection.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testConnectionDataSource1() {
        Datasource declared_datasource = TestDatasources.PGSQL;
        if (null == declared_datasource) {
            System.out.println("WARNING : Datasource testConnectionDataSource test not executed (postgresql missing)");
            return;
        }
        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();
        pgdatasource.setDatabaseName("unittests");
        pgdatasource.setServerName("localhost");
        pgdatasource.setPortNumber(5432);
        pgdatasource.setUser("unittests");
        pgdatasource.setPassword("password");
        Datasource datasource = new Datasource(pgdatasource, declared_datasource.getDriver(), "unittests", "password", 5);

        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        assertNotNull(connection);
        try {
            connection.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testConnectionDataSource2() {
        Datasource declared_datasource = TestDatasources.PGSQL;
        if (null == declared_datasource) {
            System.out.println("WARNING : Datasource testConnectionDataSource test not executed (postgresql missing)");
            return;
        }
        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();
        pgdatasource.setDatabaseName("unittests");
        pgdatasource.setServerName("localhost");
        pgdatasource.setPortNumber(5432);
        pgdatasource.setUser("unittests");
        pgdatasource.setPassword("password");
        Datasource datasource = new Datasource(pgdatasource, 5);

        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
        assertNotNull(connection);
        try {
            connection.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testConnectionPreservation() {
        Datasource datasource = TestDatasources.PGSQL;
        if (null == datasource) {
            System.out.println("WARNING : Datasource testConnectionPreservation test not executed (postgresql missing)");
            return;
        }

        try {
            DbConnection connection1 = null;
            DbConnection connection2 = null;

            connection1 = datasource.getConnection();
            connection2 = datasource.getConnection();
            assertTrue(connection1 != connection2);
            connection1.close();
            connection2.close();

            connection1 = datasource.getConnection();
            connection1.beginTransaction();
            connection2 = datasource.getConnection();
            assertTrue(connection1 == connection2);
            connection1.rollback();
            connection1.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testConnectionPreservationDatasource1() {
        Datasource declared_datasource = TestDatasources.PGSQL;
        if (null == declared_datasource) {
            System.out.println("WARNING : Datasource testConnectionPreservationDatasource test not executed (postgresql missing)");
            return;
        }
        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();
        pgdatasource.setDatabaseName("unittests");
        pgdatasource.setServerName("localhost");
        pgdatasource.setPortNumber(5432);
        pgdatasource.setUser("unittests");
        pgdatasource.setPassword("password");
        Datasource datasource = new Datasource(pgdatasource, declared_datasource.getDriver(), "unittests", "password", 5);

        try {
            DbConnection connection1 = null;
            DbConnection connection2 = null;

            connection1 = datasource.getConnection();
            connection2 = datasource.getConnection();
            assertTrue(connection1 != connection2);
            connection1.close();
            connection2.close();

            connection1 = datasource.getConnection();
            connection1.beginTransaction();
            connection2 = datasource.getConnection();
            assertTrue(connection1 == connection2);
            connection1.rollback();
            connection1.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testConnectionPreservationDatasource2() {
        Datasource declared_datasource = TestDatasources.PGSQL;
        if (null == declared_datasource) {
            System.out.println("WARNING : Datasource testConnectionPreservationDatasource test not executed (postgresql missing)");
            return;
        }
        PGSimpleDataSource pgdatasource = new PGSimpleDataSource();
        pgdatasource.setDatabaseName("unittests");
        pgdatasource.setServerName("localhost");
        pgdatasource.setPortNumber(5432);
        pgdatasource.setUser("unittests");
        pgdatasource.setPassword("password");
        Datasource datasource = new Datasource(pgdatasource, 5);

        try {
            DbConnection connection1 = null;
            DbConnection connection2 = null;

            connection1 = datasource.getConnection();
            connection2 = datasource.getConnection();
            assertTrue(connection1 != connection2);
            connection1.close();
            connection2.close();

            connection1 = datasource.getConnection();
            connection1.beginTransaction();
            connection2 = datasource.getConnection();
            assertTrue(connection1 == connection2);
            connection1.rollback();
            connection1.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
