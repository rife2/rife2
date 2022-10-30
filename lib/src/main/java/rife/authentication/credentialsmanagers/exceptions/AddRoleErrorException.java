/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class AddRoleErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 5644080575833305836L;

    private final String role_;

    public AddRoleErrorException(String role) {
        this(role, null);
    }

    public AddRoleErrorException(String role, DatabaseException cause) {
        super("Error while adding role '" + role + "'.", cause);
        role_ = role;
    }

    public String getRole() {
        return role_;
    }
}
