/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDbConnection {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testConnection(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            assertFalse(connection.isClosed());
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

    // TODO : test fails
//    @ParameterizedTest
//    @ArgumentsSource(TestDatasources.class)
//    public void testDriverNameMapping(Datasource datasource)
//    throws Exception {
//        DbConnection connection = datasource.getConnection();
//        try {
//            String name = connection.getMetaData().getDriverName();
//            assertEquals(name + " : " + Datasource.sDriverNames.get(name) + " " + datasource.getAliasedDriver(), Datasource.sDriverNames.get(name), datasource.getAliasedDriver());
//        } finally {
//            if (null != connection) {
//                try {
//                    connection.close();
//                } catch (DatabaseException e) {
//                    fail(ExceptionUtils.getExceptionStackTrace(e));
//                }
//            }
//        }
//    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testClose(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            assertFalse(connection.isClosed());
            connection.close();
            if (datasource.isPooled()) {
                assertFalse(connection.isClosed());
            } else {
                assertTrue(connection.isClosed());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetMetaData(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            assertNotNull(connection.getMetaData());
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetStatement(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            DbStatement statement1 = connection.createStatement();
            assertNotNull(statement1);
            DbStatement statement2 = connection.createStatement();
            assertNotNull(statement2);
            assertNotSame(statement1, statement2);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetPreparedStatement(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            String sql = "CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))";
            DbPreparedStatement prepared_statement1 = connection.getPreparedStatement(sql);
            assertNotNull(prepared_statement1);
            DbPreparedStatement prepared_statement2 = connection.getPreparedStatement(sql);
            assertNotNull(prepared_statement2);
            assertNotSame(prepared_statement1, prepared_statement2);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetPreparedStatementQueryBuilder(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            CreateTable create = new CreateTable(datasource);
            create
                .table("tbltest")
                .column("id", int.class)
                .column("stringcol", String.class, 255);
            DbPreparedStatement prepared_statement1 = connection.getPreparedStatement(create);
            assertNotNull(prepared_statement1);
            DbPreparedStatement prepared_statement2 = connection.getPreparedStatement(create);
            assertNotNull(prepared_statement2);
            assertNotSame(prepared_statement1, prepared_statement2);
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testTransactionBeginCommitRollback(Datasource datasource) {
        DbConnection connection = datasource.getConnection();

        DbPreparedStatement prepared_statement_create = null;
        DbPreparedStatement prepared_statement_drop = null;
        prepared_statement_create = connection.getPreparedStatement("CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))");
        prepared_statement_create.executeUpdate();

        prepared_statement_drop = connection.getPreparedStatement("DROP TABLE tbltest");

        DbPreparedStatement prepared_statement_insert = null;
        DbPreparedStatement prepared_statement_select = null;
        try {
            prepared_statement_insert = connection.getPreparedStatement("INSERT INTO tbltest VALUES (232, 'somestring')");
            prepared_statement_select = connection.getPreparedStatement("SELECT * FROM tbltest");

            if (connection.supportsTransactions() &&
                !datasource.getAliasedDriver().equals("com.mysql.jdbc.Driver")) {
                assertTrue(connection.beginTransaction());
                assertEquals(1, prepared_statement_insert.executeUpdate());
                prepared_statement_select.executeQuery();
                assertTrue(prepared_statement_select.getResultSet().hasResultRows());
                assertTrue(connection.rollback());
                assertFalse(connection.commit());

                prepared_statement_select.executeQuery();
                assertFalse(prepared_statement_select.getResultSet().hasResultRows());

                assertTrue(connection.beginTransaction());
                assertEquals(1, prepared_statement_insert.executeUpdate());
                prepared_statement_select.executeQuery();
                assertTrue(prepared_statement_select.getResultSet().hasResultRows());
                assertTrue(connection.commit());
                assertFalse(connection.rollback());

                prepared_statement_select.executeQuery();
                assertTrue(prepared_statement_select.getResultSet().hasResultRows());
            } else {
                // FIXME: write tests with non transactional database
            }
        } catch (Exception e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            prepared_statement_insert.close();
            prepared_statement_select.close();

            assertTrue(connection.beginTransaction());
            prepared_statement_drop.executeUpdate();
            assertTrue(connection.commit());
            assertFalse(connection.rollback());

            try {
                prepared_statement_select = connection.getPreparedStatement("SELECT * FROM tbltest");
                prepared_statement_select.executeQuery();
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }

            connection.close();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testTransactionThreadValidity(Datasource datasource) {
        DbConnection connection = null;
        try {
            connection = datasource.getConnection();
            if (connection.supportsTransactions() &&
                !datasource.getAliasedDriver().equals("com.mysql.jdbc.Driver")) {
                assertFalse(connection.isTransactionValidForThread());
                assertTrue(connection.beginTransaction());
                assertTrue(connection.isTransactionValidForThread());
                ThreadImpl other_thread = new ThreadImpl(connection);
                other_thread.start();
                while (other_thread.isAlive()) {
                    synchronized (other_thread) {
                        try {
                            other_thread.wait(3600);
                        } catch (InterruptedException e) {
                            other_thread.interrupt();
                            other_thread.stop();
                            throw new RuntimeException("testTransactionThreadValidity failed for " + datasource.getAliasedDriver() + ", timeout", e);
                        }
                    }
                }
                assertTrue(connection.isTransactionValidForThread());
                assertTrue(connection.rollback());
                assertFalse(connection.commit());
                assertFalse(connection.isTransactionValidForThread());
            } else {
                // FIXME: write tests with non transactional database
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    fail(ExceptionUtils.getExceptionStackTrace(e));
                }
            }
        }
    }

//    public void testTransactionTimeoutBegin(Datasource datasource) {
//        DbConnection connection = null;
//        DbPreparedStatement prepared_statement_create = null;
//        DbPreparedStatement prepared_statement_insert = null;
//        DbPreparedStatement prepared_statement_select = null;
//        DbPreparedStatement prepared_statement_drop = null;
//        try {
//            connection = datasource.getConnection();
//            prepared_statement_create = connection.getPreparedStatement("CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))");
//            prepared_statement_drop = connection.getPreparedStatement("DROP TABLE tbltest");
//            prepared_statement_create.executeUpdate();
//
//            prepared_statement_insert = connection.getPreparedStatement("INSERT INTO tbltest VALUES (232, 'somestring')");
//            prepared_statement_select = connection.getPreparedStatement("SELECT * FROM tbltest");
//
//            if (connection.supportsTransactions() &&
//                !datasource.getAliasedDriver().equals("com.mysql.jdbc.Driver")) {
//                assertTrue(connection.beginTransaction());
//                prepared_statement_insert.executeUpdate();
//                try {
//                    Thread.sleep(RifeConfig.database().getTransactionTimeout() * 1000L + 100);
//                } catch (InterruptedException e) {
//                    fail(ExceptionUtils.getExceptionStackTrace(e));
//                }
//
//                try {
//                    connection.beginTransaction();
//                    fail();
//                } catch (TransactionTimedOutException e) {
//                    assertTrue(true);
//                }
//
//                assertFalse(connection.commit());
//                assertFalse(connection.rollback());
//                try {
//                    prepared_statement_select.executeQuery();
//                    assertFalse(prepared_statement_select.getResultSet().hasResultRows());
//                } catch (DatabaseException e) {
//                    fail(ExceptionUtils.getExceptionStackTrace(e));
//                }
//            } else {
//                // FIXME: write tests with non transactional database
//            }
//        } catch (DatabaseException e) {
//            fail(ExceptionUtils.getExceptionStackTrace(e));
//        } finally {
//            if (null != connection) {
//                try {
//                    prepared_statement_drop.executeUpdate();
//                    connection.close();
//                } catch (DatabaseException e) {
//                    fail(ExceptionUtils.getExceptionStackTrace(e));
//                }
//            }
//        }
//    }
//
//	public void testTransactionTimeoutCommit()
//	{
//		DbConnection connection = null;
//		DbPreparedStatement prepared_statement_create = null;
//		DbPreparedStatement prepared_statement_insert = null;
//		DbPreparedStatement prepared_statement_select = null;
//		DbPreparedStatement prepared_statement_drop = null;
//		try
//		{
//			connection = datasource.getConnection();
//			prepared_statement_create = connection.getPreparedStatement("CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))");
//			prepared_statement_drop = connection.getPreparedStatement("DROP TABLE tbltest");
//			prepared_statement_create.executeUpdate();
//
//			prepared_statement_insert = connection.getPreparedStatement("INSERT INTO tbltest VALUES (232, 'somestring')");
//			prepared_statement_select = connection.getPreparedStatement("SELECT * FROM tbltest");
//
//			if (connection.supportsTransactions() &&
//				false == datasource.getAliasedDriver().equals("com.mysql.jdbc.Driver"))
//			{
//				assertTrue(true == connection.beginTransaction());
//				prepared_statement_insert.executeUpdate();
//				try
//				{
//					Thread.sleep(RifeConfig.Database.getTransactionTimeout()*1000+100);
//				}
//				catch (InterruptedException e)
//				{
//		            fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//
//				try
//				{
//					connection.commit();
//					fail();
//				}
//				catch (TransactionTimedOutException e)
//				{
//					assertTrue(true);
//				}
//
//				assertTrue(false == connection.rollback());
//				try
//				{
//					prepared_statement_select.executeQuery();
//					assertTrue(false == prepared_statement_select.hasResultRows());
//				}
//				catch (DatabaseException e)
//				{
//					fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//			}
//			else
//			{
//				// FIXME: write tests with non transactional database
//			}
//		}
//		catch (DatabaseException e)
//		{
//            fail(ExceptionUtils.getExceptionStackTrace(e));
//		}
//		finally
//		{
//			if (null != connection)
//			{
//				try
//				{
//					prepared_statement_drop.executeUpdate();
//					connection.close();
//				}
//				catch (DatabaseException e)
//				{
//		            fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//			}
//		}
//	}
//
//	public void testTransactionTimeoutRollback()
//	{
//		DbConnection connection = null;
//		DbPreparedStatement prepared_statement_create = null;
//		DbPreparedStatement prepared_statement_insert = null;
//		DbPreparedStatement prepared_statement_select = null;
//		DbPreparedStatement prepared_statement_drop = null;
//		try
//		{
//			connection = datasource.getConnection();
//			prepared_statement_create = connection.getPreparedStatement("CREATE TABLE tbltest (id INTEGER, stringcol VARCHAR(255))");
//			prepared_statement_drop = connection.getPreparedStatement("DROP TABLE tbltest");
//			prepared_statement_create.executeUpdate();
//
//			prepared_statement_insert = connection.getPreparedStatement("INSERT INTO tbltest VALUES (232, 'somestring')");
//			prepared_statement_select = connection.getPreparedStatement("SELECT * FROM tbltest");
//
//			if (connection.supportsTransactions() &&
//				false == datasource.getAliasedDriver().equals("com.mysql.jdbc.Driver"))
//			{
//				assertTrue(true == connection.beginTransaction());
//				prepared_statement_insert.executeUpdate();
//				try
//				{
//					Thread.sleep(RifeConfig.Database.getTransactionTimeout()*1000+100);
//				}
//				catch (InterruptedException e)
//				{
//		            fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//
//				try
//				{
//					connection.rollback();
//					fail();
//				}
//				catch (TransactionTimedOutException e)
//				{
//					assertTrue(true);
//				}
//
//				assertTrue(false == connection.commit());
//				try
//				{
//					prepared_statement_select.executeQuery();
//					assertTrue(false == prepared_statement_select.hasResultRows());
//				}
//				catch (DatabaseException e)
//				{
//					fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//			}
//			else
//			{
//				// FIXME: write tests with non transactional database
//			}
//		}
//		catch (DatabaseException e)
//		{
//            fail(ExceptionUtils.getExceptionStackTrace(e));
//		}
//		finally
//		{
//			if (null != connection)
//			{
//				try
//				{
//					prepared_statement_drop.executeUpdate();
//					connection.close();
//				}
//				catch (DatabaseException e)
//				{
//		            fail(ExceptionUtils.getExceptionStackTrace(e));
//				}
//			}
//		}
//	}

    static class ThreadImpl extends Thread {
        private DbConnection connection_;

        public ThreadImpl(DbConnection connection) {
            connection_ = connection;
        }

        public void run() {
            try {
                assertFalse(connection_.isTransactionValidForThread());
                assertFalse(connection_.beginTransaction());
                assertFalse(connection_.commit());
                assertFalse(connection_.rollback());
            } catch (DatabaseException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            } finally {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    }
}
