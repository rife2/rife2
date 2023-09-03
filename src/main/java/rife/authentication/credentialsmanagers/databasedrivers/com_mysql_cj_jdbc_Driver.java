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
import rife.database.queries.Delete;
import rife.database.queries.Insert;
import rife.database.queries.Select;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class com_mysql_cj_jdbc_Driver extends generic {
    private final Delete removeRoleLinksByRoleId_;

    public com_mysql_cj_jdbc_Driver(Datasource datasource) {
        super(datasource);

        createTableRole_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRole())
            .column("roleId", int.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.authentication().getRoleNameMaximumLength(), CreateTable.NOTNULL)
            .customAttribute("roleId", "AUTO_INCREMENT")
            .primaryKey(RifeConfig.authentication().getTableRole().toUpperCase() + "_PK", "roleId")
            .unique(RifeConfig.authentication().getTableRole().toUpperCase() + "_NAME_UQ", "name");

        createTableRoleLink_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRoleLink())
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("roleId", int.class, CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_PK", new String[]{"userId", "roleId"});

        addRole_ = new Insert(getDatasource())
            .into(createTableRole_.getTable())
            .fieldParameter("name");

        getFreeUserId_ = new Select(getDatasource())
            .field("MAX(userId)+1 as freeUserId")
            .from(createTableUser_.getTable());

        getRoleId_ = new Select(getDatasource())
            .from(createTableRole_.getTable())
            .field("roleId")
            .whereParameter("name", "role", "=");

        removeRoleLinksByRoleId_ = new Delete(getDatasource())
            .from(createTableRoleLink_.getTable())
            .whereParameter("roleId", "=");
    }

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

    public DatabaseUsers addRole(final String role)
    throws CredentialsManagerException {
        if (null == role ||
            role.isEmpty()) {
            throw new AddRoleErrorException(role);
        }

        try {
            if (0 == executeUpdate(addRole_, new DbPreparedStatementHandler<>() {
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
                if (message.contains("DUPLICATE") &&
                    (message.contains("FOR KEY 'AUTHROLE.AUTHROLE_NAME_UQ'") /* MySQL */ ||
                     message.contains("FOR KEY 'AUTHROLE_NAME_UQ'"))) /* MariaDB */ {
                    throw new DuplicateRoleException(role, e);
                }
            }

            throw new AddRoleErrorException(role, e);
        }

        return this;
    }

    public DatabaseUsers addUser(final String login, final RoleUserAttributes attributes)
    throws CredentialsManagerException {
        if (null == login ||
            login.isEmpty() ||
            null == attributes) {
            throw new AddUserErrorException(login, attributes);
        }

        try {
            // ensure that the password is encoded if an encoder has been set
            String password = null;
            if (null == passwordEncryptor_ ||
                attributes.getPassword().startsWith(passwordEncryptor_.toString())) {
                password = attributes.getPassword();
            } else {
                try {
                    password = passwordEncryptor_.encrypt(attributes.getPassword());
                } catch (NoSuchAlgorithmException e) {
                    throw new AddUserErrorException(login, attributes, e);
                }
            }

            HashMap<String, Integer> roleids = null;
            // get the role ids
            if (attributes.getRoles() != null) {
                roleids = new HashMap<>();

                try (DbPreparedStatement ps_get_roleid = getConnection().getPreparedStatement(getRoleId_)) {
                    int roleid = -1;
                    for (String role : attributes.getRoles()) {
                        ps_get_roleid.setString(1, role);
                        ps_get_roleid.executeQuery();
                        if (ps_get_roleid.getResultSet().hasResultRows()) {
                            roleid = ps_get_roleid.getResultSet().getFirstInt();
                        }

                        if (-1 == roleid) {
                            throw new UnknownRoleErrorException(role, login, attributes);
                        }

                        roleids.put(role, roleid);
                    }
                }
            }

            synchronized (getFreeUserId_) {
                final String adapted_password = password;

                // get a new user id if it has not been provided
                if (attributes.getUserId() < 0) {
                    attributes.setUserId(executeGetFirstLong(getFreeUserId_));
                }

                if (0 == executeUpdate(addUserWithId_, new DbPreparedStatementHandler<>() {
                    public void setParameters(DbPreparedStatement statement) {
                        statement
                            .setLong("userId", attributes.getUserId())
                            .setString("login", login)
                            .setString("passwd", adapted_password);
                    }
                })) {
                    throw new AddUserErrorException(login, attributes);
                }
            }

            // ensure that the correct roles are assigned to the user
            if (attributes.getRoles() != null) {
                try (DbPreparedStatement ps_add_rolelink = getConnection().getPreparedStatement(addRoleLink_)) {
                    for (String role : attributes.getRoles()) {
                        ps_add_rolelink.setLong(1, attributes.getUserId());
                        ps_add_rolelink.setInt(2, roleids.get(role));
                        if (0 == ps_add_rolelink.executeUpdate()) {
                            throw new AddUserErrorException(login, attributes);
                        }
                        ps_add_rolelink.clearParameters();
                    }
                }
            }
        } catch (DatabaseException e) {
            if (null != e.getCause()) {
                String message = e.getCause().getMessage().toUpperCase();
                if (message.contains("DUPLICATE")) {
                    if (message.contains("FOR KEY 'AUTHUSER.PRIMARY'") /* MySQL */ ||
                        message.contains("FOR KEY 'PRIMARY'")) /* MariaDB */ {
                        throw new DuplicateUserIdException(attributes.getUserId(), e);
                    }
                    if (message.contains("FOR KEY 'AUTHUSER.AUTHUSER_LOGIN_UQ'") /* MySQL */ ||
                        message.contains("FOR KEY 'AUTHUSER_LOGIN_UQ'")) /* MariaDB */ {
                        throw new DuplicateLoginException(login, e);
                    }
                }
            }

            throw new AddUserErrorException(login, attributes, e);
        }

        return this;
    }

    public boolean updateUser(final String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        if (null == login ||
            login.isEmpty() ||
            null == attributes) {
            throw new UpdateUserErrorException(login, attributes);
        }

        try {
            HashMap<String, Integer> roleids = null;
            // get the role ids
            if (attributes.getRoles() != null) {
                roleids = new HashMap<>();

                try (DbPreparedStatement ps_get_roleid = getConnection().getPreparedStatement(getRoleId_)) {
                    int roleid = -1;
                    for (String role : attributes.getRoles()) {
                        ps_get_roleid.setString(1, role);
                        ps_get_roleid.executeQuery();
                        if (ps_get_roleid.getResultSet().hasResultRows()) {
                            roleid = ps_get_roleid.getResultSet().getFirstInt();
                        }

                        if (-1 == roleid) {
                            throw new UnknownRoleErrorException(role, login, attributes);
                        }

                        roleids.put(role, roleid);
                    }
                }
            }

            // obtain the user id
            final long userid = getUserId(login);
            if (userid < 0) {
                throw new UpdateUserErrorException(login, attributes);
            }

            // only handle the password if it has been provided
            if (attributes.getPassword() != null) {
                // ensure that the password is encoded if an encoder has been set
                String password = null;
                if (null == passwordEncryptor_ ||
                    attributes.getPassword().startsWith(passwordEncryptor_.toString())) {
                    password = attributes.getPassword();
                } else {
                    try {
                        password = passwordEncryptor_.encrypt(attributes.getPassword());
                    } catch (NoSuchAlgorithmException e) {
                        throw new UpdateUserErrorException(login, attributes, e);
                    }
                }

                // update the user password
                final String adapted_password = password;
                if (0 == executeUpdate(updateUser_, new DbPreparedStatementHandler<>() {
                    public void setParameters(DbPreparedStatement statement) {
                        statement
                            .setString("passwd", adapted_password)
                            .setString("login", login);
                    }
                })) {
                    return false;
                }
            }

            // remove the previous roles
            executeUpdate(removeRoleLinksByUserId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userid);
                }
            });

            // assign the correct roles to the user
            if (attributes.getRoles() != null) {
                // add the provided roles
                try (DbPreparedStatement ps_add_rolelink = getConnection().getPreparedStatement(addRoleLink_)) {
                    for (String role : attributes.getRoles()) {
                        ps_add_rolelink.setLong(1, userid);
                        ps_add_rolelink.setInt(2, roleids.get(role));
                        if (0 == ps_add_rolelink.executeUpdate()) {
                            throw new AddUserErrorException(login, attributes);
                        }
                        ps_add_rolelink.clearParameters();
                    }
                }
            }
        } catch (DatabaseException e) {
            throw new UpdateUserErrorException(login, attributes, e);
        }

        return true;
    }

    public boolean removeUser(final String login)
    throws CredentialsManagerException {
        if (null == login ||
            login.isEmpty()) {
            return false;
        }

        boolean result = false;

        try {
            final long userid = executeGetFirstLong(getUserId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("login", login);
                }
            });

            if (0 != executeUpdate(removeUserByLogin_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("login", login);
                }
            })) {
                result = true;
            }

            if (0 != executeUpdate(removeRoleLinksByUserId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userid);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveUserErrorException(login, e);
        }

        return result;
    }

    public boolean removeUser(final long userId)
    throws CredentialsManagerException {
        if (userId < 0) {
            return false;
        }

        boolean result = false;

        try {
            if (0 != executeUpdate(removeUserByUserId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userId);
                }
            })) {
                result = true;
            }

            if (0 != executeUpdate(removeRoleLinksByUserId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setLong("userId", userId);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveUserErrorException(userId, e);
        }

        return result;
    }

    public boolean removeRole(final String name)
    throws CredentialsManagerException {
        if (null == name ||
            name.isEmpty()) {
            return false;
        }

        boolean result = false;

        try {
            final int roleid = executeGetFirstInt(getRoleId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("role", name);
                }
            });

            if (0 != executeUpdate(removeRole_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setString("role", name);
                }
            })) {
                result = true;
            }

            if (0 != executeUpdate(removeRoleLinksByRoleId_, new DbPreparedStatementHandler<>() {
                public void setParameters(DbPreparedStatement statement) {
                    statement
                        .setInt("roleId", roleid);
                }
            })) {
                result = true;
            }
        } catch (DatabaseException e) {
            throw new RemoveRoleErrorException(name, e);
        }

        return result;
    }
}
