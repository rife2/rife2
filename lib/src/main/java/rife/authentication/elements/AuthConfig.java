/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.Credentials;
import rife.authentication.SessionValidator;
import rife.authentication.credentials.RoleUser;
import rife.authentication.credentialsmanagers.RoleUserIdentity;
import rife.engine.Context;
import rife.engine.Route;

public class AuthConfig {
    public static final String DEFAULT_IDENTITY_ATTRIBUTE_NAME = RoleUserIdentity.class.getName();
    public static final String DEFAULT_AUTH_COOKIE_NAME = "authId";
    public static final String DEFAULT_REMEMBER_COOKIE_NAME = "rememberId";
    public static final int DEFAULT_REMEMBER_MAX_AGE = 60 * 60 * 24 * 30 * 3;  // three months
    public static final boolean DEFAULT_PROHIBIT_REMEMBER = false;
    public static final boolean DEFAULT_ENFORCE_AUTHENTICATION = true;
    public static final Class<RoleUser> DEFAULT_CREDENTIALS_CLASS = RoleUser.class;

    private SessionValidator sessionValidator_;
    private Route loginRoute_;
    private Route landingRoute_;
    private String identityAttributeName_ = DEFAULT_IDENTITY_ATTRIBUTE_NAME;
    private String authCookieName_ = DEFAULT_AUTH_COOKIE_NAME;
    private String rememberCookieName_ = DEFAULT_REMEMBER_COOKIE_NAME;
    private int rememberMaxAge_ = DEFAULT_REMEMBER_MAX_AGE;
    private boolean prohibitRemember_ = DEFAULT_PROHIBIT_REMEMBER;
    private boolean enforceAuthentication_ = DEFAULT_ENFORCE_AUTHENTICATION;
    private Class<? extends Credentials> credentialsClass_ = DEFAULT_CREDENTIALS_CLASS;
    private String role_ = null;
    private String staticAuthData_ = null;

    public AuthConfig(SessionValidator sessionValidator) {
        sessionValidator_ = sessionValidator;
    }

    public SessionValidator sessionValidator() {
        return sessionValidator_;
    }

    public AuthConfig sessionValidator(SessionValidator sessionValidator) {
        sessionValidator_ = sessionValidator;
        return this;
    }

    public Route loginRoute() {
        return loginRoute_;
    }

    public AuthConfig loginRoute(Route route) {
        loginRoute_ = route;
        return this;
    }

    public Route landingRoute() {
        return landingRoute_;
    }

    public AuthConfig landingRoute(Route route) {
        landingRoute_ = route;
        return this;
    }

    public String identityAttributeName() {
        return identityAttributeName_;
    }

    public AuthConfig identityAttributeName(String name) {
        identityAttributeName_ = name;
        return this;
    }

    public RoleUserIdentity identityAttribute(Context c) {
        return (RoleUserIdentity) c.attribute(identityAttributeName());
    }

    public String authCookieName() {
        return authCookieName_;
    }

    public AuthConfig authCookieName(String name) {
        authCookieName_ = name;
        return this;
    }

    public String rememberCookieName() {
        return rememberCookieName_;
    }

    public AuthConfig rememberCookieName(String name) {
        rememberCookieName_ = name;
        return this;
    }

    public int rememberMaxAge() {
        return rememberMaxAge_;
    }

    public AuthConfig rememberMaxAge(int maxAge) {
        rememberMaxAge_ = maxAge;
        return this;
    }

    public boolean prohibitRemember() {
        return prohibitRemember_;
    }

    public AuthConfig prohibitRemember(boolean prohibitRemember) {
        prohibitRemember_ = prohibitRemember;
        return this;
    }

    public boolean enforceAuthentication() {
        return enforceAuthentication_;
    }

    public AuthConfig enforceAuthentication(boolean enforceAuthentication) {
        enforceAuthentication_ = enforceAuthentication;
        return this;
    }

    public Class<? extends Credentials> credentialsClass() {
        return credentialsClass_;
    }

    public AuthConfig credentialsClass(Class<? extends Credentials> credentialsClass) {
        credentialsClass_ = credentialsClass;
        return this;
    }

    public String role() {
        return role_;
    }

    public AuthConfig role(String role) {
        role_ = role;
        return this;
    }

    public String generateAuthData(Context c) {
        if (staticAuthData_ != null) {
            return staticAuthData_;
        }

        return c.remoteAddr();
    }

    public String staticAuthData() {
        return staticAuthData_;
    }

    public AuthConfig staticAuthData(String authData) {
        staticAuthData_ = authData;
        return this;
    }
}
