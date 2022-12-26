/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
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
    public void testInstantiateSchedulerManager(Datasource datasource) {
        DatabaseScheduler manager = DatabaseSchedulerFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstall(Datasource datasource) {
        DatabaseScheduler manager = DatabaseSchedulerFactory.instance(datasource);

        try {
            assertTrue(manager.install());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemove(Datasource datasource) {
        DatabaseScheduler manager = DatabaseSchedulerFactory.instance(datasource);

        try {
            assertTrue(manager.remove());
        } catch (SchedulerManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
