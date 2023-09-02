/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class RoleUserAttributes implements Cloneable {
    private long userId_ = -1;
    private String password_ = null;
    private HashSet<String> roles_ = null;

    public RoleUserAttributes() {
    }

    public RoleUserAttributes(long userId, String password) {
        setUserId(userId);
        setPassword(password);
    }

    public RoleUserAttributes(long userId, String password, String... roles) {
        setUserId(userId);
        setPassword(password);
        setRoles(roles);
    }

    public RoleUserAttributes(long userId, String password, Collection<String> roles) {
        setUserId(userId);
        setPassword(password);
        setRoles(roles);
    }

    public RoleUserAttributes(String password) {
        setPassword(password);
    }

    public RoleUserAttributes(String password, String... roles) {
        setPassword(password);
        setRoles(roles);
    }

    public RoleUserAttributes(String password, Collection<String> roles) {
        setPassword(password);
        setRoles(roles);
    }

    public RoleUserAttributes(long userId) {
        setUserId(userId);
    }

    public RoleUserAttributes(long userId, String... roles) {
        setUserId(userId);
        setRoles(roles);
    }

    public RoleUserAttributes(long userId, Collection<String> roles) {
        setUserId(userId);
        setRoles(roles);
    }

    public RoleUserAttributes(String... roles) {
        setRoles(roles);
    }

    public RoleUserAttributes(Collection<String> roles) {
        setRoles(roles);
    }

    public void setUserId(long userId) {
        if (userId < 0) throw new IllegalArgumentException("userId can't be negative.");

        userId_ = userId;
    }

    public RoleUserAttributes userId(long userId) {
        setUserId(userId);
        return this;
    }

    public long getUserId() {
        return userId_;
    }

    public void setPassword(String password) {
        if (password != null && password.isEmpty()) throw new IllegalArgumentException("password can't be empty.");

        password_ = password;
    }

    public RoleUserAttributes password(String password) {
        setPassword(password);
        return this;
    }

    public String getPassword() {
        return password_;
    }

    public void setRoles(Collection<String> roles) {
        if (null == roles) {
            roles_ = null;
            return;
        }

        roles_ = new HashSet<>(roles);
    }

    public void setRoles(String... roles) {
        if (roles != null &&
            roles.length > 0) {
            setRoles(new HashSet<>(Arrays.asList(roles)));
        }
    }

    public RoleUserAttributes roles(String... roles) {
        setRoles(roles);
        return this;
    }

    public void addRole(String role) {
        if (null == roles_) {
            roles_ = new HashSet<>();
        }
        roles_.add(role);
    }

    public RoleUserAttributes role(String role) {
        addRole(role);
        return this;
    }

    public void removeRole(String role) {
        if (null == roles_) {
            return;
        }
        roles_.remove(role);
    }

    public Collection<String> getRoles() {
        if (null == roles_) {
            roles_ = new HashSet<>();
        }

        return roles_;
    }

    public boolean isInRole(String role) {
        if (null == role) throw new IllegalArgumentException("role can't be null.");
        if (role.isEmpty()) throw new IllegalArgumentException("role can't be empty.");

        if (null == roles_) {
            return false;
        }

        return roles_.contains(role);
    }

    public boolean isValid(String password) {
        if (null == password) throw new IllegalArgumentException("password can't be null.");
        if (password.isEmpty()) throw new IllegalArgumentException("password can't be empty.");

        return password.equals(password_);
    }

    public boolean isValid(String password, String role) {
        return isValid(password) && isInRole(role);
    }

    @Override
    public RoleUserAttributes clone() {
        RoleUserAttributes new_attributes = null;
        try {
            new_attributes = (RoleUserAttributes) super.clone();

            if (roles_ != null) {
                new_attributes.roles_ = new HashSet<>(roles_);
            }
        } catch (CloneNotSupportedException ignored) {
        }

        return new_attributes;
    }

    public boolean equals(Object other) {
        if (null == other) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (!(other instanceof RoleUserAttributes other_attributes)) {
            return false;
        }

        if (getUserId() != other_attributes.getUserId()) {
            return false;
        }
        if (!getPassword().equals(other_attributes.getPassword())) {
            return false;
        }
        var roles = getRoles();
        var other_roles = other_attributes.getRoles();
        if ((roles != null || other_roles != null)) {
            if (null == roles || null == other_roles) {
                return false;
            }
            if (roles.size() != other_roles.size()) {
                return false;
            }

            for (var role : roles) {
                if (!other_roles.contains(role)) {
                    return false;
                }
            }
        }

        return true;
    }
}

