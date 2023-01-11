/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.scheduler.exceptions.SchedulerManagerException;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseSchedulerInstallation {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiateSchedulerManager(Datasource datasource) {
        var manager = DatabaseSchedulingFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstall(Datasource datasource) {
        var manager = DatabaseSchedulingFactory.instance(datasource);

        try {
            assertTrue(manager.install());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemove(Datasource datasource) {
        var manager = DatabaseSchedulingFactory.instance(datasource);

        try {
            assertTrue(manager.remove());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
