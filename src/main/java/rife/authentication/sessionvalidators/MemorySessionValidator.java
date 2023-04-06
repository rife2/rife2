/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.sessionvalidators;

import rife.authentication.RememberManager;
import rife.authentication.credentialsmanagers.MemoryUsers;
import rife.authentication.sessionmanagers.MemorySessions;

public class MemorySessionValidator extends BasicSessionValidator<MemoryUsers, MemorySessions, RememberManager> {
    public MemorySessionValidator() {
        setCredentialsManager(new MemoryUsers());
        setSessionManager(new MemorySessions());
    }
}