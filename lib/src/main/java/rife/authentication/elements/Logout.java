/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import jakarta.servlet.http.Cookie;
import rife.authentication.elements.exceptions.UndefinedLogoutRememberManagerException;
import rife.engine.Context;
import rife.engine.Element;
import rife.template.Template;

public class Logout implements Element {
    protected final AuthenticationConfig authConfig_;
    protected final Template template_;

    public Logout(AuthenticationConfig config, Template template) {
        authConfig_ = config;
        template_ = template;
    }

    protected void initializeLogout() {
    }

    protected void entrance(Template template) {
    }

    protected void loggedOut(Template template) {
    }

    public void process(Context c)
    throws Exception {
        initializeLogout();

        final Template template;
        if (template_ == null) {
            template = null;
        } else {
            template = template_.createNewInstance();
        }

        entrance(template);

        String authid = null;
        var auth_cookie_name = authConfig_.authCookieName();
        if (c.hasCookie(authConfig_.authCookieName())) {
            var auth_cookie = c.cookie(auth_cookie_name);
            authid = auth_cookie.getValue();
        }

        if (authid != null) {
            authConfig_.sessionValidator().getSessionManager().eraseSession(authid);

            // clear remember cookie for the user
            if (c.hasCookie(authConfig_.rememberCookieName())) {
                if (null == authConfig_.sessionValidator().getRememberManager()) {
                    throw new UndefinedLogoutRememberManagerException();
                }

                var remember_cookie_name = authConfig_.rememberCookieName();
                authConfig_.sessionValidator().getRememberManager().eraseRememberId(c.cookieValue(remember_cookie_name));

                var remember_cookie = c.cookie(remember_cookie_name);
                remember_cookie.setMaxAge(-1);
                remember_cookie.setPath("/");
                remember_cookie.setValue("");
                c.cookie(remember_cookie);
            }

            // clear the authentication cookie
            if (c.hasCookie(auth_cookie_name)) {
                var cookie = c.cookie(auth_cookie_name);
                cookie.setMaxAge(-1);
                cookie.setPath("/");
                cookie.setValue("");
                c.cookie(cookie);
            }
        }

        c.removeAttribute(authConfig_.identityAttributeName());

        loggedOut(template);

        if (template != null) {
            c.print(template);
        }
    }
}