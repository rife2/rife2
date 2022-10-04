/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.exceptions.EngineException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.template.exceptions.TemplateException;

public class Context {
    private final Request request_;
    private final Response response_;

    public Context(Request request, Response response) {
        this.request_ = request;
        this.response_ = response;
    }

    public Request request() {
        return request_;
    }

    public Response response() {
        return response_;
    }

    public void print(Object o) {
        response_.print(o);
    }

    public void print(Template template)
    throws TemplateException, EngineException {
        // TODO
//        List<String> set_values = new EngineTemplateProcessor(this, template).processTemplate();

        // set the content type
        if (!response_.isContentTypeSet()) {
            String content_type = template.getDefaultContentType();
            if (null == content_type) {
                content_type = RifeConfig.engine().defaultContentType();
            }

            response_.setContentType(content_type);
        }

        // print the element contents with the auto-generated values
        response_.print(template);

        // clean up the values that were set
        // TODO
//        template.removeValues(set_values);
    }

    public Template getHtmlTemplate(String name)
    throws TemplateException, EngineException {
        return getHtmlTemplate(name, null);
    }

    public Template getHtmlTemplate(String name, String encoding)
    throws TemplateException, EngineException
    {
        if (null == name)			throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length())		throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.HTML.get(name, encoding);
    }
}
