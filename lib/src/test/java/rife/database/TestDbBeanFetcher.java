/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.DropTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;
import rife.tools.ExceptionUtils;
import rife.tools.InnerClassException;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestDbBeanFetcher {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testFetchBean(Datasource datasource)
    throws Exception {
        final DbQueryManager manager = new DbQueryManager(datasource);

        // create the temporary table
        CreateTable query_create = new CreateTable(datasource);
        query_create.table("tmp_beanmanager")
            .columns(BeanImpl.class)
            .precision("propertyString", 255)
            .precision("propertyStringbuffer", 255)
            .precision("propertyChar", 1)
            .precision("propertyDouble", 8, 3)
            .precision("propertyFloat", 10, 5)
            .precision("propertyDoubleObject", 8, 3)
            .precision("propertyFloatObject", 8, 2)
            .precision("propertyBigDecimal", 16, 6);


        DbBeanFetcher<BeanImpl> fetcher = null;
        try {
            manager.executeUpdate(query_create);

            fetcher = manager.inTransaction(new DbTransactionUser() {
                public DbBeanFetcher<BeanImpl> useTransaction()
                throws InnerClassException {
                    DbBeanFetcher<BeanImpl> fetcher = null;
                    try {
                        // populate with test data
                        Calendar cal = Calendar.getInstance();
                        cal.set(2002, 5, 17, 15, 36);
                        cal.set(Calendar.MILLISECOND, 0);    // milliseconds are only correctly supported by postgresql, don't include them in generic tests

                        BeanImpl bean_populated = new BeanImpl();
                        bean_populated.setPropertyString("somestring");
                        bean_populated.setPropertyStringbuffer(new StringBuffer("somestringbuffer"));
                        bean_populated.setPropertyDate(cal.getTime());
                        bean_populated.setPropertyCalendar(cal);
                        bean_populated.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
                        bean_populated.setPropertyTime(new Time(cal.getTime().getTime()));
                        bean_populated.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));
                        bean_populated.setPropertyChar('v');
                        bean_populated.setPropertyBoolean(true);
                        bean_populated.setPropertyByte((byte) 127);
                        bean_populated.setPropertyDouble(78634.433d);
                        bean_populated.setPropertyFloat(76734.87834f);
                        bean_populated.setPropertyDoubleObject(81432.971d);
                        bean_populated.setPropertyFloatObject(311423.98f);
                        bean_populated.setPropertyInt(13);
                        bean_populated.setPropertyLong(18753L);
                        bean_populated.setPropertyShort((short) 23);
                        bean_populated.setPropertyBigDecimal(new BigDecimal("7653564654.426587"));
                        Insert query_insert = new Insert(datasource);
                        query_insert
                            .into("tmp_beanmanager")
                            .fields(bean_populated);
                        assertEquals(manager.executeUpdate(query_insert), 1);

                        // construct the select query
                        Select query_select = new Select(datasource);
                        query_select
                            .from("tmp_beanmanager")
                            .fields(BeanImpl.class);
                        fetcher = new DbBeanFetcher<BeanImpl>(datasource, BeanImpl.class);

                        BeanImpl bean = null;
                        DbStatement statement = null;
                        try {
                            statement = manager.executeQuery(query_select);
                            manager.fetch(statement.getResultSet(), fetcher);
                            bean = fetcher.getBeanInstance();
                            statement.close();
                        } catch (DatabaseException e) {
                            fail(ExceptionUtils.getExceptionStackTrace(e));
                        }
                        assertNotNull(bean);
                        assertEquals(bean.getPropertyString(), bean_populated.getPropertyString());
                        assertEquals(bean.getPropertyStringbuffer().toString(), bean_populated.getPropertyStringbuffer().toString());
                        assertEquals(bean.getPropertyDate().getTime(), bean_populated.getPropertyDate().getTime());
                        assertEquals(bean.getPropertyCalendar(), bean_populated.getPropertyCalendar());
                        assertEquals(bean.getPropertySqlDate().toString(), bean_populated.getPropertySqlDate().toString());
                        assertEquals(bean.getPropertyTime().toString(), bean_populated.getPropertyTime().toString());
                        assertEquals(bean.getPropertyTimestamp(), bean_populated.getPropertyTimestamp());
                        assertEquals(bean.isPropertyBoolean(), bean_populated.isPropertyBoolean());
                        assertEquals(bean.getPropertyChar(), bean_populated.getPropertyChar());
                        assertEquals(bean.getPropertyByte(), bean_populated.getPropertyByte());
                        assertEquals(bean.getPropertyDouble(), bean_populated.getPropertyDouble(), 0.01);
                        assertEquals(bean.getPropertyFloat(), bean_populated.getPropertyFloat(), 0.01);
                        assertEquals(bean.getPropertyDoubleObject(), bean_populated.getPropertyDoubleObject(), 0.01);
                        assertEquals(bean.getPropertyFloatObject(), bean_populated.getPropertyFloatObject(), 0.01);
                        assertEquals(bean.getPropertyInt(), bean_populated.getPropertyInt());
                        assertEquals(bean.getPropertyLong(), bean_populated.getPropertyLong());
                        assertEquals(bean.getPropertyShort(), bean_populated.getPropertyShort());
                        assertEquals(bean.getPropertyBigDecimal(), bean_populated.getPropertyBigDecimal());
                    } catch (DatabaseException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                        throw e;
                    }

                    return fetcher;
                }
            });
        } finally {
            // remove the temporary table
            DropTable query_drop = new DropTable(datasource);
            query_drop.table(query_create.getTable());
            manager.executeUpdate(query_drop);
        }

        assertNotNull(fetcher.getBeanInstance());
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testFetchNullBean(Datasource datasource)
    throws Exception {
        final DbQueryManager manager = new DbQueryManager(datasource);

        // create the temporary table
        CreateTable query_create = new CreateTable(datasource);
        query_create.table("tmp_beanmanager")
            .columns(BeanImpl.class)
            .precision("propertyString", 255)
            .precision("propertyStringbuffer", 255)
            .precision("propertyChar", 1)
            .precision("propertyDouble", 5, 4)
            .precision("propertyFloat", 5, 5)
            .precision("propertyDoubleObject", 5, 4)
            .precision("propertyFloatObject", 5, 5)
            .precision("propertyBigDecimal", 16, 6);


        DbBeanFetcher<BeanImpl> fetcher = null;
        try {
            manager.executeUpdate(query_create);

            fetcher = manager.inTransaction(new DbTransactionUser() {
                public DbBeanFetcher<BeanImpl> useTransaction()
                throws InnerClassException {
                    DbBeanFetcher<BeanImpl> fetcher = null;
                    try {
                        BeanImpl bean_null = new BeanImpl();
                        Insert query_insert = new Insert(datasource);
                        query_insert
                            .into("tmp_beanmanager")
                            .fields(bean_null);
                        assertEquals(manager.executeUpdate(query_insert), 1);

                        // construct the select query
                        Select query_select = new Select(datasource);
                        query_select
                            .from("tmp_beanmanager")
                            .fields(BeanImpl.class);
                        fetcher = new DbBeanFetcher<BeanImpl>(datasource, BeanImpl.class);

                        BeanImpl bean = null;
                        DbStatement statement = null;
                        try {
                            statement = manager.executeQuery(query_select);
                            manager.fetch(statement.getResultSet(), fetcher);
                            bean = fetcher.getBeanInstance();
                            statement.close();
                        } catch (DatabaseException e) {
                            fail(ExceptionUtils.getExceptionStackTrace(e));
                        }
                        assertNotNull(bean);
                        assertEquals(bean.getPropertyString(), bean_null.getPropertyString());
                        assertEquals(bean.getPropertyStringbuffer(), bean_null.getPropertyStringbuffer());
                        assertEquals(bean.getPropertyDate(), bean_null.getPropertyDate());
                        assertEquals(bean.getPropertyCalendar(), bean_null.getPropertyCalendar());
                        assertEquals(bean.getPropertySqlDate(), bean_null.getPropertySqlDate());
                        assertEquals(bean.getPropertyTime(), bean_null.getPropertyTime());
                        assertEquals(bean.getPropertyTimestamp(), bean_null.getPropertyTimestamp());
                        assertEquals(bean.isPropertyBoolean(), bean_null.isPropertyBoolean());
                        assertEquals(bean.getPropertyChar(), bean_null.getPropertyChar());
                        assertEquals(bean.getPropertyByte(), bean_null.getPropertyByte());
                        assertEquals(bean.getPropertyDouble(), bean_null.getPropertyDouble(), 0.01);
                        assertEquals(bean.getPropertyFloat(), bean_null.getPropertyFloat(), 0.01);
                        assertEquals(bean.getPropertyDoubleObject(), bean_null.getPropertyDoubleObject());
                        assertEquals(bean.getPropertyFloatObject(), bean_null.getPropertyFloatObject());
                        assertEquals(bean.getPropertyInt(), bean_null.getPropertyInt());
                        assertEquals(bean.getPropertyLong(), bean_null.getPropertyLong());
                        assertEquals(bean.getPropertyShort(), bean_null.getPropertyShort());
                        assertEquals(bean.getPropertyBigDecimal(), bean_null.getPropertyBigDecimal());
                    } catch (DatabaseException e) {
                        fail(ExceptionUtils.getExceptionStackTrace(e));
                        throw e;
                    }

                    return fetcher;
                }
            });
        } finally {
            // remove the temporary table
            DropTable query_drop = new DropTable(datasource);
            query_drop.table(query_create.getTable());
            manager.executeUpdate(query_drop);
        }

        assertNotNull(fetcher.getBeanInstance());
    }
}
