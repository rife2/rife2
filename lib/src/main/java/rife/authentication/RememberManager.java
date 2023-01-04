/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication;

import rife.authentication.exceptions.RememberManagerException;
import rife.config.RifeConfig;

/**
 * This interface defines the methods that classes with
 * {@code RememberManager} functionalities have to implement.
 * <p>A {@code RememberManager} is reponsible for coupling a user ID to
 * an expiring remember ID. The remember ID is typically stored in a cookie in
 * the browser and expires after a certain duration. An authentication element
 * that uses a {@code RememberManager}, should erase the remember ID
 * after using it once, create a new one immediately and send it to the
 * client. This ensures that each remember ID can only be used once.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.authentication.credentials.RememberMe
 * @since 1.0
 */
public interface RememberManager {
    /**
     * Obtains the maximum time that a remember ID can be used before it
     * becomes invalid.
     *
     * @return The maximum lifetime in milliseconds.
     * @since 1.0
     */
    long getRememberDuration();

    /**
     * Sets the maximum time that a remember ID can be used before it becomes
     * invalid.
     *
     * @param milliseconds The lifetime in milliseconds.
     * @since 1.0
     */
    void setRememberDuration(long milliseconds);

    /**
     * Obtains the frequency at which the purging will happen in relationship
     * to the scale.
     * <p>
     * This defaults to {@link RifeConfig.AuthenticationConfig#getRememberPurgeFrequency()}.
     *
     * @return the purge frequency
     * @see #setRememberPurgeFrequency
     * @see #getRememberPurgeScale
     * @see #setRememberPurgeScale
     * @since 1.0
     */
    int getRememberPurgeFrequency();

    /**
     * Set the frequency at which the purging will happen in relationship
     * to the scale.
     * <p>
     * By default, the frequency and scale respectively are 20 and 1000,
     * which means that the purging will have once every fifty times the
     * remember sessions are accessed.
     *
     * @param frequency the purge frequency
     * @see #getRememberPurgeFrequency
     * @see #getRememberPurgeScale
     * @see #setRememberPurgeScale
     * @since 1.0
     */
    void setRememberPurgeFrequency(int frequency);

    /**
     * Obtains the scale at which the purging will happen in relationship
     * to the frequency.
     * <p>
     * This defaults to {@link RifeConfig.AuthenticationConfig#getRememberPurgeScale()}.
     *
     * @return the purge scale
     * @see #getRememberPurgeFrequency
     * @see #setRememberPurgeFrequency
     * @see #setRememberPurgeScale
     * @since 1.0
     */
    int getRememberPurgeScale();

    /**
     * Set the scale at which the purging will happen in relationship
     * to the frequency.
     * <p>
     * By default, the frequency and scale respectively are 20 and 1000,
     * which means that the purging will have once every fifty times the
     * remember sessions are accessed.
     *
     * @param scale the purge scale
     * @see #getRememberPurgeFrequency
     * @see #setRememberPurgeFrequency
     * @see #getRememberPurgeScale
     * @since 1.0
     */
    void setRememberPurgeScale(int scale);

    /**
     * Starts a new session.
     *
     * @param userId   The ID that uniquely identifies the user that has to be
     *                 remembered.
     * @param authData The authentication data that is associated with the session.
     * @return A {@code String} that uniquely identifies the remembered
     * user ID.
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a remember ID is created. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    String createRememberId(long userId, String authData)
    throws RememberManagerException;

    /**
     * Removes one particular remember ID. This makes it instantly invalid.
     *
     * @param rememberId The remember ID that needs to be erased.
     * @return {@code true} if the ID was successfully erased; or
     * <p>{@code false} if this was not possible.
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a remember ID is erased. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    boolean eraseRememberId(String rememberId)
    throws RememberManagerException;

    /**
     * Removes all remember IDs for a particular user. This makes all issued
     * remember IDs instantly invalid.
     *
     * @param userId The id that uniquely identifies the user whose remember
     *               IDs are to be erased.
     * @return {@code true} if the IDs were successfully erased; or
     * <p>{@code false} if this was not possible
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a remember ID is erased. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    boolean eraseUserRememberIds(long userId)
    throws RememberManagerException;

    /**
     * Removes all available remember ID. This makes all existing remember IDs
     * instantly invalid and unusable for all users.
     *
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a remember ID is erased. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    void eraseAllRememberIds()
    throws RememberManagerException;

    /**
     * Retrieves the user ID that corresponds to a certain remember ID.
     *
     * @param rememberId The remember ID that maps to the user ID.
     * @return the ID of the user that corresponds to the provided remember
     * ID; or
     * <p>{@code -1} if no user ID corresponds to the provided remember
     * ID.
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a user ID is retrieved. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    long getRememberedUserId(String rememberId)
    throws RememberManagerException;

    /**
     * Removes all remember IDs that are expired. This means that all remember
     * IDs where the lifetime has been exceeded, will be removed.
     *
     * @throws RememberManagerException An undefined number of exceptional
     *                                  cases or error situations can occur when a remember ID is purged. They
     *                                  are all indicated by throwing an instance of
     *                                  {@code RememberManagerException}. It's up to the implementations
     *                                  of this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    void purgeRememberIds()
    throws RememberManagerException;
}

