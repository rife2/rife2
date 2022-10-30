/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import rife.authentication.credentialsmanagers.MemoryUsers;
import rife.authentication.sessionmanagers.MemorySessions;

public class MemorySessionValidator extends BasicSessionValidator {
    private final MemoryUsers memoryUsers_ = new MemoryUsers();

    public MemorySessionValidator() {
        setCredentialsManager(memoryUsers_);
        setSessionManager(new MemorySessions());
    }

    public MemoryUsers getMemoryUsers() {
        return memoryUsers_;
    }
}