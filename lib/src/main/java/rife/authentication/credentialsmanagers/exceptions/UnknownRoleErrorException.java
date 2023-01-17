/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class UnknownRoleErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -1534822390523586792L;

    private final String role_;
    private final String login_;
    private final RoleUserAttributes attributes_;

    public UnknownRoleErrorException(String role, String login, RoleUserAttributes attributes) {
        this(role, login, attributes, null);
    }

    public UnknownRoleErrorException(String role, String login, RoleUserAttributes attributes, Throwable cause) {
        super("The role '" + role + "' couldn't be found while adding the adding user with login '" + login + "' and attributes '" + attributes + "'.", cause);
        role_ = role;
        login_ = login;
        attributes_ = attributes;
    }

    public String getLogin() {
        return login_;
    }

    public String getRole() {
        return role_;
    }

    public RoleUserAttributes getAttributes() {
        return attributes_;
    }
}
