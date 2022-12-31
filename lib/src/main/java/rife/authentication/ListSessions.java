/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication;

/**
 * <p>This interface to be able to list all the active sessions in a {@link
 * SessionManager} without having to store them all in memory.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
@FunctionalInterface
public interface ListSessions {
    /**
     * <p>This method is called when active authentication session was found.
     *
     * @param userId the unique ID of the user
     * @param hostIp the IP address of the host that initiated the session
     * @param authId the unique identifier of this authentication session
     * @return {@code true} when the next active session should be
     * returned; or
     * <p>{@code false} if the process should be interrupted
     * @since 1.0
     */
    boolean foundSession(long userId, String hostIp, String authId);
}

