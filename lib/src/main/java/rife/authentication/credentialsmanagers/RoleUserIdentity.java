/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

public class RoleUserIdentity implements Cloneable {
    private String login_ = null;
    private RoleUserAttributes attributes_ = null;

    public RoleUserIdentity(String login, RoleUserAttributes attributes) {
        if (null == login) throw new IllegalArgumentException("login can't be null.");
        if (0 == login.length()) throw new IllegalArgumentException("login can't be empty.");
        if (null == attributes) throw new IllegalArgumentException("attributes can't be null.");

        login_ = login;
        attributes_ = attributes;
    }

    public String getLogin() {
        return login_;
    }

    public RoleUserAttributes getAttributes() {
        return attributes_;
    }

    public RoleUserIdentity clone() {
        RoleUserIdentity new_identity = null;
        try {
            new_identity = (RoleUserIdentity) super.clone();
            new_identity.attributes_ = attributes_.clone();
        } catch (CloneNotSupportedException ignored) {
        }

        return new_identity;
    }

    public boolean equals(Object other) {
        if (null == other) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (!(other instanceof RoleUserIdentity other_identity)) {
            return false;
        }

        if (!getLogin().equals(other_identity.getLogin())) {
            return false;
        }

        return getAttributes().equals(other_identity.getAttributes());
    }
}

