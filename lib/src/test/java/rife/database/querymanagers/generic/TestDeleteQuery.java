/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.queries.Delete;
import rife.database.queries.Select;
import rife.database.querymanagers.generic.beans.BeanImpl;
import rife.database.querymanagers.generic.beans.LinkBean;
import rife.database.querymanagers.generic.beans.SimpleBean;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDeleteQuery {
    private GenericQueryManager<BeanImpl> bigBeanManager_ = null;
    private GenericQueryManager<SimpleBean> manager_ = null;
    private GenericQueryManager<LinkBean> linkManager_ = null;

    protected void setUp(Datasource datasource) {
        manager_ = GenericQueryManagerFactory.instance(datasource, SimpleBean.class);
        linkManager_ = GenericQueryManagerFactory.instance(datasource, LinkBean.class);
        bigBeanManager_ = GenericQueryManagerFactory.instance(datasource, BeanImpl.class);
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
            var query = bigBeanManager_.getDeleteQuery().where("propertyString", "=", "bean set 1");

            assertEquals(query.toString(), "DELETE FROM BeanImpl WHERE propertyString = 'bean set 1'");

            var query_clone = query.clone();

            assertEquals(query_clone.toString(), "DELETE FROM BeanImpl WHERE propertyString = 'bean set 1'");

            query_clone.where("propertyString", "!=", "bean set 2");

            assertEquals(query_clone.toString(), "DELETE FROM BeanImpl WHERE propertyString = 'bean set 1' AND propertyString != 'bean set 2'");

            query_clone.clear();

            assertEquals(query_clone.toString(), "DELETE FROM BeanImpl WHERE propertyString = 'bean set 1'");

            query.clear();

            assertEquals(query.toString(), "DELETE FROM BeanImpl");
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetParameters(Datasource datasource) {
        setUp(datasource);
        try {
            var delete = new Delete(datasource);
            delete
                .from("simplebean")
                .whereParameter("testString", "=");

            var query = new DeleteQuery(delete);

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
    public void testGetDatasource(Datasource datasource) {
        setUp(datasource);
        try {
            assertEquals(datasource, bigBeanManager_.getDeleteQuery().getDatasource());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetFrom(Datasource datasource) {
        setUp(datasource);
        try {
            assertEquals(bigBeanManager_
                .getDeleteQuery()
                .getFrom(), BeanImpl.class
                .getName()
                .replaceAll(BeanImpl.class
                                .getPackage()
                                .getName() + ".", ""));
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

            var bean1id = bigBeanManager_.save(bean1);
            bigBeanManager_.save(BeanImpl.getPopulatedBean());

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyString = 'nostringhere'"));
            var list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyBoolean", "=", false));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyByte", "=", (byte) 90));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyChar", "=", 'w'));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyDouble", "=", 37478.34d));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("ABS(propertyFloat - 34241.2) < 0.001"));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyLong", "=", 23432L));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("propertyShort", "=", (short) 44));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);

            var query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyString = 'nostringhere'"));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyBoolean", "=", false));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyByte", "=", (byte) 90));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyChar", "=", 'w'));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyDouble", "=", 37478.34d));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("ABS(propertyFloat - 34241.2) < 0.001"));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("id", "=", bean1id)); // primary key
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyLong", "=", 23432L));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
            bean1id = bigBeanManager_.save(bean1);
            query = new DeleteQuery(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).getDelegate());

            bigBeanManager_.delete(query.where("propertyShort", "=", (short) 44));
            list = bigBeanManager_.restore();
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

            var bean1id = bigBeanManager_.save(bean1);
            bigBeanManager_.save(BeanImpl.getPopulatedBean());

            bigBeanManager_.delete(
                bigBeanManager_.getDeleteQuery()
                    .where("id", "=", bean1id)
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

            var list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);

            bean1id = bigBeanManager_.save(bean1);
            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).whereAnd("ABS(propertyFloat - 34241.2) < 0.001"));
            list = bigBeanManager_.restore();
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

            var bean1id = bigBeanManager_.save(bean1);
            bigBeanManager_.save(BeanImpl.getPopulatedBean());

            bigBeanManager_.delete(
                bigBeanManager_.getDeleteQuery()
                    .where("id", "=", bean1id)
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

            var list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);

            bean1id = bigBeanManager_.save(bean1);

            bigBeanManager_.delete(bigBeanManager_.getDeleteQuery().where("id", "=", bean1id).whereOr("ABS(propertyFloat - 34241.2) < 0.001"));
            list = bigBeanManager_.restore();
            assertEquals(list.size(), 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testWhereSubselect(Datasource datasource) {
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

            var query = manager_.getDeleteQuery();
            query
                .where("linkBean = (" + select.getSql() + ")")
                .whereSubselect(select);

            manager_.delete(query);
            var list = manager_.restore();
            assertEquals(list.size(), 2);
        } finally {
            tearDown();
        }
    }
}
