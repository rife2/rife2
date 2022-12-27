/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.config.RifeConfig;
import rife.template.*;
import java.util.Date;

public class DateRenderer implements ValueRenderer {
    public String render(Template template, String valueId, String differentiator) {
        return RifeConfig.tools().getDefaultShortDateFormat().format(new Date());
    }
}