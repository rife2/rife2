/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.SessionAttributes;
import rife.authentication.SessionValidator;
import rife.engine.Context;
import rife.engine.Route;

/**
 * Requires that the user has a valid authentication session before access
 * is allowed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Authenticated extends Identified implements SessionAttributes {
    /**
     * This constructor is meant to be used when extending the {@code Authenticated}
     * element with your custom authenticated class.
     * <p>Don't forget to also override the `getAuthConfig()` methodss.
     * @since 1.0
     */
    protected Authenticated() {
    }

    /**
     * This constructor is meant to be used when the {@code Authenticated} element
     * is used directly as a route in your site.
     * <p>When extending this element, use the default constructor instead
     * and override the `getAuthConfig()` method.
     * @param config the auth config to use
     * @since 1.0
     */
    public Authenticated(AuthConfig config) {
        super(config);
    }

    /**
     * Hook method that is called at the start of the element's processing.
     * @param c the element processing context
     * @since 1.0
     */
    protected void initializeAuthenticated(Context c) {
    }

    /**
     * Hook method that is called when the <code>SessionValidator</code> doesn't
     * accept the authentication ID that a user provides after having been logged
     * in.
     * <p>
     * This can happen for example happen when the maximum duration has expired,
     * when the authentication ID has been tampered with, or when the
     * authentication ID isn't known anymore by the backing store.
     *
     * @param authCookieName  the name of the cookie that contains
     *                        the authentication ID
     * @param authCookieValue the value of the cookie with the
     *                        authentication ID
     * @param validityId      a number that indicates the validation state of the
     *                        session, as used by the <code>SessionValidator</code>, more information can
     *                        be found here: {@link SessionValidator#validateSession}
     * @since 1.0
     */
    protected void sessionNotValid(String authCookieName, String authCookieValue, int validityId) {
    }

    public void process(Context c)
    throws Exception {
        initializeAuthenticated(c);

        final var auth_config = getAuthConfig();
        if (c.hasCookie(auth_config.authCookieName())) {
            var auth_id = c.cookieValue(auth_config.authCookieName());
            var auth_attribute = createAuthAttributeName(auth_config.loginRoute(), auth_config.authCookieName(), auth_id);
            var auth_data = auth_config.generateAuthData(c);

            if (c.hasAttribute(auth_attribute)) {
                c.next();
            } else {
                var session_validator = auth_config.sessionValidator();
                assert session_validator != null;

                // validate the session
                var session_validity_id = session_validator.validateSession(auth_id, auth_data, this);

                // check if the validation allows access
                if (session_validator.isAccessAuthorized(session_validity_id)) {
                    var session_manager = session_validator.getSessionManager();

                    // prohibit access if the authentication session was
                    // started through remembered credentials and that
                    // had been set to not allowed
                    if (auth_config.prohibitRemember() &&
                        session_manager.wasRemembered(auth_id)) {
                        sessionNotValid(auth_config.authCookieName(), auth_id, session_validity_id);
                    }
                    // continue the session
                    else {
                        if (session_manager.continueSession(auth_id)) {
                            c.setAttribute(auth_attribute, true);
                            setIdentityAttribute(c);

                            c.next();
                        }
                    }
                } else {
                    sessionNotValid(auth_config.authCookieName(), auth_id, session_validity_id);
                }
            }
        }

        if (auth_config.enforceAuthentication()) {
            c.redirect(c.urlFor(auth_config.loginRoute()));
        }
    }

    public boolean hasAttribute(String key) {
        return key.equals("role") && getAuthConfig().role() != null;
    }

    public String getAttribute(String key) {
        if (key.equals("role")) {
            return getAuthConfig().role();
        }

        return null;
    }

    /**
     * Creates a name for the current authentication context that can be used to
     * cache the authentication process' result as a request attribute. This name
     * is built from the login route path, the name of the
     * authentication cookie and its value.
     *
     * @param route the login route
     * @param name  the name of the authentication cookie
     * @param value the value of the authentication cookie
     * @return the created name
     * @since 1.0
     */
    public static String createAuthAttributeName(Route route, String name, String value) {
        return route.path() + "\t" + name + "\t" + value;
    }
}



