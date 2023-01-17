/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import org.junit.jupiter.api.Test;
import rife.authentication.credentials.RoleUser;
import rife.authentication.credentialsmanagers.exceptions.DuplicateLoginException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateRoleException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateUserIdException;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.tools.ExceptionUtils;
import rife.tools.StringEncryptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestMemoryUsers {
    @Test
    void testInstantiation() {
        var users = new MemoryUsers();

        assertNotNull(users);
    }

    @Test
    void testNoInitialUsers() {
        var users = new MemoryUsers();

        assertEquals(0, users.countUsers());
    }

    @Test
    void testAddRoles() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");
            try {
                users.addRole("role2");
                fail();
            } catch (DuplicateRoleException e) {
                assertEquals("role2", e.getRole());
            }

            assertEquals(3, users.countRoles());

            assertTrue(users.containsRole("role1"));
            assertTrue(users.containsRole("role2"));
            assertTrue(users.containsRole("role3"));
            assertFalse(users.containsRole("role4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testAddUsers() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            users.addUser("login1", user1_attributes);
            assertEquals(0, user1_attributes.getUserId());
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            assertEquals(1, user2_attributes.getUserId());
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            users.addUser("login3", user3_attributes);
            assertEquals(174, user3_attributes.getUserId());
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users.addUser("login4", user4_attributes);
            assertEquals(2, user4_attributes.getUserId());
            try {
                var user5_attributes = new RoleUserAttributes("thepassword5", new String[]{"role1"});
                users.addUser("login1", user5_attributes);
                fail();
            } catch (DuplicateLoginException e) {
                assertEquals(e.getLogin(), "login1");
            }

            assertEquals(4, users.countUsers());

            assertTrue(users.containsUser("login1"));
            assertTrue(users.containsUser("login2"));
            assertTrue(users.containsUser("login3"));
            assertTrue(users.containsUser("login4"));
            assertFalse(users.containsUser("login5"));

            assertEquals(3, users.countRoles());

            assertTrue(users.containsRole("role1"));
            assertTrue(users.containsRole("role2"));
            assertTrue(users.containsRole("role3"));
            assertFalse(users.containsRole("role4"));

            assertEquals(users.getUserId("login1"), user1_attributes.getUserId());
            assertEquals(users.getUserId("login2"), user2_attributes.getUserId());
            assertEquals(users.getUserId("login3"), user3_attributes.getUserId());
            assertEquals(users.getUserId("login4"), user4_attributes.getUserId());

            assertEquals(users.getLogin(user1_attributes.getUserId()), "login1");
            assertEquals(users.getLogin(user2_attributes.getUserId()), "login2");
            assertEquals(users.getLogin(user3_attributes.getUserId()), "login3");
            assertEquals(users.getLogin(user4_attributes.getUserId()), "login4");
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testUpdateUsers() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            var user1_attributes_new = new RoleUserAttributes(4, "thepassword_new", new String[]{"role1", "role2"});
            var user2_attributes_new = new RoleUserAttributes(3, new String[]{"role2"});
            var user3_attributes_new = new RoleUserAttributes(2, new String[]{"role1"});
            var user4_attributes_new = new RoleUserAttributes(1, "thepassword_new4");
            assertTrue(users.updateUser("login1", user1_attributes_new));
            assertTrue(users.updateUser("login2", user2_attributes_new));
            assertTrue(users.updateUser("login3", user3_attributes_new));
            assertTrue(users.updateUser("login4", user4_attributes_new));

            assertEquals(4, users.countUsers());

            assertTrue(users.containsUser("login1"));
            assertTrue(users.containsUser("login2"));
            assertTrue(users.containsUser("login3"));
            assertTrue(users.containsUser("login4"));
            assertFalse(users.containsUser("login5"));

            assertEquals(users.getAttributes("login1").getPassword(), user1_attributes_new.getPassword());
            assertEquals(users.getAttributes("login1").getUserId(), user1_attributes.getUserId());
            assertEquals(users.getAttributes("login1").getRoles().size(), user1_attributes_new.getRoles().size());
            assertTrue(users.getAttributes("login1").getRoles().contains("role1"));
            assertTrue(users.getAttributes("login1").getRoles().contains("role2"));
            assertEquals(users.getAttributes("login2").getPassword(), user2_attributes.getPassword());
            assertEquals(users.getAttributes("login2").getUserId(), user2_attributes.getUserId());
            assertEquals(users.getAttributes("login2").getRoles().size(), user2_attributes_new.getRoles().size());
            assertTrue(users.getAttributes("login2").getRoles().contains("role2"));
            assertEquals(users.getAttributes("login3").getPassword(), user3_attributes.getPassword());
            assertEquals(users.getAttributes("login3").getUserId(), user3_attributes.getUserId());
            assertEquals(users.getAttributes("login3").getRoles().size(), user3_attributes_new.getRoles().size());
            assertTrue(users.getAttributes("login3").getRoles().contains("role1"));
            assertEquals(users.getAttributes("login4").getPassword(), user4_attributes_new.getPassword());
            assertEquals(users.getAttributes("login4").getUserId(), user4_attributes.getUserId());
            assertEquals(0, users.getAttributes("login4").getRoles().size());

            assertEquals(2, users.countRoles());

            assertTrue(users.containsRole("role1"));
            assertTrue(users.containsRole("role2"));
            assertFalse(users.containsRole("role3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListRoles() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            ListMemoryRoles listroles = null;

            listroles = new ListMemoryRoles();
            assertTrue(users.listRoles(listroles));
            assertEquals(3, listroles.getRoles().size());
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
            assertTrue(listroles.getRoles().contains("role3"));

            users.removeUser("login4");

            listroles = new ListMemoryRoles();
            assertTrue(users.listRoles(listroles));
            assertEquals(3, listroles.getRoles().size());
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
            assertTrue(listroles.getRoles().contains("role3"));

            users.removeUser("login3");

            listroles = new ListMemoryRoles();
            assertTrue(users.listRoles(listroles));
            assertEquals(2, listroles.getRoles().size());
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListUsers() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            var listusers = new ListMemoryUsers();
            assertTrue(users.listUsers(listusers));
            assertEquals(4, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));
            assertTrue(listusers.getUsers().contains("2,login4,thepassword4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListUsersRanged() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            ListMemoryUsers listusers = null;

            listusers = new ListMemoryUsers();
            assertTrue(users.listUsers(listusers, 2, 1));
            assertEquals(2, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));

            listusers = new ListMemoryUsers();
            assertTrue(users.listUsers(listusers, 3, 0));
            assertEquals(3, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));

            listusers = new ListMemoryUsers();
            assertFalse(users.listUsers(listusers, 0, 3));
            assertEquals(0, listusers.getUsers().size());

            listusers = new ListMemoryUsers();
            assertTrue(users.listUsers(listusers, 2, 3));
            assertEquals(1, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("2,login4,thepassword4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    static class ListMemoryRoles implements ListRoles {
        private final ArrayList<String> roles_ = new ArrayList<>();

        public ArrayList<String> getRoles() {
            return roles_;
        }

        public boolean foundRole(String name) {
            roles_.add(name);

            return true;
        }
    }

    static class ListMemoryUsers implements ListUsers {
        private final ArrayList<String> users_ = new ArrayList<>();

        public ArrayList<String> getUsers() {
            return users_;
        }

        public boolean foundUser(long userId, String login, String password) {
            users_.add(userId + "," + login + "," + password);

            return true;
        }
    }

    @Test
    void testGetUserAttributes() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            var attributes1 = users.getAttributes("login1");
            var attributes2 = users.getAttributes("login2");
            var attributes3 = users.getAttributes("login3");
            var attributes4 = users.getAttributes("login4");
            assertEquals(attributes1.getUserId(), 0);
            assertEquals(0, attributes1.getRoles().size());
            assertEquals(attributes1.getPassword(), "thepassword");

            assertEquals(attributes2.getUserId(), 1);
            assertEquals(attributes2.getRoles().size(), 2);
            assertTrue(attributes2.getRoles().contains("role1"));
            assertTrue(attributes2.getRoles().contains("role2"));
            assertEquals(attributes2.getPassword(), "thepassword2");

            assertEquals(attributes3.getUserId(), 174);
            assertEquals(attributes3.getRoles().size(), 3);
            assertTrue(attributes3.getRoles().contains("role1"));
            assertTrue(attributes3.getRoles().contains("role2"));
            assertTrue(attributes3.getRoles().contains("role3"));
            assertEquals(attributes3.getPassword(), "thepassword3");

            assertEquals(attributes4.getUserId(), 2);
            assertEquals(attributes4.getRoles().size(), 2);
            assertTrue(attributes4.getRoles().contains("role2"));
            assertTrue(attributes4.getRoles().contains("role3"));
            assertEquals(attributes4.getPassword(), "thepassword4");
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testUserIdSpecification() {
        var users = new MemoryUsers();

        try {
            users
                .addUser("login1", new RoleUserAttributes(0, "thepassword"))
                .addUser("login2", new RoleUserAttributes("thepassword"));
            try {
                users.addUser("login3", new RoleUserAttributes(1, "thepassword"));
                fail();
            } catch (DuplicateUserIdException e) {
                assertTrue(true);
            }
            users
                .addUser("login4", new RoleUserAttributes(2, "thepassword"))
                .addUser("login5", new RoleUserAttributes("thepassword"))
                .addUser("login6", new RoleUserAttributes(847, "thepassword"));

            assertTrue(users.containsUser("login1"));
            assertTrue(users.containsUser("login2"));
            assertFalse(users.containsUser("login3"));
            assertTrue(users.containsUser("login4"));
            assertTrue(users.containsUser("login5"));
            assertTrue(users.containsUser("login6"));

            assertEquals("login1", users.getLogin(0));
            assertEquals("login2", users.getLogin(1));
            assertEquals("login4", users.getLogin(2));
            assertEquals("login5", users.getLogin(3));
            assertEquals("login6", users.getLogin(847));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testValidUsers() {
        var users = new MemoryUsers();
        users.setPasswordEncryptor(StringEncryptor.SHA);

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes("SHA:iTeooS7tJ7m1mdRrbUacq/pr1uM=", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"});
            users.addUser("login3", user3_attributes);
            var user4_attributes = new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"});
            users.addUser("login4", user4_attributes);

            assertEquals(-1, users.verifyCredentials(new RoleUser("login", "thepassword")));

            assertEquals(0, users.verifyCredentials(new RoleUser("login1", "thepassword")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login1", "thepassword2")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login1", "thepassword", "role1")));

            assertEquals(1, users.verifyCredentials(new RoleUser("login2", "thepassword2")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login2", "thepassword3")));
            assertEquals(1, users.verifyCredentials(new RoleUser("login2", "thepassword2", "role1")));
            assertEquals(1, users.verifyCredentials(new RoleUser("login2", "thepassword2", "role2")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login2", "thepassword2", "role3")));

            assertEquals(2, users.verifyCredentials(new RoleUser("login3", "thepassword3")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login3", "thepassword4")));
            assertEquals(2, users.verifyCredentials(new RoleUser("login3", "thepassword3", "role1")));
            assertEquals(2, users.verifyCredentials(new RoleUser("login3", "thepassword3", "role2")));
            assertEquals(2, users.verifyCredentials(new RoleUser("login3", "thepassword3", "role3")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login3", "thepassword3", "role4")));

            assertEquals(174, users.verifyCredentials(new RoleUser("login4", "thepassword4")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login4", "thepassword")));
            assertEquals(-1, users.verifyCredentials(new RoleUser("login4", "thepassword4", "role1")));
            assertEquals(174, users.verifyCredentials(new RoleUser("login4", "thepassword4", "role2")));
            assertEquals(174, users.verifyCredentials(new RoleUser("login4", "thepassword4", "role3")));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testUsersInRole() {
        var users = new MemoryUsers();
        users.setPasswordEncryptor(StringEncryptor.MD5);

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"});
            users.addUser("login3", user3_attributes);
            var user4_attributes = new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"});
            users.addUser("login4", user4_attributes);

            assertFalse(users.isUserInRole(user1_attributes.getUserId(), "role1"));
            assertTrue(users.isUserInRole(user2_attributes.getUserId(), "role1"));
            assertTrue(users.isUserInRole(user3_attributes.getUserId(), "role1"));
            assertFalse(users.isUserInRole(user4_attributes.getUserId(), "role1"));

            assertFalse(users.isUserInRole(user1_attributes.getUserId(), "role2"));
            assertTrue(users.isUserInRole(user2_attributes.getUserId(), "role2"));
            assertTrue(users.isUserInRole(user3_attributes.getUserId(), "role2"));
            assertTrue(users.isUserInRole(user4_attributes.getUserId(), "role2"));

            assertFalse(users.isUserInRole(user1_attributes.getUserId(), "role3"));
            assertFalse(users.isUserInRole(user2_attributes.getUserId(), "role3"));
            assertTrue(users.isUserInRole(user3_attributes.getUserId(), "role3"));
            assertTrue(users.isUserInRole(user4_attributes.getUserId(), "role3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListUsersInRole() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var listusers = new ListMemoryUsers();

            var user1_attributes = new RoleUserAttributes("thepassword");
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(174, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            assertFalse(users.listUsersInRole(null, "role1"));
            assertFalse(users.listUsersInRole(listusers, null));
            assertFalse(users.listUsersInRole(listusers, ""));

            assertTrue(users.listUsersInRole(listusers, "role1"));
            assertEquals(2, listusers.getUsers().size());
            assertFalse(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));
            assertFalse(listusers.getUsers().contains("2,login4,thepassword4"));

            listusers = new ListMemoryUsers();
            assertTrue(users.listUsersInRole(listusers, "role2"));
            assertEquals(3, listusers.getUsers().size());
            assertFalse(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));
            assertTrue(listusers.getUsers().contains("2,login4,thepassword4"));

            listusers = new ListMemoryUsers();
            assertTrue(users.listUsersInRole(listusers, "role3"));
            assertEquals(2, listusers.getUsers().size());
            assertFalse(listusers.getUsers().contains("0,login1,thepassword"));
            assertFalse(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("174,login3,thepassword3"));
            assertTrue(listusers.getUsers().contains("2,login4,thepassword4"));

            listusers = new ListMemoryUsers();
            assertFalse(users.listUsersInRole(listusers, "role4"));
            assertEquals(0, listusers.getUsers().size());
            assertFalse(listusers.getUsers().contains("0,login1,thepassword"));
            assertFalse(listusers.getUsers().contains("1,login2,thepassword2"));
            assertFalse(listusers.getUsers().contains("174,login3,thepassword3"));
            assertFalse(listusers.getUsers().contains("2,login4,thepassword4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testRemoveUsersByLogin() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes("thepassword"))
                .addUser("login2", new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"}))
                .addUser("login4", new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"}));

            assertEquals(4, users.countUsers());
            assertFalse(users.removeUser("login5"));
            assertEquals(4, users.countUsers());
            assertTrue(users.removeUser("login4"));
            assertEquals(3, users.countUsers());
            assertTrue(users.removeUser("login1"));
            assertEquals(2, users.countUsers());
            assertTrue(users.removeUser("login2"));
            assertEquals(1, users.countUsers());
            assertTrue(users.removeUser("login3"));
            assertEquals(0, users.countUsers());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testRemoveUsersByUserId() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes(1, "thepassword"))
                .addUser("login2", new RoleUserAttributes(23, "thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes(14, "thepassword3", new String[]{"role1", "role2", "role3"}))
                .addUser("login4", new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"}));

            assertEquals(4, users.countUsers());
            assertFalse(users.removeUser(5));
            assertEquals(4, users.countUsers());
            assertTrue(users.removeUser(174));
            assertEquals(3, users.countUsers());
            assertTrue(users.removeUser(1));
            assertEquals(2, users.countUsers());
            assertTrue(users.removeUser(23));
            assertEquals(1, users.countUsers());
            assertTrue(users.removeUser(14));
            assertEquals(0, users.countUsers());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testRemoveRole() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes(1, "thepassword"))
                .addUser("login2", new RoleUserAttributes(23, "thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes(14, "thepassword3", new String[]{"role1", "role2", "role3"}))
                .addUser("login4", new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"}));

            assertEquals(3, users.countRoles());
            assertFalse(users.removeRole("role4"));
            assertEquals(3, users.countRoles());
            assertTrue(users.removeRole("role3"));
            assertEquals(2, users.countRoles());

            var attributes = users.getAttributes("login3");
            assertTrue(attributes.getRoles().contains("role1"));
            assertTrue(attributes.getRoles().contains("role2"));
            assertFalse(attributes.getRoles().contains("role3"));

            assertTrue(users.removeRole("role2"));
            attributes = users.getAttributes("login4");
            assertEquals(attributes.getRoles().size(), 0);

            attributes = users.getAttributes("login2");
            assertTrue(attributes.getRoles().contains("role1"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testClearUsers() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes("thepassword"))
                .addUser("login2", new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"}))
                .addUser("login4", new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"}));

            assertEquals(4, users.countUsers());
            users.clearUsers();
            assertEquals(0, users.countUsers());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testVerifyCredentials() {
        var users = new MemoryUsers();
        users.setPasswordEncryptor(StringEncryptor.OBF);

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"});
            users.addUser("login3", user3_attributes);

            var user = new RoleUser();
            user.setLogin("login2");
            user.setPassword("thepassword2");
            user.setRole("role2");

            assertEquals(user2_attributes.getUserId(), users.verifyCredentials(user));

            user.setRole(null);

            assertEquals(user2_attributes.getUserId(), users.verifyCredentials(user));

            user.setRole("role3");

            assertEquals(-1, users.verifyCredentials(user));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @Test
    void testListUserRoles() {
        var users = new MemoryUsers();

        try {
            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(49, "thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes(322, "thepassword2", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
            users.addUser("login3", user3_attributes);

            var listroles = new ListMemoryRoles();

            assertFalse(users.listUserRoles("login1", listroles));

            assertTrue(users.listUserRoles("login2", listroles));
            assertEquals(listroles.getRoles().size(), 2);
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));

            listroles = new ListMemoryRoles();

            assertTrue(users.listUserRoles("login3", listroles));
            assertEquals(listroles.getRoles().size(), 3);
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
            assertTrue(listroles.getRoles().contains("role3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }
}


