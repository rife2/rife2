/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.tools.StringUtils;

public class EncoderSql implements TemplateEncoder {
    EncoderSql() {
    }

    public static EncoderSql instance() {
        return EncoderSqlSingleton.INSTANCE;
    }

    public String encode(String value) {
        return StringUtils.encodeSql(value);
    }

    public final String encodeDefensive(String value) {
        return value;
    }
}
