/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.exceptions.DatabaseException;

import java.io.Serial;

public class RemoveRoleErrorException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 1481690228170243838L;

    private final String name_;

    public RemoveRoleErrorException(String role) {
        this(role, null);
    }

    public RemoveRoleErrorException(String name, DatabaseException cause) {
        super("Error while removing role with name '" + name + "'.", cause);
        name_ = name;
    }

    public String getName() {
        return name_;
    }
}
