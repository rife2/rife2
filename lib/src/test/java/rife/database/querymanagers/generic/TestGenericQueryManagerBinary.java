/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.beans.BinaryBean;

import static org.junit.jupiter.api.Assertions.*;

public class TestGenericQueryManagerBinary {
    protected GenericQueryManager<BinaryBean> setUp(Datasource datasource) {
        var manager = GenericQueryManagerFactory.getInstance(datasource, BinaryBean.class);
        manager.install();
        return manager;
    }

    protected void tearDown(GenericQueryManager<BinaryBean> manager) {
        manager.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testGetBaseClass(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            assertSame(BinaryBean.class, manager.getBaseClass());
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
    public void testSaveRestoreBinary(Datasource datasource) {
        var manager = setUp(datasource);
        try {
            var bean = new BinaryBean();
            BinaryBean newbean = null;

            var bytes1 = new byte[]{1, 3, 5, 7, 11, 13, 17, 19, 23};
            bean.setTheBytes(bytes1);

            var id = manager.save(bean);

            newbean = manager.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);
            assertArrayEquals(newbean.getTheBytes(), bean.getTheBytes());
            assertEquals(newbean.getId(), id);

            var bytes2 = new byte[]{10, 30, 50, 70, 110};
            bean.setId(id);
            bean.setTheBytes(bytes2);

            assertEquals(manager.save(bean), id);
            assertEquals(bean.getId(), id);

            newbean = manager.restore(id);

            assertNotNull(newbean);
            assertNotSame(newbean, bean);

            assertArrayEquals(newbean.getTheBytes(), bytes2);

            var bytes3 = new byte[]{89, 22, 9, 31, 89};
            bean.setId(999999);
            bean.setTheBytes(bytes3);

            assertNotEquals(999999, manager.save(bean));
            assertEquals(bean.getId(), id + 1);

            var manager_othertable = GenericQueryManagerFactory.getInstance(datasource, BinaryBean.class, "othertable");
            manager_othertable.install();

            var bytes4 = new byte[]{79, 15, 88, 42};
            var bean2 = new BinaryBean();
            bean2.setTheBytes(bytes4);

            manager_othertable.save(bean2);

            var bean3 = manager_othertable.restore(bean2.getId());

            assertArrayEquals(bean3.getTheBytes(), bean2.getTheBytes());

            manager_othertable.remove();
        } finally {
            tearDown(manager);
        }
    }
}
