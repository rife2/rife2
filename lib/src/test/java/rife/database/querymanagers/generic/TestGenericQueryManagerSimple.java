/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.*;
import rife.database.exceptions.DatabaseException;
import rife.database.querymanagers.generic.beans.*;
import rife.database.querymanagers.generic.exceptions.MissingDefaultConstructorException;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerSimple {
    protected GenericQueryManager<SimpleBean> setUp(Datasource datasource) {
        var manager = GenericQueryManagerFactory.instance(datasource, SimpleBean.class);
        manager.install();
        return manager;
    }

    protected void tearDown(GenericQueryManager<SimpleBean> manager) {
        manager.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testNoDefaultConstructor(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            GenericQueryManagerFactory.instance(datasource, NoDefaultConstructorBean.class);
            fail("MissingDefaultConstructorException exception wasn't thrown");
        } catch (MissingDefaultConstructorException e) {
            assertSame(e.getBeanClass(), NoDefaultConstructorBean.class);
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClass(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            assertSame(SimpleBean.class, manager.getBaseClass());
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQuery(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            manager.remove();
            manager.install(manager.getInstallTableQuery());
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveRestore(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean = new SimpleBean();
            SimpleBean newbean = null;

            var uuid1 = UUID.randomUUID();
            bean.setTestString("This is my test string");
            bean.setUuid(uuid1);
            bean.setEnum(SomeEnum.VALUE_TWO);

            var id = manager.save(bean);

            newbean = manager.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);
            assertEquals(newbean.getTestString(), bean.getTestString());
            assertEquals(newbean.getId(), id);
            assertEquals(newbean.getUuid(), uuid1);
            assertEquals(newbean.getEnum(), SomeEnum.VALUE_TWO);

            var uuid2 = UUID.randomUUID();
            bean.setId(id);
            bean.setTestString("This is a new test string");
            bean.setUuid(uuid2);
            bean.setEnum(SomeEnum.VALUE_THREE);

            assertEquals(manager.save(bean), id);
            assertEquals(bean.getId(), id);

            newbean = manager.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);

            assertEquals(newbean.getTestString(), "This is a new test string");
            assertEquals(newbean.getUuid(), uuid2);
            assertEquals(newbean.getEnum(), SomeEnum.VALUE_THREE);

            bean.setId(999999);
            bean.setTestString("This is another test string");

            assertNotEquals(999999, manager.save(bean));
            assertEquals(bean.getId(), id + 1);

            var manager_othertable = GenericQueryManagerFactory.instance(datasource, SimpleBean.class, "othertable");
            manager_othertable.install();

            var bean2 = new SimpleBean();
            bean2.setTestString("test");

            manager_othertable.save(bean2);

            var bean3 = manager_othertable.restore(bean2.getId());

            assertEquals(bean3.getTestString(), bean2.getTestString());

            manager_othertable.remove();
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSparseIdentifier(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var manager_sparsebean = GenericQueryManagerFactory.instance(datasource, SparseBean.class);
            var sparse_bean = new SparseBean();

            manager_sparsebean.install();

            sparse_bean.setId(1000);
            sparse_bean.setTestString("Test String");
            assertEquals(1000, manager_sparsebean.save(sparse_bean));

            var restored_sparsebean = manager_sparsebean.restore(1000);
            assertEquals(restored_sparsebean.getId(), 1000);
            assertEquals(restored_sparsebean.getTestString(), "Test String");

            try {
                manager_sparsebean.insert(sparse_bean);
                fail();
            } catch (DatabaseException e) {
                assertTrue(true);
            }
            assertEquals(1000, manager_sparsebean.update(sparse_bean));

            sparse_bean.setId(1001);
            assertEquals(-1, manager_sparsebean.update(sparse_bean));    // not there; update should fail

            manager_sparsebean.remove();
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDelete(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean = new SimpleBean();

            bean.setTestString("This is my test string");

            var id1 = manager.save(bean);
            assertNotNull(manager.restore(id1));
            manager.delete(id1);
            assertNull(manager.restoreFirst(manager.getRestoreQuery(id1)));

            var id2 = manager.save(bean);
            assertNotNull(manager.restoreFirst(manager.getRestoreQuery(id2)));
            manager.delete(manager.getDeleteQuery(id2));
            assertNull(manager.restore(id2));
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestore(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            var uuid1 = UUID.randomUUID();
            var uuid2 = UUID.randomUUID();
            var uuid3 = UUID.randomUUID();

            bean1.setTestString("This is bean1");
            bean1.setUuid(uuid1);
            bean1.setEnum(SomeEnum.VALUE_ONE);
            bean2.setTestString("This is bean2");
            bean2.setUuid(uuid2);
            bean2.setEnum(SomeEnum.VALUE_TWO);
            bean3.setTestString("This is bean3");
            bean3.setUuid(uuid3);
            bean3.setEnum(SomeEnum.VALUE_THREE);

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            var list = manager.restore();

            assertEquals(list.size(), 3);

            for (var bean : list) {
                assertNotNull(bean);
                assertTrue(bean != bean1 || bean != bean2 || bean != bean3);
                assertTrue(
                    bean.getTestString().equals("This is bean1") &&
                    bean.getUuid().equals(uuid1) &&
                    bean.getEnum().equals(SomeEnum.VALUE_ONE) ||

                    bean.getTestString().equals("This is bean2") &&
                    bean.getUuid().equals(uuid2) &&
                    bean.getEnum().equals(SomeEnum.VALUE_TWO) ||

                    bean.getTestString().equals("This is bean3") &&
                    bean.getUuid().equals(uuid3) &&
                    bean.getEnum().equals(SomeEnum.VALUE_THREE));
            }
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreRowProcessor(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            final var uuid1 = UUID.randomUUID();
            final var uuid2 = UUID.randomUUID();
            final var uuid3 = UUID.randomUUID();

            bean1.setTestString("This is bean1");
            bean1.setUuid(uuid1);
            bean1.setEnum(SomeEnum.VALUE_ONE);
            bean2.setTestString("This is bean2");
            bean2.setUuid(uuid2);
            bean2.setEnum(SomeEnum.VALUE_TWO);
            bean3.setTestString("This is bean3");
            bean3.setUuid(uuid3);
            bean3.setEnum(SomeEnum.VALUE_THREE);

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            final var count = new int[]{0};
            manager.restore(new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    count[0]++;

                    var test_string = resultSet.getString("testString");
                    assertTrue(
                        test_string.equals("This is bean1") ||
                        test_string.equals("This is bean2") ||
                        test_string.equals("This is bean3"));

                    var uuid = resultSet.getString("uuid");
                    assertTrue(
                        uuid.equals(uuid1.toString()) ||
                        uuid.equals(uuid2.toString()) ||
                        uuid.equals(uuid3.toString()));

                    return true;
                }
            });

            assertEquals(count[0], 3);
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreQueryRowProcessor(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            final var uuid1 = UUID.randomUUID();
            final var uuid2 = UUID.randomUUID();
            final var uuid3 = UUID.randomUUID();

            bean1.setTestString("This is bean1");
            bean1.setUuid(uuid1);
            bean2.setTestString("This is bean2");
            bean2.setUuid(uuid2);
            bean3.setTestString("This is bean3");
            bean3.setUuid(uuid3);

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            final var count = new int[]{0};
            manager.restore(manager.getRestoreQuery().where("testString", "LIKE", "%bean2"), new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    count[0]++;

                    var test_string = resultSet.getString("testString");
                    assertEquals("This is bean2", test_string);

                    var uuid = resultSet.getString("uuid");
                    assertEquals(uuid, uuid2.toString());

                    return true;
                }
            });

            assertEquals(count[0], 1);
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreBeanFetcher(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            final var uuid1 = UUID.randomUUID();
            final var uuid2 = UUID.randomUUID();
            final var uuid3 = UUID.randomUUID();

            bean1.setTestString("This is bean1");
            bean1.setUuid(uuid1);
            bean1.setEnum(SomeEnum.VALUE_ONE);
            bean2.setTestString("This is bean2");
            bean2.setUuid(uuid2);
            bean2.setEnum(SomeEnum.VALUE_TWO);
            bean3.setTestString("This is bean3");
            bean3.setUuid(uuid3);
            bean3.setEnum(SomeEnum.VALUE_THREE);

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            final var count = new int[]{0};
            manager.restore(bean -> {
                count[0]++;

                assertTrue(
                    bean.getTestString().equals("This is bean1") ||
                    bean.getTestString().equals("This is bean2") ||
                    bean.getTestString().equals("This is bean3"));

                assertTrue(
                    bean.getUuid().equals(uuid1) ||
                    bean.getUuid().equals(uuid2) ||
                    bean.getUuid().equals(uuid3));
            });

            assertEquals(count[0], 3);
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreQueryBeanFetcher(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean1 = new SimpleBean();
            var bean2 = new SimpleBean();
            var bean3 = new SimpleBean();

            final var uuid1 = UUID.randomUUID();
            final var uuid2 = UUID.randomUUID();
            final var uuid3 = UUID.randomUUID();

            bean1.setTestString("This is bean1");
            bean1.setUuid(uuid1);
            bean2.setTestString("This is bean2");
            bean2.setUuid(uuid2);
            bean3.setTestString("This is bean3");
            bean3.setUuid(uuid3);

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            final var count = new int[]{0};
            manager.restore(manager.getRestoreQuery().where("testString", "LIKE", "%bean2"), bean -> {
                count[0]++;
                assertEquals("This is bean2", bean.getTestString());
                assertEquals(uuid2, bean.getUuid());
            });

            assertEquals(count[0], 1);
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testCount(Datasource datasource) {
        var manager = setUp(datasource);
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

            manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);
            manager.save(bean4);
            manager.save(bean5);

            assertEquals(manager.count(), 5);

            manager.delete(bean1.getId());
            manager.delete(bean2.getId());
            manager.delete(bean3.getId());

            assertEquals(manager.count(), 2);
        } finally {
            tearDown(manager);
        }
    }
}
