/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.remembermanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.exceptions.RememberManagerException;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestPurgingDatabaseRemember {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        var manager = DatabaseRememberFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStartSession(Datasource datasource) {
        var remember = DatabaseRememberFactory.instance(datasource);
        remember.setRememberPurgeFrequency(0);

        var user_id = 143;

        String remember_id = null;
        try {
            remember.install();

            remember_id = remember.createRememberId(user_id);

            assertNotNull(remember_id);
            assertTrue(remember_id.length() > 0);

            assertEquals(user_id, remember.getRememberedUserId(remember_id));
        } catch (RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                remember.remove();
            } catch (RememberManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testPurgeRemember(Datasource datasource) {
        var remember = DatabaseRememberFactory.instance(datasource);
        remember.setRememberDuration(2000);
        remember.setRememberPurgeFrequency(1);
        remember.setRememberPurgeScale(1);

        try {
            remember.install();

            remember.eraseAllRememberIds();

            var user_id1 = 143;
            var remember_id1 = remember.createRememberId(user_id1);

            assertEquals(user_id1, remember.getRememberedUserId(remember_id1));

            Thread.sleep(2000);

            var user_id2 = 143;
            var remember_id2 = remember.createRememberId(user_id2);

            assertEquals(user_id2, remember.getRememberedUserId(remember_id2));
            assertEquals(-1, remember.getRememberedUserId(remember_id1));
        } catch (InterruptedException | RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                remember.remove();
            } catch (RememberManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
