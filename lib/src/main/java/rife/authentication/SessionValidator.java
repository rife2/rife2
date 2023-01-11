/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication;

import rife.authentication.exceptions.SessionValidatorException;

/**
 * This interface defines the methods that classes with
 * {@code SessionValidator} functionalities have to implement.
 * <p>
 * A {@code SessionValidator} is essentially a bridge between a
 * {@code CredentialsManager} and a {@code SessionManager}. The
 * validity of a session is often dependent on external attributes which define
 * the context for a valid session that goes beyond a valid session id.
 * <p>
 * Typical uses can be:
 * <ul>
 * <li>a user can become blocked during an active session,</li>
 * <li>a user is a member of different groups (roles) and only
 * has access to certain resources when being part of a
 * particular group,</li>
 * <li>a user needs to provide information at the first valid log-in,
 * without providing this information the user can't access any
 * of the resources in the application.</li>
 * </ul>
 * <p>
 * All these scenarios require additional information and additional processing
 * that are often specific to each implementation of a
 * {@code CredentialsManager}.
 * <p>
 * Since any {@code CredentialsManager} can be combined with any
 * {@code SessionManager}, performance would often not be optimal.
 * <p>
 * For example, if the credentials and the session information are stored in the
 * same database. Completely isolating all functionalities would cause more
 * database queries to be executed than what's really needed. By implementing
 * the combined functionality of verifying a valid authentication session in a
 * bridge class that implements the {@code SessionValidator} interface,
 * only one query can be used to provide the same results. Thus, dramatically
 * increasing performance.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.authentication.sessionvalidators.AbstractSessionValidator
 * @see rife.authentication.SessionAttributes
 * @see rife.authentication.CredentialsManager
 * @see rife.authentication.SessionManager
 * @since 1.0
 */
public interface SessionValidator<C extends CredentialsManager, S extends SessionManager, R extends RememberManager> {
    /**
     * Validates an existing session according to a set of attributes that
     * define the context in which this validation occurs.
     * <p>
     * This method is typically executed for each access to a secured resource,
     * performance is thus of critical importance.
     * <p>
     * The implementation of this method should be optimal for the combination
     * of the used {@code CredentialsManager} and
     * {@code SessionManager}. Specific code that combines the features of
     * both managers should be written, instead of relying on the abstracted api
     * of each manager. Paying attention to the implementation of this method
     * can dramatically reduce the overhead of securing resources.
     *
     * @param authId     The unique id of the authentication session that needs
     *                   to be validated.
     * @param authData   Data that was associated with the session
     * @param attributes Access to the attributes that define that context
     *                   in which the session has to be validated.
     * @return A number that indicates the validation state of the session.
     * This allows the application to go beyond <em>valid</em> or
     * <em>invalid</em>. Additional states like for example : <em>blocked</em>,
     * <em>initial login</em> and <em>disabled</em>, can be used by using
     * different numbers.
     * @throws SessionValidatorException An undefined number of exceptional
     *                                   cases or error situations can occur when a session is validated. They are
     *                                   all indicated by throwing an instance of
     *                                   {@code SessionValidatorException}. It's up to the implementations of
     *                                   this interface to give more specific meanings to these exceptions.
     * @since 1.0
     */
    int validateSession(String authId, String authData, SessionAttributes attributes)
    throws SessionValidatorException;

    /**
     * Indicates if the provided validity identifier is considered as
     * <em>valid</em> and that the access to the secured resource is thus
     * authorized.
     * <p>
     * Normally, specific business logic is only required for the situations in
     * which access has prohibited. This method is used to make it possible to
     * provide automatic access to the secured resource.
     *
     * @param id The numeric identifier that is returned by the
     *           {@code validateSession}  method.
     * @return {@code true} if access to the secured resource was
     * authorized; or
     * <p>
     * {@code false} if access was prohibited.
     * @since 1.0
     */
    boolean isAccessAuthorized(int id);

    /**
     * Sets the {@code CredentialsManager} that will be used.
     *
     * @param credentialsManager The new {@code CredentialsManager}.
     * @since 1.0
     */
    void setCredentialsManager(C credentialsManager);

    /**
     * Retrieves the currently used {@code CredentialsManager}.
     *
     * @return The current {@code CredentialsManager}.
     * @since 1.0
     */
    C getCredentialsManager();

    /**
     * Sets the {@code SessionManager} that will be used.
     *
     * @param sessionManager The new {@code SessionManager}.
     * @since 1.0
     */
    void setSessionManager(S sessionManager);

    /**
     * Retrieves the currently used {@code SessionManager}.
     *
     * @return The current {@code SessionManager}.
     * @since 1.0
     */
    S getSessionManager();

    /**
     * Sets the {@code RememberManager} that will be used.
     *
     * @param rememberManager The new {@code RememberManager}.
     * @since 1.0
     */
    void setRememberManager(R rememberManager);

    /**
     * Retrieves the currently used {@code RememberManager}.
     *
     * @return The current {@code RememberManager}.
     * @since 1.0
     */
    R getRememberManager();
}

