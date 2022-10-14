/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentials;

import rife.authentication.Credentials;

/**
 * <p>This interface needs to be implemented by all credentials classes that
 * work with {@link
 * rife.authentication.credentialsmanagers.RoleUsersManager}s, which
 * is the default user management in RIFE.
 * <p>Credentials aren't the same as the actual account information of a user,
 * they provide the data that is submitted and that needs to be verified.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public interface RoleUserCredentials extends Credentials, RememberMe {
    /**
     * Retrieves the submitted login.
     *
     * @return the login
     * @since 1.0
     */
    String getLogin();

    /**
     * Sets the login to submit.
     *
     * @param login the login
     * @since 1.0
     */
    void setLogin(String login);

    /**
     * Retrieves the submitted password.
     *
     * @return the password
     * @since 1.0
     */
    String getPassword();

    /**
     * Sets the password to submit.
     *
     * @param password the password
     * @since 1.0
     */
    void setPassword(String password);

    /**
     * Retrieves the submitted role.
     *
     * @return the role
     * @since 1.0
     */
    String getRole();

    /**
     * Sets the role to submit.
     *
     * @param role the role
     * @since 1.0
     */
    void setRole(String role);
}
