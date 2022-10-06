/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

public class EncoderHtml implements TemplateEncoder {
    EncoderHtml() {
    }

    public static EncoderHtml instance() {
        return EncoderHtmlSingleton.INSTANCE;
    }

    public String encode(String value) {
        return StringUtils.encodeHtml(value);
    }

    public String encodeDefensive(String value) {
        return StringUtils.encodeHtmlDefensive(value);
    }
}
