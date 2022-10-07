/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.exceptions.DeferException;
import rife.engine.exceptions.EngineException;
import rife.engine.exceptions.RedirectException;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.template.exceptions.TemplateException;
import rife.tools.StringUtils;

public class Context {
    public static final String ID_WEBAPP_ROOT_URL = "webapp:rootUrl";
    public static final String ID_SERVER_ROOT_URL = "server:rootUrl";

    private final String gateUrl_;
    private final Request request_;
    private final Response response_;
    private final Site.RouteMatch route_;
    private final Element element_;

    public Context(String gateUrl, Request request, Response response, Site.RouteMatch route) {
        gateUrl_ = gateUrl;
        request_ = request;
        response_ = response;
        route_ = route;
        element_ = route_.route().getElementInstance(this);
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

    public String pathInfo() {
        if (route_.pathInfo() != null) {
            return route_.pathInfo();
        }

        return null;
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
        return getHtmlTemplate(route_.route().getDefaultElementId(), null);
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

    public String urlFor(Route route) {
        return urlFor(route, null, null);
    }

    public String urlFor(Route route, String pathInfo) {
        return urlFor(route, pathInfo, null);
    }

    public String urlFor(Route route, Map<String, String[]> parameters) {
        return urlFor(route, null, parameters);
    }

    public String urlFor(Route route, String pathInfo, Map<String, String[]> parameters) {
        StringBuilder url = new StringBuilder(getWebappRootUrl(-1));

        url.append(StringUtils.stripFromFront(route.path(), "/"));

        if (pathInfo != null) {
            url.append(pathInfo);
        }

        if (parameters != null &&
            parameters.size() > 0) {
            StringBuilder query_parameters = new StringBuilder("?");

            for (var parameter_entry : parameters.entrySet()) {
                String parameter_name = parameter_entry.getKey();
                String[] parameter_values = parameter_entry.getValue();
                if (null == parameter_values) {
                    continue;
                }

                boolean added_separator = false;
                if (query_parameters.length() > 1 &&
                    !added_separator) {
                    added_separator = true;
                    query_parameters.append("&");
                }

                for (int i = 0; i < parameter_values.length; i++) {
                    query_parameters.append(StringUtils.encodeUrl(parameter_name));
                    query_parameters.append("=");
                    query_parameters.append(StringUtils.encodeUrl(parameter_values[i]));
                    if (i + 1 < parameter_values.length) {
                        query_parameters.append("&");
                    }
                }
            }

            url.append(query_parameters);
        }

        return url.toString();
    }


    /**
     * Interrupts the execution in RIFE completely and defers it to the
     * servlet container.
     * <p>If RIFE is being run as a filter, it will execute the next filter in
     * the chain.
     * <p>If RIFE is being run as a servlet, the status code {@code 404: Not
     * Found} will be sent to the client.
     *
     * @throws rife.engine.exceptions.EngineException a runtime
     *                                                exception that is used to immediately interrupt the execution, don't
     *                                                catch this exception
     * @since 1.0
     */
    public void defer()
    throws EngineException {
        throw new DeferException();
    }

    /**
     * Interrupts the execution in this element and redirects the client to
     * another URL.
     *
     * @param url the URL to which the request will be redirected
     * @throws rife.engine.exceptions.EngineException a runtime
     *                                                exception that is used to immediately interrupt the execution, don't
     *                                                catch this exception
     * @since 1.0
     */
    public void redirect(String url)
    throws EngineException {
        throw new RedirectException(url);
    }
}
