/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import org.junit.jupiter.api.Test;
import rife.authentication.SessionAttributes;
import rife.authentication.credentialsmanagers.MemoryUsers;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.authentication.exceptions.SessionManagerException;
import rife.authentication.exceptions.SessionValidatorException;
import rife.authentication.sessionmanagers.MemorySessions;
import rife.config.RifeConfig;
import rife.tools.ExceptionUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestBasicSessionValidator {
    @Test
    void testInstantiation() {
        AbstractSessionValidator validator = null;

        validator = new BasicSessionValidator();

        assertNotNull(validator);
    }

    @Test
    void testValidSessionId() {
        var validator = new BasicSessionValidator();

        assertTrue(validator.isAccessAuthorized(1));
    }

    @Test
    void testSessionValidity() {
        var validator = new BasicSessionValidator();
        var sessions = new MemorySessions();
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);

        var user_id = 9478;
        var auth_data = "98.232.12.456";

        String auth_id = null;
        try {
            auth_id = sessions.startSession(user_id, auth_data, false);
            assertTrue(validator.isAccessAuthorized(validator.validateSession(auth_id, auth_data, new DummyAttributes())));
            sessions.setRestrictAuthData(true);
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            sessions.setRestrictAuthData(false);
            assertEquals(AbstractSessionValidator.SESSION_VALID, validator.validateSession(auth_id, "1.1.1.1", new DummyAttributes()));
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession("not_valid", auth_data, new DummyAttributes()));

            sessions.setSessionDuration(0);

            Thread.sleep(2);
            assertEquals(AbstractSessionValidator.SESSION_INVALID, validator.validateSession(auth_id, auth_data, new DummyAttributes()));
        } catch (InterruptedException | SessionManagerException | SessionValidatorException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testSessionValidityRole() {
        var validator = new BasicSessionValidator();
        var users = new MemoryUsers();
        var sessions = new MemorySessions();
        sessions.setSessionDuration(120000);
        validator.setSessionManager(sessions);
        validator.setCredentialsManager(users);

        var auth_data = "98.232.12.456";

        String auth_id1 = null;
        String auth_id2 = null;
        String auth_id3 = null;
        try {
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
        } catch (SessionManagerException | SessionValidatorException | CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    static class DummyAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return false;
        }

        public String getAttribute(String key) {
            return null;
        }
    }

    static class RoleMaintAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "maint";
            }

            return null;
        }
    }

    static class RoleAdminAttributes implements SessionAttributes {
        public boolean hasAttribute(String key) {
            return key.equals("role");
        }

        public String getAttribute(String key) {
            if (key.equals("role")) {
                return "admin";
            }

            return null;
        }
    }
}
