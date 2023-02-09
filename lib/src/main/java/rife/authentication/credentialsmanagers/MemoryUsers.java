/*
 * Copyright 2001-2008 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * Steven Grimm (koreth[remove] at midwinter dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 * $Id$
 */
package rife.authentication.credentialsmanagers;

import rife.authentication.Credentials;
import rife.authentication.CredentialsManager;
import rife.authentication.PasswordEncrypting;
import rife.authentication.credentials.RoleUserCredentials;
import rife.authentication.credentialsmanagers.exceptions.*;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.tools.StringEncryptor;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.*;

public class MemoryUsers implements CredentialsManager, RoleUsersManager, PasswordEncrypting {
    private final Map<Long, String> userIdMapping_ = new HashMap<>();
    private final Map<String, RoleUserAttributes> users_ = new TreeMap<>();
    private final Map<String, ArrayList<String>> roles_ = new TreeMap<>();
    private long userIdSequence_ = 0;

    private final ReadWriteLock lock_ = new ReentrantReadWriteLock();
    private final Lock readLock_ = lock_.readLock();
    private final Lock writeLock_ = lock_.writeLock();

    protected StringEncryptor passwordEncryptor_ = null;

    public MemoryUsers() {
    }

    public StringEncryptor getPasswordEncryptor() {
        return passwordEncryptor_;
    }

    public void setPasswordEncryptor(StringEncryptor passwordEncryptor) {
        passwordEncryptor_ = passwordEncryptor;
    }

    public long verifyCredentials(Credentials credentials)
    throws CredentialsManagerException {
        RoleUserCredentials role_user = null;
        if (credentials instanceof RoleUserCredentials) {
            role_user = (RoleUserCredentials) credentials;
        } else {
            throw new UnsupportedCredentialsTypeException(credentials);
        }

        readLock_.lock();
        try {
            if (null == role_user.getLogin()) {
                return -1;
            }

            var user_attributes = users_.get(role_user.getLogin());

            if (null == user_attributes) {
                return -1;
            }

            // handle automatic password encoding
            String password = null;
            try {
                if (null == passwordEncryptor_ || passwordEncryptor_.requiresAdaptiveVerification()) {
                    // correctly handle encoded passwords
                    password = StringEncryptor.adaptiveEncrypt(role_user.getPassword(), user_attributes.getPassword());
                } else {
                    password = passwordEncryptor_.encrypt(role_user.getPassword());
                }
            } catch (NoSuchAlgorithmException e) {
                throw new VerifyCredentialsErrorException(credentials, e);
            }

            // handle roles
            if (role_user.getRole() != null) {
                if (user_attributes.isValid(password, role_user.getRole())) {
                    return users_.get(role_user.getLogin()).getUserId();
                }
            } else {
                if (user_attributes.isValid(password)) {
                    return users_.get(role_user.getLogin()).getUserId();
                }
            }
        } finally {
            readLock_.unlock();
        }

        return -1;
    }

    public MemoryUsers addRole(String role)
    throws CredentialsManagerException {
        if (null == role ||
            0 == role.length()) {
            throw new AddRoleErrorException(role);
        }

        readLock_.lock();
        try {
            if (roles_.containsKey(role)) {
                throw new DuplicateRoleException(role);
            }

            roles_.put(role, new ArrayList<>());
        } finally {
            readLock_.unlock();
        }

        return this;
    }

    public long countRoles() {
        readLock_.lock();
        try {
            return roles_.size();
        } finally {
            readLock_.unlock();
        }
    }

    public boolean containsRole(String role) {
        if (null == role ||
            0 == role.length()) {
            return false;
        }

        readLock_.lock();
        try {
            return roles_.containsKey(role);
        } finally {
            readLock_.unlock();
        }
    }

    public MemoryUsers addUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        if (null == login ||
            0 == login.length() ||
            null == attributes) {
            throw new AddUserErrorException(login, attributes);
        }

        writeLock_.lock();
        try {
            // throw an exception if the user already exists
            if (users_.containsKey(login)) {
                throw new DuplicateLoginException(login);
            }

            // correctly handle implicit and specific user ids
            if (-1 == attributes.getUserId()) {
                while (userIdMapping_.containsKey(userIdSequence_)) {
                    // check for overflow and reset to 0
                    if (++userIdSequence_ < 0) {
                        userIdSequence_ = 0;
                    }
                }

                attributes.setUserId(userIdSequence_);
                userIdMapping_.put(userIdSequence_, login);
            } else {
                if (userIdMapping_.containsKey(attributes.getUserId())) {
                    throw new DuplicateUserIdException(attributes.getUserId());
                }

                userIdMapping_.put(attributes.getUserId(), login);
            }

            // correctly handle password encoding
            var attributes_clone = attributes.clone();
            if (passwordEncryptor_ != null &&
                !attributes_clone.getPassword().startsWith(passwordEncryptor_.prefix())) {
                try {
                    attributes_clone.setPassword(passwordEncryptor_.encrypt(attributes_clone.getPassword()));
                } catch (NoSuchAlgorithmException e) {
                    throw new AddUserErrorException(login, attributes, e);
                }
            }

            users_.put(login, attributes_clone);

            // create reverse links from the roles to the logins
            createRoleLinks(login, attributes_clone);
        } finally {
            writeLock_.unlock();
        }

