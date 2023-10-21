/*
 * Copyright 2001-2008 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * Steven Grimm (koreth[remove] at midwinter dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.authentication.sessionmanagers;

import org.junit.jupiter.api.Test;
import rife.authentication.exceptions.SessionManagerException;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemorySessions {
    @Test
    void testStartSession() {
        var sessions = new MemorySessions();
        try {
            sessions.eraseAllSessions();
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        var user_id = 143;
        var auth_data = "189.38.987.43";

        String auth_id = null;
        try {
            auth_id = sessions.startSession(user_id, auth_data, false);
            assertFalse(sessions.wasRemembered(auth_id));

            assertNotNull(auth_id);
            assertFalse(auth_id.isEmpty());

            assertEquals(1, sessions.countSessions());

            var session_instance = sessions.getSession(auth_id);
            assertEquals(user_id, sessions.getSessionUserId(auth_id));
            assertEquals(auth_id, session_instance.getAuthId());
            assertEquals(user_id, session_instance.getUserId());
            assertEquals(auth_data, session_instance.getAuthData());
            assertTrue(session_instance.getStart() <= System.currentTimeMillis());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testStartRememberedSession() {
        var sessions = new MemorySessions();
        try {
            sessions.eraseAllSessions();
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }

        var user_id = 143;
        var auth_data = "189.38.987.43";

        String auth_id = null;
        try {
            auth_id = sessions.startSession(user_id, auth_data, true);
            assertTrue(sessions.wasRemembered(auth_id));

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertFalse(auth_id.isEmpty());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testSessionExpiration() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(500);

        var user_id = 1243;
        var auth_data = "837.234.23.434";

        String auth_id = null;
        try {
            sessions.eraseAllSessions();

            auth_id = sessions.startSession(user_id, auth_data, false);
            assertEquals(1, sessions.countSessions());

            assertTrue(sessions.isSessionValid(auth_id, auth_data));
            Thread.sleep(200);
            assertTrue(sessions.isSessionValid(auth_id, auth_data));

            Thread.sleep(301);
            assertFalse(sessions.isSessionValid(auth_id, auth_data));

            assertEquals(0, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testContinueSession() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(2000);

        var user_id = 41;
        var auth_data = "113.98.46.140";

        String auth_id = null;
        try {
            auth_id = sessions.startSession(user_id, auth_data, false);
            assertTrue(sessions.isSessionValid(auth_id, auth_data));
            Thread.sleep(1900);
            assertTrue(sessions.continueSession(auth_id));
            Thread.sleep(100);
            assertTrue(sessions.isSessionValid(auth_id, auth_data));
            Thread.sleep(1901);
            assertFalse(sessions.isSessionValid(auth_id, auth_data));
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testContinueUnknownSession() {
        var sessions = new MemorySessions();

        var auth_id = "unknown";
        try {
            assertFalse(sessions.continueSession(auth_id));
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEraseSession() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(1200000);

        var user_id = 93;
        var auth_data = "24.534.23.444";

        String auth_id = null;
        try {
            auth_id = sessions.startSession(user_id, auth_data, false);
            var number_of_sessions = sessions.countSessions();
            assertTrue(sessions.eraseSession(auth_id));
            assertEquals(number_of_sessions - 1, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEraseUnknownSession() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(1200000);

        var auth_id = "unknown";
        try {
            assertFalse(sessions.eraseSession(auth_id));
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEraseAllSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(1200000);

        try {
            sessions.startSession(8433, "143.98.32.545", false);
            assertTrue(sessions.countSessions() > 0);
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEraseUserSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(1200000);

        try {
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());
            sessions.startSession(8433, "143.98.32.545", false);
            sessions.startSession(8433, "143.98.32.545", false);
            sessions.startSession(8432, "143.98.32.545", false);
            sessions.startSession(8431, "143.98.32.545", false);
            assertTrue(sessions.countSessions() > 0);
            assertTrue(sessions.eraseUserSessions(8433));
            assertEquals(2, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testEraseUnkownUserSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(1200000);

        try {
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());
            sessions.startSession(8432, "143.98.32.545", false);
            sessions.startSession(8431, "143.98.32.545", false);
            assertTrue(sessions.countSessions() > 0);
            assertFalse(sessions.eraseUserSessions(8433));
            assertEquals(2, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testPurgeSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(2000);

        var user_id = 9478;
        var auth_data = "98.232.12.456";

        try {
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());

            sessions.startSession(user_id, auth_data, false);
            assertEquals(1, sessions.countSessions());

            Thread.sleep(2010);

            sessions.purgeSessions();

            sessions.startSession(user_id, auth_data, false);
            assertEquals(1, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testCountSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(4000);

        var user_id1 = 9478;
        var auth_data1 = "98.232.12.456";

        var user_id2 = 9479;
        var auth_data2 = "98.232.12.457";

        var user_id3 = 9480;
        var auth_data3 = "98.232.12.458";

        try {
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());

            sessions.startSession(user_id1, auth_data1, false);
            assertEquals(1, sessions.countSessions());

            Thread.sleep(2000);

            sessions.startSession(user_id2, auth_data2, false);
            assertEquals(2, sessions.countSessions());

            Thread.sleep(1000);

            sessions.startSession(user_id3, auth_data3, false);
            assertEquals(3, sessions.countSessions());

            Thread.sleep(1100);

            assertEquals(2, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListSessions() {
        var sessions = new MemorySessions();
        sessions.setSessionDuration(4000);

        final var user_id1 = 9478;
        final var auth_data1 = "98.232.12.456";

        final var user_id2 = 9479;
        final var auth_data2 = "98.232.12.457";

        final var user_id3 = 9480;
        final var auth_data3 = "98.232.12.458";

        final var count = new int[1];
        count[0] = 0;
        try {
            sessions.eraseAllSessions();

            assertFalse(sessions.listSessions((userId, authData, authId) -> {
                fail();
                return true;
            }));

            sessions.startSession(user_id1, auth_data1, false);

            count[0] = 0;
            assertTrue(sessions.listSessions((userId, authData, authId) -> {
                count[0]++;
                assertTrue(count[0] <= 1);

                assertEquals(9478, userId);
                assertEquals(auth_data1, authData);

                return true;
            }));

            Thread.sleep(2000);

            sessions.startSession(user_id2, auth_data2, false);

            count[0] = 0;
            assertTrue(sessions.listSessions((userId, authData, authId) -> {
                count[0]++;
                assertTrue(count[0] <= 2);

                assertTrue(9478 == userId || 9479 == userId);
                assertTrue(auth_data1.equals(authData) || auth_data2.equals(authData));

                return true;
            }));

            Thread.sleep(1000);

            sessions.startSession(user_id3, auth_data3, false);

            count[0] = 0;
            assertTrue(sessions.listSessions((userId, authData, authId) -> {
                count[0]++;
                assertTrue(count[0] <= 3);

                assertTrue(9478 == userId || 9479 == userId || 9480 == userId);
                assertTrue(auth_data1.equals(authData) || auth_data2.equals(authData) || auth_data3.equals(authData));

                return true;
            }));

            Thread.sleep(1100);


            count[0] = 0;
            assertTrue(sessions.listSessions((userId, authData, authId) -> {
                count[0]++;
                assertTrue(count[0] <= 2);

                assertTrue(9479 == userId || 9480 == userId);
                assertTrue(auth_data2.equals(authData) || auth_data3.equals(authData));

                return true;
            }));
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}
