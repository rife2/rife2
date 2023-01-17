/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import rife.template.Template;
import rife.template.ValueRenderer;

import java.io.Serial;

public class RendererWrongTypeException extends TemplateException {
    @Serial private static final long serialVersionUID = -9116436595859491104L;

    private final Template template_;
    private final String rendererClassname_;

    public RendererWrongTypeException(Template template, String rendererClassname) {
        super("The renderer '" + rendererClassname + "' of template '" + template.getName() + "' doesn't implement '" + ValueRenderer.class.getName() + "'.", null);

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
