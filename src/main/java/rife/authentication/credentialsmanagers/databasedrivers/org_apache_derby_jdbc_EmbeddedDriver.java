/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.databasedrivers;

import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.credentialsmanagers.exceptions.*;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.database.DbPreparedStatement;
import rife.database.DbPreparedStatementHandler;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Insert;
import rife.database.queries.Select;

public class org_apache_derby_jdbc_EmbeddedDriver extends generic {
    public org_apache_derby_jdbc_EmbeddedDriver(Datasource datasource) {
        super(datasource);

        createTableRole_
            .customAttribute("roleId", "GENERATED ALWAYS AS IDENTITY");

        createTableRoleLink_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRoleLink())
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("roleId", int.class, CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_PK", new String[]{"userId", "roleId"})
            .foreignKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_UI_FK", createTableUser_.getTable(), "userId", "userId", null, CreateTable.CASCADE)
            .foreignKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_RI_FK", createTableRole_.getTable(), "roleId", "roleId", null, CreateTable.CASCADE);

        addRole_ = new Insert(getDatasource())
            .into(createTableRole_.getTable())
            .fieldParameter("name");

        getFreeUserId_ = new Select(getDatasource())
            .field("CASE WHEN MAX(userId) IS NULL THEN 0 ELSE MAX(userId)+1 END AS freeUserId")
            .from(createTableUser_.getTable());
    }

    @Override
    public boolean install()
    throws CredentialsManagerException {
        try {
            executeUpdate(createTableRole_);
            executeUpdate(createTableUser_);
            executeUpdate(createTableRoleLink_);
        } catch (DatabaseException e) {
            throw new InstallCredentialsErrorException(e);
        }

        return true;
    }

    @Override
    public boolean remove()
    throws CredentialsManagerException {
        try {
            executeUpdate(dropTableRoleLink_);
            executeUpdate(dropTableUser_);
            executeUpdate(dropTableRole_);
        } catch (DatabaseException e) {
            throw new RemoveCredentialsErrorException(e);
        }

        return true;
    }

    @Override
    public DatabaseUsers addRole(final String role)
    throws CredentialsManagerException {
        if (null == role ||
            role.isEmpty()) {
            throw new AddRoleErrorException(role);
        }

        try {
            if (0 == executeUpdate(addRole_, new DbPreparedStatementHandler<>() {
                @Override
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("name", role);
                }
            })) {
                throw new AddRoleErrorException(role);
            }
        } catch (DatabaseException e) {
            if (null != e.getCause()) {
                String message = e.getCause().getMessage().toUpperCase();
                if (message.contains("AUTHROLE_NAME_UQ")) {
                    throw new DuplicateRoleException(role, e);
                }
            }

            throw new AddRoleErrorException(role, e);
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
                if (message.contains("AUTHUSER_LOGIN_UQ")) {
                    throw new DuplicateLoginException(login, e);
                }
                if (message.contains("AUTHUSER_PK")) {
                    throw new DuplicateUserIdException(attributes.getUserId(), e);
                }
            }

            throw e;
        }

        return this;
    }
}
