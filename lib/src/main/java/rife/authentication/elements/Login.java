/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.Credentials;
import rife.authentication.SessionAttributes;
import rife.authentication.credentials.RememberMe;
import rife.authentication.credentials.RoleUserCredentials;
import rife.authentication.elements.exceptions.UndefinedAuthenticationRememberManagerException;
import rife.engine.*;
import rife.template.Template;
import rife.validation.ValidationError;

/**
 * Provides a login form so that the user can start a valid authentication session.
 * <p>
 * To customize the behavior of the authentication, it's the easiest to override
 * one of the hook methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Login extends Identified implements SessionAttributes {
    private final Template template_;

    /**
     * This constructor is meant to be used when extending the {@code Login} element
     * with your custom login class.
     * <p>Don't forget to also override the `getAuthConfig()` and `getTemplate()`
     * methods.
     * @since 1.0
     */
    protected Login() {
        template_ = null;
    }

    /**
     * This constructor is meant to be used when the {@code Login} element is used
     * directly as a route in your site.
     * <p>When extending this element, use the default constructor instead
     * and override the `getAuthConfig()` and `getTemplate()` methods.
     * @param config the auth config to use
     * @param template the template instance blueprint to use
     * @since 1.0
     */
    public Login(AuthConfig config, Template template) {
        super(config);
        template_ = template;
    }

    /**
     * Hook method that is called at the start of the element's processing.
     * @param c the element processing context
     * @since 1.0
     */
    protected void initializeLogin(Context c) {
    }

    /**
     * Hook method that is called to create the template instance.
     *
     * @return the template to use for login
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
     * {@code CredentialsManager} that backs this authentication element.
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
     * {@code CredentialsManager} that backs this authentication element.
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
     * Hook method that is called when the {@code SessionManager} couldn't
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
        initializeLogin(c);

        final var auth_config = getAuthConfig();
        final var credentials_class = auth_config.credentialsClass();
        final var session_validator = auth_config.sessionValidator();

        assert credentials_class != null;
        assert session_validator != null;

        var auth_data = auth_config.generateAuthData(c);

        final Template template = getTemplate();

        // check if a cookie for remember id is provided
        var remember_id = c.cookieValue(auth_config.rememberCookieName());
        if (remember_id != null && auth_config.allowRemember()) {
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
                startNewSession(c, userid, auth_data, true, true);
            }
        }

        if (c.method() == RequestMethod.POST) {
            var credentials = c.parametersBean(credentials_class);

            if (!credentials.validate()) {
                unvalidatedCredentials(template, credentials);
                c.generateForm(template, credentials);
            } else {
                if (auth_config.role() != null && credentials instanceof RoleUserCredentials role_user) {
                    role_user.setRole(auth_config.role());
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
                    if (!startNewSession(c, userid, auth_data, remember, false)) {
                        // errors occurred, notify user
                        sessionCreationError(template, credentials);
                    }
                }
            }
        }

        c.print(template);
    }

    private boolean startNewSession(Context c, long userid, String authData, boolean remember, boolean remembered)
    throws Exception {
        final var auth_config = getAuthConfig();

        if (remember) {
            var remember_manager = auth_config.sessionValidator().getRememberManager();
            if (null == remember_manager) {
                throw new UndefinedAuthenticationRememberManagerException();
            }

            var remember_id = remember_manager.createRememberId(userid);

            if (remember_id != null) {
                c.addCookie(new CookieBuilder(auth_config.rememberCookieName(), remember_id)
                    .path("/")
                    .maxAge(auth_config.rememberMaxAge())
                    .secure(true)
                    .httpOnly(true));
            }
        }

        var authid = auth_config.sessionValidator().getSessionManager().startSession(userid, authData, remembered);

        if (null != authid) {
            authenticated(userid);

            // set cookie
            c.addCookie(new CookieBuilder(auth_config.authCookieName(), authid)
                .path("/")
                .secure(true)
                .httpOnly(true));

            var session_validator = auth_config.sessionValidator();
            assert session_validator != null;

            // validate the session
            var session_validity_id = session_validator.validateSession(authid, authData, this);

            // check if the validation allows access
            if (session_validator.isAccessAuthorized(session_validity_id)) {
                var auth_attribute = Authenticated.createAuthAttributeName(c.route(), auth_config.authCookieName(), authid);
                c.setAttribute(auth_attribute, true);
                setIdentityAttribute(c);

                // successful login
                c.redirect(auth_config.landingRoute());
                return true;
            }
        }

        return false;
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
}


