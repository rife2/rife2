/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class ContainsRoleErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 793122840724780390L;

    private final String role_;

    public ContainsRoleErrorException(String role) {
        this(role, null);
    }

    public ContainsRoleErrorException(String role, DatabaseException cause) {
        super("Error while checking if role '" + role + "' is present.", cause);
        role_ = role;
    }

    public String getRole() {
        return role_;
    }
}
