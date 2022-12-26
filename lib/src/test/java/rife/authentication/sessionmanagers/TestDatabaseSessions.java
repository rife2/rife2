/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionmanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.ListSessions;
import rife.authentication.exceptions.SessionManagerException;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseSessions {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstantiation(Datasource datasource) {
        DatabaseSessions manager = DatabaseSessionsFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstall(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            assertTrue(sessions.install());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemove(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        try {
            assertTrue(sessions.remove());
        } catch (SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testStartSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        int user_id = 143;
        String host_ip = "189.38.987.43";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, host_ip, false);
            assertFalse(sessions.wasRemembered(auth_id));

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertTrue(auth_id.length() > 0);
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
    public void testStartRememberedSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        int user_id = 143;
        String host_ip = "189.38.987.43";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, host_ip, true);
            assertTrue(sessions.wasRemembered(auth_id));

            assertEquals(1, sessions.countSessions());

            assertNotNull(auth_id);
            assertTrue(auth_id.length() > 0);
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
    public void testSessionExpiration(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);

        int user_id = 1243;
        String host_ip = "837.234.23.434";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, host_ip, false);
            assertTrue(sessions.isSessionValid(auth_id, host_ip));
            assertEquals(1, sessions.countSessions());

            long start = System.currentTimeMillis();

            Thread.sleep(1500);
            if (System.currentTimeMillis() - start <= 2000) {
                assertTrue(sessions.isSessionValid(auth_id, host_ip));
                assertEquals(1, sessions.countSessions());
                Thread.sleep(510);
            }

            assertFalse(sessions.isSessionValid(auth_id, host_ip));
            assertEquals(0, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
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
    public void testContinueSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);

        int user_id = 41;
        String host_ip = "113.98.46.140";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, host_ip, false);
            assertTrue(sessions.isSessionValid(auth_id, host_ip));
            Thread.sleep(1900);
            assertTrue(sessions.continueSession(auth_id));
            Thread.sleep(100);
            assertTrue(sessions.isSessionValid(auth_id, host_ip));
            Thread.sleep(1901);
            assertFalse(sessions.isSessionValid(auth_id, host_ip));
        } catch (InterruptedException | SessionManagerException e) {
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
    public void testContinueUnknownSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        String auth_id = "unknown";
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
    public void testEraseSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        int user_id = 93;
        String host_ip = "24.534.23.444";

        try {
            sessions.install();

            long number_of_sessions = sessions.countSessions();
            String auth_id = null;
            auth_id = sessions.startSession(user_id, host_ip, false);
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
    public void testEraseUnknownSession(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

        String auth_id = "unknown";
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
    public void testEraseAllSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

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
    public void testEraseUserSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

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
    public void testEraseUnkownUserSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);

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
    public void testPurgeSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(2000);

        int user_id = 9478;
        String host_ip = "98.232.12.456";

        try {
            sessions.install();

            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());

            sessions.startSession(user_id, host_ip, false);
            assertEquals(1, sessions.countSessions());

            Thread.sleep(2010);

            sessions.purgeSessions();

            sessions.startSession(user_id, host_ip, false);
            assertEquals(1, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
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
    public void testCountSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(4000);

        int user_id1 = 9478;
        String host_ip1 = "98.232.12.456";

        int user_id2 = 9479;
        String host_ip2 = "98.232.12.457";

        int user_id3 = 9480;
        String host_ip3 = "98.232.12.458";

        try {
            sessions.install();

            sessions.eraseAllSessions();
            assertEquals(0, sessions.countSessions());

            sessions.startSession(user_id1, host_ip1, false);
            assertEquals(1, sessions.countSessions());

            Thread.sleep(2000);

            sessions.startSession(user_id2, host_ip2, false);
            assertEquals(2, sessions.countSessions());

            Thread.sleep(1000);

            sessions.startSession(user_id3, host_ip3, false);
            assertEquals(3, sessions.countSessions());

            Thread.sleep(1100);

            assertEquals(2, sessions.countSessions());
        } catch (InterruptedException | SessionManagerException e) {
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
    public void testListSessions(Datasource datasource) {
        DatabaseSessions sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(4000);

        final int user_id1 = 9478;
        final String host_ip1 = "98.232.12.456";

        final int user_id2 = 9479;
        final String host_ip2 = "98.232.12.457";

        final int user_id3 = 9480;
        final String host_ip3 = "98.232.12.458";

        final int[] count = new int[1];
        count[0] = 0;
        try {
            sessions.install();

            sessions.eraseAllSessions();

            assertFalse(sessions.listSessions(new ListSessions() {
                public boolean foundSession(long userId, String hostIp, String authId) {
                    fail();
                    return true;
                }
            }));

            sessions.startSession(user_id1, host_ip1, false);

            count[0] = 0;
            assertTrue(sessions.listSessions(new ListSessions() {
                public boolean foundSession(long userId, String hostIp, String authId) {
                    count[0]++;
                    assertTrue(count[0] <= 1);

                    assertEquals(9478, userId);
                    assertEquals(host_ip1, hostIp);

                    return true;
                }
            }));

            Thread.sleep(2000);

            sessions.startSession(user_id2, host_ip2, false);

            count[0] = 0;
            assertTrue(sessions.listSessions(new ListSessions() {
                public boolean foundSession(long userId, String hostIp, String authId) {
                    count[0]++;
                    assertTrue(count[0] <= 2);

                    assertTrue(9478 == userId || 9479 == userId);
                    assertTrue(host_ip1.equals(hostIp) || host_ip2.equals(hostIp));

                    return true;
                }
            }));

            Thread.sleep(1000);

            sessions.startSession(user_id3, host_ip3, false);

            count[0] = 0;
            assertTrue(sessions.listSessions(new ListSessions() {
                public boolean foundSession(long userId, String hostIp, String authId) {
                    count[0]++;
                    assertTrue(count[0] <= 3);

                    assertTrue(9478 == userId || 9479 == userId || 9480 == userId);
                    assertTrue(host_ip1.equals(hostIp) || host_ip2.equals(hostIp) || host_ip3.equals(hostIp));

                    return true;
                }
            }));

            Thread.sleep(1100);


            count[0] = 0;
            assertTrue(sessions.listSessions(new ListSessions() {
                public boolean foundSession(long userId, String hostIp, String authId) {
                    count[0]++;
                    assertTrue(count[0] <= 2);

                    assertTrue(9479 == userId || 9480 == userId);
                    assertTrue(host_ip2.equals(hostIp) || host_ip3.equals(hostIp));

                    return true;
                }
            }));
        } catch (InterruptedException | SessionManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
