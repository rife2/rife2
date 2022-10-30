/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
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
    public void testInstantiation(Datasource datasource) {
        DatabaseRemember manager = DatabaseRememberFactory.getInstance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStartSession(Datasource datasource) {
        PurgingRememberManager remember = new PurgingRememberManager(DatabaseRememberFactory.getInstance(datasource));
        remember.setRememberPurgeFrequency(0);

        int user_id = 143;

        String remember_id = null;
        try {
            ((DatabaseRemember) remember.getRememberManager()).install();

            remember_id = remember.createRememberId(user_id, "123.98.23.3");

            assertNotNull(remember_id);
            assertTrue(remember_id.length() > 0);

            assertEquals(user_id, remember.getRememberedUserId(remember_id));
        } catch (RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                ((DatabaseRemember) remember.getRememberManager()).remove();
            } catch (RememberManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testPurgeRemember(Datasource datasource) {
        PurgingRememberManager remember = new PurgingRememberManager(DatabaseRememberFactory.getInstance(datasource));
        remember.setRememberDuration(2000);
        remember.setRememberPurgeFrequency(1);
        remember.setRememberPurgeScale(1);

        try {
            ((DatabaseRemember) remember.getRememberManager()).install();

            remember.eraseAllRememberIds();

            int user_id1 = 143;
            String remember_id1 = remember.createRememberId(user_id1, "123.98.23.3");

            assertEquals(user_id1, remember.getRememberedUserId(remember_id1));

            Thread.sleep(2000);

            int user_id2 = 143;
            String remember_id2 = remember.createRememberId(user_id2, "123.98.23.39");

            assertEquals(user_id2, remember.getRememberedUserId(remember_id2));
            assertEquals(-1, remember.getRememberedUserId(remember_id1));
        } catch (InterruptedException | RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                ((DatabaseRemember) remember.getRememberManager()).remove();
            } catch (RememberManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
