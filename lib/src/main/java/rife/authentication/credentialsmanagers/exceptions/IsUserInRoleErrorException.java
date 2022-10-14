/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class IsUserInRoleErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 8354257394797764661L;

    private final long userId_;
    private final String role_;

    public IsUserInRoleErrorException(long userId, String role) {
        this(userId, role, null);
    }

    public IsUserInRoleErrorException(long userId, String role, DatabaseException cause) {
        super("Error while verifying if the user id '" + userId + "' has access to role '" + role + "'.", cause);
        userId_ = userId;
        role_ = role;
    }

    public long getUserId() {
        return userId_;
    }

    public String getRole() {
        return role_;
    }
}
