/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentials;

import org.junit.jupiter.api.Test;
import rife.validation.ValidationError;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class TestRoleUser {
    @Test
    void testInstantiation() {
        RoleUser user = null;

        user = new RoleUser();

        assertNotNull(user);
    }

    @Test
    void testInitialEmptyLogin() {
        var user = new RoleUser();

        assertNull(user.getLogin());
    }

    @Test
    void testInitialEmptyPassword() {
        var user = new RoleUser();

        assertNull(user.getPassword());
    }

    @Test
    void testInitialEmptyRole() {
        var user = new RoleUser();

        assertNull(user.getRole());
    }

    @Test
    void testPopulation() {
        var user = new RoleUser();

        user.setLogin("the login");
        user.setPassword("the password");
        user.setRole("the role");

        assertEquals("the login", user.getLogin());
        assertEquals("the password", user.getPassword());
        assertEquals("the role", user.getRole());
    }

    @Test
    void testValidation() {
        var user = new RoleUser();

        Iterator<ValidationError> validationerrors_it = null;
        ValidationError validationerror = null;

        assertFalse(user.validate());

        validationerrors_it = user.getValidationErrors().iterator();
        assertTrue(validationerrors_it.hasNext());
        validationerror = validationerrors_it.next();
        assertEquals("mandatory", validationerror.getIdentifier());
        assertEquals("login", validationerror.getSubject());
        assertTrue(validationerrors_it.hasNext());
        validationerror = validationerrors_it.next();
        assertEquals("mandatory", validationerror.getIdentifier());
        assertEquals("password", validationerror.getSubject());
        assertFalse(validationerrors_it.hasNext());

        user.resetValidation();

        user.setLogin("e");
        user.setPassword("f");

        assertFalse(user.validate());

        validationerrors_it = user.getValidationErrors().iterator();
        assertTrue(validationerrors_it.hasNext());
        validationerror = validationerrors_it.next();
        assertEquals("wrongLength", validationerror.getIdentifier());
        assertEquals("login", validationerror.getSubject());
        assertTrue(validationerrors_it.hasNext());
        validationerror = validationerrors_it.next();
        assertEquals("wrongLength", validationerror.getIdentifier());
        assertEquals("password", validationerror.getSubject());
        assertFalse(validationerrors_it.hasNext());

        user.resetValidation();

        user.setLogin("alogin");
        user.setPassword("apassword");

        assertTrue(user.validate());

        validationerrors_it = user.getValidationErrors().iterator();
        assertFalse(validationerrors_it.hasNext());
    }
}
