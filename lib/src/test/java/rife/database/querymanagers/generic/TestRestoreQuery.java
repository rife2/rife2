/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.UnsupportedSqlFeatureException;
import rife.database.queries.Select;
import rife.database.querymanagers.generic.beans.BeanImpl;
import rife.database.querymanagers.generic.beans.LinkBean;
import rife.database.querymanagers.generic.beans.SimpleBean;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class TestRestoreQuery {
    private GenericQueryManager<SimpleBean> manager_ = null;
    private GenericQueryManager<LinkBean> linkManager_ = null;
    private GenericQueryManager<BeanImpl> bigBeanManager_ = null;

    protected void setUp(Datasource datasource) {
        manager_ = GenericQueryManagerFactory.getInstance(datasource, SimpleBean.class);
        linkManager_ = GenericQueryManagerFactory.getInstance(datasource, LinkBean.class);
        bigBeanManager_ = GenericQueryManagerFactory.getInstance(datasource, BeanImpl.class);
        manager_.install();
        linkManager_.install();
        bigBeanManager_.install();
    }

    protected void tearDown() {
        manager_.remove();
        linkManager_.remove();
        bigBeanManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testLimit(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);

            var list = manager_.restore(manager_.getRestoreQuery().limit(2));

            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testOffset(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);

            var list = manager_.restore(manager_.getRestoreQuery().limit(1).offset(1).orderBy("id"));

            assertEquals(list.size(), 1);

            assertEquals("This is bean2", list.get(0).getTestString());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testCloneToStringAndClear(Datasource datasource) {
        setUp(datasource);
        try {
            var query = manager_.getRestoreQuery().where("testString", "=", "bean set 1");

            assertEquals(query.toString(), "SELECT * FROM SimpleBean WHERE testString = 'bean set 1'");

            var queryclone = query.clone();

            assertEquals(queryclone.toString(), "SELECT * FROM SimpleBean WHERE testString = 'bean set 1'");

            queryclone.where("testString", "!=", "bean set 2");

            assertEquals(queryclone.toString(), "SELECT * FROM SimpleBean WHERE testString = 'bean set 1' AND testString != 'bean set 2'");

            queryclone.clear();

            assertEquals(queryclone.toString(), "SELECT * FROM SimpleBean WHERE testString = 'bean set 1'");

            query.clear();

            assertEquals(query.toString(), "SELECT * FROM SimpleBean");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDistinctOn(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var query = manager_.getRestoreQuery().distinctOn("testString");

            try {
                var bean_list = manager_.restore(query);

                assertEquals(bean_list.size(), 2);

                if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver()) ||
                    "oracle.jdbc.driver.OracleDriver".equals(datasource.getAliasedDriver()) ||
                    "org.hsqldb.jdbcDriver".equals(datasource.getAliasedDriver()) ||
                    "org.h2.Driver".equals(datasource.getAliasedDriver()) ||
                    "org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    fail();
                }
            } catch (UnsupportedSqlFeatureException e) {
                assertEquals("DISTINCT ON", e.getFeature());
                assertTrue("com.mysql.cj.jdbc.Driver".equals(e.getDriver()) ||
                           "oracle.jdbc.driver.OracleDriver".equals(e.getDriver()) ||
                           "org.hsqldb.jdbcDriver".equals(e.getDriver()) ||
                           "org.h2.Driver".equals(e.getDriver()) ||
                           "org.apache.derby.jdbc.EmbeddedDriver".equals(e.getDriver()));
            }

            query.clear();

            query = manager_.getRestoreQuery().distinctOn(new String[]{"testString"});

            try {
                var bean_list = manager_.restore(query);

                assertEquals(bean_list.size(), 2);

                if ("com.mysql.cj.jdbc.Driver".equals(datasource.getAliasedDriver()) ||
                    "oracle.jdbc.driver.OracleDriver".equals(datasource.getAliasedDriver()) ||
                    "org.hsqldb.jdbcDriver".equals(datasource.getAliasedDriver()) ||
                    "org.h2.Driver".equals(datasource.getAliasedDriver()) ||
                    "org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    fail();
                }
            } catch (UnsupportedSqlFeatureException e) {
                assertEquals("DISTINCT ON", e.getFeature());
                assertTrue("com.mysql.cj.jdbc.Driver".equals(e.getDriver()) ||
                           "oracle.jdbc.driver.OracleDriver".equals(e.getDriver()) ||
                           "org.hsqldb.jdbcDriver".equals(e.getDriver()) ||
                           "org.h2.Driver".equals(e.getDriver()) ||
                           "org.apache.derby.jdbc.EmbeddedDriver".equals(e.getDriver()));
            }
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetDatasource(Datasource datasource) {
        setUp(datasource);
        try {
            assertEquals(datasource, manager_.getRestoreQuery().getDatasource());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetFrom(Datasource datasource) {
        setUp(datasource);
        try {
            assertEquals(manager_
                .getRestoreQuery()
                .getFrom(), SimpleBean.class
                .getName()
                .replaceAll(SimpleBean.class
                                .getPackage()
                                .getName() + ".", ""));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetParameters(Datasource datasource) {
        setUp(datasource);
        try {
            var select = new Select(datasource);
            select
                .from("simplebean")
                .whereParameter("testString", "=");

            var query = new RestoreQuery(select);

            assertEquals(query.getParameters().getOrderedNames().size(), 1);
            assertTrue(query.getParameters().getOrderedNames().contains("testString"));

            assertEquals(query.getParameters().getOrderedNamesArray().length, 1);
            assertEquals(query.getParameters().getOrderedNamesArray()[0], "testString");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreFirst(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var query = manager_.getRestoreQuery();
            query
                .where("testString", "=", "bean set 1")
                .orderBy("id", Select.ASC);

            var new_bean = manager_.restoreFirst(query);

            assertNotSame(new_bean, bean1);
            assertEquals(new_bean.getTestString(), bean1.getTestString());
            assertEquals(new_bean.getId(), bean1.getId());

            query.clear();
            query
                .where("testString", "=", "bean set 2")
                .orderBy("id", Select.DESC);

            var other_bean = manager_.restoreFirst(query);

            assertNotSame(other_bean, bean5);
            assertEquals(other_bean.getTestString(), bean5.getTestString());
            assertEquals(other_bean.getId(), bean5.getId());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testJoin(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getRestoreQuery()
                .join(table2)
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            var list = manager_.restore(query);

            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testJoinCross(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getRestoreQuery()
                .joinCross(table2)
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            try {
                var list = manager_.restore(query);

                assertEquals(list.size(), 2);

                if ("org.hsqldb.jdbcDriver".equals(datasource.getAliasedDriver()) ||
                    "org.apache.derby.jdbc.EmbeddedDriver".equals(datasource.getAliasedDriver())) {
                    fail();
                }
            } catch (UnsupportedSqlFeatureException e) {
                assertEquals("CROSS JOIN", e.getFeature());
                assertTrue("org.hsqldb.jdbcDriver".equals(e.getDriver()) ||
                           "org.apache.derby.jdbc.EmbeddedDriver".equals(e.getDriver()));
            }
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testJoinInner(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getRestoreQuery()
                .joinInner(table2, Select.ON, "0 = 0") // evals to true for mysql sake
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            var list = manager_.restore(query);

            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testJoinOuter(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getRestoreQuery()
                .joinOuter(table2, Select.LEFT, Select.ON, "0 = 0") // evals to true for mysql sake
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            var list = manager_.restore(query);

            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testJoinCustom(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getRestoreQuery()
                .joinCustom("LEFT OUTER JOIN " + table2 + " ON 0 = 0") // evals to true for mysql sake
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            var list = manager_.restore(query);

            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testOrderBy(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);

            var list = manager_.restore(manager_.getRestoreQuery().orderBy("id", Select.DESC));

            assertEquals(list.size(), 3);

            assertEquals(list.get(0).getId(), bean3.getId());
            assertEquals(list.get(1).getId(), bean2.getId());
            assertEquals(list.get(2).getId(), bean1.getId());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhere(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new BeanImpl();

            var cal = Calendar.getInstance();
            cal.set(2004, Calendar.JULY, 19, 16, 27, 15);
            cal.set(Calendar.MILLISECOND, 765);
            bean1.setPropertyBigDecimal(new BigDecimal("384834838434.38483"));
            bean1.setPropertyBoolean(false);
            bean1.setPropertyBooleanObject(true);
            bean1.setPropertyByte((byte) 90);
            bean1.setPropertyByteObject((byte) 35);
            bean1.setPropertyCalendar(cal);
            bean1.setPropertyChar('w');
            bean1.setPropertyCharacterObject('s');
            bean1.setPropertyDate(cal.getTime());
            bean1.setPropertyDouble(37478.34d);
            bean1.setPropertyDoubleObject(384724.692d);
            bean1.setPropertyFloat(34241.2f);
            bean1.setPropertyFloatObject(3432.7f);
            bean1.setPropertyLong(23432L);
            bean1.setPropertyLongObject(23423L);
            bean1.setPropertyShort((short) 44);
            bean1.setPropertyShortObject((short) 69);
            bean1.setPropertyIntegerObject(421);
            bean1.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
            bean1.setPropertyString("nostringhere");
            bean1.setPropertyStringBuffer(new StringBuffer("buffbuffbuff"));
            bean1.setPropertyTime(new Time(cal.getTime().getTime()));
            bean1.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));

            bigBeanManager_.save(bean1);

            var list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyString = 'nostringhere'"));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyBoolean", "=", false));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyByte", "=", (byte) 90));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyChar", "=", 'w'));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyDouble", "=", 37478.34d));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("ABS(propertyFloat - 34241.2) < 0.001"));

            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyLong", "=", 23432L));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("propertyShort", "=", (short) 44));
            assertEquals(list.size(), 1);

            // cheating because the GQM doesn't currently return any queries with a where clause already
            var query = new RestoreQuery(bigBeanManager_.getRestoreQuery().where("id", "=", bean1.getId()).getDelegate());

            list = bigBeanManager_.restore(query.where("propertyString = 'nostringhere'"));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyBoolean", "=", false));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyByte", "=", (byte) 90));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyChar", "=", 'w'));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyDouble", "=", 37478.34d));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("ABS(propertyFloat - 34241.2) < 0.001"));

            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("id", "=", bean1.getId())); // primary key
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyLong", "=", 23432L));
            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(query.where("propertyShort", "=", (short) 44));
            assertEquals(list.size(), 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhereAnd(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new BeanImpl();

            var cal = Calendar.getInstance();
            cal.set(2004, Calendar.JULY, 19, 16, 27, 15);
            cal.set(Calendar.MILLISECOND, 765);
            bean1.setPropertyBigDecimal(new BigDecimal("384834838434.38483"));
            bean1.setPropertyBoolean(false);
            bean1.setPropertyBooleanObject(true);
            bean1.setPropertyByte((byte) 90);
            bean1.setPropertyByteObject((byte) 35);
            bean1.setPropertyCalendar(cal);
            bean1.setPropertyChar('w');
            bean1.setPropertyCharacterObject('s');
            bean1.setPropertyDate(cal.getTime());
            bean1.setPropertyDouble(37478.34d);
            bean1.setPropertyDoubleObject(384724.692d);
            bean1.setPropertyFloat(34241.2f);
            bean1.setPropertyFloatObject(3432.7f);
            bean1.setPropertyLong(23432L);
            bean1.setPropertyLongObject(23423L);
            bean1.setPropertyShort((short) 44);
            bean1.setPropertyShortObject((short) 69);
            bean1.setPropertyIntegerObject(421);
            bean1.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
            bean1.setPropertyString("nostringhere");
            bean1.setPropertyStringBuffer(new StringBuffer("buffbuffbuff"));
            bean1.setPropertyTime(new Time(cal.getTime().getTime()));
            bean1.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));

            bigBeanManager_.save(bean1);

            var list = bigBeanManager_.restore(
                bigBeanManager_.getRestoreQuery()
                    .where("id", "=", bean1.getId())
                    .whereAnd("propertyString = 'nostringhere'")
                    .whereAnd("propertyBoolean", "=", false)
                    .whereAnd("propertyByte", "=", (byte) 90)
                    .whereAnd("propertyChar", "=", 'w')
                    .whereAnd("propertyDouble", "=", 37478.34d)
                    .whereAnd("propertyLong", "=", 23432L)
                    .whereAnd("propertyString", "=", "nostringhere")
                    .whereAnd("propertyIntegerObject", "=", 421)
                    .whereAnd("propertyShort", "=", (short) 44)
            );

            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("id", "=", bean1.getId()).whereAnd("ABS(propertyFloat - 34241.2) < 0.001"));

            assertEquals(list.size(), 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhereOr(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new BeanImpl();

            var cal = Calendar.getInstance();
            cal.set(2004, Calendar.JULY, 19, 16, 27, 15);
            cal.set(Calendar.MILLISECOND, 765);
            bean1.setPropertyBigDecimal(new BigDecimal("384834838434.38483"));
            bean1.setPropertyBoolean(false);
            bean1.setPropertyBooleanObject(true);
            bean1.setPropertyByte((byte) 90);
            bean1.setPropertyByteObject((byte) 35);
            bean1.setPropertyCalendar(cal);
            bean1.setPropertyChar('w');
            bean1.setPropertyCharacterObject('s');
            bean1.setPropertyDate(cal.getTime());
            bean1.setPropertyDouble(37478.34d);
            bean1.setPropertyDoubleObject(384724.692d);
            bean1.setPropertyFloat(34241.2f);
            bean1.setPropertyFloatObject(3432.7f);
            bean1.setPropertyLong(23432L);
            bean1.setPropertyLongObject(23423L);
            bean1.setPropertyShort((short) 44);
            bean1.setPropertyShortObject((short) 69);
            bean1.setPropertyIntegerObject(421);
            bean1.setPropertySqlDate(new java.sql.Date(cal.getTime().getTime()));
            bean1.setPropertyString("nostringhere");
            bean1.setPropertyStringBuffer(new StringBuffer("buffbuffbuff"));
            bean1.setPropertyTime(new Time(cal.getTime().getTime()));
            bean1.setPropertyTimestamp(new Timestamp(cal.getTime().getTime()));

            bigBeanManager_.save(bean1);

            var list = bigBeanManager_.restore(
                bigBeanManager_.getRestoreQuery()
                    .where("id = 1")
                    .whereOr("propertyString = 'nostringhere'")
                    .whereOr("propertyBoolean", "=", false)
                    .whereOr("propertyByte", "=", (byte) 90)
                    .whereOr("propertyChar", "=", 'w')
                    .whereOr("propertyDouble", "=", 37478.34d)
                    .whereOr("propertyLong", "=", 23432L)
                    .whereOr("propertyIntegerObject", "=", 421)
                    .whereOr("propertyShort", "=", (short) 44)
                    .whereOr("propertyString", "=", "nostringhere")
            );

            assertEquals(list.size(), 1);

            list = bigBeanManager_.restore(bigBeanManager_.getRestoreQuery().where("id = 1").whereOr("ABS(propertyFloat - 34241.2) < 0.001"));

            assertEquals(list.size(), 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUnion(Datasource datasource) {
        setUp(datasource);
        try {
            var query = manager_.getRestoreQuery();

            query
                .union("uexpr1")
                .union(new Select(datasource).from("table2"));

            assertEquals(query.getSql(), "SELECT * FROM SimpleBean UNION uexpr1 UNION SELECT * FROM table2");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhereSubSelect(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var link_bean1 = new LinkBean();
            var link_bean2 = new LinkBean();

            link_bean1.setTestString("linkbean 1");
            link_bean2.setTestString("linkbean 2");

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(link_bean1.getId());
            bean2.setLinkBean(link_bean1.getId());
            bean3.setLinkBean(link_bean1.getId());
            bean4.setLinkBean(link_bean2.getId());
            bean5.setLinkBean(link_bean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var select = new Select(datasource);
            select
                .from(linkManager_.getTable())
                .field("id")
                .where("id", "=", link_bean1.getId());

            var query = manager_.getRestoreQuery();
            query
                .where("linkBean = (" + select.getSql() + ")")
                .whereSubselect(select);

            var list = manager_.restore(query);
            assertEquals(list.size(), 3);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testFields(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();

            var link_bean1 = new LinkBean();

            link_bean1.setTestString("linkbean 1");

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");

            bean1.setLinkBean(1);
            bean2.setLinkBean(1);

            linkManager_.save(link_bean1);

            manager_.save(bean1);
            manager_.save(bean2);

            var query = manager_.getRestoreQuery();
            query
                .field("testString");

            var bean_list = manager_.restore(query);

            for (var bean : bean_list) {
                assertEquals(-1, bean.getLinkBean());
                assertEquals("bean set 1", bean.getTestString());
            }

            query = manager_.getRestoreQuery();
            query
                .fields(SimpleBean.class);

            bean_list = manager_.restore(query);

            query = manager_.getRestoreQuery();
            query
                .fields("simplebean", SimpleBean.class);

            bean_list = manager_.restore(query);

            for (var bean : bean_list) {
                assertEquals(1, bean.getLinkBean());
                assertEquals("bean set 1", bean.getTestString());
            }

            query = manager_.getRestoreQuery();
            query
                .fieldsExcluded(SimpleBean.class, "testString");

            bean_list = manager_.restore(query);

            query = manager_.getRestoreQuery();
            query
                .fieldsExcluded("simplebean", SimpleBean.class, "testString");

            bean_list = manager_.restore(query);

            for (var bean : bean_list) {
                assertEquals(1, bean.getLinkBean());
                assertNull(bean.getTestString());
            }

            query = manager_.getRestoreQuery();
            query
                .fields("linkBean");

            bean_list = manager_.restore(query);

            for (var bean : bean_list) {
                assertEquals(1, bean.getLinkBean());
                assertNull(bean.getTestString());
            }

            var select = new Select(datasource).field("name").from("tablename");

            query = manager_.getRestoreQuery();
            query
                .fieldSubselect(select)
                .field('(' + select.getSql() + ')');

            assertEquals("SELECT (SELECT name FROM tablename) FROM SimpleBean", query.getSql());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhereGroupChaining(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            bean1.setTestString("test");

            manager_.save(bean1);

            var query = manager_.getRestoreQuery()
                .where("id", ">=", 0)
                .startWhereAnd()
                .where("testString", "=", "test")
                .end();

            assertTrue(manager_.restore(query).size() > 0);
        } finally {
            tearDown();
        }
    }
}


