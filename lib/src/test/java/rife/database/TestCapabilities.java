/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.ExecutionErrorException;
import rife.database.exceptions.UndefinedVirtualParameterException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCapabilities {
    public void setup(Datasource datasource) {
        DbQueryManager manager = new DbQueryManager(datasource);

        CreateTable createtable = new CreateTable(datasource);
        createtable.table("tablename")
            .columns(BeanImpl.class)
            .precision("propertyBigDecimal", 18, 9)
            .precision("propertyChar", 1)
            .precision("propertyDouble", 12, 3)
            .precision("propertyDoubleObject", 12, 3)
            .precision("propertyFloat", 13, 2)
            .precision("propertyFloatObject", 13, 2)
            .precision("propertyString", 255)
            .precision("propertyStringbuffer", 100);

        try {
            // prepare table and data
            manager.executeUpdate(createtable);

            Insert insert = new Insert(datasource);
            insert.into("tablename")
                .fields(BeanImpl.getPopulatedBean());
            manager.executeUpdate(insert);

            insert.clear();
            insert.into("tablename")
                .fields(BeanImpl.getNullBean());
            manager.executeUpdate(insert);

            BeanImpl impl = BeanImpl.getPopulatedBean();
            insert.clear();
            impl.setPropertyInt(3);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);
            insert.clear();
            impl.setPropertyInt(4);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);
            insert.clear();
            impl.setPropertyInt(5);
            insert.into("tablename")
                .fields(impl);
            manager.executeUpdate(insert);
        } catch (DatabaseException e) {
            tearDown(datasource);
            throw new RuntimeException(e);
        }
    }

    public void tearDown(Datasource datasource) {
        DbQueryManager manager = new DbQueryManager(datasource);

        // clean up nicely
        DropTable drop_table = new DropTable(datasource);
        try {
            drop_table.table("tablename");
            manager.executeUpdate(drop_table);
        } catch (DatabaseException e) {
            System.out.println(e.toString());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testLimitOffset(Datasource datasource) {
        setup(datasource);
        try {
            DbQueryManager manager = new DbQueryManager(datasource);

            final List<Integer> limit_ids = new ArrayList<Integer>();

            Select query = new Select(datasource);
            query.from("tablename")
                .orderBy("propertyInt")
                .limit(3);

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    limit_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }));
            assertEquals(3, limit_ids.size());
            assertEquals(0, limit_ids.get(0).intValue());
            assertEquals(3, limit_ids.get(1).intValue());
            assertEquals(4, limit_ids.get(2).intValue());

            final List<Integer> offset_ids = new ArrayList<Integer>();

            query.offset(1);

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    offset_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }));
            assertEquals(3, offset_ids.size());
            assertEquals(3, offset_ids.get(0).intValue());
            assertEquals(4, offset_ids.get(1).intValue());
            assertEquals(5, offset_ids.get(2).intValue());

            query.clear();

            final List<Integer> plain_ids = new ArrayList<Integer>();

            query.from("tablename")
                .orderBy("propertyInt")
                .offset(10);

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    plain_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }));
            assertEquals(5, plain_ids.size());
            assertEquals(0, plain_ids.get(0).intValue());
            assertEquals(3, plain_ids.get(1).intValue());
            assertEquals(4, plain_ids.get(2).intValue());
            assertEquals(5, plain_ids.get(3).intValue());
            assertEquals(545, plain_ids.get(4).intValue());
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testLimitOffsetParameters(Datasource datasource) {
        setup(datasource);

        try {
            DbQueryManager manager = new DbQueryManager(datasource);

            final List<Integer> limit_ids = new ArrayList<Integer>();

            Select query = new Select(datasource);
            query.from("tablename")
                .orderBy("propertyInt")
                .limitParameter("limit");

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    limit_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("limit", 3);
                }
            }));
            assertEquals(3, limit_ids.size());
            assertEquals(0, limit_ids.get(0).intValue());
            assertEquals(3, limit_ids.get(1).intValue());
            assertEquals(4, limit_ids.get(2).intValue());

            final List<Integer> offset_ids = new ArrayList<Integer>();

            query.offsetParameter("offset");

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    offset_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }, new DbPreparedStatementHandler() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("limit", 3)
                        .setInt("offset", 1);
                }
            }));
            assertEquals(3, offset_ids.size());
            assertEquals(3, offset_ids.get(0).intValue());
            assertEquals(4, offset_ids.get(1).intValue());
            assertEquals(5, offset_ids.get(2).intValue());

            query.clear();

            final List<Integer> plain_ids = new ArrayList<Integer>();

            query.from("tablename")
                .orderBy("propertyInt")
                .offsetParameter("offset");

            assertTrue(manager.executeFetchAll(query, new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    plain_ids.add(resultSet.getInt("propertyInt"));
                    return true;
                }
            }));
            assertEquals(5, plain_ids.size());
            assertEquals(0, plain_ids.get(0).intValue());
            assertEquals(3, plain_ids.get(1).intValue());
            assertEquals(4, plain_ids.get(2).intValue());
            assertEquals(5, plain_ids.get(3).intValue());
            assertEquals(545, plain_ids.get(4).intValue());
        } finally {
            tearDown(datasource);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testLimitOffsetParametersMissing(Datasource datasource) {
        setup(datasource);

        try {
            DbQueryManager manager = new DbQueryManager(datasource);

            Select query = new Select(datasource);
            query.from("tablename")
                .orderBy("propertyInt")
                .limitParameter("limit");

            try {
                manager.executeFetchAll(query, new DbRowProcessor() {
                    public boolean processRow(ResultSet resultSet)
                    throws SQLException {
                        return true;
                    }
                });
                assertEquals("org.hsqldb.jdbcDriver", datasource.getAliasedDriver());    // hsqldb 1.8.0 doesn't throw an exception when no limit parameter is provided
            } catch (ExecutionErrorException e) {
                assertTrue(e.getCause() instanceof SQLException);
            } catch (UndefinedVirtualParameterException e) {
                assertEquals("limit", e.getParameterName());
            }

            query.offsetParameter("offset");

            try {
                manager.executeFetchAll(query, new DbRowProcessor() {
                    public boolean processRow(ResultSet resultSet)
                    throws SQLException {
                        return true;
                    }
                }, new DbPreparedStatementHandler() {
                    public void setParameters(DbPreparedStatement statement) {
                        statement
                            .setInt("limit", 3);
                    }
                });
                assertEquals("org.hsqldb.jdbcDriver", datasource.getAliasedDriver());    // hsqldb 1.8.0 doesn't throw an exception when no offset parameter is provided
            } catch (ExecutionErrorException e) {
                assertTrue(e.getCause() instanceof SQLException);
            } catch (UndefinedVirtualParameterException e) {
                assertEquals("offset", e.getParameterName());
            }
        } finally {
            tearDown(datasource);
        }
    }
}
