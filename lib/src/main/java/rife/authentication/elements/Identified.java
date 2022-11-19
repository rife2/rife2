/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.credentialsmanagers.*;
import rife.engine.Context;
import rife.engine.Element;

public class Identified implements Element {
    protected final AuthConfig authConfig_;

    public Identified(AuthConfig config) {
        authConfig_ = config;
    }

    public void process(Context c)
    throws Exception {
        if (!c.hasAttribute(authConfig_.identityAttributeName())) {
            setIdentityAttribute(c);
        }
    }

    public void setIdentityAttribute(Context c)
    throws Exception {
        var identity = getIdentity(c);
        if (identity != null) {
            c.setAttribute(authConfig_.identityAttributeName(), identity);
        }
    }

    public RoleUserIdentity getIdentity(Context c)
    throws Exception {
        if (!c.hasCookie(authConfig_.authCookieName())) {
            return null;
        }

        var value = c.cookieValue(authConfig_.authCookieName());
        String login = null;
        RoleUserAttributes attributes = null;

        var validator = authConfig_.sessionValidator();
        if (validator.getCredentialsManager() instanceof IdentifiableUsersManager credentials) {
            var auth_attribute = Authenticated.createAuthAttributeName(authConfig_.loginRoute(), authConfig_.authCookieName(), value);

            if (c.hasAttribute(auth_attribute) ||
                validator.getSessionManager().isSessionValid(value, c.remoteAddr())) {
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
