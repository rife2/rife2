/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.elements;

import rife.engine.Context;
import rife.engine.Element;
import rife.template.Template;

/**
 * Standard element that will print a template with particular name.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Context#print(Template)
 * @since 1.0
 */
public class PrintTemplate implements Element {
    final String templateName_;

    /**
     * Constructs the element with the name of the template to print.
     *
     * @param name the name of the template to print
     * @since 1.0
     */
    public PrintTemplate(String name) {
        templateName_ = name;
    }

    private Template getTemplate(Context c) {
        return c.template(templateName_);
    }

    public void process(Context c) {
        c.print(getTemplate(c));
    }
}

