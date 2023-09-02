/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class DuplicateRoleException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 3859722464041667678L;

    private final String role_;

    public DuplicateRoleException(String role) {
        this(role, null);
    }

    public DuplicateRoleException(String role, Throwable e) {
        super("The role '" + role + "' is already present.", e);

        role_ = role;
    }

    public String getRole() {
        return role_;
    }
}