        return this;
    }

    private void createRoleLinks(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        assert login != null;
        assert login.length() > 0;

        if (attributes.getRoles() != null &&
            attributes.getRoles().size() > 0) {
            ArrayList<String> logins = null;
            readLock_.lock();
            try {
                for (var role : attributes.getRoles()) {
                    if (!roles_.containsKey(role)) {
                        throw new UnknownRoleErrorException(role, login, attributes);
                    } else {
                        logins = roles_.get(role);

                        if (!logins.contains(login)) {
                            logins.add(login);
                        }
                    }
                }
            } finally {
                readLock_.unlock();
            }
        }
    }

    public RoleUserAttributes getAttributes(String login) {
        if (null == login ||
            0 == login.length()) {
            return null;
        }

        readLock_.lock();
        try {
            return users_.get(login);
        } finally {
            readLock_.unlock();
        }
    }

    public long countUsers() {
        readLock_.lock();
        try {
            return users_.size();
        } finally {
            readLock_.unlock();
        }
    }

    public boolean listRoles(ListRoles processor) {
        if (null == processor) {
            return false;
        }

        readLock_.lock();
        try {
            if (0 == roles_.size()) {
                return true;
            }

            var result = false;

            for (var role : roles_.keySet()) {
                result = true;

                if (!processor.foundRole(role)) {
                    break;
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }

    public boolean listUsers(ListUsers processor) {
        if (null == processor) {
            return false;
        }

        readLock_.lock();
        try {
            if (0 == users_.size()) {
                return false;
            }

            var result = false;

            RoleUserAttributes attributes = null;
            for (var login : users_.keySet()) {
                result = true;

                attributes = users_.get(login);

                if (!processor.foundUser(attributes.getUserId(), login, attributes.getPassword())) {
                    break;
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }

    public boolean listUsers(ListUsers processor, int limit, int offset) {
        readLock_.lock();
        try {
            if (null == processor ||
                limit <= 0 ||
                0 == users_.size()) {
                return false;
            }

            var result = false;

            RoleUserAttributes attributes = null;
            var count = 0;
            for (var login : users_.keySet()) {
                if (count < offset) {
                    count++;
                    continue;
                }

                if (count - offset >= limit) {
                    break;
                }

                count++;
                result = true;

                attributes = users_.get(login);

                if (!processor.foundUser(attributes.getUserId(), login, attributes.getPassword())) {
                    break;
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }

    public boolean containsUser(String login) {
        if (null == login ||
            0 == login.length()) {
            return false;
        }

        readLock_.lock();
        try {
            return users_.containsKey(login);
        } finally {
            readLock_.unlock();
        }
    }

    public boolean listUsersInRole(ListUsers processor, String role)
    throws CredentialsManagerException {
        if (null == processor) {
            return false;
        }

        if (null == role ||
            0 == role.length()) {
            return false;
        }

        if (0 == users_.size()) {
            return false;
        }

        var result = false;

        readLock_.lock();
        try {
            RoleUserAttributes attributes = null;
            for (var login : users_.keySet()) {
                attributes = users_.get(login);
                if (null == attributes.getRoles() ||
                    !attributes.getRoles().contains(role)) {
                    continue;
                }

                result = true;
                if (!processor.foundUser(attributes.getUserId(), login, attributes.getPassword())) {
                    break;
                }
            }
        } finally {
            readLock_.unlock();
        }

        return result;
    }

    public boolean isUserInRole(long userId, String role) {
        if (userId < 0 ||
            null == role ||
            0 == role.length()) {
            return false;
        }

        readLock_.lock();
        try {
            var login = userIdMapping_.get(userId);

            if (null == login) {
                return false;
            }

            var user_attributes = users_.get(login);

            if (null == user_attributes) {
                return false;
            }

            return user_attributes.isInRole(role);
        } finally {
            readLock_.unlock();
        }
    }

    public String getLogin(long userId) {
        if (userId < 0) {
            return null;
        }

        String login = null;


        readLock_.lock();
        try {
            login = userIdMapping_.get(userId);
        } finally {
            readLock_.unlock();
        }
        return login;
    }

    public long getUserId(String login) {
        if (null == login ||
            0 == login.length()) {
            return -1;
        }

        long userid = -1;

        readLock_.lock();
        try {
            var attributes = users_.get(login);
            if (attributes != null) {
                userid = attributes.getUserId();
            }
        } finally {
            readLock_.unlock();
        }

        return userid;
    }

    public boolean updateUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        if (null == login ||
            0 == login.length() ||
            null == attributes) {
            throw new UpdateUserErrorException(login, attributes);
        }

        writeLock_.lock();
        try {
            if (!users_.containsKey(login)) {
                return false;
            }

            // get the current attributes
            var current_attributes = users_.get(login);

            // set the current password if it has not been provided
            var attributes_clone = attributes.clone();
            if (null == attributes_clone.getPassword()) {
                attributes_clone.setPassword(current_attributes.getPassword());
            } else {
                // correctly handle password encoding
                if (passwordEncryptor_ != null &&
                    !attributes_clone.getPassword().startsWith(passwordEncryptor_.toString())) {
                    try {
                        attributes_clone.setPassword(passwordEncryptor_.encrypt(attributes_clone.getPassword()));
                    } catch (NoSuchAlgorithmException e) {
                        throw new UpdateUserErrorException(login, attributes, e);
                    }
                }
            }

            // ensure that the user id remains the same
            attributes_clone.setUserId(current_attributes.getUserId());

            // update the reverse link from the roles collection
            removeRoleLinks(login);

            // store the new user attributes
            users_.put(login, attributes_clone);

            // create reverse links from the roles to the logins
            createRoleLinks(login, attributes_clone);
        } finally {
            writeLock_.unlock();
        }

        return true;
    }

    public boolean removeUser(String login) {
        if (null == login ||
            0 == login.length()) {
            return false;
        }

        writeLock_.lock();
        try {
            // update the reverse link from the roles collection
            removeRoleLinks(login);

            // remove the user
            return null != users_.remove(login);
        } finally {
            writeLock_.unlock();
        }
    }

    public boolean removeUser(long userId) {
        if (userId < 0) {
            return false;
        }

        String login = null;

        writeLock_.lock();
        try {
            if (null == userIdMapping_.get(userId)) {
                return false;
            } else {
                login = userIdMapping_.get(userId);

                // update the reverse link from the roles collection
                removeRoleLinks(login);

                // remove the user
                return null != users_.remove(login);

            }
        } finally {
            writeLock_.unlock();
        }
    }

    public boolean removeRole(String name) {
        if (null == name ||
            0 == name.length()) {
            return false;
        }

        writeLock_.lock();
        try {
            if (roles_.remove(name) == null) {
                return false;
            }

            for (var key : users_.keySet()) {
                var roles = users_.get(key).getRoles();

                if (roles != null) {
                    roles.remove(name);
                }
            }
        } finally {
            writeLock_.unlock();
        }
        return true;
    }

    private void removeRoleLinks(String login) {
        assert login != null;
        assert login.length() > 0;

        var attributes = users_.get(login);
        if (attributes != null &&
            attributes.getRoles() != null &&
            attributes.getRoles().size() > 0) {
            // remove the login from the roles it's registered for
            ArrayList<String> logins = null;
            ArrayList<String> roles_to_delete = null;
            for (var role : attributes.getRoles()) {
                logins = roles_.get(role);
                logins.remove(login);
                if (0 == logins.size()) {
                    if (null == roles_to_delete) {
                        roles_to_delete = new ArrayList<>();
                    }

                    roles_to_delete.add(role);
                }
            }

            // remove the roles that now don't have any logins anymore
            if (roles_to_delete != null) {
                for (var role : roles_to_delete) {
                    roles_.remove(role);
                }
            }
        }
    }

    public void clearUsers() {
        writeLock_.lock();
        try {
            userIdSequence_ = 0;
            userIdMapping_.clear();
            users_.clear();
            roles_.clear();
        } finally {
            writeLock_.unlock();
        }
    }

    public boolean listUserRoles(String login, ListRoles processor)
    throws CredentialsManagerException {
        readLock_.lock();
        try {
            if (null == users_.get(login)) {
                return false;
            }

            if (null == processor) {
                return false;
            }

            if (0 == roles_.size()) {
                return true;
            }

            var result = false;

            for (var role : roles_.keySet()) {
                var attributes = users_.get(login);

                if (attributes.isInRole(role)) {
                    result = true;

                    if (!processor.foundRole(role)) {
                        break;
                    }
                }
            }

            return result;
        } finally {
            readLock_.unlock();
        }
    }
}

