/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

/**
 * Helper class to avoid Double Check Locking
 * and still have a thread-safe singleton pattern
 */
class EncoderJsonSingleton {
    static final EncoderJson INSTANCE = new EncoderJson();
}

