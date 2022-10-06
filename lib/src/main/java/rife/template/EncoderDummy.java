/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

public class EncoderDummy implements TemplateEncoder {
    EncoderDummy() {
    }

    public static EncoderDummy instance() {
        return EncoderDummySingleton.INSTANCE;
    }

    public final String encode(String value) {
        return value;
    }

    public final String encodeDefensive(String value) {
        return value;
    }
}
