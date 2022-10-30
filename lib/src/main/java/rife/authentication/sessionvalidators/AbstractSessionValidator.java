/*
 * Copyright 2001-2008 Steven Grimm (koreth[remove] at midwinter dot com) and
 * Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.authentication.sessionvalidators;

import rife.authentication.CredentialsManager;
import rife.authentication.RememberManager;
import rife.authentication.SessionManager;
import rife.authentication.SessionValidator;

/**
 * Convenience superclass for {@link SessionValidator} implementations.
 * This provides simple getters and setters for the various properties
 * that can be set on a session validator.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.6
 */
public abstract class AbstractSessionValidator implements SessionValidator {
    /**
     * Predefined return type for validateSession(): session is invalid
     */
    public static final int SESSION_INVALID = 0;
    /**
     * Predefined return type for validateSession(): session is valid
     */
    public static final int SESSION_VALID = 1;

    protected CredentialsManager credentialsManager_ = null;
    protected SessionManager sessionManager_ = null;
    protected RememberManager rememberManager_ = null;

    /**
     * Set's this validator's credentials manager.
     *
     * @param credentialsManager the {@code CredentialsManager} instance
     * @since 1.6
     */
    public void setCredentialsManager(CredentialsManager credentialsManager) {
        credentialsManager_ = credentialsManager;
    }

    /**
     * Retrieves this validator's credentials manager.
     *
     * @return the requested {@code CredentialsManager}; or
     * {@code null} if none has been set up yet
     * @since 1.6
     */
    public CredentialsManager getCredentialsManager() {
        return credentialsManager_;
    }

    /**
     * Set's this validator's session manager.
     *
     * @param sessionManager the {@code SessionManager} instance
     * @since 1.6
     */
    public void setSessionManager(SessionManager sessionManager) {
        sessionManager_ = sessionManager;
    }

    /**
     * Retrieves this validator's session manager.
     *
     * @return the requested {@code SessionManager}; or
     * {@code null} if none has been set up yet
     * @since 1.6
     */
    public SessionManager getSessionManager() {
        return sessionManager_;
    }

    /**
     * Set's this validator's "remember me" manager.
     *
     * @param rememberManager the {@code RememberManager} instance
     * @since 1.6
     */
    public void setRememberManager(RememberManager rememberManager) {
        rememberManager_ = rememberManager;
    }

    /**
     * Retrieves this validator's "remember me" manager.
     *
     * @return the requested {@code RememberManager}; or
     * {@code null} if none has been set up yet
     * @since 1.6
     */
    public RememberManager getRememberManager() {
        return rememberManager_;
    }
}
