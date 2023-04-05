/*
 * Copyright 2001-2008 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * Steven Grimm (koreth[remove] at midwinter dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.authentication.sessionmanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.exceptions.SessionManagerException;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestPurgingDatabaseSessions {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStartSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionPurgeFrequency(0);

        var user_id = 143;
        var auth_data = "189.38.987.43";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, auth_data, false);

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertTrue(auth_id.length() > 0);
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            sessions.setSessionPurgeFrequency(RifeConfig.authentication().getSessionPurgeFrequency());
            sessions.setSessionPurgeScale(RifeConfig.authentication().getSessionPurgeScale());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testPurgeSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);
        sessions.setSessionPurgeFrequency(1);
        sessions.setSessionPurgeScale(1);

        var user_id = 9478;
        var auth_data = "98.232.12.456";

        try {
            sessions.install();

            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());

            sessions.startSession(user_id, auth_data, false);
            assertEquals(1, sessions.countSessions());

            Thread.sleep(2010);

            sessions.startSession(user_id, auth_data, false);
            assertEquals(1, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            sessions.setSessionPurgeFrequency(RifeConfig.authentication().getSessionPurgeFrequency());
            sessions.setSessionPurgeScale(RifeConfig.authentication().getSessionPurgeScale());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
