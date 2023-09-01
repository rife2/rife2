/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.databasedrivers;

import rife.database.queries.*;

import rife.authentication.Credentials;
import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.ListRoles;
import rife.authentication.credentialsmanagers.ListUsers;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.credentialsmanagers.exceptions.DuplicateLoginException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateRoleException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateUserIdException;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.config.RifeConfig;
import rife.database.Datasource;

public class generic extends DatabaseUsers {
    protected final CreateSequence createSequenceRole_;
    protected CreateTable createTableRole_;
    protected final CreateTable createTableUser_;
    protected CreateTable createTableRoleLink_;
    protected final Select verifyCredentialsNoRole_;
    protected final Select verifyCredentialsRole_;
    protected Select getRoleId_;
    protected final SequenceValue getNewRoleId_;
    protected Insert addRole_;
    protected final Select containsRole_;
    protected final Select countRoles_;
    protected final Select listRoles_;
    protected final Insert addUserWithId_;
    protected Select getFreeUserId_;
    protected final Insert addRoleLink_;
    protected final Select getAttributes_;
    protected final Select getUserRoles_;
    protected final Select containsUser_;
    protected final Select countUsers_;
    protected final Select getLogin_;
    protected final Select getUserId_;
    protected final Select getPassword_;
    protected final Select listUsers_;
    protected final Select listUsersRanged_;
    protected final Select isUserInRole_;
    protected final Select listUsersInRole_;
    protected final Update updateUser_;
    protected final Delete removeRoleLinksByUserId_;
    protected final Delete removeUserByLogin_;
    protected final Delete removeUserByUserId_;
    protected final Delete removeRole_;
    protected final Delete clearUsers_;
    protected final Select listUserRoles_;
    protected final DropSequence dropSequenceRole_;
    protected final DropTable dropTableRole_;
    protected final DropTable dropTableUser_;
    protected final DropTable dropTableRoleLink_;

    public generic(Datasource datasource) {
        super(datasource);

        createSequenceRole_ = new CreateSequence(getDatasource())
            .name(RifeConfig.authentication().getSequenceRole());

        createTableRole_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRole())
            .column("roleId", int.class, CreateTable.NOTNULL)
            .column("name", String.class, RifeConfig.authentication().getRoleNameMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableRole().toUpperCase() + "_PK", "roleId")
            .unique(RifeConfig.authentication().getTableRole().toUpperCase() + "_NAME_UQ", "name");

