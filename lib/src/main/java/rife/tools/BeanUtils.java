/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public abstract class BeanUtils {
    public static DateFormat getConcisePreciseDateFormat() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ", Localization.getLocale());
        sf.setTimeZone(RifeConfig.instance().tools().defaultTimeZone());
        return sf;
    }
}
