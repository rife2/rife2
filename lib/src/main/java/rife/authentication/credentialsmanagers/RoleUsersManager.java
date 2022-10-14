/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import rife.authentication.exceptions.CredentialsManagerException;

public interface RoleUsersManager extends IdentifiableUsersManager {
    RoleUsersManager addRole(String role)
    throws CredentialsManagerException;

    boolean containsRole(String role)
    throws CredentialsManagerException;

    long countRoles()
    throws CredentialsManagerException;

    boolean listRoles(ListRoles processor)
    throws CredentialsManagerException;

    RoleUsersManager addUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException;

    boolean containsUser(String login)
    throws CredentialsManagerException;

    long countUsers()
    throws CredentialsManagerException;

    long getUserId(String login)
    throws CredentialsManagerException;

    boolean listUsers(ListUsers processor)
    throws CredentialsManagerException;

    boolean listUsers(ListUsers processor, int limit, int offset)
    throws CredentialsManagerException;

    boolean isUserInRole(long userId, String role)
    throws CredentialsManagerException;

    boolean listUsersInRole(ListUsers processor, String role)
    throws CredentialsManagerException;

    boolean updateUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException;

    boolean removeUser(String login)
    throws CredentialsManagerException;

    boolean removeUser(long userId)
    throws CredentialsManagerException;

    boolean removeRole(String name)
    throws CredentialsManagerException;

    void clearUsers()
    throws CredentialsManagerException;

    boolean listUserRoles(String login, ListRoles processor)
    throws CredentialsManagerException;
}
