/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import jakarta.servlet.http.Cookie;
import rife.authentication.Credentials;
import rife.authentication.SessionAttributes;
import rife.authentication.credentials.RememberMe;
import rife.authentication.credentials.RoleUserCredentials;
import rife.authentication.elements.exceptions.UndefinedAuthenticationRememberManagerException;
import rife.engine.Context;
import rife.engine.RequestMethod;
import rife.template.Template;
import rife.validation.ValidationError;

/**
 * Provides a login form so that the user can start a valid authentication session.
 * <p>
 * To customize the behavior of the authentication, it's the easiest to override
 * one of the hook methods.
 *
 * @author Steven Grimm (koreth[remove] at midwinter dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Login extends Identified implements SessionAttributes {
    protected final Template template_;

    public Login(AuthConfig config, Template template) {
        super(config);
        template_ = template;
    }

    /**
     * Hook method that is called at the start of the element's execution.
     *
     * @since 1.0
     */
    protected void initializeLogin() {
    }

    /**
     * Hook method that is called after the template instance has been instantiated.
     *
     * @param template the template instance that has been instantiated
     * @since 1.0
     */
    protected void entrance(Template template) {
    }

    /**
     * Hook method that is called on login form submission when validation of the
     * credentials produces validation errors.
     *
     * @param template    this authentication element's template
     * @param credentials the credentials object that was invalid
     * @since 1.0
     */
    protected void unvalidatedCredentials(Template template, Credentials credentials) {
    }

    /**
     * Hook method that is called on login form submission when the credentials
     * are validated without errors
     *
     * @param credentials the credentials object that was valid
     * @since 1.0
     */
    protected void validatedCredentials(Credentials credentials) {
    }

    /**
     * Hook method that is called when valid credentials have been accepted by the
     * <code>CredentialsManager</code> that backs this authentication element.
     *
     * @param credentials the credentials object that was accepted
     * @since 1.0
     */
    protected void acceptedCredentials(Credentials credentials) {
    }

    /**
     * Hook method that is called after a new authentication session has been
     * successfully created.
     *
     * @param userId the user ID of the user that was successfully authenticated
     * @since 1.0
     */
    protected void authenticated(long userId) {
    }

    /**
     * Hook method that is called when valid credentials have been rejected by the
     * <code>CredentialsManager</code> that backs this authentication element.
     * <p>
     * This can for example happen when the password is not correct.
     * <p>
     * Note that there is already a default implementation of this hook method that
     * simply adds a validation error to the credentials object. If you want to
     * preserve this when you implement your own hook method, you need to call the
     * super class's method in your implementation.
     *
     * @param template    this authentication element's template
     * @param credentials the credentials object that was rejected
     * @since 1.0
     */
    protected void refusedCredentials(Template template, Credentials credentials) {
        credentials.addValidationError(new ValidationError.INVALID("credentials"));
    }

    /**
     * Hook method that is called when the <code>SessionManager</code> couldn't
     * create a new authentication session of valid and accepted credentials.
     * <p>
     * Note that there is already a default implementation of this hook method that
     * simply adds a validation error to the credentials object. If you want to
     * preserve this when you implement your own hook method, you need to call the
     * super class's method in your implementation.
     *
     * @param template    this authentication element's template
     * @param credentials the credentials object that was used when creating the
     *                    authentication session
     * @since 1.0
     */
    protected void sessionCreationError(Template template, Credentials credentials) {
        credentials.addValidationError(new ValidationError.UNEXPECTED("sessioncreation"));
    }

    public void process(Context c)
    throws Exception {
        final var credentials_class = authConfig_.credentialsClass();
        final var session_validator = authConfig_.sessionValidator();

        assert credentials_class != null;
        assert session_validator != null;

        initializeLogin();

        final Template template = template_.createNewInstance();
        entrance(template);

        // check if a cookie for remember id is provided
        var remember_id = c.cookieValue(authConfig_.rememberCookieName());
        if (remember_id != null && !authConfig_.prohibitRemember()) {
            var remember_manager = session_validator.getRememberManager();
            if (null == remember_manager) {
                throw new UndefinedAuthenticationRememberManagerException();
            }

            var userid = remember_manager.getRememberedUserId(remember_id);
            remember_manager.eraseRememberId(remember_id);

            // only start a new session if the userid could be retrieved
            if (userid != -1) {
                // try to start a new session, if it hasn't succeeded,
                // regular authentication will kick in
                startNewSession(c, userid, true, true);
            }
        }

        if (c.method() == RequestMethod.POST) {
            var credentials = c.parametersBean(credentials_class);

            if (!credentials.validate()) {
                unvalidatedCredentials(template, credentials);
                c.generateForm(template, credentials);
            } else {
                if (authConfig_.role() != null && credentials instanceof RoleUserCredentials role_user) {
                    role_user.setRole(authConfig_.role());
                }

                validatedCredentials(credentials);

                var userid = session_validator.getCredentialsManager().verifyCredentials(credentials);

                // verify login attempt
                if (userid < 0) {
                    refusedCredentials(template, credentials);
                    c.generateForm(template, credentials);
                } else {
                    acceptedCredentials(credentials);

                    // if the session has to be remembered, do so
                    var remember = false;
                    if (credentials instanceof RememberMe remember_me) {
                        remember = remember_me.getRemember();
                    }

                    // start a new session
                    if (!startNewSession(c, userid, remember, false)) {
                        // errors occurred, notify user
                        sessionCreationError(template, credentials);
                    }
                }
            }
        }

        c.print(template);
    }

    private boolean startNewSession(Context c, long userid, boolean remember, boolean remembered)
    throws Exception {
        if (remember) {
            var remember_manager = authConfig_.sessionValidator().getRememberManager();
            if (null == remember_manager) {
                throw new UndefinedAuthenticationRememberManagerException();
            }

            var remember_id = remember_manager.createRememberId(userid, c.remoteAddr());

            if (remember_id != null) {
                var remember_cookie = new Cookie(authConfig_.rememberCookieName(), remember_id);
                remember_cookie.setPath("/");
                remember_cookie.setMaxAge(authConfig_.rememberMaxAge());
                c.addCookie(remember_cookie);
            }
        }

        var authid = authConfig_.sessionValidator().getSessionManager().startSession(userid, c.remoteAddr(), remembered);

        if (null != authid) {
            authenticated(userid);

            // set cookie
            var auth_cookie = new Cookie(authConfig_.authCookieName(), authid);
            auth_cookie.setPath("/");
            c.addCookie(auth_cookie);

            var session_validator = authConfig_.sessionValidator();
            assert session_validator != null;

            // validate the session
            var session_validity_id = session_validator.validateSession(authid, c.remoteAddr(), this);

            // check if the validation allows access
            if (session_validator.isAccessAuthorized(session_validity_id)) {
                var auth_attribute = Authenticated.createAuthAttributeName(c.route(), authConfig_.authCookieName(), authid);
                c.setAttribute(auth_attribute, true);
                setIdentityAttribute(c);

                // successful login
                c.redirect(authConfig_.landingRoute());
                return true;
            }
        }

        return false;
    }

    public boolean hasAttribute(String key) {
        return key.equals("role") && authConfig_.role() != null;
    }

    public String getAttribute(String key) {
        if (key.equals("role")) {
            return authConfig_.role();
        }

        return null;
    }
}


