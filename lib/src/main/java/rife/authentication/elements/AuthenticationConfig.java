/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.elements;

import rife.authentication.Credentials;
import rife.authentication.SessionValidator;
import rife.authentication.credentials.RoleUser;
import rife.authentication.credentialsmanagers.RoleUserIdentity;
import rife.engine.Route;

public class AuthenticationConfig {
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

    public AuthenticationConfig(SessionValidator sessionValidator) {
        sessionValidator_ = sessionValidator;
    }

    public SessionValidator sessionValidator() {
        return sessionValidator_;
    }

    public AuthenticationConfig sessionValidator(SessionValidator sessionValidator) {
        sessionValidator_ = sessionValidator;
        return this;
    }

    public Route loginRoute() {
        return loginRoute_;
    }

    public AuthenticationConfig loginRoute(Route route) {
        loginRoute_ = route;
        return this;
    }

    public Route landingRoute() {
        return landingRoute_;
    }

    public AuthenticationConfig landingRoute(Route route) {
        landingRoute_ = route;
        return this;
    }

    public String identityAttributeName() {
        return identityAttributeName_;
    }

    public AuthenticationConfig identityAttributeName(String identityAttributeName) {
        identityAttributeName_ = identityAttributeName;
        return this;
    }

    public String authCookieName() {
        return authCookieName_;
    }

    public AuthenticationConfig authCookieName(String authCookieName) {
        authCookieName_ = authCookieName;
        return this;
    }

    public String rememberCookieName() {
        return rememberCookieName_;
    }

    public AuthenticationConfig rememberCookieName(String rememberCookieName) {
        rememberCookieName_ = rememberCookieName;
        return this;
    }

    public int rememberMaxAge() {
        return rememberMaxAge_;
    }

    public AuthenticationConfig rememberMaxAge(int maxAge) {
        rememberMaxAge_ = maxAge;
        return this;
    }

    public boolean prohibitRemember() {
        return prohibitRemember_;
    }

    public AuthenticationConfig prohibitRemember(boolean prohibitRemember) {
        prohibitRemember_ = prohibitRemember;
        return this;
    }

    public boolean enforceAuthentication() {
        return enforceAuthentication_;
    }

    public AuthenticationConfig enforceAuthentication(boolean enforceAuthentication) {
        enforceAuthentication_ = enforceAuthentication;
        return this;
    }

    public Class<? extends Credentials> credentialsClass() {
        return credentialsClass_;
    }

    public AuthenticationConfig credentialsClass(Class<? extends Credentials> credentialsClass) {
        credentialsClass_ = credentialsClass;
        return this;
    }

    public String role() {
        return role_;
    }

    public AuthenticationConfig role(String role) {
        role_ = role;
        return this;
    }
}
