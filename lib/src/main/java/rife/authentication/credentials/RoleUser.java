/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentials;

import rife.config.RifeConfig;
import rife.validation.*;

/**
 * <p>Provides standard {@link RoleUserCredentials} functionalities by
 * implementing the property accessors and setting up basic validation rules.
 * These rules make the login and password mandatory and limit their length
 * according to the settings in {@link
 * rife.config.RifeConfig.AuthenticationConfig}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class RoleUser extends MetaData implements RoleUserCredentials, RememberMe {
    private String login_ = null;
    private String password_ = null;
    private String role_ = null;
    private boolean remember_ = false;

    public RoleUser() {
    }

    public void activateMetaData() {
        addConstraint(new ConstrainedProperty("login")
            .notNull(true)
            .minLength(RifeConfig.authentication().getLoginMinimumLength())
            .maxLength(RifeConfig.authentication().getLoginMaximumLength()));
        addConstraint(new ConstrainedProperty("password")
            .notNull(true)
            .minLength(RifeConfig.authentication().getPasswordMinimumLength())
            .maxLength(RifeConfig.authentication().getPasswordMaximumLength()));
    }

    public RoleUser(String login, String password) {
        setLogin(login);
        setPassword(password);
    }

    public RoleUser(String login, String password, String role) {
        setLogin(login);
        setPassword(password);
        setRole(role);
    }

    public String getLogin() {
        return login_;
    }

    public RoleUser login(String login) {
        setLogin(login);

        return this;
    }

    public void setLogin(String login) {
        login_ = login;
    }

    public String getPassword() {
        return password_;
    }

    public RoleUser password(String password) {
        setPassword(password);

        return this;
    }

    public void setPassword(String password) {
        password_ = password;
    }

    public String getRole() {
        return role_;
    }

    public RoleUser role(String role) {
        setRole(role);

        return this;
    }

    public void setRole(String role) {
        role_ = role;
    }

    public boolean getRemember() {
        return remember_;
    }

    public RoleUser remember(boolean remember) {
        setRemember(remember);

        return this;
    }

    public void setRemember(boolean remember) {
        remember_ = remember;
    }
}

