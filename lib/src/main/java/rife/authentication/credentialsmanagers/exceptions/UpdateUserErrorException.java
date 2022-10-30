/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class UpdateUserErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 644224221069008281L;

    private final String login_;
    private final RoleUserAttributes attributes_;

    public UpdateUserErrorException(String role, RoleUserAttributes attributes) {
        this(role, attributes, null);
    }

    public UpdateUserErrorException(String login, RoleUserAttributes attributes, Throwable cause) {
        super("Error while updating user with login '" + login + "'.", cause);
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
