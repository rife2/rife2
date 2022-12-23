/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.beans.*;
import rife.tools.StringUtils;

import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerCallbacks {
    protected GenericQueryManager<CallbacksBean> setUpCallbacksBeanManager(Datasource datasource) {
        var manager = GenericQueryManagerFactory.getInstance(datasource, CallbacksBean.class);
        try {
            var listener = new AggregatingCallbacksBeanListener();
            manager.addListener(listener);
            manager.install();
            assertEquals(1, listener.getHistory().size());
            assertEquals("installed0", listener.getHistory().entrySet().iterator().next().getKey());
        } finally {
            manager.removeListeners();
        }
        return manager;
    }

    protected void tearDownCallbacksBean(GenericQueryManager<CallbacksBean> manager) {
        try {
            var listener = new AggregatingCallbacksBeanListener();
            manager.addListener(listener);
            manager.remove();
            assertEquals(1, listener.getHistory().size());
            assertEquals("removed0", listener.getHistory().entrySet().iterator().next().getKey());
        } finally {
            manager.removeListeners();
        }
    }

    protected GenericQueryManager<CallbacksSparseBean> setUpCallbacksSparseBeanManager(Datasource datasource) {
        var manager = GenericQueryManagerFactory.getInstance(datasource, CallbacksSparseBean.class);
        manager.install();
        return manager;
    }

    protected void tearDownCallbacksSparseBean(GenericQueryManager<CallbacksSparseBean> manager) {
        manager.remove();
    }

    protected GenericQueryManager<CallbacksProviderBean> setUpCallbacksProviderBeanManager(Datasource datasource) {
        var manager = GenericQueryManagerFactory.getInstance(datasource, CallbacksProviderBean.class);
        manager.install();
        return manager;
    }

    protected void tearDownCallbacksProviderBean(GenericQueryManager<CallbacksProviderBean> manager) {
        manager.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClass(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            assertSame(CallbacksBean.class, manager.getBaseClass());
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClassSparse(Datasource datasource) {
        var manager = setUpCallbacksSparseBeanManager(datasource);
        try {
            assertSame(CallbacksSparseBean.class, manager.getBaseClass());
        } finally {
            tearDownCallbacksSparseBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClassProvider(Datasource datasource) {
        var manager = setUpCallbacksProviderBeanManager(datasource);
        try {
            assertSame(CallbacksProviderBean.class, manager.getBaseClass());
        } finally {
            tearDownCallbacksProviderBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQuery(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            manager.remove();
            manager.install(manager.getInstallTableQuery());
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQuerySparse(Datasource datasource) {
        var manager = setUpCallbacksSparseBeanManager(datasource);
        try {
            manager.remove();
            manager.install(manager.getInstallTableQuery());
        } finally {
            tearDownCallbacksSparseBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstallCustomQueryProvider(Datasource datasource) {
        var manager = setUpCallbacksProviderBeanManager(datasource);
        try {
            manager.remove();
            manager.install(manager.getInstallTableQuery());
        } finally {
            tearDownCallbacksProviderBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidateCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            manager.validate(bean);
            assertEquals("beforeValidate -1;This is my test string\n" +
                         "afterValidate -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            manager.validate(bean);
            assertEquals("beforeValidate -1;This is a new test string\n" +
                         "afterValidate -1;This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setId(999999);
            bean.setTestString("This is another test string");

            manager.validate(bean);
            assertEquals("beforeValidate 999999;This is another test string\n" +
                         "afterValidate 999999;This is another test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testValidateCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            bean.setBeforeValidateReturn(false);
            manager.validate(bean);
            assertEquals("beforeValidate -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setBeforeValidateReturn(true);
            bean.setAfterValidateReturn(false);
            manager.validate(bean);
            assertEquals("beforeValidate -1;This is my test string\n" +
                         "afterValidate -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            var id = manager.save(bean);
            assertEquals("beforeSave -1;This is my test string\n" +
                         "beforeInsert -1;This is my test string\n" +
                         "afterInsert true " + id + ";This is my test string\n" +
                         "afterSave true " + id + ";This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            id = manager.save(bean);
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string\n" +
                         "afterUpdate true " + id + ";This is a new test string\n" +
                         "afterSave true " + id + ";This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setId(999999);
            bean.setTestString("This is another test string");

            id = manager.save(bean);
            assertEquals("beforeSave 999999;This is another test string\n" +
                         "beforeUpdate 999999;This is another test string\n" +
                         "afterUpdate false 999999;This is another test string\n" +
                         "beforeInsert 999999;This is another test string\n" +
                         "afterInsert true " + id + ";This is another test string\n" +
                         "afterSave true " + id + ";This is another test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            // test immediate inserts
            bean.setBeforeSaveReturn(false);
            manager.save(bean);
            assertEquals("beforeSave -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertNull(manager.restore(1));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setBeforeSaveReturn(true);
            bean.setBeforeInsertReturn(false);
            manager.save(bean);
            assertEquals("beforeSave -1;This is my test string\n" +
                         "beforeInsert -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertNull(manager.restore(1));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setBeforeInsertReturn(true);
            bean.setAfterInsertReturn(false);
            var id = manager.save(bean);
            assertEquals("beforeSave -1;This is my test string\n" +
                         "beforeInsert -1;This is my test string\n" +
                         "afterInsert true " + id + ";This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertNotNull(manager.restore(id));

            // test updates
            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            bean.setBeforeUpdateReturn(false);
            bean.setAfterInsertReturn(true);
            assertEquals(-1, manager.save(bean));
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertEquals("This is my test string", manager.restore(id).getTestString());

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setBeforeUpdateReturn(true);
            bean.setAfterUpdateReturn(false);
            assertEquals(id, manager.save(bean));
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string\n" +
                         "afterUpdate true " + id + ";This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertEquals("This is a new test string", manager.restore(id).getTestString());

            // test insert after failed update
            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setId(999999);
            bean.setTestString("This is another test string");

            bean.setAfterUpdateReturn(true);
            bean.setBeforeInsertReturn(false);

            assertEquals(-1, manager.save(bean));
            assertEquals("""
                beforeSave 999999;This is another test string
                beforeUpdate 999999;This is another test string
                afterUpdate false 999999;This is another test string
                beforeInsert 999999;This is another test string""", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertNull(manager.restore(999999));
            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setBeforeInsertReturn(true);
            bean.setAfterInsertReturn(false);

            assertEquals(id + 1, manager.save(bean));
            id = id + 1;
            assertEquals("beforeSave 999999;This is another test string\n" +
                         "beforeUpdate 999999;This is another test string\n" +
                         "afterUpdate false 999999;This is another test string\n" +
                         "beforeInsert 999999;This is another test string\n" +
                         "afterInsert true " + id + ";This is another test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            assertNotNull(manager.restore(id));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveCallbacksSparse(Datasource datasource) {
        var manager = setUpCallbacksSparseBeanManager(datasource);
        try {
            var bean = new CallbacksSparseBean();

            bean.setId(1000);
            bean.setTestString("Test String");

            var id = manager.save(bean);
            assertEquals("beforeSave " + id + ";Test String\n" +
                         "beforeInsert " + id + ";Test String\n" +
                         "afterInsert true " + id + ";Test String\n" +
                         "afterSave true " + id + ";Test String", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            id = manager.save(bean);
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeInsert " + id + ";This is a new test string\n" +
                         "afterInsert false " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string\n" +
                         "afterUpdate true " + id + ";This is a new test string\n" +
                         "afterSave true " + id + ";This is a new test string", StringUtils.join(bean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksSparseBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveCallbacksSparseReturns(Datasource datasource) {
        var manager = setUpCallbacksSparseBeanManager(datasource);
        try {
            var bean = new CallbacksSparseBean();

            bean.setId(1000);
            bean.setTestString("Test String");

            var id = 1000;

            // test immediate insert
            bean.setBeforeSaveReturn(false);
            assertEquals(-1, manager.save(bean));
            assertEquals("beforeSave " + id + ";Test String", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.setBeforeSaveReturn(true);
            bean.setBeforeInsertReturn(false);

            assertEquals(-1, manager.save(bean));
            assertEquals("beforeSave " + id + ";Test String\n" +
                         "beforeInsert " + id + ";Test String", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.setBeforeInsertReturn(true);
            bean.setAfterInsertReturn(false);

            assertEquals(id, manager.save(bean));
            assertEquals("beforeSave " + id + ";Test String\n" +
                         "beforeInsert " + id + ";Test String\n" +
                         "afterInsert true " + id + ";Test String", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));


            // test update after failed insert
            bean.setTestString("This is a new test string");

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.setAfterInsertReturn(true);
            bean.setBeforeUpdateReturn(false);

            assertEquals(-1, manager.save(bean));
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeInsert " + id + ";This is a new test string\n" +
                         "afterInsert false " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(bean.getExecutedCallbacks(), "\n"));

            bean.setBeforeUpdateReturn(true);
            bean.setAfterUpdateReturn(false);

            assertEquals(id, manager.save(bean));
            assertEquals("beforeSave " + id + ";This is a new test string\n" +
                         "beforeInsert " + id + ";This is a new test string\n" +
                         "afterInsert false " + id + ";This is a new test string\n" +
                         "beforeUpdate " + id + ";This is a new test string\n" +
                         "afterUpdate true " + id + ";This is a new test string", StringUtils.join(bean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksSparseBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testSaveListeners(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var listener = new AggregatingCallbacksBeanListener();
            manager.addListener(listener);
            try {
                var bean = new CallbacksBean();

                bean.setTestString("This is my test string");

                var id = manager.save(bean);
                var history = listener.getHistory();
                assertEquals(1, history.size());
                var entry = history.entrySet().iterator().next();
                assertEquals("inserted0", entry.getKey());
                assertSame(bean, entry.getValue());
                assertEquals("beforeSave -1;This is my test string\n" +
                             "beforeInsert -1;This is my test string\n" +
                             "afterInsert true " + id + ";listener inserted\n" +
                             "afterSave true " + id + ";listener inserted", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                CallbacksBean.clearExecuteCallbacks();
                listener.clearHistory();
                assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                bean.setTestString("This is a new test string");

                id = manager.save(bean);
                assertEquals(1, history.size());
                entry = history.entrySet().iterator().next();
                assertEquals("updated0", entry.getKey());
                assertSame(bean, entry.getValue());
                assertEquals("beforeSave " + id + ";This is a new test string\n" +
                             "beforeUpdate " + id + ";This is a new test string\n" +
                             "afterUpdate true " + id + ";listener updated\n" +
                             "afterSave true " + id + ";listener updated", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                CallbacksBean.clearExecuteCallbacks();
                listener.clearHistory();
                assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                bean.setId(999999);
                bean.setTestString("This is another test string");

                id = manager.save(bean);
                assertEquals(1, history.size());
                entry = history.entrySet().iterator().next();
                assertEquals("inserted0", entry.getKey());
                assertSame(bean, entry.getValue());
                assertEquals("beforeSave 999999;This is another test string\n" +
                             "beforeUpdate 999999;This is another test string\n" +
                             "afterUpdate false 999999;This is another test string\n" +
                             "beforeInsert 999999;This is another test string\n" +
                             "afterInsert true " + id + ";listener inserted\n" +
                             "afterSave true " + id + ";listener inserted", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            } finally {
                manager.removeListeners();
            }
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInsertCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            var id = manager.insert(bean);
            assertEquals("beforeInsert -1;This is my test string\n" +
                         "afterInsert true " + id + ";This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInsertCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            bean.setBeforeInsertReturn(false);
            manager.insert(bean);
            assertEquals("beforeInsert -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            assertNull(manager.restore(1));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUpdateCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            manager.update(bean);
            assertEquals("beforeUpdate -1;This is my test string\n" +
                         "afterUpdate false -1;This is my test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            var id = manager.insert(bean);
            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            manager.update(bean);
            assertEquals("beforeUpdate " + id + ";This is a new test string\n" +
                         "afterUpdate true " + id + ";This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testUpdateCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();
            bean.setTestString("This is my test string");

            var id = manager.insert(bean);
            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            bean.setTestString("This is a new test string");

            bean.setBeforeUpdateReturn(false);
            manager.update(bean);
            assertEquals("beforeUpdate " + id + ";This is a new test string", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            assertEquals("This is my test string", manager.restore(id).getTestString());
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDeleteCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            var id = manager.save(bean);

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            manager.delete(id);
            assertEquals("beforeDelete " + id + "\n" +
                         "afterDelete true " + id + "", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDeleteCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            var id = manager.save(bean);

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.setBeforeDeleteReturn(false);
            manager.delete(id);
            assertEquals("beforeDelete " + id + "", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            assertNotNull(manager.restore(id));

            CallbacksBean.setBeforeDeleteReturn(true);
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testDeleteListeners(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean = new CallbacksBean();

            bean.setTestString("This is my test string");

            var id = manager.save(bean);

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            var listener = new AggregatingCallbacksBeanListener();
            try {
                manager.addListener(listener);
                manager.delete(id);
                var history = listener.getHistory();
                assertEquals(1, history.size());
                var entry = history.entrySet().iterator().next();
                assertEquals("deleted0", entry.getKey());
                assertEquals(id, entry.getValue());
                assertEquals("beforeDelete " + id + "\n" +
                             "afterDelete true " + id + "", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));
            } finally {
                manager.removeListeners();
            }
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreCallbacks(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean1 = new CallbacksBean();
            var bean2 = new CallbacksBean();
            var bean3 = new CallbacksBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            var id1 = manager.save(bean1);
            var id2 = manager.save(bean2);
            var id3 = manager.save(bean3);

            CallbacksBean.clearExecuteCallbacks();

            // restore all beans
            assertEquals(3, manager.restore(manager.getRestoreQuery().orderBy("id")).size());

            assertEquals("afterRestore " + id1 + ";This is bean1\n" +
                         "afterRestore " + id2 + ";This is bean2\n" +
                         "afterRestore " + id3 + ";This is bean3", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            // restore a specific bean
            manager.restore(bean2.getId());

            assertEquals("afterRestore " + id2 + ";This is bean2", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            // restore the first bean
            manager.restoreFirst(manager.getRestoreQuery().orderBy("id"));

            assertEquals("afterRestore " + id1 + ";This is bean1", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.clearExecuteCallbacks();
            CallbacksBean.setAfterRestoreReturn(true);
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreCallbacksReturns(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean1 = new CallbacksBean();
            var bean2 = new CallbacksBean();
            var bean3 = new CallbacksBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            var id = manager.save(bean1);
            manager.save(bean2);
            manager.save(bean3);

            CallbacksBean.clearExecuteCallbacks();
            CallbacksBean.setAfterRestoreReturn(false);

            assertEquals(1, manager.restore(manager.getRestoreQuery().orderBy("id")).size());

            assertEquals("afterRestore " + id + ";This is bean1", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

            CallbacksBean.setAfterRestoreReturn(true);
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRestoreListeners(Datasource datasource) {
        var manager = setUpCallbacksBeanManager(datasource);
        try {
            CallbacksBean.clearExecuteCallbacks();

            var bean1 = new CallbacksBean();
            var bean2 = new CallbacksBean();
            var bean3 = new CallbacksBean();

            bean1.setTestString("This is bean1");
            bean2.setTestString("This is bean2");
            bean3.setTestString("This is bean3");

            var id1 = manager.save(bean1);
            var id2 = manager.save(bean2);
            var id3 = manager.save(bean3);

            CallbacksBean.clearExecuteCallbacks();

            var listener = new AggregatingCallbacksBeanListener();
            manager.addListener(listener);
            try {
                // restore all beans
                var restored = manager.restore(manager.getRestoreQuery().orderBy("id"));
                assertEquals(3, restored.size());

                Map<String, Object> history;
                Iterator<Map.Entry<String, Object>> it;
                Map.Entry<String, Object> entry;

                history = listener.getHistory();
                assertEquals(3, history.size());
                it = history.entrySet().iterator();
                entry = it.next();
                assertEquals("restored0", entry.getKey());
                assertSame(restored.get(0), entry.getValue());
                entry = it.next();
                assertEquals("restored1", entry.getKey());
                assertSame(restored.get(1), entry.getValue());
                entry = it.next();
                assertEquals("restored2", entry.getKey());
                assertSame(restored.get(2), entry.getValue());
                assertEquals("afterRestore " + id1 + ";listener restored\n" +
                             "afterRestore " + id2 + ";listener restored\n" +
                             "afterRestore " + id3 + ";listener restored", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                CallbacksBean.clearExecuteCallbacks();
                listener.clearHistory();
                assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                // restore a specific bean
                var restored_specific = manager.restore(bean2.getId());

                history = listener.getHistory();
                assertEquals(1, history.size());
                it = history.entrySet().iterator();
                entry = it.next();
                assertEquals("restored0", entry.getKey());
                assertSame(restored_specific, entry.getValue());
                assertEquals("afterRestore " + id2 + ";listener restored", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                CallbacksBean.clearExecuteCallbacks();
                listener.clearHistory();
                assertEquals("", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                // restore the first bean
                var restored_first = manager.restoreFirst(manager.getRestoreQuery().orderBy("id"));

                history = listener.getHistory();
                assertEquals(1, history.size());
                it = history.entrySet().iterator();
                entry = it.next();
                assertEquals("restored0", entry.getKey());
                assertSame(restored_first, entry.getValue());
                assertEquals("afterRestore " + id1 + ";listener restored", StringUtils.join(CallbacksBean.getExecutedCallbacks(), "\n"));

                CallbacksBean.clearExecuteCallbacks();
                listener.clearHistory();
                CallbacksBean.setAfterRestoreReturn(true);
            } finally {
                manager.removeListeners();
            }
        } finally {
            tearDownCallbacksBean(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testCallbacksProvider(Datasource datasource) {
        var manager = setUpCallbacksProviderBeanManager(datasource);
        try {
            CallbacksProviderBean.clearExecuteCallbacks();

            var bean = new CallbacksProviderBean();
            CallbacksProviderBean new_bean = null;

            bean.setTestString("This is my test string");

            var id1 = manager.save(bean);

            new_bean = manager.restore(id1);

            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);
            assertEquals(new_bean.getTestString(), bean.getTestString());
            assertEquals(new_bean.getId(), id1);

            bean.setId(id1);
            bean.setTestString("This is a new test string");

            assertEquals(manager.save(bean), id1);
            assertEquals(bean.getId(), id1);

            new_bean = manager.restore(id1);

            assertNotNull(new_bean);
            assertNotSame(new_bean, bean);

            assertEquals(new_bean.getTestString(), "This is a new test string");

            bean.setId(999999);
            bean.setTestString("This is another test string");

            var id2 = id1 + 1;

            assertEquals(id2, manager.save(bean));
            assertNotEquals(999999, id2);
            assertEquals(bean.getId(), id1 + 1);

            bean.setId(76876);
            bean.setTestString("This is a last test string");

            var id3 = id2 + 1;
            assertEquals(id3, manager.insert(bean));

            bean.setTestString("This is an updated test string");
            assertEquals(id3, manager.update(bean));

            assertTrue(manager.delete(id2));
            assertEquals(2, manager.restore().size());

            assertEquals("beforeSave -1;This is my test string\n" +
                         "beforeInsert -1;This is my test string\n" +
                         "afterInsert true " + id1 + ";This is my test string\n" +
                         "afterSave true " + id1 + ";This is my test string\n" +
                         "afterRestore " + id1 + ";This is my test string\n" +
                         "beforeSave " + id1 + ";This is a new test string\n" +
                         "beforeUpdate " + id1 + ";This is a new test string\n" +
                         "afterUpdate true " + id1 + ";This is a new test string\n" +
                         "afterSave true " + id1 + ";This is a new test string\n" +
                         "afterRestore " + id1 + ";This is a new test string\n" +
                         "beforeSave 999999;This is another test string\n" +
                         "beforeUpdate 999999;This is another test string\n" +
                         "afterUpdate false 999999;This is another test string\n" +
                         "beforeInsert 999999;This is another test string\n" +
                         "afterInsert true " + id2 + ";This is another test string\n" +
                         "afterSave true " + id2 + ";This is another test string\n" +
                         "beforeInsert 76876;This is a last test string\n" +
                         "afterInsert true " + id3 + ";This is a last test string\n" +
                         "beforeUpdate " + id3 + ";This is an updated test string\n" +
                         "afterUpdate true " + id3 + ";This is an updated test string\n" +
                         "beforeDelete " + id2 + "\n" +
                         "afterDelete true " + id2 + "\n" +
                         "afterRestore " + id1 + ";This is a new test string\n" +
                         "afterRestore " + id3 + ";This is an updated test string", StringUtils.join(CallbacksProviderBean.getExecutedCallbacks(), "\n"));
        } finally {
            tearDownCallbacksProviderBean(manager);
        }
    }
}
