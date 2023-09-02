/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.SessionAttributes;
import rife.authentication.SessionValidator;
import rife.authentication.credentialsmanagers.DatabaseUsersFactory;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.exceptions.SessionValidatorException;
import rife.authentication.sessionmanagers.DatabaseSessionsFactory;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseSessionValidator {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        assertNotNull(validator);
        assertTrue(validator instanceof DatabaseSessionValidator);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidSessionId(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);

        assertTrue(validator.isAccessAuthorized(1));
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testSessionValidity(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);

        var user_id = 9478;
        var auth_data = "98.232.12.456";

        String auth_id = null;
        try {
            sessions.install();

            auth_id = sessions.startSession(user_id, auth_data, false);
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id, auth_data, new DummyAttributes())));
            sessions.setRestrictAuthData(true);
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            sessions.setRestrictAuthData(false);
            assertEquals(DatabaseSessionValidator.SESSION_VALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession("not_valid", auth_data, new DummyAttributes()));

            sessions.setSessionDuration(0);

            Thread.sleep(2);
            assertEquals(DatabaseSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, auth_data, new DummyAttributes()));
        } catch (InterruptedException | SessionValidatorException | SessionManagerException e) {
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
    void testSessionValidityRole(Datasource datasource) {
        SessionValidator validator = DatabaseSessionValidatorFactory.instance(datasource);
        var users = DatabaseUsersFactory.instance(datasource);
        var sessions = DatabaseSessionsFactory.instance(datasource);
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);

        var auth_data = "98.232.12.456";

        String auth_id1 = null;
        String auth_id2 = null;
        String auth_id3 = null;
        try {
            users.install();
            sessions.install();

            users.addRole("admin");
            users.addRole("maint");

            users.addUser("login1", new RoleUserAttributes(1, "thepassword", new String[]{"admin", "maint"}));
            users.addUser("login2", new RoleUserAttributes(2, "thepassword", new String[]{"maint"}));
            users.addUser("login3", new RoleUserAttributes(3, "thepassword"));

            auth_id1 = sessions.startSession(1, auth_data, false);
            auth_id2 = sessions.startSession(2, auth_data, false);
            auth_id3 = sessions.startSession(3, auth_data, false);

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, auth_data, new DummyAttributes())));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, auth_data, new RoleAdminAttributes())));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id1, auth_data, new RoleMaintAttributes())));

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id2, auth_data, new DummyAttributes())));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id2, auth_data, new RoleAdminAttributes()));
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id2, auth_data, new RoleMaintAttributes())));

            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id3, auth_data, new DummyAttributes())));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id3, auth_data, new RoleAdminAttributes()));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id3, auth_data, new RoleMaintAttributes()));
        } catch (SessionManagerException | SessionValidatorException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } catch (CredentialsManagerException e) {
            e.printStackTrace();
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            sessions.setSessionDuration(RifeConfig.authentication().getSessionDuration());
            try {
                sessions.remove();
            } catch (SessionManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    static class DummyAttributes implements SessionAttributes {
        @Override
        public boolean hasAttribute(String key) {
            return false;
        }

        @Override
        public String getAttribute(String key) {
            return null;
        }
    }

    static class RoleMaintAttributes implements SessionAttributes {
        @Override
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        @Override
        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "maint";
            }

            return null;
        }
    }

    static class RoleAdminAttributes implements SessionAttributes {
        @Override
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        @Override
        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "admin";
            }

            return null;
        }
    }
}
