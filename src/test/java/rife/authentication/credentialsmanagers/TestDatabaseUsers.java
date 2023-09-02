/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import rife.authentication.credentials.RoleUser;
import rife.authentication.credentialsmanagers.exceptions.DuplicateLoginException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateRoleException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateUserIdException;
import rife.authentication.credentialsmanagers.exceptions.UnknownRoleErrorException;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.Datasource;
import rife.database.TestDatasources;
import rife.tools.ExceptionUtils;
import rife.tools.StringEncryptor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseUsers {
    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstantiation(Datasource datasource) {
        var manager = DatabaseUsersFactory.instance(datasource);
        assertNotNull(manager);
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testInstall(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            assertTrue(users.install());
            users.remove();
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemove(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();
            assertTrue(users.remove());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAddRoles(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRolesList(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var listroles = new ListDatabaseRoles();
            assertTrue(users.listRoles(listroles));
            assertEquals(3, listroles.getRoles().size());
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
            assertTrue(listroles.getRoles().contains("role3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    static class ListDatabaseRoles implements ListRoles {
        private final ArrayList<String> roles_ = new ArrayList<>();

        public ArrayList<String> getRoles() {
            return roles_;
        }

        @Override
        public boolean foundRole(String name) {
            roles_.add(name);

            return true;
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testAddUsers(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
            assertEquals(175, user4_attributes.getUserId());
            try {
                var user5_attributes = new RoleUserAttributes("thepassword5", new String[]{"role1"});
                users.addUser("login1", user5_attributes);
                fail();
            } catch (DuplicateLoginException e) {
                assertEquals(e.getLogin(), "login1");
            }

            var user6_attributes = new RoleUserAttributes("thepassword6", new String[]{"role_unknown"});
            try {
                users.addUser("login6", user6_attributes);
                fail();
            } catch (UnknownRoleErrorException e) {
                assertEquals(e.getRole(), "role_unknown");
                assertEquals(e.getLogin(), "login6");
                assertEquals(e.getAttributes(), user6_attributes);
            }

            assertEquals(4, users.countUsers());

            assertTrue(users.containsUser("login1"));
            assertTrue(users.containsUser("login2"));
            assertTrue(users.containsUser("login3"));
            assertTrue(users.containsUser("login4"));
            assertFalse(users.containsUser("login5"));

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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUpdateUsers(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
            var user5_attributes_new = new RoleUserAttributes(5, new String[]{"role_unknown"});
            assertTrue(users.updateUser("login1", user1_attributes_new));
            assertTrue(users.updateUser("login2", user2_attributes_new));
            assertTrue(users.updateUser("login3", user3_attributes_new));
            assertTrue(users.updateUser("login4", user4_attributes_new));

            try {
                users.updateUser("login4", user5_attributes_new);
                fail();
            } catch (UnknownRoleErrorException e) {
                assertEquals(e.getRole(), "role_unknown");
                assertEquals(e.getLogin(), "login4");
                assertEquals(e.getAttributes(), user5_attributes_new);
            }

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
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testGetUserAttributes(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(0, "thepassword");
            var user2_attributes = new RoleUserAttributes(1, "thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes);

            var attributes1 = users.getAttributes("login1");
            var attributes2 = users.getAttributes("login2");
            var attributes3 = users.getAttributes("login3");

            assertEquals(attributes1.getUserId(), user1_attributes.getUserId());
            assertEquals(attributes1.getRoles(), user1_attributes.getRoles());
            assertEquals(attributes1.getPassword(), user1_attributes.getPassword());

            assertEquals(attributes2.getUserId(), user2_attributes.getUserId());
            assertEquals(attributes2.getRoles().size(), user2_attributes.getRoles().size());
            assertTrue(attributes2.getRoles().contains("role1"));
            assertTrue(attributes2.getRoles().contains("role2"));
            assertEquals(attributes2.getPassword(), user2_attributes.getPassword());

            assertEquals(attributes3.getUserId(), user3_attributes.getUserId());
            assertEquals(attributes3.getRoles().size(), user3_attributes.getRoles().size());
            assertTrue(attributes3.getRoles().contains("role1"));
            assertTrue(attributes3.getRoles().contains("role2"));
            assertTrue(attributes3.getRoles().contains("role3"));
            assertEquals(attributes3.getPassword(), user3_attributes.getPassword());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUsersList(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(0, "thepassword");
            var user2_attributes = new RoleUserAttributes(1, "thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes);

            var listusers = new ListDatabaseUsers();
            assertTrue(users.listUsers(listusers));
            assertEquals(3, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("2,login3,thepassword3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUsersListRanged(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(0, "thepassword");
            var user2_attributes = new RoleUserAttributes(1, "thepassword2", new String[]{"role1", "role2"});
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
            var user4_attributes = new RoleUserAttributes("thepassword4", new String[]{"role1", "role2"});
            users
                .addUser("login1", user1_attributes)
                .addUser("login2", user2_attributes)
                .addUser("login3", user3_attributes)
                .addUser("login4", user4_attributes);

            ListDatabaseUsers listusers = null;

            listusers = new ListDatabaseUsers();
            assertTrue(users.listUsers(listusers, 2, 1));
            assertEquals(2, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("2,login3,thepassword3"));

            listusers = new ListDatabaseUsers();
            assertTrue(users.listUsers(listusers, 3, 0));
            assertEquals(3, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("0,login1,thepassword"));
            assertTrue(listusers.getUsers().contains("1,login2,thepassword2"));
            assertTrue(listusers.getUsers().contains("2,login3,thepassword3"));

            listusers = new ListDatabaseUsers();
            assertFalse(users.listUsers(listusers, 0, 3));
            assertEquals(0, listusers.getUsers().size());

            listusers = new ListDatabaseUsers();
            assertTrue(users.listUsers(listusers, 2, 3));
            assertEquals(1, listusers.getUsers().size());
            assertTrue(listusers.getUsers().contains("3,login4,thepassword4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    static class ListDatabaseUsers implements ListUsers {
        private final ArrayList<String> users_ = new ArrayList<>();

        public ArrayList<String> getUsers() {
            return users_;
        }

        @Override
        public boolean foundUser(long userId, String login, String password) {
            users_.add(userId + "," + login + "," + password);

            return true;
        }
    }


    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUserIdSpecification(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidUsers(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);
        users.setPasswordEncryptor(StringEncryptor.SHA);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(0, "thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes(1, "SHA:iTeooS7tJ7m1mdRrbUacq/pr1uM=", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            users.setPasswordEncryptor(null);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testValidUsersDrupal(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);
        users.setPasswordEncryptor(StringEncryptor.DRUPAL);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes(0, "thepassword");
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes(1, "$S$D4PAggxs.rhAU1xLj2vt6swy4fGrY0qNHinb0Om0N9U7OdZAnKqP", new String[]{"role1", "role2"});
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes(2, "thepassword3", new String[]{"role1", "role2", "role3"});
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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            users.setPasswordEncryptor(null);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testUsersInRole(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);
        users.setPasswordEncryptor(StringEncryptor.MD5);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var user1_attributes = new RoleUserAttributes("thepassword");
            user1_attributes.setUserId(0);
            users.addUser("login1", user1_attributes);
            var user2_attributes = new RoleUserAttributes("thepassword2", new String[]{"role1", "role2"});
            user2_attributes.setUserId(43);
            users.addUser("login2", user2_attributes);
            var user3_attributes = new RoleUserAttributes("thepassword3", new String[]{"role1", "role2", "role3"});
            user3_attributes.setUserId(23);
            users.addUser("login3", user3_attributes);
            var user4_attributes = new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"});
            user4_attributes.setUserId(98);
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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            users.setPasswordEncryptor(null);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testListUsersInRole(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3");

            var list_users = new ListDatabaseUsers();

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
            assertFalse(users.listUsersInRole(list_users, null));
            assertFalse(users.listUsersInRole(list_users, ""));

            assertTrue(users.listUsersInRole(list_users, "role1"));
            assertEquals(2, list_users.getUsers().size());
            assertFalse(list_users.getUsers().contains("0,login1,thepassword"));
            assertTrue(list_users.getUsers().contains("1,login2,thepassword2"));
            assertTrue(list_users.getUsers().contains("174,login3,thepassword3"));
            assertFalse(list_users.getUsers().contains("175,login4,thepassword4"));

            list_users = new ListDatabaseUsers();
            assertTrue(users.listUsersInRole(list_users, "role2"));
            assertEquals(3, list_users.getUsers().size());
            assertFalse(list_users.getUsers().contains("0,login1,thepassword"));
            assertTrue(list_users.getUsers().contains("1,login2,thepassword2"));
            assertTrue(list_users.getUsers().contains("174,login3,thepassword3"));
            assertTrue(list_users.getUsers().contains("175,login4,thepassword4"));

            list_users = new ListDatabaseUsers();
            assertTrue(users.listUsersInRole(list_users, "role3"));
            assertEquals(2, list_users.getUsers().size());
            assertFalse(list_users.getUsers().contains("0,login1,thepassword"));
            assertFalse(list_users.getUsers().contains("1,login2,thepassword2"));
            assertTrue(list_users.getUsers().contains("174,login3,thepassword3"));
            assertTrue(list_users.getUsers().contains("175,login4,thepassword4"));

            list_users = new ListDatabaseUsers();
            assertFalse(users.listUsersInRole(list_users, "role4"));
            assertEquals(0, list_users.getUsers().size());
            assertFalse(list_users.getUsers().contains("0,login1,thepassword"));
            assertFalse(list_users.getUsers().contains("1,login2,thepassword2"));
            assertFalse(list_users.getUsers().contains("174,login3,thepassword3"));
            assertFalse(list_users.getUsers().contains("2,login4,thepassword4"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemoveUsersByLogin(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes(1, "thepassword"))
                .addUser("login2", new RoleUserAttributes(23, "thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes(14, "thepassword3", new String[]{"role1", "role2", "role3"}))
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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemoveUsersByUserId(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testRemoveRole(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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
            assertEquals(0, attributes.getRoles().size());

            attributes = users.getAttributes("login2");
            assertTrue(attributes.getRoles().contains("role1"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testClearUsers(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

            users
                .addRole("role1")
                .addRole("role2")
                .addRole("role3")
                .addUser("login1", new RoleUserAttributes(43, "thepassword"))
                .addUser("login2", new RoleUserAttributes(432, "thepassword2", new String[]{"role1", "role2"}))
                .addUser("login3", new RoleUserAttributes(1, "thepassword3", new String[]{"role1", "role2", "role3"}))
                .addUser("login4", new RoleUserAttributes(174, "thepassword4", new String[]{"role2", "role3"}));

            assertEquals(4, users.countUsers());
            users.clearUsers();
            assertEquals(0, users.countUsers());
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testVerifyCredentials(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);
        users.setPasswordEncryptor(StringEncryptor.OBF);

        try {
            users.install();

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
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
            users.setPasswordEncryptor(null);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(TestDatasources.class)
    void testListUserRoles(Datasource datasource) {
        var users = DatabaseUsersFactory.instance(datasource);

        try {
            users.install();

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

            var listroles = new ListDatabaseRoles();

            assertFalse(users.listUserRoles("login1", listroles));

            assertTrue(users.listUserRoles("login2", listroles));
            assertEquals(listroles.getRoles().size(), 2);
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));

            listroles = new ListDatabaseRoles();

            assertTrue(users.listUserRoles("login3", listroles));
            assertEquals(listroles.getRoles().size(), 3);
            assertTrue(listroles.getRoles().contains("role1"));
            assertTrue(listroles.getRoles().contains("role2"));
            assertTrue(listroles.getRoles().contains("role3"));
        } catch (CredentialsManagerException e) {
            fail(ExceptionUtils.getExceptionStackTrace(e));
        } finally {
            try {
                users.remove();
            } catch (CredentialsManagerException e) {
                fail(ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}


