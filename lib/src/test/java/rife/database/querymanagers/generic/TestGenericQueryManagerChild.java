/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.database.querymanagers.generic.beans.ChildBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TestGenericQueryManagerChild {
    protected GenericQueryManager<ChildBean> setup(Datasource datasource) {
        var manager = GenericQueryManagerFactory.instance(datasource, ChildBean.class);
        manager.install();
        return manager;
    }

    protected void tearDown(GenericQueryManager<ChildBean> manager) {
        manager.remove();
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetBaseClass(Datasource datasource) {
        var manager = setup(datasource);
        try {
            assertSame(ChildBean.class, manager.getBaseClass());
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstallCustomQuery(Datasource datasource) {
        var manager = setup(datasource);
        try {
            manager.remove();
            manager.install(manager.getInstallTableQuery());
        } finally {
            tearDown(manager);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testChildBean(Datasource datasource) {
        var manager = setup(datasource);
        try {
            var bean = new ChildBean();

            bean.setParentString("This is bean");
            bean.setChildString("This is childbean");

            var id = manager.save(bean);

            var rbean = manager.restore(id);

            assertEquals(rbean.getParentString(), bean.getParentString());
            assertEquals(rbean.getChildString(), bean.getChildString());
        } finally {
            tearDown(manager);
        }
    }
}
