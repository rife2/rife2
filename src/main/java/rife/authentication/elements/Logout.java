/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.elements.exceptions.UndefinedLogoutRememberManagerException;
import rife.engine.Context;
import rife.engine.Element;
import rife.template.Template;

/**
 * Provides logout logic with an optional template that can be printed for
 * configuration.
 * <p>
 * To customize the behavior of the authentication, it's the easiest to override
 * one of the hook methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Logout implements Element {
    private final AuthConfig authConfig_;
    private final Template template_;

    /**
     * This constructor is meant to be used when extending the {@code Logout} element
     * with your custom logout class.
     * <p>Don't forget to also override the `getAuthConfig()` and `getTemplate()`
     * methods.
     * @since 1.0
     */
    protected Logout() {
        authConfig_ = null;
        template_ = null;
    }

    /**
     * This constructor is meant to be used when the {@code Logout} element is used
     * directly as a route in your site.
     * <p>When extending this element, use the default constructor instead
     * and override the `getAuthConfig()` and `getTemplate()` methods.
     * @param config the auth config to use
     * @since 1.0
     */
    public Logout(AuthConfig config) {
        this(config, null);
    }

    /**
     * This constructor is meant to be used when the {@code Logout} element is used
     * directly as a route in your site.
     * <p>When extending this element, use the default constructor instead
     * and override the `getAuthConfig()` and `getTemplate()` methods.
     * @param config the auth config to use
     * @param template the template instance blueprint to use
     * @since 1.0
     */
    public Logout(AuthConfig config, Template template) {
        authConfig_ = config;
        template_ = template;
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

    /**
     * Hook method that is called to create the template instance.
     *
     * @return the template to use for logout
     * @since 1.0
     */
    protected Template getTemplate() {
        if (template_ == null) {
            return null;
        }

        final Template template = template_.createNewInstance();
        template.addResourceBundles(template_.getResourceBundles());
        return template;
    }

    /**
     * Hook method that is called at the start of the element's processing.
     * @param c the element processing context
     * @since 1.0
     */
    protected void initializeLogout(Context c) {
    }

    /**
     * Hook method that is called after logging out and before printing the template.
     * @param template the template that will be printed; or {@code null} if no
     *                 template was set up
     * @since 1.0
     */
    protected void loggedOut(Template template) {
    }

    @Override
    public void process(Context c) {
        initializeLogout(c);

        final Template template = getTemplate();

        String authid = null;
        var auth_cookie_name = getAuthConfig().authCookieName();
        if (c.hasCookie(getAuthConfig().authCookieName())) {
            authid = c.cookieValue(auth_cookie_name);
        }

        if (authid != null) {
            getAuthConfig().sessionValidator().getSessionManager().eraseSession(authid);

            // clear remember cookie for the user
            if (c.hasCookie(getAuthConfig().rememberCookieName())) {
                if (null == getAuthConfig().sessionValidator().getRememberManager()) {
                    throw new UndefinedLogoutRememberManagerException();
                }

                var remember_cookie_name = getAuthConfig().rememberCookieName();
                getAuthConfig().sessionValidator().getRememberManager().eraseRememberId(c.cookieValue(remember_cookie_name));

                c.removeCookie("/", remember_cookie_name);
            }

            // clear the authentication cookie
            if (c.hasCookie(auth_cookie_name)) {
                c.removeCookie("/", auth_cookie_name);
            }
        }

        c.removeAttribute(getAuthConfig().identityAttributeName());

        loggedOut(template);

        if (template != null) {
            c.print(template);
        }
    }
}