/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.security.*;

/**
 * Generates a 216 bit unique ID that is more secure than the standard 128 bit UUID,
 * which has an effective 122 bits of entropy.
 * <p>
 * This <code>UniqueID</code> has the same string representation length, meaning that
 * it can be a drop-in replacement for the standard UUID string.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class UniqueIDGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static UniqueID generate() {
        byte[] buffer = new byte[27];
        RANDOM.nextBytes(buffer);
        return new UniqueID(buffer);
    }
}