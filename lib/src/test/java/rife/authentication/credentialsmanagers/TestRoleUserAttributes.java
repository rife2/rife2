/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestRoleUserAttributes {
    @Test
    void testInstantiation() {
        RoleUserAttributes user_attributes = null;

        user_attributes = new RoleUserAttributes("thepassword");
        assertNotNull(user_attributes);
        assertEquals("thepassword", user_attributes.getPassword());

        var roles = new ArrayList<String>();
        roles.add("firstrole");
        roles.add("secondrole");
        user_attributes = new RoleUserAttributes("thepassword", roles);
        assertNotNull(user_attributes);
        assertEquals("thepassword", user_attributes.getPassword());

        assertEquals(2, user_attributes.getRoles().size());
        var firstrole = false;
        var secondrole = false;
        for (var role : user_attributes.getRoles()) {
            if (role.equals("firstrole")) {
                firstrole = true;
            } else if (role.equals("secondrole")) {
                secondrole = true;
            }
        }
        assertTrue(firstrole && secondrole);

        user_attributes = new RoleUserAttributes("thepassword", new String[]{"firstrole", "secondrole"});
        assertNotNull(user_attributes);
        assertEquals("thepassword", user_attributes.getPassword());

        assertEquals(2, user_attributes.getRoles().size());
        firstrole = false;
        secondrole = false;
        for (var role : user_attributes.getRoles()) {
            if (role.equals("firstrole")) {
                firstrole = true;
            } else if (role.equals("secondrole")) {
                secondrole = true;
            }
        }
        assertTrue(firstrole && secondrole);
    }

    @Test
    void testEquals() {
        var user_attributes1 = new RoleUserAttributes("thepassword");
        var user_attributes2 = new RoleUserAttributes("thepassword");
        var user_attributes3 = new RoleUserAttributes("thepassword2");
        var user_attributes4 = new RoleUserAttributes(12, "thepassword2");
        var user_attributes5 = new RoleUserAttributes("thepassword", new String[]{"firstrole", "secondrole"});
        var user_attributes6 = new RoleUserAttributes("thepassword", new String[]{"firstrole", "secondrole"});
        var user_attributes7 = new RoleUserAttributes("thepassword", new String[]{"firstrole"});
        var user_attributes8 = new RoleUserAttributes("thepassword", new String[]{"firstrole", "thirdrole"});
        var user_attributes9 = new RoleUserAttributes(13, "thepassword", new String[]{"firstrole", "secondrole"});
        var user_attributes10 = new RoleUserAttributes(13, "thepassword", new String[]{"firstrole", "secondrole"});

        assertEquals(user_attributes1, user_attributes1);
        assertEquals(user_attributes1, user_attributes2);
        assertNotEquals(user_attributes1, user_attributes3);
        assertNotEquals(user_attributes1, user_attributes4);
        assertNotEquals(user_attributes1, user_attributes5);
        assertNotEquals(user_attributes1, user_attributes6);
        assertNotEquals(user_attributes1, user_attributes7);
        assertNotEquals(user_attributes1, user_attributes8);
        assertNotEquals(user_attributes1, user_attributes9);
        assertNotEquals(user_attributes1, user_attributes10);

        assertEquals(user_attributes2, user_attributes2);
        assertNotEquals(user_attributes2, user_attributes3);
        assertNotEquals(user_attributes2, user_attributes4);
        assertNotEquals(user_attributes2, user_attributes5);
        assertNotEquals(user_attributes2, user_attributes6);
        assertNotEquals(user_attributes2, user_attributes7);
        assertNotEquals(user_attributes2, user_attributes8);
        assertNotEquals(user_attributes2, user_attributes9);
        assertNotEquals(user_attributes2, user_attributes10);

        assertEquals(user_attributes3, user_attributes3);
        assertNotEquals(user_attributes3, user_attributes4);
        assertNotEquals(user_attributes3, user_attributes5);
        assertNotEquals(user_attributes3, user_attributes6);
        assertNotEquals(user_attributes3, user_attributes7);
        assertNotEquals(user_attributes3, user_attributes8);
        assertNotEquals(user_attributes3, user_attributes9);
        assertNotEquals(user_attributes3, user_attributes10);

        assertEquals(user_attributes4, user_attributes4);
        assertNotEquals(user_attributes4, user_attributes5);
        assertNotEquals(user_attributes4, user_attributes6);
        assertNotEquals(user_attributes4, user_attributes7);
        assertNotEquals(user_attributes4, user_attributes8);
        assertNotEquals(user_attributes4, user_attributes9);
        assertNotEquals(user_attributes4, user_attributes10);

        assertEquals(user_attributes5, user_attributes5);
        assertEquals(user_attributes5, user_attributes6);
        assertNotEquals(user_attributes5, user_attributes7);
        assertNotEquals(user_attributes5, user_attributes8);
        assertNotEquals(user_attributes5, user_attributes9);
        assertNotEquals(user_attributes5, user_attributes10);

        assertEquals(user_attributes6, user_attributes6);
        assertNotEquals(user_attributes6, user_attributes7);
        assertNotEquals(user_attributes6, user_attributes8);
        assertNotEquals(user_attributes6, user_attributes9);
        assertNotEquals(user_attributes6, user_attributes10);

        assertEquals(user_attributes7, user_attributes7);
        assertNotEquals(user_attributes7, user_attributes8);
        assertNotEquals(user_attributes7, user_attributes9);
        assertNotEquals(user_attributes7, user_attributes10);

        assertEquals(user_attributes8, user_attributes8);
        assertNotEquals(user_attributes8, user_attributes9);
        assertNotEquals(user_attributes8, user_attributes10);

        assertEquals(user_attributes9, user_attributes9);
        assertEquals(user_attributes9, user_attributes10);

        assertEquals(user_attributes10, user_attributes10);
    }

    @Test
    void testEmptyInitialRoles() {
        var user_attributes = new RoleUserAttributes("thepassword");

        assertEquals(0, user_attributes.getRoles().size());
    }

    @Test
    void testPopulate() {
        var user_attributes = new RoleUserAttributes("thepassword");

        var roles = new ArrayList<String>();
        roles.add("firstrole");
        roles.add("secondrole");
        user_attributes.setRoles(roles);

        assertEquals("thepassword", user_attributes.getPassword());

        assertEquals(2, user_attributes.getRoles().size());
        var firstrole = false;
        var secondrole = false;
        for (var role : user_attributes.getRoles()) {
            if (role.equals("firstrole")) {
                firstrole = true;
            } else if (role.equals("secondrole")) {
                secondrole = true;
            }
        }

        assertTrue(firstrole && secondrole);
    }

    @Test
    void testIsInRole() {
        var user_attributes = new RoleUserAttributes("thepassword");

        var roles = new ArrayList<String>();
        roles.add("firstrole");
        roles.add("secondrole");
        user_attributes.setRoles(roles);

        assertTrue(user_attributes.isInRole("firstrole"));
        assertTrue(user_attributes.isInRole("secondrole"));
        assertFalse(user_attributes.isInRole("thirdrole"));
    }

    @Test
    void testIsValid() {
        var user_attributes = new RoleUserAttributes("thepassword");

        var roles = new ArrayList<String>();
        roles.add("firstrole");
        roles.add("secondrole");
        user_attributes.setRoles(roles);

        assertTrue(user_attributes.isValid("thepassword"));
        assertFalse(user_attributes.isValid("anotherpassword"));
        assertTrue(user_attributes.isValid("thepassword", "firstrole"));
        assertTrue(user_attributes.isValid("thepassword", "secondrole"));
        assertFalse(user_attributes.isValid("anotherpassword", "firstrole"));
        assertFalse(user_attributes.isValid("anotherpassword", "secondrole"));
        assertFalse(user_attributes.isValid("thepassword", "thirdrole"));
        assertFalse(user_attributes.isValid("anotherpassword", "thirdrole"));
    }
}
