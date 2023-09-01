/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
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

public class TestDatabaseSessions {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        var manager = DatabaseSessionsFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstall(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            assertTrue(sessions.install());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            sessions.remove();
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemove(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        sessions.install();
        try {
            assertTrue(sessions.remove());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStartSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        var user_id = 143;
        var auth_data = "189.38.987.43";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, auth_data, false);
            assertFalse(sessions.wasRemembered(auth_id));

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertTrue(!auth_id.isEmpty());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testStartRememberedSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        var user_id = 143;
        var auth_data = "189.38.987.43";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, auth_data, true);
            assertTrue(sessions.wasRemembered(auth_id));

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertTrue(!auth_id.isEmpty());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSessionExpiration(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);

        var user_id = 1243;
        var auth_data = "837.234.23.434";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, auth_data, false);
            assertTrue(sessions.isSessionValid(auth_id, auth_data));
            assertEquals(1, sessions.countSessions());

            var start = System.currentTimeMillis();

            Thread.sleep(1500);
            if (System.currentTimeMillis() - start <= 2000) {
                assertTrue(sessions.isSessionValid(auth_id, auth_data));
                assertEquals(1, sessions.countSessions());
                Thread.sleep(510);
            }

            assertFalse(sessions.isSessionValid(auth_id, auth_data));
            assertEquals(0, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContinueSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);

        var user_id = 41;
        var auth_data = "113.98.46.140";

        String auth_id = null;
        try {
            sessions.install();

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
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testContinueUnknownSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        var auth_id = "unknown";
        try {
            sessions.install();

            assertFalse(sessions.continueSession(auth_id));
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEraseSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        var user_id = 93;
        var auth_data = "24.534.23.444";

        try {
            sessions.install();

            var number_of_sessions = sessions.countSessions();
            String auth_id = null;
            auth_id = sessions.startSession(user_id, auth_data, false);
            assertEquals(number_of_sessions + 1, sessions.countSessions());
            assertTrue(sessions.eraseSession(auth_id));
            assertEquals(number_of_sessions, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEraseUnknownSession(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        var auth_id = "unknown";
        try {
            sessions.install();

            assertFalse(sessions.eraseSession(auth_id));
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEraseAllSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            sessions.install();

            sessions.startSession(232, "873.232.44.333", false);
            sessions.startSession(232, "873.232.44.333", false);
            sessions.startSession(23, "873.232.44.333", false);
            sessions.startSession(53, "873.232.44.333", false);
            sessions.startSession(53, "873.232.44.333", false);
            sessions.startSession(232, "873.232.44.333", false);
            sessions.startSession(23, "873.232.44.333", false);

            assertTrue(sessions.countSessions() > 0);
            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEraseUserSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            sessions.install();

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
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testEraseUnkownUserSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            sessions.install();

            assertEquals(0, sessions.countSessions());
            sessions.startSession(8432, "143.98.32.545", false);
            sessions.startSession(8431, "143.98.32.545", false);
            assertTrue(sessions.countSessions() > 0);
            assertFalse(sessions.eraseUserSessions(8433));
            assertEquals(2, sessions.countSessions());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
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

        var user_id = 9478;
        var auth_data = "98.232.12.456";

        try {
            sessions.install();

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
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testCountSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(4000);

        var user_id1 = 9478;
        var auth_data1 = "98.232.12.456";

        var user_id2 = 9479;
        var auth_data2 = "98.232.12.457";

        var user_id3 = 9480;
        var auth_data3 = "98.232.12.458";

        try {
            sessions.install();

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
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testListSessions(Datasource datasource) {
        var sessions = DatabaseSessionsFactory.instance(datasource);
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
            sessions.install();

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
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
