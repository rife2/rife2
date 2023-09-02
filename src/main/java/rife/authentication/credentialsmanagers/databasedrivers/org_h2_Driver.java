/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.databasedrivers;

import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.credentialsmanagers.exceptions.DuplicateLoginException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateRoleException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateUserIdException;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.Datasource;

public class org_h2_Driver extends generic {
    public org_h2_Driver(Datasource datasource) {
        super(datasource);
    }

    @Override
    public DatabaseUsers addRole(String role)
    throws CredentialsManagerException {
        try {
            _addRole(getNewRoleId_, addRole_, role);
        } catch (CredentialsManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains("AUTHROLE_NAME_UQ_INDEX")) {
                    throw new DuplicateRoleException(role, e);
                }
            }

            throw e;
        }

        return this;
    }

    @Override
    public DatabaseUsers addUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        try {
            _addUser(addUserWithId_, getFreeUserId_, getRoleId_, addRoleLink_, login, attributes);
        } catch (CredentialsManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains("AUTHUSER_LOGIN_UQ_INDEX")) {
                    throw new DuplicateLoginException(login, e);
                }
                if (message.contains("PRIMARY KEY ON")) {
                    throw new DuplicateUserIdException(attributes.getUserId(), e);
                }
            }

            throw e;
        }

        return this;
    }
}