        createTableUser_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableUser())
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("login", String.class, RifeConfig.authentication().getLoginMaximumLength(), CreateTable.NOTNULL)
            .column("passwd", String.class, RifeConfig.authentication().getPasswordMaximumLength(), CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableUser().toUpperCase() + "_PK", "userId")
            .unique(RifeConfig.authentication().getTableUser().toUpperCase() + "_LOGIN_UQ", "login");

        createTableRoleLink_ = new CreateTable(getDatasource())
            .table(RifeConfig.authentication().getTableRoleLink())
            .column("userId", long.class, CreateTable.NOTNULL)
            .column("roleId", int.class, CreateTable.NOTNULL)
            .primaryKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_PK", new String[]{"userId", "roleId"})
            .foreignKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_USERID_FK", createTableUser_.getTable(), "userId", "userId", null, CreateTable.CASCADE)
            .foreignKey(RifeConfig.authentication().getTableRoleLink().toUpperCase() + "_ROLEID_FK", createTableRole_.getTable(), "roleId", "roleId", null, CreateTable.CASCADE);

        verifyCredentialsNoRole_ = new Select(getDatasource())
            .from(createTableUser_.getTable())
            .field("userId")
            .whereParameter("login", "=")
            .whereParameterAnd("passwd", "=");

        verifyCredentialsRole_ = new Select(getDatasource())
            .from(createTableUser_.getTable())
            .join(createTableRoleLink_.getTable())
            .join(createTableRole_.getTable())
            .field(createTableUser_.getTable() + ".userId")
            .whereParameter("login", "=")
            .whereParameterAnd("passwd", "=")
            .whereAnd(createTableUser_.getTable() + ".userId = " + createTableRoleLink_.getTable() + ".userId")
            .whereParameterAnd("name", "role", "=")
            .whereAnd(createTableRole_.getTable() + ".roleId = " + createTableRoleLink_.getTable() + ".roleId");

        getRoleId_ = new Select(getDatasource())
            .from(createTableRole_.getTable())
            .field("roleId")
            .whereParameter("name", "=");

        getNewRoleId_ = new SequenceValue(getDatasource())
            .name(createSequenceRole_.getName())
            .next();

        addRole_ = new Insert(getDatasource())
            .into(createTableRole_.getTable())
            .fieldParameter("roleId")
            .fieldParameter("name");

        containsRole_ = new Select(getDatasource())
            .from(createTableRole_.getTable())
            .whereParameter("name", "=");

        countRoles_ = new Select(getDatasource())
            .field("count(*)")
            .from(createTableRole_.getTable());

        listRoles_ = new Select(getDatasource())
            .from(createTableRole_.getTable())
            .orderBy("name");

        addUserWithId_ = new Insert(getDatasource())
            .into(createTableUser_.getTable())
            .fieldParameter("userId")
            .fieldParameter("login")
            .fieldParameter("passwd");

        getFreeUserId_ = new Select(getDatasource())
            .field("COALESCE(MAX(userId)+1, 0) as freeUserId")
            .from(createTableUser_.getTable());

        addRoleLink_ = new Insert(getDatasource())
            .into(createTableRoleLink_.getTable())
            .fieldParameter("userId")
            .fieldParameter("roleId");

        getAttributes_ = new Select(getDatasource())
            .field("userId")
            .field("passwd AS password")
            .from(createTableUser_.getTable())
            .whereParameter("login", "=");

        getUserRoles_ = new Select(getDatasource())
            .field("name")
            .from(createTableRoleLink_.getTable())
            .join(createTableRole_.getTable())
            .whereParameter("userId", "=")
            .whereAnd(createTableRoleLink_.getTable() + ".roleId = " + createTableRole_.getTable() + ".roleId")
            .orderBy("name");

        containsUser_ = new Select(getDatasource())
            .from(createTableUser_.getTable())
            .whereParameter("login", "=");

        countUsers_ = new Select(getDatasource())
            .field("count(*)")
            .from(createTableUser_.getTable());

        getLogin_ = new Select(getDatasource())
            .field("login")
            .from(createTableUser_.getTable())
            .whereParameter("userId", "=");

        getUserId_ = new Select(getDatasource())
            .field("userId")
            .from(createTableUser_.getTable())
            .whereParameter("login", "=");

        getPassword_ = new Select(getDatasource())
            .field("passwd AS password")
            .from(createTableUser_.getTable())
            .whereParameter("login", "=");

        listUsers_ = new Select(getDatasource())
            .from(createTableUser_.getTable())
            .orderBy("login");

        listUsersRanged_ = new Select(getDatasource())
            .from(createTableUser_.getTable())
            .orderBy("login")
            .limitParameter("limit")
            .offsetParameter("offset");

        isUserInRole_ = new Select(getDatasource())
            .from(createTableRoleLink_.getTable())
            .join(createTableRole_.getTable())
            .field(createTableRoleLink_.getTable() + ".userId")
            .whereParameter(createTableRoleLink_.getTable() + ".userId", "=")
            .whereParameterAnd("name", "role", "=")
            .whereAnd(createTableRole_.getTable() + ".roleId = " + createTableRoleLink_.getTable() + ".roleId");

        listUsersInRole_ = new Select(getDatasource())
            .field(createTableUser_.getTable() + ".userId")
            .field("login")
            .field("passwd")
            .from(createTableUser_.getTable())
            .join(createTableRoleLink_.getTable())
            .join(createTableRole_.getTable())
            .where(createTableUser_.getTable() + ".userId = " + createTableRoleLink_.getTable() + ".userId")
            .whereAnd(createTableRoleLink_.getTable() + ".roleId = " + createTableRole_.getTable() + ".roleId")
            .whereParameterAnd(createTableRole_.getTable() + ".name", "role", "=")
            .orderBy("login");

        updateUser_ = new Update(getDatasource())
            .table(createTableUser_.getTable())
            .fieldParameter("passwd")
            .whereParameter("login", "=");

        removeRoleLinksByUserId_ = new Delete(getDatasource())
            .from(createTableRoleLink_.getTable())
            .whereParameter("userId", "=");

        removeUserByLogin_ = new Delete(getDatasource())
            .from(createTableUser_.getTable())
            .whereParameter("login", "=");

        removeUserByUserId_ = new Delete(getDatasource())
            .from(createTableUser_.getTable())
            .whereParameter("userId", "=");

        removeRole_ = new Delete(getDatasource())
            .from(createTableRole_.getTable())
            .whereParameter("name", "role", "=");

        clearUsers_ = new Delete(getDatasource())
            .from(createTableUser_.getTable());

        listUserRoles_ = new Select(getDatasource())
            .from(createTableRole_.getTable())
            .join(createTableRoleLink_.getTable())
            .join(createTableUser_.getTable())
            .field(createTableRole_.getTable() + ".name")
            .where(createTableRole_.getTable() + ".roleId = " + createTableRoleLink_.getTable() + ".roleId")
            .whereParameterAnd(createTableUser_.getTable() + ".login", "=")
            .whereAnd(createTableUser_.getTable() + ".userId = " + createTableRoleLink_.getTable() + ".userId");

        dropSequenceRole_ = new DropSequence(getDatasource())
            .name(createSequenceRole_.getName());

        dropTableRole_ = new DropTable(getDatasource())
            .table(createTableRole_.getTable());

        dropTableUser_ = new DropTable(getDatasource())
            .table(createTableUser_.getTable());

        dropTableRoleLink_ = new DropTable(getDatasource())
            .table(createTableRoleLink_.getTable());
    }

    public boolean install()
    throws CredentialsManagerException {
        return _install(createSequenceRole_, createTableRole_, createTableUser_, createTableRoleLink_);
    }

    public boolean remove()
    throws CredentialsManagerException {
        return _remove(dropSequenceRole_, dropTableRole_, dropTableUser_, dropTableRoleLink_);
    }

    public long verifyCredentials(Credentials credentials)
    throws CredentialsManagerException {
        return _verifyCredentials(verifyCredentialsNoRole_, verifyCredentialsRole_, credentials);
    }

    public DatabaseUsers addRole(String role)
    throws CredentialsManagerException {
        try {
            _addRole(getNewRoleId_, addRole_, role);
        } catch (CredentialsManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableRole_.getUniqueConstraints().get(0).getName())) {
                    throw new DuplicateRoleException(role);
                }
            }

            throw e;
        }

        return this;
    }

    public boolean containsRole(String role)
    throws CredentialsManagerException {
        return _containsRole(containsRole_, role);
    }

    public long countRoles()
    throws CredentialsManagerException {
        return _countRoles(countRoles_);
    }

    public boolean listRoles(ListRoles processor)
    throws CredentialsManagerException {
        return _listRoles(listRoles_, processor);
    }

    public DatabaseUsers addUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        try {
            _addUser(addUserWithId_, getFreeUserId_, getRoleId_, addRoleLink_, login, attributes);
        } catch (CredentialsManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableUser_.getPrimaryKeys().get(0).getName())) {
                    throw new DuplicateUserIdException(attributes.getUserId());
                }
                if (message.contains(createTableUser_.getUniqueConstraints().get(0).getName())) {
                    throw new DuplicateLoginException(login);
                }
            }

            throw e;
        }

        return this;
    }

    public RoleUserAttributes getAttributes(String login)
    throws CredentialsManagerException {
        return _getAttributes(getAttributes_, getUserRoles_, login);
    }

    public boolean containsUser(String login)
    throws CredentialsManagerException {
        return _containsUser(containsUser_, login);
    }

    public long countUsers()
    throws CredentialsManagerException {
        return _countUsers(countUsers_);
    }

    public String getLogin(long userId)
    throws CredentialsManagerException {
        return _getLogin(getLogin_, userId);
    }

    public long getUserId(String login)
    throws CredentialsManagerException {
        return _getUserId(getUserId_, login);
    }

    public String getPassword(String login)
    throws CredentialsManagerException {
        return _getPassword(getPassword_, login);
    }

    public boolean listUsers(ListUsers processor)
    throws CredentialsManagerException {
        return _listUsers(listUsers_, processor);
    }

    public boolean listUsers(ListUsers processor, int limit, int offset)
    throws CredentialsManagerException {
        return _listUsers(listUsersRanged_, processor, limit, offset);
    }

    public boolean isUserInRole(long userId, String role)
    throws CredentialsManagerException {
        return _isUserInRole(isUserInRole_, userId, role);
    }

    public boolean listUsersInRole(ListUsers processor, String role)
    throws CredentialsManagerException {
        return _listUsersInRole(listUsersInRole_, processor, role);
    }

    public boolean updateUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        return _updateUser(updateUser_, removeRoleLinksByUserId_, getRoleId_, addRoleLink_, login, attributes);
    }

    public boolean removeUser(String login)
    throws CredentialsManagerException {
        return _removeUser(removeUserByLogin_, login);
    }

    public boolean removeUser(long userId)
    throws CredentialsManagerException {
        return _removeUser(removeUserByUserId_, userId);
    }

    public boolean removeRole(String name)
    throws CredentialsManagerException {
        return _removeRole(removeRole_, name);
    }

    public void clearUsers()
    throws CredentialsManagerException {
        _clearUsers(clearUsers_);
    }

    public boolean listUserRoles(String login, ListRoles processor)
    throws CredentialsManagerException {
        return _listUserRoles(listUserRoles_, login, processor);
    }
}



