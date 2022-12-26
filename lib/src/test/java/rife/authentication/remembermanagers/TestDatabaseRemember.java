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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseRemember {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstantiation(Datasource datasource) {
        DatabaseRemember manager = DatabaseRememberFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testInstall(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            assertTrue(remember.install());
        } catch (RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testRemove(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            assertTrue(remember.remove());
        } catch (RememberManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    public void testCreateRememberId(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        int user_id = 143;

        String remember_id = null;
        try {
            remember.install();

            remember_id = remember.createRememberId(user_id, "123.98.23.3");

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
    public void testGetRememberedUserId(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            remember.install();

            ArrayList<String> remember_ids1 = new ArrayList<String>();
            ArrayList<String> remember_ids2 = new ArrayList<String>();
            ArrayList<String> remember_ids3 = new ArrayList<String>();
            remember_ids1.add(remember.createRememberId(232, "123.98.23.3"));
            remember_ids1.add(remember.createRememberId(232, "123.98.23.32"));
            remember_ids2.add(remember.createRememberId(23, "123.98.23.3"));
            remember_ids3.add(remember.createRememberId(53, "123.98.23.3"));
            remember_ids3.add(remember.createRememberId(53, "123.98.23.3"));
            remember_ids1.add(remember.createRememberId(232, "123.98.23.34"));
            remember_ids2.add(remember.createRememberId(23, "123.98.23.3"));

            for (String remember_id : remember_ids1) {
                assertEquals(232, remember.getRememberedUserId(remember_id));
            }

            for (String remember_id : remember_ids2) {
                assertEquals(23, remember.getRememberedUserId(remember_id));
            }

            for (String remember_id : remember_ids3) {
                assertEquals(53, remember.getRememberedUserId(remember_id));
            }
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
    public void testEraseRememberId(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        int user_id = 93;

        try {
            remember.install();

            String remember_id = null;
            remember_id = remember.createRememberId(user_id, "123.98.23.3");
            assertEquals(user_id, remember.getRememberedUserId(remember_id));
            assertTrue(remember.eraseRememberId(remember_id));
            assertEquals(-1, remember.getRememberedUserId(remember_id));
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
    public void testEraseUnknownSession(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        String remember_id = "unknown";
        try {
            remember.install();

            assertFalse(remember.eraseRememberId(remember_id));
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
    public void testEraseAllRememberIds(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            remember.install();

            ArrayList<String> remember_ids = new ArrayList<String>();
            remember_ids.add(remember.createRememberId(232, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(232, "123.98.23.34"));
            remember_ids.add(remember.createRememberId(23, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(53, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(53, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(232, "123.98.23.31"));
            remember_ids.add(remember.createRememberId(23, "123.98.23.3"));

            for (String remember_id : remember_ids) {
                assertTrue(remember.getRememberedUserId(remember_id) != -1);
            }

            remember.eraseAllRememberIds();

            for (String remember_id : remember_ids) {
                assertEquals(-1, remember.getRememberedUserId(remember_id));
            }
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
    public void testEraseUserRememberIds(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            remember.install();

            ArrayList<String> remember_ids = new ArrayList<String>();
            remember_ids.add(remember.createRememberId(8433, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(8433, "123.98.23.33"));
            remember_ids.add(remember.createRememberId(8432, "123.98.23.31"));
            remember_ids.add(remember.createRememberId(8431, "123.98.23.3"));

            for (String remember_id : remember_ids) {
                assertTrue(remember.getRememberedUserId(remember_id) != -1);
            }

            assertTrue(remember.eraseUserRememberIds(8433));

            assertEquals(-1, remember.getRememberedUserId(remember_ids.get(0)));
            assertEquals(-1, remember.getRememberedUserId(remember_ids.get(1)));
            assertTrue(remember.getRememberedUserId(remember_ids.get(2)) != -1);
            assertTrue(remember.getRememberedUserId(remember_ids.get(3)) != -1);
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
    public void testEraseUnkownUserRememberIds(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);

        try {
            remember.install();

            ArrayList<String> remember_ids = new ArrayList<String>();
            remember_ids.add(remember.createRememberId(8432, "123.98.23.3"));
            remember_ids.add(remember.createRememberId(8431, "123.98.23.3"));

            for (String remember_id : remember_ids) {
                assertTrue(remember.getRememberedUserId(remember_id) != -1);
            }

            assertFalse(remember.eraseUserRememberIds(8433));

            for (String remember_id : remember_ids) {
                assertTrue(remember.getRememberedUserId(remember_id) != -1);
            }
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
    public void testPurgeRememberIds(Datasource datasource) {
        DatabaseRemember remember = DatabaseRememberFactory.instance(datasource);
        remember.setRememberDuration(2000);

        int user_id = 9478;

        try {
            remember.install();

            remember.eraseAllRememberIds();

            String remember_id = remember.createRememberId(user_id, "123.98.23.3");

            remember.purgeRememberIds();

            assertEquals(remember.getRememberedUserId(remember_id), user_id);

            Thread.sleep(2010);

            remember.purgeRememberIds();

            assertEquals(remember.getRememberedUserId(remember_id), -1);
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
