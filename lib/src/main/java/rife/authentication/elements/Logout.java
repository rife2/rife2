/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.elements.exceptions.UndefinedLogoutRememberManagerException;
import rife.engine.*;
import rife.template.Template;

public class Logout implements Element {
    protected final AuthConfig authConfig_;
    protected final Template template_;

    public Logout(AuthConfig config) {
        this(config, null);
    }
    public Logout(AuthConfig config, Template template) {
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
            template.addResourceBundles(template_.getResourceBundles());
        }

        entrance(template);

        String authid = null;
        var auth_cookie_name = authConfig_.authCookieName();
        if (c.hasCookie(authConfig_.authCookieName())) {
            authid = c.cookieValue(auth_cookie_name);
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

                c.addCookie(new CookieBuilder(remember_cookie_name, "").path("/").maxAge(-1));
            }

            // clear the authentication cookie
            if (c.hasCookie(auth_cookie_name)) {
                c.addCookie(new CookieBuilder(auth_cookie_name, "").path("/").maxAge(-1));
            }
        }

        c.removeAttribute(AuthConfig.identityAttributeName());

        loggedOut(template);

        if (template != null) {
            c.print(template);
        }
    }
}