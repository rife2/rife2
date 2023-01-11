/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.renderers;

import rife.config.RifeConfig;
import rife.template.*;

import java.time.LocalDateTime;
import java.util.Date;

public class DateRenderer implements ValueRenderer {
    public String render(Template template, String valueId, String differentiator) {
        return RifeConfig.tools().getDefaultShortDateFormat().format(LocalDateTime.now());
    }
}