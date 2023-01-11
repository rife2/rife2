/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.Credentials;
import rife.authentication.SessionValidator;
import rife.authentication.credentials.RoleUser;
import rife.authentication.credentials.RoleUserCredentials;
import rife.authentication.credentialsmanagers.RoleUserIdentity;
import rife.authentication.elements.exceptions.UndefinedLandingRouteException;
import rife.authentication.elements.exceptions.UndefinedLoginRouteException;
import rife.engine.Context;
import rife.engine.Route;

/**
 * Configuration class that determines how the authentication element implementations
 * should behave. Each authenticated section usually has one {@code AuthConfig} instance
 * that is shared in order to agree upon how to work together for a coherent authentication
 * flow.
 * <p>
 * Most of the configuration options have defaults, except for {@code loginRoute} and
 * {@code landingRoute}. Without specifying those two routes, the authentication system
 * will not work properly.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class AuthConfig {
    public static final String DEFAULT_IDENTITY_ATTRIBUTE_NAME = RoleUserIdentity.class.getName();
    public static final String DEFAULT_AUTH_COOKIE_NAME = "authId";
    public static final String DEFAULT_REMEMBER_COOKIE_NAME = "rememberId";
    public static final int DEFAULT_REMEMBER_MAX_AGE = 60 * 60 * 24 * 30 * 3;  // three months
    public static final boolean DEFAULT_ALLOW_REMEMBER = true;
    public static final boolean DEFAULT_ENFORCE_AUTHENTICATION = true;
    public static final Class<RoleUser> DEFAULT_CREDENTIALS_CLASS = RoleUser.class;

    private SessionValidator<?, ?, ?> sessionValidator_;
    private Route loginRoute_;
    private Route landingRoute_;
    private String identityAttributeName_ = DEFAULT_IDENTITY_ATTRIBUTE_NAME;
    private String authCookieName_ = DEFAULT_AUTH_COOKIE_NAME;
    private String rememberCookieName_ = DEFAULT_REMEMBER_COOKIE_NAME;
    private int rememberMaxAge_ = DEFAULT_REMEMBER_MAX_AGE;
    private boolean allowRemember_ = DEFAULT_ALLOW_REMEMBER;
    private boolean enforceAuthentication_ = DEFAULT_ENFORCE_AUTHENTICATION;
    private Class<? extends Credentials> credentialsClass_ = DEFAULT_CREDENTIALS_CLASS;
    private String role_ = null;
    private String staticAuthData_ = null;

    /**
     * Create an {@code AuthConfig} instance for a specific {@code SessionValidator}.
     *
     * @param sessionValidator the session validator to use for authentication
     * @see SessionValidator
     * @since 1.0
     */
    public AuthConfig(SessionValidator<?, ?, ?> sessionValidator) {
        sessionValidator_ = sessionValidator;
    }

    /**
     * Sets the session validator for this config.
     *
     * @param sessionValidator the session validator to use.
     * @return the instance of this config
     * @since 1.0
     */
    public AuthConfig sessionValidator(SessionValidator<?, ?, ?> sessionValidator) {
        sessionValidator_ = sessionValidator;
        return this;
    }

    /**
     * Retrieve the session validator for this config.
     *
     * @return the session validator for this config.
     * @since 1.0
     */
    public SessionValidator<?, ?, ?> sessionValidator() {
        return sessionValidator_;
    }

    /**
     * Sets the route that should be used when user login is necessary.
     *
     * @param route the user login route to use
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig loginRoute(Route route) {
        loginRoute_ = route;
        return this;
    }

    /**
     * Retrieves the route that is used when user login is necessary.
     *
     * @return this config's user login route
     * @since 1.0
     */
    public Route loginRoute() {
        if (loginRoute_ == null) {
            throw new UndefinedLoginRouteException();
        }
        return loginRoute_;
    }

    /**
     * Sets the route that should be used as the landing page when login
     * is successful.
     *
     * @param route the landing route to use
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig landingRoute(Route route) {
        landingRoute_ = route;
        return this;
    }

    /**
     * Retrieves the route that is used as the landing page when login is
     * successful.
     *
     * @return this config's login landing route
     * @since 1.0
     */
    public Route landingRoute() {
        if (landingRoute_ == null) {
            throw new UndefinedLandingRouteException();
        }
        return landingRoute_;
    }

    /**
     * Sets the name of the request attribute to use to store the instance of
     * {@link RoleUserIdentity} when a user was successfully identified.
     * <p>
     * Defaults to {@link #DEFAULT_IDENTITY_ATTRIBUTE_NAME}.
     *
     * @param name the name of the request attribute to use
     * @return this config instance
     * since 1.0
     */
    public AuthConfig identityAttributeName(String name) {
        identityAttributeName_ = name;
        return this;
    }

    /**
     * Retrieves the name of the request attribute in which the {@link RoleUserIdentity}
     * of a successfully identified user will be stored.
     *
     * @return the name of the request attribute
     * @since 1.0
     */
    public String identityAttributeName() {
        return identityAttributeName_;
    }

    /**
     * Retrieves the {@link RoleUserIdentity} request attribute from a {@code Context}.
     *
     * @param c the context to look the request attribute up in
     * @return the stored {@link RoleUserIdentity}; or {@code null} if the context
     * doesn't contain an identity with that attribute name, or if no user is identified
     * in this request.
     * @since 1.0
     */
    public RoleUserIdentity identityAttribute(Context c) {
        return (RoleUserIdentity) c.attribute(identityAttributeName());
    }

    /**
     * Sets the name of the cookie to use for the authentication ID.
     * <p>
     * Defaults to {@link #DEFAULT_AUTH_COOKIE_NAME}.
     *
     * @param name the authentication ID cookie name
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig authCookieName(String name) {
        authCookieName_ = name;
        return this;
    }

    /**
     * Retrieves the name of the authentication ID cookie.
     *
     * @return the name of the authentication ID cookie.
     * @since 1.0
     */
    public String authCookieName() {
        return authCookieName_;
    }

    /**
     * Sets the name of the cookie to use for the remember ID.
     * <p>
     * Defaults to {@link #DEFAULT_REMEMBER_COOKIE_NAME}.
     *
     * @param name the remember ID cookie name
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig rememberCookieName(String name) {
        rememberCookieName_ = name;
        return this;
    }

    /**
     * Retrieves the name of the remember ID cookie.
     *
     * @return the name of the remember ID cookie.
     * @since 1.0
     */
    public String rememberCookieName() {
        return rememberCookieName_;
    }

    /**
     * Sets the maximum age the remember cookie can have, when exceeded
     * the full login logic will have to be used again to authenticate a user.
     * <p>
     * Defaults to {@link #DEFAULT_REMEMBER_MAX_AGE}
     *
     * @param maxAge the maximum age in seconds
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig rememberMaxAge(int maxAge) {
        rememberMaxAge_ = maxAge;
        return this;
    }

    /**
     * Retrieves the maximum age the remember cookie can have.
     *
     * @return the maximum age in seconds
     * @since 1.0
     */
    public int rememberMaxAge() {
        return rememberMaxAge_;
    }

    /**
     * Sets whether remembering an authentication session is allowed or not.
     * <p>
     * Defaults to {@link #DEFAULT_ALLOW_REMEMBER}
     *
     * @param allowRemember {@code true} to allow remembering a session; or
     *                         {@code false} of remembering a session is prohibited
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig allowRemember(boolean allowRemember) {
        allowRemember_ = allowRemember;
        return this;
    }

    /**
     * Retrieves whether an authentication session can be remembered.
     *
     * @return {@code true} when the session can be remembered; or
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean allowRemember() {
        return allowRemember_;
    }

    /**
     * Sets whether authentication should be enforced in the {@link Authenticated element}.
     * <p>
     * When authentication is enforced, the client will be redirected to the login page
     * when no valid authentication session could be found.
     * <p>
     * Defaults to {@link #DEFAULT_ENFORCE_AUTHENTICATION}.
     *
     * @param enforceAuthentication {@code true} to enforce authentication; or {@code false} otherwise
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig enforceAuthentication(boolean enforceAuthentication) {
        enforceAuthentication_ = enforceAuthentication;
        return this;
    }

    /**
     * Retrieves whether authentication should be enforced in the {@link Authenticated} element.
     *
     * @return {@code true} when authentication should be enforced; or
     * {@code false} otherwise
     * @since 1.0
     */
    public boolean enforceAuthentication() {
        return enforceAuthentication_;
    }

    /**
     * Sets the credentials class that will be used by the {@link Login} element.
     * <p>
     * Defaults to {@link #DEFAULT_CREDENTIALS_CLASS}.
     *
     * @param credentialsClass the crendentials class to use
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig credentialsClass(Class<? extends Credentials> credentialsClass) {
        credentialsClass_ = credentialsClass;
        return this;
    }

    /**
     * Retrieves the credentials class that will be used by the {@link Login} element.
     *
     * @return the credentials class to use
     * @since 1.0
     */
    public Class<? extends Credentials> credentialsClass() {
        return credentialsClass_;
    }

    /**
     * Sets the role that the user is expecting to be in when validating the credentials.
     * <p>
     * This only works when the credentials class implements the {@link RoleUserCredentials} interface.
     * <p>
     * Defaults to {@code null}.
     *
     * @param role the role that is expected for authentication; or {@code null} if no role is required
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig role(String role) {
        role_ = role;
        return this;
    }

    /**
     * Retrieves the role that is expected for authentication.
     *
     * @return the role that is expected for authentication; or
     * {@code null} if no role is required
     * @since 1.0
     */
    public String role() {
        return role_;
    }

    /**
     * Generate data that will be associated with each authentication session.
     * <p>
     * By default, this will return the remote address of the client.
     * <p>
     * This data can be used to only allow authentication sessions to be retrieved
     * when the associated data corresponds, for instance to only limit the
     * session to the IP address that it was initiated from. This feature is
     * by default disabled, but it can be enabled in {@link rife.config.RifeConfig.AuthenticationConfig#setSessionRestrictAuthData}.
     *
     * @param c the current element processing context
     * @return the associated data for an authentication session
     * @since 1.0
     */
    public String generateAuthData(Context c) {
        if (staticAuthData_ != null) {
            return staticAuthData_;
        }

        return c.remoteAddr();
    }

    /**
     * Sets static authentication data to be associated with the sessions that
     * are initiated with this {@code AuthConfig}.
     * <p>
     * When setting static data, it will be returned by {@link #generateAuthData} instead of
     * the client's IP address.
     * <p>
     * Setting different static authentication data in authentication configs that share
     * the same {@code SessionValidator}, makes it possible to store different types of
     * sessions in the same structure with it being possible for the authentication IDs
     * to be interchanged
     *
     * @param authData the static authentication data to associated with sessions
     * @return this config instance
     * @since 1.0
     */
    public AuthConfig staticAuthData(String authData) {
        staticAuthData_ = authData;
        return this;
    }

    /**
     * Retrieves the static authentication data that will be associated with this config's
     * authentication sessions.
     *
     * @return the static authentication data for this config; or
     * {@code null} if no static authentication data is set up
     * @since 1.0
     */
    public String staticAuthData() {
        return staticAuthData_;
    }
}
