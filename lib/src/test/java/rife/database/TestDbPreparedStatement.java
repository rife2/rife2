/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.*;
import rife.database.queries.*;
import rife.tools.ExceptionUtils;
import rife.tools.exceptions.BeanUtilsException;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestDbPreparedStatement {
    public void setup(Datasource datasource) {
        // create the temporary table
        CreateTable query_create = new CreateTable(datasource);
        query_create.table("parametersbean")
            .columns(BeanImpl.class)
            .column("notbeanInt", int.class)
            .precision("propertyString", 255)
            .precision("propertyStringbuffer", 255)
            .precision("propertyChar", 1)
            .precision("propertyDouble", 7, 2)
            .precision("propertyFloat", 8, 3)
            .precision("propertyBigDecimal", 16, 6);
        DbStatement statement = datasource.getConnection().createStatement();
        try {
            try {
                statement.executeUpdate(query_create);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                statement.close();
            } catch (DatabaseException e) {
                // do nothing
            }
        }
    }

    public void tearDown(Datasource datasource) {
        try {
            DbConnection connection = datasource.getConnection();

            // drop temporary table
            DropTable query_drop = new DropTable(datasource);
            query_drop.table("parametersbean");
            connection.createStatement().executeUpdate(query_drop);

            connection.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiationSql(Datasource datasource) {
        setup(datasource);
        try {
            String sql = "DELETE FROM parametersbean";
            DbPreparedStatement statement_delete = datasource.getConnection().getPreparedStatement(sql);
            assertEquals(sql, statement_delete.getSql());
            assertNull(statement_delete.getQuery());
            statement_delete.executeUpdate();
            statement_delete.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiationQuery(Datasource datasource) {
        setup(datasource);
        try {
            Delete query_delete = new Delete(datasource);
            query_delete
                .from("parametersbean");
            DbPreparedStatement statement_delete = datasource.getConnection().getPreparedStatement(query_delete);
            assertEquals(query_delete.getSql(), statement_delete.getSql());
            assertEquals(query_delete, statement_delete.getQuery());
            statement_delete.executeUpdate();
            statement_delete.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQuery(Datasource datasource) {
        setup(datasource);
        try {
            Select query_select = new Select(datasource);
            query_select
                .from("parametersbean");
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(query_select);
            statement_select.executeQuery();
            assertNotNull(statement_select.getResultSet());
            statement_select.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteQueryException(Datasource datasource) {
        setup(datasource);
        try {
            Select query_select = new Select(datasource);
            query_select
                .from("inexistenttable");

            DbPreparedStatement statement_select = null;
            try {
                statement_select = datasource.getConnection().getPreparedStatement(query_select);

                try {
                    statement_select.executeQuery();
                    fail();
                } catch (ExecutionErrorException e) {
                    assertSame(datasource, e.getDatasource());
                    assertEquals(query_select.getSql(), e.getSql());
                }
                assertNull(statement_select.getResultSet());
            } catch (PreparedStatementCreationErrorException e) {
                assertSame(datasource, e.getDatasource());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdate(Datasource datasource) {
        setup(datasource);
        try {
            Delete query_delete = new Delete(datasource);
            query_delete
                .from("parametersbean");
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(query_delete);
            statement_select.executeUpdate();
            statement_select.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testExecuteUpdateException(Datasource datasource) {
        setup(datasource);
        try {
            Delete query_delete = new Delete(datasource);
            query_delete
                .from("inexistenttable");
            DbPreparedStatement statement_update = null;

            try {
                statement_update = datasource.getConnection().getPreparedStatement(query_delete);
                try {
                    statement_update.executeUpdate();
                    fail();
                } catch (ExecutionErrorException e) {
                    assertSame(datasource, e.getDatasource());
                    assertEquals(query_delete.getSql(), e.getSql());
                }
            } catch (PreparedStatementCreationErrorException e) {
                assertSame(datasource, e.getDatasource());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testNotParametrized(Datasource datasource) {
        setup(datasource);
        try {
            String sql = "SELECT * FROM parametersbean WHERE propertyString = ?";
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(sql);
            try {
                statement_select.setString("propertyString", "ok");
                fail();
            } catch (DatabaseException e) {
                assertTrue(e instanceof NoParametrizedQueryException);
                assertSame(statement_select, ((NoParametrizedQueryException) e).getPreparedStatement());
            }
            statement_select.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testNoParameters(Datasource datasource) {
        setup(datasource);
        try {
            Select query_select = new Select(datasource);
            query_select.from("parametersbean");
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(query_select);
            try {
                statement_select.setString("propertyString", "ok");
                fail();
            } catch (DatabaseException e) {
                assertTrue(e instanceof NoParametersException);
                assertSame(statement_select, ((NoParametersException) e).getPreparedStatement());
            }
            statement_select.close();
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAddBatch(Datasource datasource) {
        setup(datasource);

        CreateTable query_create = new CreateTable(datasource);
        query_create
            .table("batchtest")
            .column("intcol", int.class);
        try {
            DbPreparedStatement statement_create = datasource.getConnection().getPreparedStatement(query_create);
            statement_create.executeUpdate();
            statement_create.close();

            Insert query_insert = new Insert(datasource);
            query_insert
                .into(query_create.getTable())
                .fieldParameter("intcol");
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            int first = 1;
            int second = 5;
            int third = 9;
            int fourth = 12;
            statement_insert.setInt("intcol", first);
            statement_insert.addBatch();
            statement_insert.setInt("intcol", second);
            statement_insert.addBatch();
            statement_insert.setInt("intcol", third);
            statement_insert.addBatch();
            statement_insert.setInt("intcol", fourth);
            statement_insert.addBatch();
            statement_insert.executeBatch();
            statement_insert.close();

            Select query_select = new Select(datasource);
            query_select
                .from(query_create.getTable());
            DbStatement statement_select = datasource.getConnection().createStatement();
            statement_select.executeQuery(query_select);
            boolean got_first = false;
            boolean got_second = false;
            boolean got_third = false;
            boolean got_fourth = false;
            ResultSet resultset = statement_select.getResultSet();
            int result = -1;
            while (resultset.next()) {
                result = resultset.getInt("intcol");
                if (first == result) {
                    if (got_first) {
                        fail("Got " + first + " more than once");
                    }
                    got_first = true;
                } else if (second == result) {
                    if (got_second) {
                        fail("Got " + second + " more than once");
                    }
                    got_second = true;
                } else if (third == result) {
                    if (got_third) {
                        fail("Got " + third + " more than once");
                    }
                    got_third = true;
                } else if (fourth == result) {
                    if (got_fourth) {
                        fail("Got " + fourth + " more than once");
                    }
                    got_fourth = true;
                } else {
                    fail("Unknown value : " + result);
                }
            }
            statement_select.close();

            assertTrue(got_first);
            assertTrue(got_second);
            assertTrue(got_third);
            assertTrue(got_fourth);
        } catch (SQLException | DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                DropTable query_drop = new DropTable(datasource);
                query_drop
                    .table(query_create.getTable());
                DbPreparedStatement statement_drop = datasource.getConnection().getPreparedStatement(query_drop);
                statement_drop.executeUpdate();
                statement_drop.close();
            } catch (DatabaseException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            } finally {
                tearDown(datasource);
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetMetaData(Datasource datasource) {
        setup(datasource);

        try {
            Select query_select = new Select(datasource);
            query_select
                .from("parametersbean")
                .whereParameter("propertyString", "=");
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(query_select);
            statement_select.setString("propertyString", "ok");
            ResultSetMetaData metadata = null;
            metadata = statement_select.getMetaData();
            assertNotNull(metadata);
            statement_select.close();
        } catch (DatabaseException e) {
            if (e.getCause() != null) {
                // mysql
                if (e.getCause().getClass().getName().equals("com.mysql.jdbc.NotImplemented")) {
                    return;
                }
                // oracle
                if (e.getCause().getClass().getName().equals("java.sql.SQLException") &&
                    e.getCause().getMessage().contains("statement handle not executed: getMetaData")) {
                    return;
                }
            }

            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetParameterMetaData(Datasource datasource) {
        setup(datasource);

        try {
            Select query_select = new Select(datasource);
            query_select
                .from("parametersbean")
                .whereParameter("propertyString", "=");
            DbPreparedStatement statement_select = datasource.getConnection().getPreparedStatement(query_select);
            statement_select.setString("propertyString", "ok");
            ParameterMetaData metadata = null;
            try {
                metadata = statement_select.getParameterMetaData();
                assertNotNull(metadata);
            } catch (AbstractMethodError e) {
                assertTrue(datasource.getDriver().equals("oracle.jdbc.driver.OracleDriver") ||
                    datasource.getDriver().equals("org.apache.derby.jdbc.EmbeddedDriver"));
            }
            statement_select.close();
        } catch (DatabaseException e) {
            if (e.getCause() != null) {
                if (e.getCause().getClass().getName().equals("com.mysql.jdbc.NotImplemented")) {
                    return;
                }
                if (e.getCause().getClass().getName().equals("org.postgresql.util.PSQLException") &&
                    e.getCause().getMessage().equals("This method is not yet implemented.")) {
                    return;
                }
            }

            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSetBeanNull(Datasource datasource) {
        setup(datasource);

        try {
            // insert some data
            Insert query_insert = new Insert(datasource);
            query_insert.into("parametersbean")
                .fieldsParameters(BeanImpl.class);
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            try {
                statement_insert.setBean(null);
                fail();
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            } finally {
                statement_insert.close();
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSetBeanError(Datasource datasource) {
        setup(datasource);

        try {
            // insert some data
            Insert query_insert = new Insert(datasource);
            query_insert.into("parametersbean")
                .fieldsParameters(BeanImpl.class);
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            try {
                statement_insert.setBean(BeanErrorImpl.getPopulatedBean());
                fail();
            } catch (DatabaseException e) {
                assertTrue(e.getCause() instanceof BeanUtilsException);
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSetBean(Datasource datasource) {
        setup(datasource);

        try {
            // insert some data
            Insert query_insert = new Insert(datasource);
            query_insert.into("parametersbean")
                .fieldsParameters(BeanImpl.class)
                .fieldParameter("notbeanInt");
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            try {
                try {
                    statement_insert.setBean(null);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                statement_insert.setBean(BeanImpl.getPopulatedBean());
                statement_insert.setInt("notbeanInt", 23);
                statement_insert.executeUpdate();

                // retrieve the data
                BeanManager bean_manager = new BeanManager(datasource);
                BeanImpl retrieved_bean = bean_manager.fetchBean();
                BeanImpl new_bean = BeanImpl.getPopulatedBean();
                assertEquals(retrieved_bean.getPropertyString(), new_bean.getPropertyString());
                assertEquals(retrieved_bean.getPropertyStringbuffer().toString(), new_bean.getPropertyStringbuffer().toString());

                // don't compare milliseconds since each db stores it differently
                if (datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver")) {
                    // round up MySQL and H2 milliseconds since that's how it behaves
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, ((new_bean.getPropertyDate().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, ((new_bean.getPropertyCalendar().getTime().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, ((new_bean.getPropertyTimestamp().getTime() + 500) / 1000) * 1000);
                    assertEquals(new Time((retrieved_bean.getPropertyTime().getTime() / 1000) * 1000).toString(), new Time(((new_bean.getPropertyTime().getTime() + 500) / 1000) * 1000).toString());
                } else if(datasource.getAliasedDriver().equals("org.h2.Driver")) {
                    // H2 rounds up the SQL time
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, (new_bean.getPropertyDate().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (new_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, (new_bean.getPropertyTimestamp().getTime() / 1000) * 1000);
                    assertEquals(new Time((retrieved_bean.getPropertyTime().getTime() / 1000) * 1000).toString(), new Time(((new_bean.getPropertyTime().getTime() + 500) / 1000) * 1000).toString());
                } else {
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, (new_bean.getPropertyDate().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (new_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, (new_bean.getPropertyTimestamp().getTime() / 1000) * 1000);
                    assertEquals(retrieved_bean.getPropertyTime().toString(), new_bean.getPropertyTime().toString());
                }

                assertEquals(retrieved_bean.getPropertySqlDate().toString(), new_bean.getPropertySqlDate().toString());
                assertEquals(retrieved_bean.getPropertyChar(), new_bean.getPropertyChar());
                assertEquals(retrieved_bean.getPropertyCharacterObject(), new_bean.getPropertyCharacterObject());
                assertEquals(retrieved_bean.isPropertyBoolean(), new_bean.isPropertyBoolean());
                assertEquals(retrieved_bean.getPropertyBooleanObject(), new_bean.getPropertyBooleanObject());
                assertEquals(retrieved_bean.getPropertyByte(), new_bean.getPropertyByte());
                assertEquals(retrieved_bean.getPropertyByteObject(), new_bean.getPropertyByteObject());
                assertEquals(retrieved_bean.getPropertyDouble(), new_bean.getPropertyDouble(), 0.01);
                assertEquals(retrieved_bean.getPropertyDoubleObject(), new_bean.getPropertyDoubleObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloat(), new_bean.getPropertyFloat(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloatObject(), new_bean.getPropertyFloatObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyInt(), new_bean.getPropertyInt());
                assertEquals(retrieved_bean.getPropertyIntegerObject(), new_bean.getPropertyIntegerObject());
                assertEquals(retrieved_bean.getPropertyLong(), new_bean.getPropertyLong());
                assertEquals(retrieved_bean.getPropertyLongObject(), new_bean.getPropertyLongObject());
                assertEquals(retrieved_bean.getPropertyShort(), new_bean.getPropertyShort());
                assertEquals(retrieved_bean.getPropertyShortObject(), new_bean.getPropertyShortObject());
                assertEquals(retrieved_bean.getPropertyBigDecimal(), new_bean.getPropertyBigDecimal());
            } finally {
                statement_insert.close();
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSetBeanNulls(Datasource datasource) {
        setup(datasource);

        try {
            // insert some data
            Insert query_insert = new Insert(datasource);
            query_insert.into("parametersbean")
                .fieldsParameters(BeanImpl.class);
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            try {
                BeanImpl null_bean = BeanImpl.getNullBean();
                // each database has its oddities here, sadly
                Calendar cal = Calendar.getInstance();
                cal.set(2002, 5, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 764);
                if (datasource.getDriver().equals("org.postgresql.Driver")) {
                    // postgres doesn't handle null chars
                    null_bean.setPropertyChar(' ');
                } else if (datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver")) {
                    // mysql automatically set the current time to timestamps
                    null_bean.setPropertyDate(cal.getTime());
                    null_bean.setPropertyCalendar(cal);
                    null_bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
                }
                statement_insert.setBean(null_bean);
                statement_insert.executeUpdate();

                // retrieve the data
                BeanManager bean_manager = new BeanManager(datasource);
                BeanImpl retrieved_bean = bean_manager.fetchBean();
                BeanImpl new_bean = BeanImpl.getNullBean();
                // apply the database oddities
                if (datasource.getDriver().equals("org.postgresql.Driver")) {
                    // postgres doesn't handle null chars
                    new_bean.setPropertyChar(' ');
                } else if (datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver")) {
                    // mysql automatically set the current time to timestamps
                    new_bean.setPropertyDate(cal.getTime());
                    new_bean.setPropertyCalendar(cal);
                    new_bean.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
                }
                assertEquals(retrieved_bean.getPropertyString(), new_bean.getPropertyString());
                assertEquals(retrieved_bean.getPropertyStringbuffer(), new_bean.getPropertyStringbuffer());
                if (datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver")) {
                    // round up MySQL milliseconds since that's how it behaves
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, ((new_bean.getPropertyDate().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, ((new_bean.getPropertyCalendar().getTime().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, ((new_bean.getPropertyTimestamp().getTime() + 500) / 1000) * 1000);
                } else {
                    assertEquals(retrieved_bean.getPropertyDate(), new_bean.getPropertyDate());
                    assertEquals(retrieved_bean.getPropertyCalendar(), new_bean.getPropertyCalendar());
                    assertEquals(retrieved_bean.getPropertyTimestamp(), new_bean.getPropertyTimestamp());
                }
                assertEquals(retrieved_bean.getPropertySqlDate(), new_bean.getPropertySqlDate());
                assertEquals(retrieved_bean.getPropertyTime(), new_bean.getPropertyTime());
                assertEquals(retrieved_bean.getPropertyChar(), new_bean.getPropertyChar());
                assertEquals(retrieved_bean.getPropertyCharacterObject(), new_bean.getPropertyCharacterObject());
                assertEquals(retrieved_bean.isPropertyBoolean(), new_bean.isPropertyBoolean());
                assertEquals(retrieved_bean.getPropertyBooleanObject(), new_bean.getPropertyBooleanObject());
                assertEquals(retrieved_bean.getPropertyByte(), new_bean.getPropertyByte());
                assertEquals(retrieved_bean.getPropertyByteObject(), new_bean.getPropertyByteObject());
                assertEquals(retrieved_bean.getPropertyDouble(), new_bean.getPropertyDouble(), 0.01);
                assertEquals(retrieved_bean.getPropertyDoubleObject(), new_bean.getPropertyDoubleObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloat(), new_bean.getPropertyFloat(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloatObject(), new_bean.getPropertyFloatObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyInt(), new_bean.getPropertyInt());
                assertEquals(retrieved_bean.getPropertyIntegerObject(), new_bean.getPropertyIntegerObject());
                assertEquals(retrieved_bean.getPropertyLong(), new_bean.getPropertyLong());
                assertEquals(retrieved_bean.getPropertyLongObject(), new_bean.getPropertyLongObject());
                assertEquals(retrieved_bean.getPropertyShort(), new_bean.getPropertyShort());
                assertEquals(retrieved_bean.getPropertyShortObject(), new_bean.getPropertyShortObject());
                assertEquals(retrieved_bean.getPropertyBigDecimal(), new_bean.getPropertyBigDecimal());
            } finally {
                statement_insert.close();
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSetNamedParameters(Datasource datasource) {
        setup(datasource);

        try {
            // insert some data
            Insert query_insert = new Insert(datasource);
            query_insert.into("parametersbean")
                .fieldsParameters(BeanImpl.class);
            DbPreparedStatement statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(2002, Calendar.JUNE, 18, 15, 26, 14);
                cal.set(Calendar.MILLISECOND, 764);
                statement_insert.setString("propertyString", "someotherstring");
                statement_insert.setString("propertyStringbuffer", "someotherstringbuff");
                statement_insert.setTimestamp("propertyDate", new Timestamp(cal.getTime().getTime()));
                statement_insert.setTimestamp("propertyCalendar", new Timestamp(cal.getTime().getTime()));
                statement_insert.setDate("propertySqlDate", new java.sql.Date(cal.getTime().getTime()));
                statement_insert.setTime("propertyTime", new Time(cal.getTime().getTime()));
                statement_insert.setTimestamp("propertyTimestamp", new Timestamp(cal.getTime().getTime()));
                statement_insert.setString("propertyChar", "v");
                statement_insert.setString("propertyCharacterObject", "r");
                statement_insert.setBoolean("propertyBoolean", true);
                statement_insert.setBoolean("propertyBooleanObject", false);
                statement_insert.setByte("propertyByte", (byte) 89);
                statement_insert.setByte("propertyByteObject", (byte) 34);
                statement_insert.setDouble("propertyDouble", 53348.34d);
                statement_insert.setDouble("propertyDoubleObject", 143298.692d);
                statement_insert.setFloat("propertyFloat", 98634.2f);
                statement_insert.setFloat("propertyFloatObject", 8734.7f);
                statement_insert.setInt("propertyInt", 545);
                statement_insert.setInt("propertyIntegerObject", 968);
                statement_insert.setLong("propertyLong", 34563L);
                statement_insert.setLong("propertyLongObject", 66875L);
                statement_insert.setShort("propertyShort", (short) 43);
                statement_insert.setShort("propertyShortObject", (short) 68);
                statement_insert.setBigDecimal("propertyBigDecimal", new BigDecimal("219038743.392874"));
                statement_insert.setString("propertyEnum", SomeEnum.VALUE_TWO.toString());

                statement_insert.executeUpdate();

                // retrieve the data
                BeanManager bean_manager = new BeanManager(datasource);
                BeanImpl retrieved_bean = bean_manager.fetchBean();
                BeanImpl new_bean = BeanImpl.getPopulatedBean();
                assertEquals(retrieved_bean.getPropertyString(), new_bean.getPropertyString());
                assertEquals(retrieved_bean.getPropertyStringbuffer().toString(), new_bean.getPropertyStringbuffer().toString());

                // don't compare milliseconds since each db stores it differently
                if (datasource.getAliasedDriver().equals("com.mysql.cj.jdbc.Driver")) {
                    // round up MySQL and H2 milliseconds since that's how it behaves
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, ((new_bean.getPropertyDate().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, ((new_bean.getPropertyCalendar().getTime().getTime() + 500) / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, ((new_bean.getPropertyTimestamp().getTime() + 500) / 1000) * 1000);
                    assertEquals(new Time((retrieved_bean.getPropertyTime().getTime() / 1000) * 1000).toString(), new Time(((new_bean.getPropertyTime().getTime() + 500) / 1000) * 1000).toString());
                } else if(datasource.getAliasedDriver().equals("org.h2.Driver")) {
                    // H2 rounds up the SQL time
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, (new_bean.getPropertyDate().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (new_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, (new_bean.getPropertyTimestamp().getTime() / 1000) * 1000);
                    assertEquals(new Time((retrieved_bean.getPropertyTime().getTime() / 1000) * 1000).toString(), new Time(((new_bean.getPropertyTime().getTime() + 500) / 1000) * 1000).toString());
                } else {
                    assertEquals((retrieved_bean.getPropertyDate().getTime() / 1000) * 1000, (new_bean.getPropertyDate().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000, (new_bean.getPropertyCalendar().getTime().getTime() / 1000) * 1000);
                    assertEquals((retrieved_bean.getPropertyTimestamp().getTime() / 1000) * 1000, (new_bean.getPropertyTimestamp().getTime() / 1000) * 1000);
                    assertEquals(retrieved_bean.getPropertyTime().toString(), new_bean.getPropertyTime().toString());
                }

                assertEquals(retrieved_bean.getPropertySqlDate().toString(), new_bean.getPropertySqlDate().toString());
                assertEquals(retrieved_bean.getPropertyChar(), new_bean.getPropertyChar());
                assertEquals(retrieved_bean.getPropertyCharacterObject(), new_bean.getPropertyCharacterObject());
                assertEquals(retrieved_bean.isPropertyBoolean(), new_bean.isPropertyBoolean());
                assertEquals(retrieved_bean.getPropertyBooleanObject(), new_bean.getPropertyBooleanObject());
                assertEquals(retrieved_bean.getPropertyByte(), new_bean.getPropertyByte());
                assertEquals(retrieved_bean.getPropertyByteObject(), new_bean.getPropertyByteObject());
                assertEquals(retrieved_bean.getPropertyDouble(), new_bean.getPropertyDouble(), 0.01);
                assertEquals(retrieved_bean.getPropertyDoubleObject(), new_bean.getPropertyDoubleObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloat(), new_bean.getPropertyFloat(), 0.01);
                assertEquals(retrieved_bean.getPropertyFloatObject(), new_bean.getPropertyFloatObject(), 0.01);
                assertEquals(retrieved_bean.getPropertyInt(), new_bean.getPropertyInt());
                assertEquals(retrieved_bean.getPropertyIntegerObject(), new_bean.getPropertyIntegerObject());
                assertEquals(retrieved_bean.getPropertyLong(), new_bean.getPropertyLong());
                assertEquals(retrieved_bean.getPropertyLongObject(), new_bean.getPropertyLongObject());
                assertEquals(retrieved_bean.getPropertyShort(), new_bean.getPropertyShort());
                assertEquals(retrieved_bean.getPropertyShortObject(), new_bean.getPropertyShortObject());
                assertEquals(retrieved_bean.getPropertyBigDecimal(), new_bean.getPropertyBigDecimal());
            } finally {
                statement_insert.close();
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalParameterName(Datasource datasource) {
        setup(datasource);

        try {
            Insert query_insert = new Insert(datasource);
            query_insert
                .into("parametersbean")
                .fieldParameter("intcol");
            DbPreparedStatement statement_insert = null;

            try {
                statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
                try {
                    statement_insert.setInt(null, 1);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setInt("", 1);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                statement_insert.close();
            } catch (PreparedStatementCreationErrorException e) {
                assertSame(datasource, e.getDatasource());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInexistantParameterName(Datasource datasource) {
        setup(datasource);

        try {
            Insert query_insert = new Insert(datasource);
            query_insert
                .into("parametersbean")
                .fieldParameter("intcol");
            DbPreparedStatement statement_insert = null;

            try {
                statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
                try {
                    statement_insert.setInt("doesntexist", 1);
                    fail();
                } catch (ParameterDoesntExistException e) {
                    assertSame(statement_insert, e.getPreparedStatement());
                    assertEquals("doesntexist", e.getParameterName());
                }
                statement_insert.close();
            } catch (PreparedStatementCreationErrorException e) {
                assertSame(datasource, e.getDatasource());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testIllegalArgumentTypedParameters(Datasource datasource) {
        setup(datasource);

        try {
            Insert query_insert = new Insert(datasource);
            query_insert
                .into("parametersbean")
                .fieldParameter("intcol");
            DbPreparedStatement statement_insert = null;

            try {
                statement_insert = datasource.getConnection().getPreparedStatement(query_insert);
                try {
                    statement_insert.setDoubles(null, 1d);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setShorts(null, (short) 1);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setNulls(null, Types.INTEGER);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setNulls(null, Types.INTEGER, "INT");
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setBooleans(null, true);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setBytes(null, (byte) 1);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setDates(null, new Date(0));
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setDates(null, new Date(0), Calendar.getInstance());
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setInts(null, 1);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setLongs(null, 1L);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setFloats(null, 1f);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setBigDecimals(null, new BigDecimal(String.valueOf(1)));
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setStrings(null, "1");
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setBytes((int[]) null, new byte[]{(byte) 1});
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setObjects(null, "1", Types.VARCHAR, 0);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setObjects(null, "1", Types.VARCHAR);
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setTimes(null, new Time(0));
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setTimes(null, new Time(0), Calendar.getInstance());
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setTimestamps(null, new Timestamp(0));
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setTimestamps(null, new Timestamp(0), Calendar.getInstance());
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    statement_insert.setObjects(null, "1");
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                try {
                    try {
                        statement_insert.setURLs(null, new URL("https://www.uwyn.com"));
                    } catch (MalformedURLException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                    }
                    fail();
                } catch (IllegalArgumentException e) {
                    assertTrue(true);
                }
                statement_insert.close();
            } catch (PreparedStatementCreationErrorException e) {
                assertSame(datasource, e.getDatasource());
            }
        } catch (DatabaseException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            tearDown(datasource);
        }
    }

    static class BeanManager extends DbQueryManager {
        public BeanManager(Datasource datasource) {
            super(datasource);
        }

        protected BeanImpl fetchBean()
        throws DatabaseException {
            Select query_select = new Select(getDatasource());
            query_select
                .from("parametersbean")
                .fields(BeanImpl.class);
            DbBeanFetcher<BeanImpl> fetcher = new DbBeanFetcher<BeanImpl>(getDatasource(), BeanImpl.class);

            DbStatement statement = executeQuery(query_select);
            fetch(statement.getResultSet(), fetcher);
            BeanImpl bean = fetcher.getBeanInstance();
            statement.close();

            return bean;
        }
    }
}
