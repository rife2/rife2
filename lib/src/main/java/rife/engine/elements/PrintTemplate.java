/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;
import rife.template.Template;

public class PrintTemplate implements Element {
    final String templateName_;

    public PrintTemplate(String name) {
        templateName_ = name;
    }

    public Template getTemplate(Context c) {
        return c.htmlTemplate(templateName_);
    }

    public void process(Context c)
    throws Exception {
        c.print(getTemplate(c));
    }
}

