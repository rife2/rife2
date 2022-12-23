/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
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

public class TestCountQuery {
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
    public void testCloneToStringAndClear(Datasource datasource) {
        setUp(datasource);
        try {
            var query = manager_.getCountQuery().where("testString", "=", "bean set 1");

            assertEquals(query.toString(), "SELECT count(*) FROM SimpleBean WHERE testString = 'bean set 1'");

            var query_clone = query.clone();

            assertEquals(query_clone.toString(), "SELECT count(*) FROM SimpleBean WHERE testString = 'bean set 1'");

            query_clone.where("testString", "!=", "bean set 2");

            assertEquals(query_clone.toString(), "SELECT count(*) FROM SimpleBean WHERE testString = 'bean set 1' AND testString != 'bean set 2'");

            query_clone.clear();

            assertEquals(query_clone.toString(), "SELECT count(*) FROM SimpleBean WHERE testString = 'bean set 1'");

            query.clear();

            assertEquals(query.toString(), "SELECT count(*) FROM SimpleBean");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetDatasource(Datasource datasource) {
        setUp(datasource);
        try {
            assertEquals(datasource, manager_.getCountQuery().getDatasource());
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
                .getCountQuery()
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

            var query = new CountQuery(select);

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
    public void testJoin(Datasource datasource) {
        setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();
            var bean4 = new SimpleBean();
            var bean5 = new SimpleBean();

            var linkbean1 = new LinkBean();
            var linkbean2 = new LinkBean();

            linkbean1.setTestString("linkbean 1");
            linkbean2.setTestString("linkbean 2");

            linkManager_.save(linkbean1);
            linkManager_.save(linkbean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(linkbean1.getId());
            bean2.setLinkBean(linkbean1.getId());
            bean3.setLinkBean(linkbean1.getId());
            bean4.setLinkBean(linkbean2.getId());
            bean5.setLinkBean(linkbean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getCountQuery()
                .join(table2)
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", linkbean2.getId());

            assertEquals(2, manager_.count(query));
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

            var linkbean1 = new LinkBean();
            var linkbean2 = new LinkBean();

            linkbean1.setTestString("linkbean 1");
            linkbean2.setTestString("linkbean 2");

            linkManager_.save(linkbean1);
            linkManager_.save(linkbean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(linkbean1.getId());
            bean2.setLinkBean(linkbean1.getId());
            bean3.setLinkBean(linkbean1.getId());
            bean4.setLinkBean(linkbean2.getId());
            bean5.setLinkBean(linkbean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getCountQuery()
                .joinCross(table2)
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", linkbean2.getId());

            try {
                assertEquals(2, manager_.count(query));

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

            var query = manager_.getCountQuery()
                .joinInner(table2, Select.ON, "0 = 0") // evals to true for mysql sake
                .where(table2 + ".id = " + table + ".linkBean")
                .whereAnd(table + ".linkBean", "=", link_bean2.getId());

            assertEquals(2, manager_.count(query));
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

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(1);
            bean2.setLinkBean(1);
            bean3.setLinkBean(1);
            bean4.setLinkBean(2);
            bean5.setLinkBean(2);

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getCountQuery()
                .joinOuter(table2, Select.LEFT, Select.ON, table2 + ".id = " + table + ".linkBean") // evals to true for mysql sake
                .where(table + ".linkBean = 2");

            assertEquals(2, manager_.count(query));
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

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(1);
            bean2.setLinkBean(1);
            bean3.setLinkBean(1);
            bean4.setLinkBean(2);
            bean5.setLinkBean(2);

            linkManager_.save(link_bean1);
            linkManager_.save(link_bean2);

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var table = manager_.getTable();
            var table2 = linkManager_.getTable();

            var query = manager_.getCountQuery()
                .joinCustom("LEFT OUTER JOIN " + table2 + " ON " + table2 + ".id = " + table + ".linkBean") // evals to true for mysql sake
                .where(table + ".linkBean = 2");

            assertEquals(2, manager_.count(query));
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

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyString = 'nostringhere'")));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyBoolean", "=", false)));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyByte", "=", (byte) 90)));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyChar", "=", 'w')));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyDouble", "=", 37478.34d)));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("ABS(propertyFloat - 34241.2) < 0.001")));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyLong", "=", 23432L)));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("propertyShort", "=", (short) 44)));

            // cheating because the GQM doesn't currently return any queries with a where clause already
            var query = new CountQuery(bigBeanManager_.getCountQuery().where("id", "=", bean1.getId()).getDelegate());

            assertEquals(1, bigBeanManager_.count(query.where("propertyString = 'nostringhere'")));

            assertEquals(1, bigBeanManager_.count(query.where("propertyBoolean", "=", false)));

            assertEquals(1, bigBeanManager_.count(query.where("propertyByte", "=", (byte) 90)));

            assertEquals(1, bigBeanManager_.count(query.where("propertyChar", "=", 'w')));

            assertEquals(1, bigBeanManager_.count(query.where("propertyDouble", "=", 37478.34d)));

            assertEquals(1, bigBeanManager_.count(query.where("ABS(propertyFloat - 34241.2) < 0.001")));

            assertEquals(1, bigBeanManager_.count(query.where("id", "=", bean1.getId()))); // primary key

            assertEquals(1, bigBeanManager_.count(query.where("propertyLong", "=", 23432L)));

            assertEquals(1, bigBeanManager_.count(query.where("propertyShort", "=", (short) 44)));
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

            assertEquals(1, bigBeanManager_.count(
                bigBeanManager_.getCountQuery()
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
            ));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("id", "=", bean1.getId()).whereAnd("ABS(propertyFloat - 34241.2) < 0.001")));
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

            assertEquals(1, bigBeanManager_.count(
                bigBeanManager_.getCountQuery()
                    .where("id", "=", bean1.getId())
                    .whereOr("propertyString = 'nostringhere'")
                    .whereOr("propertyBoolean", "=", false)
                    .whereOr("propertyByte", "=", (byte) 90)
                    .whereOr("propertyChar", "=", 'w')
                    .whereOr("propertyDouble", "=", 37478.34d)
                    .whereOr("propertyLong", "=", 23432L)
                    .whereOr("propertyIntegerObject", "=", 421)
                    .whereOr("propertyShort", "=", (short) 44)
                    .whereOr("propertyString", "=", "nostringhere")
            ));

            assertEquals(1, bigBeanManager_.count(bigBeanManager_.getCountQuery().where("id", "=", bean1.getId()).whereOr("ABS(propertyFloat - 34241.2) < 0.001")));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUnion(Datasource datasource) {
        setUp(datasource);
        try {
            var query = manager_.getCountQuery();

            query
                .union("uexpr1")
                .union(new Select(datasource).field("count(*)").from("table2"));


            assertEquals(query.getSql(), "SELECT count(*) FROM SimpleBean UNION uexpr1 UNION SELECT count(*) FROM table2");
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

            var linkbean1 = new LinkBean();
            var linkbean2 = new LinkBean();

            linkbean1.setTestString("linkbean 1");
            linkbean2.setTestString("linkbean 2");

            linkManager_.save(linkbean1);
            linkManager_.save(linkbean2);

            bean1.setTestString("bean set 1");
            bean2.setTestString("bean set 1");
            bean3.setTestString("bean set 1");
            bean4.setTestString("bean set 2");
            bean5.setTestString("bean set 2");

            bean1.setLinkBean(linkbean1.getId());
            bean2.setLinkBean(linkbean1.getId());
            bean3.setLinkBean(linkbean1.getId());
            bean4.setLinkBean(linkbean2.getId());
            bean5.setLinkBean(linkbean2.getId());

            manager_.save(bean1);
            manager_.save(bean2);
            manager_.save(bean3);
            manager_.save(bean4);
            manager_.save(bean5);

            var select = new Select(datasource);
            select
                .from(linkManager_.getTable())
                .field("id")
                .where("id", "=", linkbean1.getId());

            var query = manager_.getCountQuery();
            query
                .where("linkBean = (" + select.getSql() + ")")
                .whereSubselect(select);

            assertEquals(3, manager_.count(query));
        } finally {
            tearDown();
        }
    }
}

