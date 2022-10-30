/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.template.Template;

import java.io.Serial;

public class RendererInstantiationException extends TemplateException {
    @Serial private static final long serialVersionUID = -8504670953875633042L;

    private final Template template_;
    private final String rendererClassname_;

    public RendererInstantiationException(Template template, String rendererClassname, Throwable cause) {
        super("The renderer '" + rendererClassname + "' of template '" + template.getName() + "'.", cause);

        template_ = template;
        rendererClassname_ = rendererClassname;
    }

    public String getRendererClassname() {
        return rendererClassname_;
    }

    public Template getTemplate() {
        return template_;
    }
}
