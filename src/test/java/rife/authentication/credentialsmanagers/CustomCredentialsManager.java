/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers;

import rife.authentication.Credentials;
import rife.authentication.CredentialsManager;

/**
 * Dummy credentials manager used to verify that we can use our own
 * credentials' manager.
 */
public class CustomCredentialsManager implements CredentialsManager {
    private String id_;

    public CustomCredentialsManager() {
    }

    public CustomCredentialsManager(String id) {
        id_ = id;
    }

    public String getId() {
        return id_;
    }

    public long verifyCredentials(Credentials credentials) {
        return 0;
    }
}
