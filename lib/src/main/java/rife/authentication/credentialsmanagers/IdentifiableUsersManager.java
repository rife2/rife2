/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import rife.authentication.exceptions.CredentialsManagerException;

/**
 * This interface defines the methods that are needed for the {@link rife.authentication.elements.Identified}
 * element to be able to setup a {@link RoleUserIdentity} instance for each authenticated user for whom the
 * {@code Identified} element is executed.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @version $Revision$
 * @see rife.authentication.elements.Identified
 * @see RoleUserIdentity
 * @since 1.6
 */
public interface IdentifiableUsersManager {
    /**
     * Retrieves the attributes of a particular user according to its unique login.
     *
     * @param login the login of the user whose attributes need to be retrieved
     * @return the requested user attributes; or
     * <p>{@code null} if the user couldn't be found
     * @throws CredentialsManagerException when a unexpected error occurred during the retrieval of the user attributes
     * @since 1.6
     */
    RoleUserAttributes getAttributes(String login)
    throws CredentialsManagerException;

    /**
     * Retrieves the login of a particular user according to its unique ID.
     *
     * @param userId the ID of the user whose login will be retrieved
     * @return the requested user login; or
     * <p>{@code null} if the user couldn't be found
     * @throws CredentialsManagerException when a unexpected error occurred during the retrieval of the user login
     * @since 1.6
     */
    String getLogin(long userId)
    throws CredentialsManagerException;
}
