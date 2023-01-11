/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

public class EncoderXml implements TemplateEncoder {
    EncoderXml() {
    }

    public static EncoderXml instance() {
        return EncoderXmlSingleton.INSTANCE;
    }

    public String encode(String value) {
        return StringUtils.encodeXml(value);
    }

    public final String encodeDefensive(String value) {
        return value;
    }
}
