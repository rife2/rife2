/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.*;
import rife.template.TemplateFactory;
import rife.tools.*;
import rife.tools.exceptions.ControlFlowRuntimeException;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static rife.database.TestDatasources.PGSQL;

public class TestDbQueryManager {
    public void tearDown(Datasource datasource) {
        try {
            var connection = datasource.getConnection();

            // drop the test table
            var statement = connection.createStatement();
            try {
                try {
                    statement.executeUpdate(new DropTable(datasource).table("tbltest"));
                } catch (DatabaseException e) { /* don't do anything */ }
            } finally {
                try {
                    statement.close();
                } catch (DatabaseException e) { /* don't do anything */ }
            }

            connection.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalInstantiation(Datasource datasource) {
        try {
            new DbQueryManager(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        try {
            var manager = new DbQueryManager(datasource);
            assertNotNull(manager);
            assertSame(manager.getDatasource(), datasource);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteUpdateSql(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUpdate((String) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @DatasourceEnabledIf(TestDatasourceIdentifier.PGSQL)
    void testTransactionUserCommit() {
        final var datasource = PGSQL;
        final var manager = new DbQueryManager(datasource);
        var create = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
        manager.executeUpdate(create);
        try {
            final var insert = "INSERT INTO tbltest VALUES (232, 'somestring')";
            final var select = new Select(datasource).from("tbltest").field("count(*)");

            final int[] other_first_int = {-1};
            if (manager.getConnection().supportsTransactions() &&
                // oracle doesn't support repeatable read isolation
                !datasource.getAliasedDriver().equals("oracle.jdbc.driver.OracleDriver") &&
                // mysql doesn't actually support repeatable read isolation
                !datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver") &&
                // these databases lock on the entire table, preventing this multithreaded test to work
                !datasource.getAliasedDriver().equals("org.hsqldb.jdbcDriver") &&
                !datasource.getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) {
                // ensure that the transaction isn't committed yet
                // since this should only happen after the last transaction user
                final var updated_monitor = new Object();
                final var isolation_monitor = new Object();
                var other_thread = new Thread(() -> {
                    synchronized (updated_monitor) {
                        try {
                            updated_monitor.wait(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    other_first_int[0] = manager.executeGetFirstInt(select);

                    synchronized (isolation_monitor) {
                        isolation_monitor.notifyAll();
                    }
                });

                other_thread.start();

                manager.inTransaction(new DbTransactionUserWithoutResult<>() {
                    public void useTransactionWithoutResult()
                    throws InnerClassException {
                        manager.executeUpdate(insert);
                        assertEquals(1, manager.executeGetFirstInt(select));

                        manager.inTransaction(() -> {
                            manager.inTransaction(() -> {
                                manager.executeUpdate(insert);
                                assertEquals(2, manager.executeGetFirstInt(select));
                            });

                            manager.executeUpdate(insert);
                            assertEquals(3, manager.executeGetFirstInt(select));
                        });

                        assertEquals(3, manager.executeGetFirstInt(select));

                        synchronized (updated_monitor) {
                            updated_monitor.notifyAll();
                        }

                        synchronized (isolation_monitor) {
                            if (other_thread.isAlive()) {
                                try {
                                    isolation_monitor.wait(5000);
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }
                    }

                    public int getTransactionIsolation() {
                        return Connection.TRANSACTION_REPEATABLE_READ;
                    }
                });

                try {
                    other_thread.join(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                assertEquals(0, other_first_int[0]);

                assertEquals(3, manager.executeGetFirstInt(select));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTransactionUserRecommendedRollback(Datasource datasource) {
        final var manager = new DbQueryManager(datasource);
        var create = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
        manager.executeUpdate(create);
        try {
            final var insert = "INSERT INTO tbltest VALUES (232, 'somestring')";
            final var select = new Select(datasource).from("tbltest").field("count(*)");

            if (manager.getConnection().supportsTransactions()) {
                manager.inTransaction(() -> {
                    manager.executeUpdate(insert);
                    assertEquals(1, manager.executeGetFirstInt(select));

                    manager.inTransaction(() -> {
                        manager.inTransaction(new DbTransactionUserWithoutResult<>() {
                            public void useTransactionWithoutResult()
                            throws InnerClassException {
                                manager.executeUpdate(insert);
                                rollback();
                            }
                        });

                        manager.executeUpdate(insert);
                        fail();
                    });

                    fail();
                });
                assertEquals(0, manager.executeGetFirstInt(select));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTransactionUserRuntimeException(Datasource datasource) {
        final var manager = new DbQueryManager(datasource);
        var create = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
        manager.executeUpdate(create);
        try {
            final var insert = "INSERT INTO tbltest VALUES (232, 'somestring')";
            final var select = new Select(datasource).from("tbltest").field("count(*)");

            if (manager.getConnection().supportsTransactions()) {
                try {
                    manager.inTransaction(() -> {
                        manager.executeUpdate(insert);
                        assertEquals(1, manager.executeGetFirstInt(select));

                        manager.inTransaction(() -> {
                            manager.inTransaction(() -> {
                                manager.executeUpdate(insert);
                                throw new RuntimeException("something happened");
                            });

                            manager.executeUpdate(insert);
                            fail();
                        });

                        fail();
                    });

                    fail();
                } catch (RuntimeException e) {
                    assertEquals("something happened", e.getMessage());
                }

                assertEquals(0, manager.executeGetFirstInt(select));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTransactionUserRegularRollback(Datasource datasource) {
        final var manager = new DbQueryManager(datasource);
        var create = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
        manager.executeUpdate(create);
        try {
            final var insert = "INSERT INTO tbltest VALUES (232, 'somestring')";
            final var select = new Select(datasource).from("tbltest").field("count(*)");

            if (manager.getConnection().supportsTransactions()) {
                manager.inTransaction(() -> {
                    manager.executeUpdate(insert);
                    assertEquals(1, manager.executeGetFirstInt(select));

                    manager.getConnection().rollback();
                });
                assertEquals(0, manager.executeGetFirstInt(select));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testTransactionUserCommittingException(Datasource datasource) {
        final var manager = new DbQueryManager(datasource);
        var create = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
        manager.executeUpdate(create);
        try {
            final var insert = "INSERT INTO tbltest VALUES (232, 'somestring')";
            final var select = new Select(datasource).from("tbltest").field("count(*)");

            if (manager.getConnection().supportsTransactions()) {
                try {
                    manager.inTransaction(() -> {
                        manager.executeUpdate(insert);
                        assertEquals(1, manager.executeGetFirstInt(select));

                        manager.inTransaction(() -> {
                            manager.inTransaction(() -> {
                                manager.executeUpdate(insert);
                                throw new TestCommittingRuntimeException("something happened");
                            });

                            manager.executeUpdate(insert);
                            fail();
                        });

                        fail();
                    });

                    fail();
                } catch (RuntimeException e) {
                    assertEquals("something happened", e.getMessage());
                }

                assertEquals(2, manager.executeGetFirstInt(select));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    public static class TestCommittingRuntimeException extends RuntimeException implements ControlFlowRuntimeException {
        public TestCommittingRuntimeException(String message) {
            super(message);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateSqlSuccess(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("column1", String.class, 50);
            manager.executeUpdate(create_query.getSql());

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query.getSql()));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteUpdateBuilder(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUpdate((Query) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateBuilderSuccess(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateBuilderError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column2", "sometext");
            try {
                manager.executeUpdate(insert_query);
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteUpdateHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUpdate(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .fieldParameter("name");
            assertEquals(1, manager.executeUpdate(insert_query, s -> s.setString("name", "me")));

            assertEquals("me", manager.executeGetFirstString(new Select(datasource).from("tbltest")));

            manager.executeUpdate(new Delete(datasource).from("tbltest"));

            insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("name", "me2");
            assertEquals(1, manager.executeUpdate(insert_query, null));

            assertEquals("me2", manager.executeGetFirstString(new Select(datasource).from("tbltest")));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateSimpleHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .fieldParameter("name");
            assertEquals(1, manager.executeUpdate(insert_query, statement -> statement.setString("name", "me")));

            assertEquals("me", manager.executeGetFirstString(new Select(datasource).from("tbltest")));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteHasResultRows(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeHasResultRows(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteReadQueryString(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            manager.executeUpdate(new Insert(datasource).into("tbltest").field("name", "me"));

            assertTrue(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = 'me'")));

            assertTrue(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = ?"), s -> s.setString(1, "me")));
            assertTrue(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = ?"), s -> s.setString(1, "me")));

            manager.executeUpdate(new Delete(datasource).from("tbltest"));

            assertFalse(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = 'me'")));

            assertFalse(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = ?"), s -> s.setString(1, "me")));
            assertFalse(manager.executeHasResultRows(new ReadQueryString("SELECT name FROM tbltest WHERE name = ?"), s -> s.setString(1, "me")));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteReadQueryTemplate(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            manager.executeUpdate(new Insert(datasource).into("tbltest").field("name", "me"));

            var template1 = TemplateFactory.SQL.get("readquery_blocks");
            template1.setValue("name", template1.getEncoder().encode("me"));
            assertTrue(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query1")));

            var template2 = TemplateFactory.SQL.get("readquery_content");
            template2.setValue("name", template2.getEncoder().encode("me"));
            assertTrue(manager.executeHasResultRows(new ReadQueryTemplate(template2)));

            assertTrue(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query2"), s -> s.setString(1, "me")));
            assertTrue(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query2"), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(1, "me");
                }
            }));

            manager.executeUpdate(new Delete(datasource).from("tbltest"));

            assertFalse(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query1")));

            assertFalse(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query2"), s -> s.setString(1, "me")));
            assertFalse(manager.executeHasResultRows(new ReadQueryTemplate(template1, "query2"), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString(1, "me");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteHasResultRows(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            manager.executeUpdate(new Insert(datasource).into("tbltest").field("name", "me"));

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            assertTrue(manager.executeHasResultRows(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .whereParameter("name", "=");
            assertTrue(manager.executeHasResultRows(select_query, s -> s.setString("name", "me")));
            assertTrue(manager.executeHasResultRows(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("name", "me");
                }
            }));

            manager.executeUpdate(new Delete(datasource).from("tbltest"));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            assertFalse(manager.executeHasResultRows(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .whereParameter("name", "=");
            assertFalse(manager.executeHasResultRows(select_query, s -> s.setString("name", "me")));
            assertFalse(manager.executeHasResultRows(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("name", "me");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteHasResultRowsConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeHasResultRows(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstString(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstString(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("firstcol", String.class, 50).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeGetFirstString(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "John", "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "Piet", "lastcol", "Smith"}));

            assertEquals("John", manager.executeGetFirstString(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .whereParameter("lastcol", "=");
            assertEquals("Piet", manager.executeGetFirstString(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals("Piet", manager.executeGetFirstString(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstStringConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstString(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstBoolean(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstBoolean(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBoolean(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", boolean.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertFalse(manager.executeGetFirstBoolean(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", true, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", false, "lastcol", "Smith"}));

            assertTrue(manager.executeGetFirstBoolean(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertFalse(manager.executeGetFirstBoolean(select_query, s -> s.setString("lastcol", "Smith")));
            assertFalse(manager.executeGetFirstBoolean(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBooleanConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstBoolean(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getBoolean("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstByte(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstByte(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstByte(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", byte.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1, manager.executeGetFirstByte(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", (byte) 12, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", (byte) 23, "lastcol", "Smith"}));

            assertEquals(12, manager.executeGetFirstByte(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(23, manager.executeGetFirstByte(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals(23, manager.executeGetFirstByte(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstByteConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstByte(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getByte("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstShort(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstShort(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstShort(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", short.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1, manager.executeGetFirstShort(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", (short) 98, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", (short) 243, "lastcol", "Smith"}));

            assertEquals(98, manager.executeGetFirstShort(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(243, manager.executeGetFirstShort(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals(243, manager.executeGetFirstShort(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstShortConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstShort(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getShort("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstInt(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstInt(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstInt(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", int.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1, manager.executeGetFirstInt(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 827, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 154, "lastcol", "Smith"}));

            assertEquals(827, manager.executeGetFirstInt(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(154, manager.executeGetFirstInt(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals(154, manager.executeGetFirstInt(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstIntConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstInt(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getInt("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstLong(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstLong(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstLong(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", long.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1, manager.executeGetFirstLong(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 92873, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 14873, "lastcol", "Smith"}));

            assertEquals(92873, manager.executeGetFirstLong(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(14873, manager.executeGetFirstLong(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals(14873, manager.executeGetFirstLong(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstLongConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstLong(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getLong("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstFloat(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstFloat(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstFloat(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", float.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1.0f, manager.executeGetFirstFloat(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 12.4, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 23.5, "lastcol", "Smith"}));

            assertEquals(12.4f, manager.executeGetFirstFloat(select_query));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(23.5f, manager.executeGetFirstFloat(select_query, s -> s.setString("lastcol", "Smith")));
            assertEquals(23.5f, manager.executeGetFirstFloat(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstFloatConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstFloat(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getFloat("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstDouble(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstDouble(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstDouble(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", double.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertEquals(-1.0d, manager.executeGetFirstDouble(select_query));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 287.52, "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", 1984.328, "lastcol", "Smith"}));

            assertEquals(287.52d, manager.executeGetFirstDouble(select_query), 0.001);

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(1984.328d, manager.executeGetFirstDouble(select_query, s -> s.setString("lastcol", "Smith")), 0.001);
            assertEquals(1984.328d, manager.executeGetFirstDouble(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }), 0.001);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstDoubleConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstDouble(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getDouble("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstBytes(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstBytes(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBytes(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            if (datasource.getAliasedDriver().equals("oracle.jdbc.driver.OracleDriver")) {
                create_query.table("tbltest").column("datacol", String.class).column("lastcol", String.class, 50);
            } else {
                create_query.table("tbltest").column("datacol", Blob.class).column("lastcol", String.class, 50);
            }
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeGetFirstBytes(select_query));

            var insert = new Insert(datasource).into("tbltest").fieldParameter("datacol").fieldParameter("lastcol");
            manager.executeUpdate(insert, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    if (datasource.getAliasedDriver().equals("oracle.jdbc.driver.OracleDriver")) {
                        statement.setString("datacol", "abc");
                    } else {
                        statement.setBytes("datacol", "abc".getBytes());
                    }
                    statement.setString("lastcol", "Doe");
                }
            });
            manager.executeUpdate(insert, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    if (datasource.getAliasedDriver().equals("oracle.jdbc.driver.OracleDriver")) {
                        statement.setString("datacol", "def");
                    } else {
                        statement.setBytes("datacol", "def".getBytes());
                    }
                    statement.setString("lastcol", "Smith");
                }
            });

            byte[] result = null;
            result = manager.executeGetFirstBytes(select_query);
            assertArrayEquals(new byte[]{97, 98, 99}, result);

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            result = manager.executeGetFirstBytes(select_query, s -> s.setString("lastcol", "Smith"));
            assertArrayEquals(new byte[]{100, 101, 102}, result);
            result = manager.executeGetFirstBytes(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            });
            assertArrayEquals(new byte[]{100, 101, 102}, result);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBytesConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstBytes(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        resultSet.getBytes("unknown");
                        return null;
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstDate(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstDate(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeGetFirstDate(null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstDate(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", java.sql.Date.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeGetFirstDate(select_query));
            assertNull(manager.executeGetFirstDate(select_query, Calendar.getInstance()));

            var cal1 = Calendar.getInstance();
            cal1.set(2003, Calendar.DECEMBER, 12, 0, 0, 0);
            cal1.set(Calendar.MILLISECOND, 0);
            var cal2 = Calendar.getInstance();
            cal2.set(2004, Calendar.MARCH, 7, 0, 0, 0);
            cal2.set(Calendar.MILLISECOND, 0);
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Date(cal1.getTimeInMillis()), "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Date(cal2.getTimeInMillis()), "lastcol", "Smith"}));

            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstDate(select_query).getTime());
            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstDate(select_query, Calendar.getInstance()).getTime());

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstDate(select_query, s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstDate(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstDate(select_query, Calendar.getInstance(), s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstDate(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstDateConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstDate(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getDate("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
            try {
                manager.executeGetFirstDate(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getDate("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstTime(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstTime(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeGetFirstTime(null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstTime(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", java.sql.Time.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeGetFirstTime(select_query));
            assertNull(manager.executeGetFirstTime(select_query, Calendar.getInstance()));

            var cal1 = Calendar.getInstance();
            cal1.set(1970, Calendar.JANUARY, 1, 12, 5, 12);
            cal1.set(Calendar.MILLISECOND, 0);
            var cal2 = Calendar.getInstance();
            cal2.set(1970, Calendar.JANUARY, 1, 23, 34, 27);
            cal2.set(Calendar.MILLISECOND, 0);
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Time(cal1.getTimeInMillis()), "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Time(cal2.getTimeInMillis()), "lastcol", "Smith"}));

            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstTime(select_query).getTime());
            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstTime(select_query, Calendar.getInstance()).getTime());

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTime(select_query, s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTime(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTime(select_query, Calendar.getInstance(), s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTime(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstTimeConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstTime(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getTime("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
            try {
                manager.executeGetFirstTime(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getTime("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstTimestamp(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeGetFirstTimestamp(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeGetFirstTimestamp(null, (Calendar) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstTimestamp(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("datacol", java.sql.Timestamp.class).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeGetFirstTimestamp(select_query));
            assertNull(manager.executeGetFirstTimestamp(select_query, Calendar.getInstance()));

            var cal1 = Calendar.getInstance();
            cal1.set(2003, Calendar.DECEMBER, 12, 8, 10, 8);
            cal1.set(Calendar.MILLISECOND, 0);
            var cal2 = Calendar.getInstance();
            cal2.set(2004, Calendar.MARCH, 7, 21, 34, 12);
            cal2.set(Calendar.MILLISECOND, 0);
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Timestamp(cal1.getTimeInMillis()), "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new Object[]{"datacol", new java.sql.Timestamp(cal2.getTimeInMillis()), "lastcol", "Smith"}));

            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query).getTime());
            assertEquals(cal1.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query, Calendar.getInstance()).getTime());

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol")
                .whereParameter("lastcol", "=");
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query, s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query, Calendar.getInstance(), s -> s.setString("lastcol", "Smith")).getTime());
            assertEquals(cal2.getTimeInMillis(), manager.executeGetFirstTimestamp(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            }).getTime());
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstTimestampConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeGetFirstTimestamp(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getTimestamp("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
            try {
                manager.executeGetFirstTimestamp(select_query, Calendar.getInstance(), new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getTimestamp("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstAsciiStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUseFirstAsciiStream(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeUseFirstAsciiStream(new Select(datasource).from("tbltest"), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstAsciiStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("firstcol", String.class, 50).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .where("lastcol", "=", "Doe");

            manager.executeUseFirstAsciiStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "John", "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "Piet", "lastcol", "Smith"}));

            manager.executeUseFirstAsciiStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("John", FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            });

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .whereParameter("lastcol", "=");
            manager.executeUseFirstAsciiStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("Piet", FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, s -> s.setString("lastcol", "Smith"));
            manager.executeUseFirstAsciiStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("Piet", FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            });
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstAsciiStreamConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeUseFirstAsciiStream(select_query, new InputStreamUser<>() {
                    public Object useInputStream(InputStream stream)
                    throws InnerClassException {
                        assertNotNull(stream);

                        return null;
                    }
                }, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstCharacterStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUseFirstCharacterStream(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeUseFirstCharacterStream(new Select(datasource).from("tbltest"), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstCharacterStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("firstcol", String.class, 50).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .where("lastcol", "=", "Doe");

            manager.executeUseFirstCharacterStream(select_query, new ReaderUser<>() {
                public Object useReader(Reader reader)
                throws InnerClassException {
                    assertNull(reader);

                    return null;
                }
            });

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "John", "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "Piet", "lastcol", "Smith"}));

            manager.executeUseFirstCharacterStream(select_query, new ReaderUser<>() {
                public Object useReader(Reader reader)
                throws InnerClassException {
                    assertNotNull(reader);

                    try {
                        assertEquals("John", FileUtils.readString(reader));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            });

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .whereParameter("lastcol", "=");
            manager.executeUseFirstCharacterStream(select_query, new ReaderUser<>() {
                public Object useReader(Reader reader)
                throws InnerClassException {
                    assertNotNull(reader);

                    try {
                        assertEquals("Piet", FileUtils.readString(reader));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, s -> s.setString("lastcol", "Smith"));
            manager.executeUseFirstCharacterStream(select_query, new ReaderUser<>() {
                public Object useReader(Reader reader)
                throws InnerClassException {
                    assertNotNull(reader);

                    try {
                        assertEquals("Piet", FileUtils.readString(reader));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            });
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstCharacterStreamConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeUseFirstCharacterStream(select_query, new ReaderUser<>() {
                    public Object useReader(Reader reader)
                    throws InnerClassException {
                        assertNotNull(reader);

                        return null;
                    }
                }, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteGetFirstBinaryStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeUseFirstBinaryStream(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            manager.executeUseFirstBinaryStream(new Select(datasource).from("tbltest"), null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBinaryStream(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            if (datasource.getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver") ||
                datasource.getAliasedDriver().equals("org.hsqldb.jdbcDriver")) {
                create_query.table("tbltest").column("firstcol", Blob.class).column("lastcol", String.class, 50);
            } else {
                create_query.table("tbltest").column("firstcol", String.class).column("lastcol", String.class, 50);
            }
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .where("lastcol", "=", "Doe");

            manager.executeUseFirstBinaryStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNull(stream);

                    return null;
                }
            });

            manager.executeUpdate(new Insert(datasource)
                .into("tbltest")
                .fieldParameter("firstcol")
                .field("lastcol", "Doe"), new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    if (datasource.getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver") ||
                        datasource.getAliasedDriver().equals("org.h2.Driver") ||
                        datasource.getAliasedDriver().equals("org.hsqldb.jdbcDriver")) {
                        statement.setBytes("firstcol", "John".getBytes(StandardCharsets.UTF_8));
                    } else {
                        statement.setString("firstcol", "John");
                    }
                }
            });
            manager.executeUpdate(new Insert(datasource)
                .into("tbltest")
                .fieldParameter("firstcol")
                .field("lastcol", "Smith"), statement -> {
                if (datasource.getAliasedDriver().equals("org.apache.derby.jdbc.EmbeddedDriver") ||
                    datasource.getAliasedDriver().equals("org.h2.Driver") ||
                    datasource.getAliasedDriver().equals("org.hsqldb.jdbcDriver")) {
                    statement.setBytes("firstcol", "Piet".getBytes(StandardCharsets.UTF_8));
                } else {
                    statement.setString("firstcol", "Piet");
                }
            });

            manager.executeUseFirstBinaryStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("John", FileUtils.readString(stream, "UTF-8"));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            });

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("firstcol")
                .whereParameter("lastcol", "=");
            manager.executeUseFirstBinaryStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("Piet", FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, s -> s.setString("lastcol", "Smith"));
            manager.executeUseFirstBinaryStream(select_query, new InputStreamUser<>() {
                public Object useInputStream(InputStream stream)
                throws InnerClassException {
                    assertNotNull(stream);
                    try {
                        assertEquals("Piet", FileUtils.readString(stream));
                    } catch (FileUtilsErrorException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }

                    return null;
                }
            }, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }
            });
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteGetFirstBinaryStreamConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("name")
                .where("name", "=", "me");
            try {
                manager.executeUseFirstBinaryStream(select_query, new InputStreamUser<>() {
                    public Object useInputStream(InputStream stream)
                    throws InnerClassException {
                        assertNotNull(stream);

                        return null;
                    }
                }, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteFetchFirst(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeFetchFirst(null, (DbRowProcessor) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchFirst(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("datacol", String.class, 50)
                .column("valuecol", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("datacol", "sometext")
                .field("valuecol", "thevalue");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol");

            DbRowProcessorSuccess processor = null;

            int[] count1 = {0};
            assertTrue(manager.executeFetchFirst(select_query, p -> count1[0]++));
            assertEquals(count1[0], 1);

            processor = new DbRowProcessorSuccess();
            assertTrue(manager.executeFetchFirst(select_query, processor));
            assertEquals(processor.getCounter(), 1);
            assertTrue(manager.executeFetchFirst(select_query, processor));
            assertEquals(processor.getCounter(), 2);

            select_query
                .whereParameter("valuecol", "=");

            int[] count2 = {0};
            assertTrue(manager.executeFetchFirst(select_query,
                p -> count2[0]++,
                s -> s.setString("valuecol", "thevalue")));
            assertEquals(count2[0], 1);

            processor = new DbRowProcessorSuccess();
            assertTrue(manager.executeFetchFirst(select_query, processor, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("valuecol", "thevalue");
                }
            }));
            assertEquals(processor.getCounter(), 1);

            processor = new DbRowProcessorSuccess();
            assertFalse(manager.executeFetchFirst(select_query, processor, s -> s.setString("valuecol", "not present")));
            assertEquals(processor.getCounter(), 0);

            processor = new DbRowProcessorSuccess();
            assertFalse(manager.executeFetchFirst(select_query, processor, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("valuecol", "not present");
                }
            }));
            assertEquals(processor.getCounter(), 0);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchFirstConcludeError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("datacol", String.class, 50)
                .column("valuecol", String.class, 50);
            manager.executeUpdate(create_query);

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol");

            try {
                manager.executeFetchFirst(select_query, (DbRowProcessor) null, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteFetchFirstBean(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeFetchFirstBean(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchFirstBean(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").columns(BeanImplConstrained.class);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest").fields(BeanImplConstrained.getPopulatedBean());
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest");

            BeanImplConstrained bean = null;

            bean = manager.executeFetchFirstBean(select_query, BeanImplConstrained.class);
            assertNotNull(bean);

            var bean_populated = BeanImplConstrained.getPopulatedBean();
            assertEquals(bean.getPropertyString(), bean_populated.getPropertyString());
            assertEquals(bean.getPropertyStringbuffer().toString(), bean_populated.getPropertyStringbuffer().toString());
            // don't compare milliseconds since each db stores it differently
            assertEquals((bean.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
            assertEquals(bean.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
            assertEquals(bean.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
            assertEquals(bean.isPropertyBoolean(), bean_populated.isPropertyBoolean());
            assertEquals(bean.getPropertyChar(), bean_populated.getPropertyChar());
            assertNotEquals(bean.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
            assertEquals(bean.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
            assertEquals(bean.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
            assertEquals(bean.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
            assertEquals(bean.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
            assertEquals(bean.getPropertyInt(), bean_populated.getPropertyInt());
            assertNotEquals(bean.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
            assertEquals(bean.getPropertyShort(), bean_populated.getPropertyShort());
            assertEquals(bean.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());

            select_query
                .whereParameter("propertyString", "=");

            bean = manager.executeFetchFirstBean(select_query, BeanImplConstrained.class, s -> s.setString("propertyString", "someotherstring"));
            assertNotNull(bean);
            assertEquals(bean.getPropertyString(), bean_populated.getPropertyString());
            assertEquals(bean.getPropertyStringbuffer().toString(), bean_populated.getPropertyStringbuffer().toString());
            assertEquals((bean.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
            assertEquals(bean.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
            assertEquals(bean.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
            assertEquals(bean.isPropertyBoolean(), bean_populated.isPropertyBoolean());
            assertEquals(bean.getPropertyChar(), bean_populated.getPropertyChar());
            assertNotEquals(bean.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
            assertEquals(bean.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
            assertEquals(bean.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
            assertEquals(bean.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
            assertEquals(bean.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
            assertEquals(bean.getPropertyInt(), bean_populated.getPropertyInt());
            assertNotEquals(bean.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
            assertEquals(bean.getPropertyShort(), bean_populated.getPropertyShort());
            assertEquals(bean.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());

            bean = manager.executeFetchFirstBean(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("propertyString", "someotherstring");
                }
            });
            assertNotNull(bean);
            assertEquals(bean.getPropertyString(), bean_populated.getPropertyString());
            assertEquals(bean.getPropertyStringbuffer().toString(), bean_populated.getPropertyStringbuffer().toString());
            assertEquals((bean.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
            assertEquals((bean.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
            assertEquals(bean.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
            assertEquals(bean.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
            assertEquals(bean.isPropertyBoolean(), bean_populated.isPropertyBoolean());
            assertEquals(bean.getPropertyChar(), bean_populated.getPropertyChar());
            assertNotEquals(bean.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
            assertEquals(bean.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
            assertEquals(bean.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
            assertEquals(bean.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
            assertEquals(bean.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
            assertEquals(bean.getPropertyInt(), bean_populated.getPropertyInt());
            assertNotEquals(bean.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
            assertEquals(bean.getPropertyShort(), bean_populated.getPropertyShort());
            assertEquals(bean.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());

            bean = manager.executeFetchFirstBean(select_query, BeanImplConstrained.class, s -> s.setString("propertyString", "not present"));
            assertNull(bean);

            bean = manager.executeFetchFirstBean(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("propertyString", "not present");
                }
            });
            assertNull(bean);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchFirstBeanConcludeError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").columns(BeanImplConstrained.class);
            manager.executeUpdate(create_query);

            var select_query = new Select(datasource);
            select_query.from("tbltest");

            try {
                manager.executeFetchFirstBean(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteFetchAll(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeFetchAll(null, (DbRowProcessor) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchAll(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("datacol", String.class, 50)
                .column("valuecol", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("datacol", "sometext1")
                .field("valuecol", "thevalue1");
            assertEquals(1, manager.executeUpdate(insert_query));
            insert_query.clear();
            insert_query.into("tbltest")
                .field("datacol", "sometext2")
                .field("valuecol", "thevalue2");
            assertEquals(1, manager.executeUpdate(insert_query));
            insert_query.clear();
            insert_query.into("tbltest")
                .field("datacol", "sometext2b")
                .field("valuecol", "thevalue2");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol");

            DbRowProcessorSuccess processor = null;

            processor = new DbRowProcessorSuccess();
            assertTrue(manager.executeFetchAll(select_query, processor));
            assertEquals(processor.getCounter(), 2); // limited to maximum 2 by the rowprocessor

            select_query
                .whereParameter("valuecol", "=");

            processor = new DbRowProcessorSuccess();
            assertTrue(manager.executeFetchAll(select_query, processor, s -> s.setString("valuecol", "thevalue2")));
            assertEquals(processor.getCounter(), 2);

            processor = new DbRowProcessorSuccess();
            assertTrue(manager.executeFetchAll(select_query, processor, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("valuecol", "thevalue2");
                }
            }));
            assertEquals(processor.getCounter(), 2);

            processor = new DbRowProcessorSuccess();
            assertFalse(manager.executeFetchAll(select_query, processor, s -> s.setString("valuecol", "not present")));
            assertEquals(processor.getCounter(), 0);

            processor = new DbRowProcessorSuccess();
            assertFalse(manager.executeFetchAll(select_query, processor, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("valuecol", "not present");
                }
            }));
            assertEquals(processor.getCounter(), 0);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchAllConcludeError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("datacol", String.class, 50)
                .column("valuecol", String.class, 50);
            manager.executeUpdate(create_query);

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("datacol");

            try {
                var processor = new DbRowProcessorSuccess();
                manager.executeFetchAll(select_query, processor, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteFetchAllBeans(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeFetchAllBeans(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchAllBeans(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").columns(BeanImplConstrained.class);
            manager.executeUpdate(create_query);

            BeanImplConstrained bean = null;
            var insert_query = new Insert(datasource);
            bean = BeanImplConstrained.getPopulatedBean();
            bean.setPropertyString("someotherstring");
            bean.setPropertyStringbuffer(new StringBuffer("someotherstringbuf1"));
            insert_query.into("tbltest").fields(bean);
            assertEquals(1, manager.executeUpdate(insert_query));
            insert_query.clear();
            bean = BeanImplConstrained.getPopulatedBean();
            bean.setPropertyString("one");
            bean.setPropertyStringbuffer(new StringBuffer("someotherstringbuf2"));
            insert_query.into("tbltest").fields(bean);
            assertEquals(1, manager.executeUpdate(insert_query));
            insert_query.clear();
            bean = BeanImplConstrained.getPopulatedBean();
            bean.setPropertyString("tw''o");
            bean.setPropertyStringbuffer(new StringBuffer("someotherstringbuf3"));
            insert_query.into("tbltest").fields(bean);
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest");

            var bean_populated = BeanImplConstrained.getPopulatedBean();
            List<BeanImplConstrained> beans = null;

            beans = manager.executeFetchAllBeans(select_query, BeanImplConstrained.class);
            assertNotNull(beans);
            assertEquals(beans.size(), 3);
            for (var bean2 : beans) {
                assertTrue(bean2.getPropertyString().equals("someotherstring") || bean2.getPropertyString().equals("one") || bean2.getPropertyString().equals("tw''o"));
                assertTrue(bean2.getPropertyStringbuffer().toString().equals("someotherstringbuf1") || bean2.getPropertyStringbuffer().toString().equals("someotherstringbuf2") || bean2.getPropertyStringbuffer().toString().equals("someotherstringbuf3"));
                // don't compare milliseconds since each db stores it differently
                assertEquals((bean2.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
                assertEquals((bean2.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
                assertEquals((bean2.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
                assertEquals(bean2.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
                assertEquals(bean2.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
                assertEquals(bean2.isPropertyBoolean(), bean_populated.isPropertyBoolean());
                assertEquals(bean2.getPropertyChar(), bean_populated.getPropertyChar());
                assertNotEquals(bean2.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
                assertEquals(bean2.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
                assertEquals(bean2.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
                assertEquals(bean2.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
                assertEquals(bean2.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
                assertEquals(bean2.getPropertyInt(), bean_populated.getPropertyInt());
                assertNotEquals(bean2.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
                assertEquals(bean2.getPropertyShort(), bean_populated.getPropertyShort());
                assertEquals(bean2.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());
            }

            select_query
                .whereParameter("propertyString", "=");

            beans = manager.executeFetchAllBeans(select_query, BeanImplConstrained.class, s -> s.setString("propertyString", "one"));
            assertNotNull(beans);
            assertEquals(beans.size(), 1);
            var bean2 = beans.get(0);
            assertEquals(bean2.getPropertyString(), "one");
            assertEquals(bean2.getPropertyStringbuffer().toString(), "someotherstringbuf2");
            // don't compare milliseconds since each db stores it differently
            assertEquals((bean2.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
            assertEquals((bean2.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
            assertEquals((bean2.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
            assertEquals(bean2.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
            assertEquals(bean2.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
            assertEquals(bean2.isPropertyBoolean(), bean_populated.isPropertyBoolean());
            assertEquals(bean2.getPropertyChar(), bean_populated.getPropertyChar());
            assertNotEquals(bean2.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
            assertEquals(bean2.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
            assertEquals(bean2.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
            assertEquals(bean2.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
            assertEquals(bean2.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
            assertEquals(bean2.getPropertyInt(), bean_populated.getPropertyInt());
            assertNotEquals(bean2.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
            assertEquals(bean2.getPropertyShort(), bean_populated.getPropertyShort());
            assertEquals(bean2.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());

            beans = manager.executeFetchAllBeans(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("propertyString", "one");
                }
            });
            assertNotNull(beans);
            assertEquals(beans.size(), 1);
            bean2 = beans.get(0);
            assertEquals(bean2.getPropertyString(), "one");
            assertEquals(bean2.getPropertyStringbuffer().toString(), "someotherstringbuf2");
            // don't compare milliseconds since each db stores it differently
            assertEquals((bean2.getPropertyDate().getTime() / 1000) * 1000, (bean_populated.getPropertyDate().getTime() / 1000) * 1000);
            assertEquals((bean2.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (bean_populated.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
            assertEquals((bean2.getPropertyTimestamp().getTime() / 1000) * 1000, (bean_populated.getPropertyTimestamp().getTime() / 1000) * 1000);
            assertEquals(bean2.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
            assertEquals(bean2.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
            assertEquals(bean2.isPropertyBoolean(), bean_populated.isPropertyBoolean());
            assertEquals(bean2.getPropertyChar(), bean_populated.getPropertyChar());
            assertNotEquals(bean2.getPropertyByte(), bean_populated.getPropertyByte()); // byte is not saved
            assertEquals(bean2.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.001);
            assertEquals(bean2.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.001);
            assertEquals(bean2.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
            assertEquals(bean2.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
            assertEquals(bean2.getPropertyInt(), bean_populated.getPropertyInt());
            assertNotEquals(bean2.getPropertyLong(), bean_populated.getPropertyLong()); // long is not persistent
            assertEquals(bean2.getPropertyShort(), bean_populated.getPropertyShort());
            assertEquals(bean2.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());

            beans = manager.executeFetchAllBeans(select_query, BeanImplConstrained.class, s -> s.setString("propertyString", "not present"));
            assertNotNull(beans);
            assertEquals(beans.size(), 0);

            beans = manager.executeFetchAllBeans(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("propertyString", "not present");
                }
            });
            assertNotNull(beans);
            assertEquals(beans.size(), 0);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteFetchAllBeansConcludeError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").columns(BeanImplConstrained.class);
            manager.executeUpdate(create_query);

            var select_query = new Select(datasource);
            select_query.from("tbltest");

            try {
                manager.executeFetchAllBeans(select_query, BeanImplConstrained.class, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteQueryDbPreparedStatementHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeQuery(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryDbPreparedStatementHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("firstcol", String.class, 50).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeQuery(select_query, null));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "John", "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "Piet", "lastcol", "Smith"}));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .whereParameter("lastcol", "=");
            assertEquals("Piet Smith", manager.executeQuery(select_query, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("lastcol", "Smith");
                }

                public Object concludeResults(DbResultSet resultSet)
                throws SQLException {
                    if (resultSet.next()) {
                        return resultSet.getString("firstcol") + " " + resultSet.getString("lastcol");
                    }

                    return null;
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryDbPreparedStatementHandlerConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest");
            try {
                manager.executeQuery(select_query, new DbPreparedStatementHandler<>() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteQueryDbResultSetHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeResultQuery(null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryDbResultSetHandler(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("firstcol", String.class, 50).column("lastcol", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest")
                .where("lastcol", "=", "Doe");

            assertNull(manager.executeResultQuery(select_query, null));

            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "John", "lastcol", "Doe"}));
            manager.executeUpdate(new Insert(datasource).into("tbltest").fields(new String[]{"firstcol", "Piet", "lastcol", "Smith"}));

            select_query = new Select(datasource);
            select_query.from("tbltest")
                .where("lastcol", "=", "Doe");
            assertEquals("John Doe", manager.executeResultQuery(select_query, new DbResultSetHandler() {
                public Object concludeResults(DbResultSet resultSet)
                throws SQLException {
                    if (resultSet.next()) {
                        return resultSet.getString("firstcol") + " " + resultSet.getString("lastcol");
                    }

                    return null;
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryDbResultSetHandlerConcludeErrors(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest").column("name", String.class, 50);
            manager.executeUpdate(create_query);

            Select select_query = null;
            select_query = new Select(datasource);
            select_query.from("tbltest");
            try {
                manager.executeResultQuery(select_query, new DbResultSetHandler() {
                    public Object concludeResults(DbResultSet resultSet)
                    throws SQLException {
                        return resultSet.getString("unknown");
                    }
                });
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testReserveConnection(Datasource datasource) {
        final var manager = new DbQueryManager(datasource);
        try {
            assertEquals("test", manager.reserveConnection(new DbConnectionUser<>() {
                public Object useConnection(final DbConnection connection) {
                    assertSame(manager.getConnection(), connection);
                    new Thread(() -> assertNotSame(manager.getConnection(), connection)).start();
                    return "test";
                }
            }));
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalExecuteQuerySql(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.executeQuery(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryBuilderSuccess(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column1");
            try (var statement = manager.executeQuery(select_query)) {
                assertNotNull(statement);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryBuilderError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column2");
            try {
                manager.executeQuery(select_query);
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalFetch(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.fetch(null, (DbRowProcessor) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFetchSuccess(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column1");

            var processor = new DbRowProcessorSuccess();
            DbResultSet resultset;
            try (var statement = manager.executeQuery(select_query)) {
                resultset = statement.getResultSet();
                assertTrue(manager.fetch(resultset, processor));
                assertEquals(processor.getCounter(), 1);
                assertFalse(manager.fetch(resultset, processor));
                assertEquals(processor.getCounter(), 1);
            }

            try (var statement = manager.executeQuery(select_query)) {
                resultset = statement.getResultSet();
                assertTrue(manager.fetch(resultset));
                assertFalse(manager.fetch(resultset));
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFetchError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column1");

            try (var statement1 = manager.executeQuery(select_query)) {
                manager.fetch(statement1.getResultSet(), new DbRowProcessorError());
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }

            try (var statement1 = manager.executeQuery(select_query)) {
                manager.fetch(statement1.getResultSet(), resultSet -> resultSet.getString("inexistant_column"));
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalFetchAll(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            manager.fetchAll(null, (DbRowProcessor) null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFetchAllSuccess(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext2");
            assertEquals(1, manager.executeUpdate(insert_query));

            insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext2");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column1");

            var processor = new DbRowProcessorSuccess();
            try (var statement = manager.executeQuery(select_query)) {
                assertTrue(manager.fetchAll(statement.getResultSet(), processor));
            }
            assertEquals(processor.getCounter(), 2);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testFetchAllError(Datasource datasource) {
        var manager = new DbQueryManager(datasource);
        try {
            var create_query = new CreateTable(datasource);
            create_query.table("tbltest")
                .column("column1", String.class, 50);
            manager.executeUpdate(create_query);

            var insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext");
            assertEquals(1, manager.executeUpdate(insert_query));

            insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext2");
            assertEquals(1, manager.executeUpdate(insert_query));

            insert_query = new Insert(datasource);
            insert_query.into("tbltest")
                .field("column1", "sometext2");
            assertEquals(1, manager.executeUpdate(insert_query));

            var select_query = new Select(datasource);
            select_query.from("tbltest")
                .field("column1");

            try {
                try (var statement = manager.executeQuery(select_query)) {
                    manager.fetchAll(statement.getResultSet(), resultSet -> resultSet.getString("inexistant_column"));
                }
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }

            try {
                try (var statement = manager.executeQuery(select_query)) {
                    manager.fetchAll(statement.getResultSet(), new DbRowProcessorError());
                }
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testClone(Datasource datasource) {
        var manager = new DbQueryManager(datasource);

        var create_query = new CreateTable(datasource);
        create_query.table("tbltest").column("column1", String.class, 50);
        manager.executeUpdate(create_query);

        var manager2 = (DbQueryManager) manager.clone();
        try (DbPreparedStatement statement = manager2.getConnection().getPreparedStatement(new Insert(datasource).into("tbltest").fieldParameter("column1"))) {
            assertNotNull(statement);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    static class DbRowProcessorSuccess extends DbRowProcessor {
        private int mCounter = 0;

        public DbRowProcessorSuccess() {
        }

        public boolean processRow(ResultSet resultSet)
        throws SQLException {
            if (2 == mCounter) {
                return false;
            }

            mCounter++;
            return true;
        }

        public int getCounter() {
            return mCounter;
        }
    }

    static class DbRowProcessorError extends DbRowProcessor {
        public DbRowProcessorError() {
        }

        public boolean processRow(ResultSet resultSet)
        throws SQLException {
            resultSet.getString("inexistant_column");
            return false;
        }
    }
}
