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