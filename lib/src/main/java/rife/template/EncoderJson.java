/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

public class EncoderJson implements TemplateEncoder {
    EncoderJson() {
    }

    public static EncoderJson instance() {
        return EncoderJsonSingleton.INSTANCE;
    }

    public String encode(String value) {
        return StringUtils.encodeJson(value);
    }

    public final String encodeDefensive(String value) {
        return value;
    }
}
