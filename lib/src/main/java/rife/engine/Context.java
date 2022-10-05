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
import rife.tools.FileUtils;
import rife.tools.StringUtils;

public class Context {
    public static final String ID_WEBAPP_ROOT_URL = "webapp:rootUrl";
    public static final String ID_SERVER_ROOT_URL = "server:rootUrl";

    private final String gateUrl_;
    private final Request request_;
    private final Response response_;
    private final Route route_;
    private final Element element_;

    public Context(String gateUrl, Request request, Response response, Route route) {
        gateUrl_ = gateUrl;
        request_ = request;
        response_ = response;
        route_ = route;
        element_ = route_.getElementInstance(this);
    }

    public void process() {
        try {
            element_.process(this);
        } catch (Exception e) {
            throw new EngineException(e);
        }
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
        var set_values = new EngineTemplateProcessor(this, template).processTemplate();

        // set the content type
        if (!response_.isContentTypeSet()) {
            String content_type = template.getDefaultContentType();
            if (null == content_type) {
                content_type = RifeConfig.engine().getDefaultContentType();
            }

            response_.setContentType(content_type);
        }

        // print the element contents with the auto-generated values
        response_.print(template);

        // clean up the values that were set
        template.removeValues(set_values);
    }

    public Template getHtmlTemplate()
    throws TemplateException, EngineException {
        return getHtmlTemplate(route_.getDefaultElementId(), null);
    }

    public Template getHtmlTemplate(String name)
    throws TemplateException, EngineException {
        return getHtmlTemplate(name, null);
    }

    public Template getHtmlTemplate(String name, String encoding)
    throws TemplateException, EngineException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.HTML.get(name, encoding);
    }

    public String getGateUrl() {
        return gateUrl_;
    }

    public String getServerRootUrl(int port) {
        return request_.getServerRootUrl(port);
    }

    public String getWebappRootUrl(int port) {
        if (RifeConfig.engine().getProxyRootUrl() != null) {
            return RifeConfig.engine().getProxyRootUrl();
        }

        StringBuilder webapp_root = new StringBuilder();
        webapp_root.append(getServerRootUrl(port));
        String gate_url = getGateUrl();
        if (!gate_url.startsWith("/")) {
            webapp_root.append("/");
        }
        webapp_root.append(gate_url);
        if (gate_url.length() > 0 &&
            !gate_url.endsWith("/")) {
            webapp_root.append("/");
        }

        return webapp_root.toString();
    }

    public String getUrlFor(Route route) {
        return getWebappRootUrl(-1) + StringUtils.stripFromFront(route.path(), "/");
    }
}
