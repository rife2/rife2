/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.credentialsmanagers.*;
import rife.engine.Context;
import rife.engine.Element;

/**
 * Tries to identify a user and store that as a request attribute.
 * <p>
 * This element can be used to customize to behavior when a user is logged
 * in, without requiring authentication for guests.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see AuthConfig#identityAttribute(Context)
 * @since 1.0
 */
public class Identified implements Element {
    private final AuthConfig authConfig_;

    /**
     * This constructor is meant to be used when extending the {@code Identified}
     * element with your custom identification class.
     * <p>Don't forget to also override the `getAuthConfig()` methods.
     * @since 1.0
     */
    protected Identified() {
        authConfig_ = null;
    }

    /**
     * This constructor is meant to be used when the {@code Identified} element
     * is used directly as a route in your site.
     * <p>When extending this element, use the default constructor instead
     * and override the `getAuthConfig()` method.
     * @param config the auth config to use
     * @since 1.0
     */
    public Identified(AuthConfig config) {
        authConfig_ = config;
    }

    /**
     * Retrieve the {@code Identified} element instance that is active in
     * this request.
     * @param c the processing context that will be used for the lookup
     * @return this request's identified element instance; or
     * {@code null} if it can't be found
     * @since 1.0
     */
    public static Identified getIdentifiedElementInRequest(Context c) {
        var identified = c.attribute(Identified.class.getName());
        if (identified instanceof Identified result) {
            return result;
        }

        return null;
    }

    /**
     * Hook method that is called at the start of the element's processing.
     * @param c the element processing context
     * @since 1.0
     */
    protected void initializeIdentified(Context c) {
    }

    /**
     * Hook method that is called to retrieve the {@code AuthConfig} to use.
     *
     * @return the config to use for authentication
     * @since 1.0
     */
    public AuthConfig getAuthConfig() {
        return authConfig_;
    }

    public void process(Context c)
    throws Exception {
        initializeIdentified(c);

        if (!c.hasAttribute(getAuthConfig().identityAttributeName())) {
            setIdentityAttribute(c);
        }
    }

    public void setIdentityAttribute(Context c) {
        var identity = getIdentity(c);
        if (identity != null) {
            c.setAttribute(Identified.class.getName(), this);
            c.setAttribute(getAuthConfig().identityAttributeName(), identity);
        }
    }

    public RoleUserIdentity getIdentity(Context c) {
        if (!c.hasCookie(getAuthConfig().authCookieName())) {
            return null;
        }

        var value = c.cookieValue(getAuthConfig().authCookieName());
        var auth_data = getAuthConfig().generateAuthData(c);
        String login = null;
        RoleUserAttributes attributes = null;

        var validator = getAuthConfig().sessionValidator();
        if (validator.getCredentialsManager() instanceof IdentifiableUsersManager credentials) {
            var auth_attribute = Authenticated.createAuthAttributeName(getAuthConfig().loginRoute(), getAuthConfig().authCookieName(), value);

            if (c.hasAttribute(auth_attribute) ||
                validator.getSessionManager().isSessionValid(value, auth_data)) {
                var sessions = validator.getSessionManager();
                var userid = sessions.getSessionUserId(value);

                if (userid > -1) {
                    login = credentials.getLogin(userid);
                    if (!c.hasAttribute(auth_attribute)) {
                        sessions.continueSession(value);
                    }
                }

                if (login != null) {
                    attributes = credentials.getAttributes(login);
                }
            }
        }

        if (login != null &&
            attributes != null) {
            return new RoleUserIdentity(login, attributes);
        }

        return null;
    }
}
