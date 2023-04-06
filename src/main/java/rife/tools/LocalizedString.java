/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

/**
 * This class makes it possible to always keep a localized string up-to-date.
 * By providing it with a lookup key instead of the final text, it is able to
 * always provide a text representation according to the active default
 * localization settings.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class LocalizedString implements CharSequence {
    private String key_ = null;

    /**
     * Instantiates a new {@code LocalizedString} instance.
     *
     * @param key the key that will be used to look up the localized string
     * @since 1.0
     */
    public LocalizedString(String key) {
        if (null == key) throw new IllegalArgumentException("key can't be null.");

        key_ = key;
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    public int length() {
        return toString().length();
    }

    public String toString() {
        return Localization.getString(key_);
    }
}

