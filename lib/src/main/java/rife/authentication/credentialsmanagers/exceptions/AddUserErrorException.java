/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class AddUserErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = -2931184377699186549L;

    private final String login_;
    private final RoleUserAttributes attributes_;

    public AddUserErrorException(String login, RoleUserAttributes attributes) {
        this(login, attributes, null);
    }

    public AddUserErrorException(String login, RoleUserAttributes attributes, Throwable cause) {
        super("Error while adding user with login '" + login + "' and attributes '" + attributes + "'.", cause);
        login_ = login;
        attributes_ = attributes;
    }

    public String getLogin() {
        return login_;
    }

    public RoleUserAttributes getAttributes() {
        return attributes_;
    }
}
