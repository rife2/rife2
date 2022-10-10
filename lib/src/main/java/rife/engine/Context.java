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

import java.util.Collection;
import java.util.Map;

public class Context {
    public static final String ID_WEBAPP_ROOT_URL = "webapp:rootUrl";
    public static final String ID_SERVER_ROOT_URL = "server:rootUrl";
    public static final String ID_CONTEXT_PATH_INFO = "context:pathInfo";

    private final String gateUrl_;
    private final Site site_;
    private final Request request_;
    private final Response response_;
    private final Site.RouteMatch route_;
    private final Element element_;

    public Context(String gateUrl, Site site, Request request, Response response, Site.RouteMatch route) {
        gateUrl_ = gateUrl;
        site_ = site;
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

    public Site site() {
        return site_;
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
            if (url.charAt(url.length() - 1) != '/') {
                url.append("/");
            }
            url.append(StringUtils.stripFromFront(pathInfo, "/"));
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
     * Sets a select box option, a radio button or a checkbox to selected or
     * checked.
     * <p>This method will check the template for certain value tags and set
     * them to the correct attributes according to the name and the provided
     * values in this method. This is dependent on the template type and
     * currently only makes sense for {@code html} templates.
     * <p>For example for select boxes, consider the name '{@code colors}',
     * the values '{@code blue}' and '{@code red}', and the
     * following HTML template excerpt:
     * <pre>&lt;select name="colors"&gt;
     * &lt;option value="blue"{{v colors:blue:SELECTED}}{{/v}}&gt;Blue&lt;/option&gt;
     * &lt;option value="orange"{{v colors:orange:SELECTED}}{{/v}}&gt;Orange&lt;/option&gt;
     * &lt;option value="red"{{v colors:red:SELECTED}}{{/v}}&gt;Red&lt;/option&gt;
     * &lt;option value="green"[!V colors:green:SELECTED'}}{{/v}}&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>the result will then be:
     * <pre>&lt;select name="colors"&gt;
     * &lt;option value="blue" selected="selected"&gt;Blue&lt;/option&gt;
     * &lt;option value="orange"&gt;Orange&lt;/option&gt;
     * &lt;option value="red" selected="selected"&gt;Red&lt;/option&gt;
     * &lt;option value="green"&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>For example for radio buttons, consider the name '{@code sex}',
     * the value '{@code male}' and the following XHTML template excerpt:
     * <pre>&lt;input type="radio" name="sex" value="male"{{v sex:male:CHECKED}}{{/v}} /&gt;
     * &lt;input type="radio" name="sex" value="female"{{v sex:female:CHECKED}}{{/v}} /&gt;</pre>
     * <p>the result will then be:
     * <pre>&lt;input type="radio" name="sex" value="male" checked="checked" /&gt;
     * &lt;input type="radio" name="sex" value="female" /&gt;</pre>
     * <p>For example for checkboxes, consider the name '{@code active}',
     * the value '{@code true}' and the following XHTML template excerpt:
     * <pre>&lt;input type="checkbox" name="active"{{v active:CHECKED}}{{/v}} /&gt;
     * &lt;input type="checkbox" name="senditnow"{{v senditnow:CHECKED}}{{/v}} /&gt;</pre>
     * <p>the result will then be:
     * <pre>&lt;input type="checkbox" name="active" checked="checked" /&gt;
     * &lt;input type="checkbox" name="senditnow" /&gt;</pre>
     *
     * @param template the template instance where the selection should happen
     * @param name     the name of the parameter
     * @param values   the values that should be selected or checked
     * @return a list with the identifiers of the template values that have
     * been set, this is never {@code null}, when no values are set an
     * empty list is returned
     * @since 1.0
     */
    public Collection<String> selectParameter(Template template, String name, String[] values) {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return EngineTemplateHelper.selectParameter(template, name, values);
    }

    /**
     * Generates a form that corresponds to a bean instance.
     *
     * @param template     the template instance where the generation should
     *                     happen
     * @param beanInstance the instance of the bean that should be used to
     *                     generate the form
     * @see rife.forms.FormBuilder
     * @see #generateForm(Template, Object, String)
     * @see #generateEmptyForm(Template, Class, String)
     * @see #removeForm(Template, Class)
     * @since 1.0
     */
    public void generateForm(Template template, Object beanInstance) {
        generateForm(template, beanInstance, null);
    }

    /**
     * Generates a form that corresponds to a bean instance.
     * <p>This method delegates all logic to the {@link
     * rife.forms.FormBuilder#generateForm(Template, Object, Map, String)}
     * method of the provided template instance.
     *
     * @param template     the template instance where the generation should
     *                     happen
     * @param beanInstance the instance of the bean that should be used to
     *                     generate the form
     * @param prefix       the prefix that will be prepended to all bean property
     *                     names
     * @see rife.forms.FormBuilder
     * @see #generateEmptyForm(Template, Class, String)
     * @see #removeForm(Template, Class)
     * @since 1.0
     */
    public void generateForm(Template template, Object beanInstance, String prefix) {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == beanInstance) throw new IllegalArgumentException("beanInstance can't be null.");

        EngineTemplateHelper.generateForm(template, beanInstance, prefix);
    }

    /**
     * Generates a form that corresponds to an empty instance of a bean class.
     *
     * @param template  the template instance where the generation should
     *                  happen
     * @param beanClass the class of the bean that should be used to generate
     *                  the form
     * @see rife.forms.FormBuilder
     * @see #generateForm(Template, Object, String)
     * @see #generateEmptyForm(Template, Class, String)
     * @see #removeForm(Template, Class)
     * @since 1.0
     */
    public void generateEmptyForm(Template template, Class beanClass) {
        generateEmptyForm(template, beanClass, null);
    }

    /**
     * Generates a form that corresponds to an empty instance of a bean class.
     * <p>An '<em>empty</em>' instance is an object that has been created by
     * calling the default constructor of the bean class, without making any
     * additional changes to it afterwards.
     * <p>This method delegates all logic to the {@link
     * rife.forms.FormBuilder#generateForm(Template, Class, Map, String)}
     * method of the provided template instance.
     *
     * @param template  the template instance where the generation should
     *                  happen
     * @param beanClass the class of the bean that should be used to generate
     *                  the form
     * @param prefix    the prefix that will be prepended to all bean property
     *                  names
     * @see rife.forms.FormBuilder
     * @see #generateForm(Template, Object, String)
     * @see #removeForm(Template, Class)
     * @since 1.0
     */
    public void generateEmptyForm(Template template, Class beanClass, String prefix) {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        EngineTemplateHelper.generateEmptyForm(template, beanClass, prefix);
    }

    /**
     * Removes a generated form, leaving the builder value tags empty again as
     * if this form had never been generated.
     *
     * @param template  the template instance where the form should be removed
     *                  from
     * @param beanClass the class of the bean that should be used to remove
     *                  the form
     * @see rife.forms.FormBuilder
     * @see #generateForm(Template, Object, String)
     * @see #generateEmptyForm(Template, Class, String)
     * @see #removeForm(Template, Class)
     * @since 1.0
     */
    public void removeForm(Template template, Class beanClass) {
        removeForm(template, beanClass, null);
    }

    /**
     * Removes a generated form, leaving the builder value tags empty again as
     * if this form had never been generated.
     * <p>This method delegates all logic to the {@link
     * rife.forms.FormBuilder#removeForm(Template, Class, String)}
     * method of the provided template instance.
     *
     * @param template  the template instance where the form should be removed
     *                  from
     * @param beanClass the class of the bean that should be used to remove
     *                  the form
     * @param prefix    the prefix that will be prepended to all bean property
     *                  names
     * @see rife.forms.FormBuilder
     * @see #generateForm(Template, Object, String)
     * @see #generateEmptyForm(Template, Class, String)
     * @since 1.0
     */
    public void removeForm(Template template, Class beanClass, String prefix) {
        if (null == template) throw new IllegalArgumentException("template can't be null.");
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        EngineTemplateHelper.removeForm(template, beanClass, prefix);
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
