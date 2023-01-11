/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.*;
import rife.database.querymanagers.generic.beans.ChildBean;
import rife.database.querymanagers.generic.beans.ConstrainedBean;
import rife.database.querymanagers.generic.beans.LinkBean;
import rife.database.querymanagers.generic.beans.SimpleBean;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerDelegate {
    static class GQMSimpleBean extends GenericQueryManagerDelegate<SimpleBean> {
        GQMSimpleBean(Datasource datasource) {
            super(datasource, SimpleBean.class);
        }
    }

    static class GQMLinkBean extends GenericQueryManagerDelegate<LinkBean> {
        GQMLinkBean(Datasource datasource) {
            super(datasource, LinkBean.class);
        }
    }

    static class GQMChildBean extends GenericQueryManagerDelegate<ChildBean> {
        GQMChildBean(Datasource datasource) {
            super(datasource, ChildBean.class);
        }
    }

    static class GQMConstrainedBean extends GenericQueryManagerDelegate<ConstrainedBean> {
        GQMConstrainedBean(Datasource datasource) {
            super(datasource, ConstrainedBean.class);
        }
    }

    private GQMSimpleBean manager_ = null;
    private GQMLinkBean linkManager_ = null;
    private GQMChildBean childManager_ = null;
    private GQMConstrainedBean constrainedManager_ = null;

    protected void setup(Datasource datasource) {
        manager_ = new GQMSimpleBean(datasource);
        linkManager_ = new GQMLinkBean(datasource);
        childManager_ = new GQMChildBean(datasource);
        constrainedManager_ = new GQMConstrainedBean(datasource);

        manager_.install();
        linkManager_.install();
        childManager_.install();
        constrainedManager_.install();
    }

    protected void tearDown() {
        constrainedManager_.remove();
        manager_.remove();
        linkManager_.remove();
        childManager_.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveRestore(Datasource datasource) {
        setup(datasource);
        try {
            var bean = new SimpleBean();
            SimpleBean newbean = null;

            bean.setTestString("This is my test string");

            var id = manager_.save(bean);

            newbean = manager_.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);
            assertEquals(newbean.getTestString(), bean.getTestString());
            assertEquals(newbean.getId(), id);

            bean.setId(id);
            bean.setTestString("This is a new test string");

            assertEquals(manager_.save(bean), id);
            assertEquals(bean.getId(), id);

            newbean = manager_.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);

            assertEquals(newbean.getTestString(), "This is a new test string");

            bean.setId(999999);
            bean.setTestString("This is another test string");

            assertNotEquals(999999, manager_.save(bean));
            assertEquals(bean.getId(), id + 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSaveRestoreConstrained(Datasource datasource) {
        setup(datasource);
        try {
            var bean = new ConstrainedBean();
            ConstrainedBean newbean = null;

            bean.setTestString("This is my test string");

            var id = constrainedManager_.save(bean);

            newbean = constrainedManager_.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);
            assertEquals(newbean.getTestString(), bean.getTestString());
            assertEquals(newbean.getIdentifier(), id);

            bean.setIdentifier(id);
            bean.setTestString("This is a new test string");

            assertEquals(constrainedManager_.save(bean), id);
            assertEquals(bean.getIdentifier(), id);

            newbean = constrainedManager_.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);

            assertEquals(newbean.getTestString(), "This is a new test string");

            bean.setIdentifier(999999);
            bean.setTestString("This is another test string");

            assertNotEquals(999999, constrainedManager_.save(bean));
            assertEquals(bean.getIdentifier(), id + 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testDelete(Datasource datasource) {
        setup(datasource);
        try {
            var bean = new SimpleBean();

            bean.setTestString("This is my test string");

            var id1 = manager_.save(bean);
            assertNotNull(manager_.restore(id1));
            manager_.delete(id1);
            assertNull(manager_.restoreFirst(manager_.getRestoreQuery(id1)));

            var id2 = manager_.save(bean);
            assertNotNull(manager_.restoreFirst(manager_.getRestoreQuery(id2)));
            manager_.delete(manager_.getDeleteQuery(id2));
            assertNull(manager_.restore(id2));
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestore(Datasource datasource) {
        setup(datasource);
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

            var list = manager_.restore();

            assertEquals(list.size(), 3);

            for (var bean : list) {
                assertNotNull(bean);
                assertTrue(bean != bean1 || bean != bean2 || bean != bean3);
                assertTrue(
                    bean.getTestString().equals("This is bean1") ||
                    bean.getTestString().equals("This is bean2") ||
                    bean.getTestString().equals("This is bean3"));
            }
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreRowProcessor(Datasource datasource) {
        setup(datasource);
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

            final var count = new int[]{0};
            manager_.restore(new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    count[0]++;

                    var test_string = resultSet.getString("testString");
                    assertTrue(
                        test_string.equals("This is bean1") ||
                        test_string.equals("This is bean2") ||
                        test_string.equals("This is bean3"));

                    return true;
                }
            });

            assertEquals(count[0], 3);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRestoreQueryRowProcessor(Datasource datasource) {
        setup(datasource);
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

            final var count = new int[]{0};
            manager_.restore(manager_.getRestoreQuery().where("testString", "LIKE", "%bean2"), new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    count[0]++;

                    var test_string = resultSet.getString("testString");
                    assertEquals("This is bean2", test_string);

                    return true;
                }
            });

            assertEquals(count[0], 1);
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testChildBean(Datasource datasource) {
        setup(datasource);
        try {
            var bean = new ChildBean();

            bean.setParentString("This is bean");
            bean.setChildString("This is childbean");

            var id = childManager_.save(bean);

            var rbean = childManager_.restore(id);

            assertEquals(rbean.getParentString(), bean.getParentString());
            assertEquals(rbean.getChildString(), bean.getChildString());
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testCount(Datasource datasource) {
        setup(datasource);
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

            assertEquals(manager_.count(), 5);

            manager_.delete(bean1.getId());
            manager_.delete(bean2.getId());
            manager_.delete(bean3.getId());

            assertEquals(manager_.count(), 2);
        } finally {
            tearDown();
        }
    }
}

