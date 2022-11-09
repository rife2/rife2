/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import rife.config.RifeConfig;
import rife.engine.exceptions.*;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.template.exceptions.TemplateException;
import rife.tools.ArrayUtils;
import rife.tools.BeanUtils;
import rife.tools.ServletUtils;
import rife.tools.StringUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.beans.PropertyDescriptor;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class Context {
    public static final boolean DEFAULT_BOOLEAN = false;
    public static final int DEFAULT_INTEGER = 0;
    public static final long DEFAULT_LONG = 0L;
    public static final double DEFAULT_DOUBLE = 0.0d;
    public static final float DEFAULT_FLOAT = 0.0f;

    public static final String ID_WEBAPP_ROOT_URL = "webapp:rootUrl";
    public static final String ID_SERVER_ROOT_URL = "server:rootUrl";
    public static final String ID_CONTEXT_PATH_INFO = "context:pathInfo";
    public static final String ID_CONTEXT_PARAM_RANDOM = "context:paramRandom";

    private final String gateUrl_;
    private final Site site_;
    private final Request request_;
    private final Response response_;
    private final RouteMatch routeMatch_;
    private Throwable engineException_;

    public Context(String gateUrl, Site site, Request request, Response response, RouteMatch routeMatch) {
        gateUrl_ = gateUrl;
        site_ = site;
        request_ = request;
        response_ = response;
        routeMatch_ = routeMatch;
    }

    public void process() {
        if (routeMatch_ == null) {
            return;
        }

        var route = routeMatch_.route();

        try {
            for (var before_route : route.router().before_) {
                var before_element = before_route.getElementInstance(this);
                response_.setLastElement(before_element);
                try {
                    before_element.process(this);
                } catch (NextException ignored) {
                    // this element is done processing
                    // move on to the next one
                }
            }

            var element = route.getElementInstance(this);
            response_.setLastElement(element);
            try {
                element.process(this);
            } catch (NextException ignored) {
                // this element is done processing
                // move on to the next one
            }

            for (var after_route : route.router().after_) {
                var after_element = after_route.getElementInstance(this);
                response_.setLastElement(after_element);
                try {
                    after_element.process(this);
                } catch (NextException ignored) {
                    // this element is done processing
                    // move on to the next one
                }
            }
        } catch (RespondException ignored) {
            // processing is over, just send the current response
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
        if (routeMatch_.pathInfo() != null) {
            return routeMatch_.pathInfo();
        }

        return null;
    }

    public Route route() {
        return routeMatch_.route();
    }

    public void print(Object o) {
        response_.print(o);
    }

    public void print(Template template)
    throws TemplateException, EngineException {
        var set_values = new EngineTemplateProcessor(this, template).processTemplate();

        // set the content type
        if (!response_.isContentTypeSet()) {
            var content_type = template.getDefaultContentType();
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

    public Template template()
    throws TemplateException, EngineException {
        return template(routeMatch_.route().getDefaultElementId(), null);
    }

    public Template template(String name)
    throws TemplateException, EngineException {
        return template(name, null);
    }

    public Template template(String name, String encoding)
    throws TemplateException, EngineException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.HTML.get(name, encoding);
    }

    public Template templateTxt()
    throws TemplateException, EngineException {
        return templateTxt(routeMatch_.route().getDefaultElementId(), null);
    }

    public Template templateTxt(String name)
    throws TemplateException, EngineException {
        return templateTxt(name, null);
    }

    public Template templateTxt(String name, String encoding)
    throws TemplateException, EngineException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.TXT.get(name, encoding);
    }

    public Template templateXml()
    throws TemplateException, EngineException {
        return templateXml(routeMatch_.route().getDefaultElementId(), null);
    }

    public Template templateXml(String name)
    throws TemplateException, EngineException {
        return templateXml(name, null);
    }

    public Template templateXml(String name, String encoding)
    throws TemplateException, EngineException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.XML.get(name, encoding);
    }

    public Template templateJson()
    throws TemplateException, EngineException {
        return templateJson(routeMatch_.route().getDefaultElementId(), null);
    }

    public Template templateJson(String name)
    throws TemplateException, EngineException {
        return templateJson(name, null);
    }

    public Template templateJson(String name, String encoding)
    throws TemplateException, EngineException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.JSON.get(name, encoding);
    }

    public String gateUrl() {
        return gateUrl_;
    }

    public String webappRootUrl(int port) {
        var webapp_root = new StringBuilder();
        webapp_root.append(serverRootUrl(port));
        var gate_url = gateUrl();
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

    public UrlBuilder urlFor(Route route) {
        return new UrlBuilder(webappRootUrl(-1), route);
    }

    void engineException(Throwable exception) {
        engineException_ = exception;
    }

    public Throwable engineException() {
        return engineException_;
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
     * &lt;option value="blue"{{v colors:blue:selected}}{{/v}}&gt;Blue&lt;/option&gt;
     * &lt;option value="orange"{{v colors:orange:selected}}{{/v}}&gt;Orange&lt;/option&gt;
     * &lt;option value="red"{{v colors:red:selected}}{{/v}}&gt;Red&lt;/option&gt;
     * &lt;option value="green"{{v colors:green:selected'}}{{/v}}&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>the result will then be:
     * <pre>&lt;select name="colors"&gt;
     * &lt;option value="blue" selected="selected"&gt;Blue&lt;/option&gt;
     * &lt;option value="orange"&gt;Orange&lt;/option&gt;
     * &lt;option value="red" selected="selected"&gt;Red&lt;/option&gt;
     * &lt;option value="green"&gt;Green&lt;/option&gt;
     * &lt;/select&gt;</pre>
     * <p>For example for radio buttons, consider the name '{@code size}',
     * the value '{@code large}' and the following HTML template excerpt:
     * <pre>&lt;input type="radio" name="size" value="large"{{v size:large:checked}}{{/v}} /&gt;
     * &lt;input type="radio" name="size" value="small"{{v size:small:checked}}{{/v}} /&gt;</pre>
     * <p>the result will then be:
     * <pre>&lt;input type="radio" name="size" value="large" checked="checked" /&gt;
     * &lt;input type="radio" name="size" value="small" /&gt;</pre>
     * <p>For example for checkboxes, consider the name '{@code active}',
     * the value '{@code true}' and the following HTML template excerpt:
     * <pre>&lt;input type="checkbox" name="active"{{v active:checked}}{{/v}} /&gt;
     * &lt;input type="checkbox" name="senditnow"{{v senditnow:checked}}{{/v}} /&gt;</pre>
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
     * @throws rife.engine.exceptions.DeferException an exception that is used to immediately interrupt the execution, don't
     *                                               catch this exception
     * @since 1.0
     */
    public void defer()
    throws DeferException {
        throw new DeferException();
    }

    /**
     * Interrupts the execution in this element and redirects the client to
     * another URL.
     *
     * @param url the URL to which the request will be redirected, <code>String.valueOf()</code>
     *            will be called with this object, so a variety of types can be used
     * @throws rife.engine.exceptions.RedirectException an exception that is used to immediately interrupt the execution, don't
     *                                                  catch this exception
     * @since 1.0
     */
    public void redirect(Object url)
    throws RedirectException {
        throw new RedirectException(url);
    }

    /**
     * Interrupts the execution in this element and redirects the client to
     * another URL.
     *
     * @param route the route to which the request will be redirected
     * @throws rife.engine.exceptions.RedirectException an exception that is used to immediately interrupt the execution, don't
     *                                                  catch this exception
     * @since 2.0
     */
    public void redirect(Route route)
    throws RedirectException {
        throw new RedirectException(urlFor(route));
    }

    /**
     * Interrupts the execution in this element, stops processing any other
     * element, and sends the current response directly to the client.
     *
     * @throws rife.engine.exceptions.RespondException an exception that is used to immediately interrupt the execution, don't
     *                                                 catch this exception
     * @since 2.0
     */
    public void respond() {
        throw new RespondException();
    }

    /**
     * Interrupts the execution in this element, moving processing on to the
     * next element in the before/route/after chain
     *
     * @throws rife.engine.exceptions.NextException an exception that is used to immediately interrupt the execution, don't
     *                                              catch this exception
     * @since 2.0
     */
    public void next() {
        throw new NextException();
    }

    /**
     * Sets up the current request to prevent all caching of the response by
     * the client.
     *
     * @since 1.0
     */
    public void preventCaching() {
        ServletUtils.preventCaching(response_);
    }

    /**
     * See {@link Request#getMethod()}.
     *
     * @since 2.0
     */
    public RequestMethod method() {
        return request_.getMethod();
    }

    /**
     * See {@link Request#getParameters()}.
     *
     * @since 2.0
     */
    public Map<String, String[]> parameters() {
        return request_.getParameters();
    }

    /**
     * Checks whether a value has been provided to a parameter.
     *
     * @param name the name of the parameter
     * @return {@code true} if the parameter has a value; or
     * <p>{@code false} otherwise
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public boolean hasParameterValue(String name) {
        return parameters().containsKey(name);
    }

    /**
     * Checks whether a parameter is empty.
     *
     * @param name the name of the parameter
     * @return {@code true} if the parameter is empty; or
     * <p>{@code false} otherwise
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public boolean isParameterEmpty(String name) {
        var parameter = parameter(name);
        return null == parameter ||
            parameter.trim().equals("");
    }

    /**
     * Retrieves the value of a parameter.
     *
     * @param name the name of the parameter
     * @return the value of the parameter; or
     * <p>{@code null} if no value is present for this parameter
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public String parameter(String name) {
        var parameters = parameters().get(name);
        if (null == parameters) {
            return null;
        }
        return parameters[0];
    }

    /**
     * Retrieves the value of a parameter and returns a default value if no
     * parameter value is present
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public String parameter(String name, String defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Retrieves the names of all the parameters that are present.
     *
     * @return the list with the parameter names
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @since 2.0
     */
    public Set<String> parameterNames() {
        return parameters().keySet();
    }

    /**
     * Retrieves the values of a parameter.
     *
     * @param name the name of the parameter
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterNames()
     * @since 2.0
     */
    public String[] parameterValues(String name) {
        return parameters().get(name);
    }

    /**
     * Retrieves the value of a parameter and converts it to a boolean.
     *
     * @param name the name of the parameter
     * @return the converted parameter value; or
     * <p>{@code false} if no parameter value is present or if the
     * parameter value is not a valid boolean
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public boolean parameterBoolean(String name) {
        return parameterBoolean(name, DEFAULT_BOOLEAN);
    }

    /**
     * Retrieves the value of a parameter and converts it to a boolean, using
     * a default value if no parameter value is present.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the converted parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public boolean parameterBoolean(String name, boolean defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }

        return StringUtils.convertToBoolean(value);
    }

    /**
     * Retrieves the value of a parameter and converts it to an integer.
     *
     * @param name the name of the parameter
     * @return the converted parameter value; or
     * <p>{@code 0} if no parameter value is present or if the parameter
     * value is not a valid integer
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public int parameterInt(String name) {
        return parameterInt(name, DEFAULT_INTEGER);
    }

    /**
     * Retrieves the value of a parameter and converts it to an integer, using
     * a default value if no parameter value is present.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the converted parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public int parameterInt(String name, int defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a parameter and converts it to a long.
     *
     * @param name the name of the parameter
     * @return the converted parameter value; or
     * <p>{@code 0L} if no parameter value is present or if the parameter
     * value is not a valid long
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public long parameterLong(String name) {
        return parameterLong(name, DEFAULT_LONG);
    }

    /**
     * Retrieves the value of a parameter and converts it to a long, using a
     * default value if no parameter value is present.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the converted parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public long parameterLong(String name, long defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a parameter and converts it to a double.
     *
     * @param name the name of the parameter
     * @return the converted parameter value; or
     * <p>{@code 0.0d} if no parameter value is present or if the
     * parameter value is not a valid double
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public double parameterDouble(String name) {
        return parameterDouble(name, DEFAULT_DOUBLE);
    }

    /**
     * Retrieves the value of a parameter and converts it to a double, using a
     * default value if no parameter value is present.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the converted parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public double parameterDouble(String name, double defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a parameter and converts it to a float.
     *
     * @param name the name of the parameter
     * @return the converted parameter value; or
     * <p>{@code 0.0f} if no parameter value is present or if the
     * parameter value is not a valid float
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public float parameterFloat(String name) {
        return parameterFloat(name, DEFAULT_FLOAT);
    }

    /**
     * Retrieves the value of a parameter and converts it to a float, using a
     * default value if no parameter value is present.
     *
     * @param name         the name of the parameter
     * @param defaultValue the default value that will be used when no
     *                     parameter value is present
     * @return the converted parameter value; or
     * <p>the default value if no parameter value is present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public float parameterFloat(String name, float defaultValue) {
        var value = parameter(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the values of a parameter as an array of integers.
     *
     * @param name the name of the parameter
     * @return an integer array with all the parameter values; or
     * <p>{@code null} if no parameter values are present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public int[] parameterInts(String name) {
        return ArrayUtils.createIntArray(parameterValues(name));
    }

    /**
     * Retrieves the values of a parameter as an array of longs.
     *
     * @param name the name of the parameter
     * @return a long array with all the parameter values; or
     * <p>{@code null} if no parameter values are present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public long[] parameterLongs(String name) {
        return ArrayUtils.createLongArray(parameterValues(name));
    }

    /**
     * Retrieves the values of a parameter as an array of floats.
     *
     * @param name the name of the parameter
     * @return a float array with all the parameter values; or
     * <p>{@code null} if no parameter values are present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public float[] parameterFloats(String name) {
        return ArrayUtils.createFloatArray(parameterValues(name));
    }

    /**
     * Retrieves the values of a parameter as an array of doubles.
     *
     * @param name the name of the parameter
     * @return a double array with all the parameter values; or
     * <p>{@code null} if no parameter values are present
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 1.0
     */
    public double[] parameterDoubles(String name) {
        return ArrayUtils.createDoubleArray(parameterValues(name));
    }

    private static <BeanType> BeanType getNewBeanInstance(Class<BeanType> beanClass)
    throws EngineException {
        BeanType bean_instance;

        try {
            bean_instance = beanClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new EngineException("Can't instantiate a bean with class '" + beanClass.getName() + "'.", e);
        } catch (IllegalAccessException e) {
            throw new EngineException("No permission to instantiate a bean with class '" + beanClass.getName() + "'.", e);
        }

        return bean_instance;
    }

    /**
     * Creates an instance of a bean and populates the properties
     * with the parameter values.
     * <p>This bean is not serialized or de-serialized, each property
     * corresponds to a parameter and is individually sent by the client.
     *
     * @param beanClass the class of the submission bean
     * @return the populated bean instance
     * @see #parametersBean(Class, String)
     * @see #parametersBean(Object)
     * @see #parametersBean(Object, String)
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public <BeanType> BeanType parametersBean(Class<BeanType> beanClass)
    throws EngineException {
        return parametersBean(beanClass, null);
    }

    /**
     * Creates an instance of a bean and populates the properties
     * with the parameter values, taking the provided prefix into account.
     * <p>This bean is not serialized or de-serialized, each property
     * corresponds to a parameter and is individually sent by the client.
     *
     * @param beanClass the class of the submission bean
     * @param prefix    the prefix that will be put in front of each property
     *                  name
     * @return the populated bean instance
     * @see #parametersBean(Class)
     * @see #parametersBean(Object)
     * @see #parametersBean(Object, String)
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public <BeanType> BeanType parametersBean(Class<BeanType> beanClass, String prefix)
    throws EngineException {
        assert beanClass != null;

        var bean_instance = getNewBeanInstance(beanClass);

        try {
            var bean_properties = BeanUtils.getUppercasedBeanProperties(beanClass);

            String[] parameter_values;

            for (var parameter_name : parameterNames()) {
                parameter_values = parameterValues(parameter_name);
                if (parameter_values != null &&
                    parameter_values.length > 0) {
                    BeanUtils.setUppercasedBeanProperty(parameter_name, parameter_values, prefix, bean_properties, bean_instance, null);
                }
            }

            for (var uploaded_file_name : fileNames()) {
                var file = file(uploaded_file_name);
                BeanUtils.setUppercasedBeanProperty(uploaded_file_name, file, prefix, bean_properties, bean_instance);
            }
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }

        return bean_instance;
    }

    /**
     * Fills the properties of an existing bean with the parameter values.
     *
     * @param bean the submission bean instance that will be filled
     * @see #parametersBean(Class)
     * @see #parametersBean(Class, String)
     * @see #parametersBean(Object, String)
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public void parametersBean(Object bean)
    throws EngineException {
        parametersBean(bean, null);
    }

    /**
     * Fills the properties of an existing bean with the parameter values that were sent,
     * taking the provided prefix into account.
     *
     * @param bean   the submission bean instance that will be filled
     * @param prefix the prefix that will be put in front of each property
     *               name
     * @see #parametersBean(Class)
     * @see #parametersBean(Class, String)
     * @see #parametersBean(Object)
     * @see #hasParameterValue(String)
     * @see #isParameterEmpty(String)
     * @see #parameter(String)
     * @see #parameter(String, String)
     * @see #parameterValues(String)
     * @see #parameterNames()
     * @since 2.0
     */
    public void parametersBean(Object bean, String prefix)
    throws EngineException {
        if (null == bean) {
            return;
        }

        try {
            var bean_properties = BeanUtils.getUppercasedBeanProperties(bean.getClass());

            String[] parameter_values;

            Object empty_bean = null;

            for (String parameter_name : parameterNames()) {
                parameter_values = parameterValues(parameter_name);
                if (null == empty_bean &&
                    (null == parameter_values ||
                        0 == parameter_values[0].length())) {
                    empty_bean = getNewBeanInstance(bean.getClass());
                }

                BeanUtils.setUppercasedBeanProperty(parameter_name, parameter_values, prefix, bean_properties, bean, empty_bean);
            }

            for (String uploaded_file_name : fileNames()) {
                UploadedFile file = file(uploaded_file_name);
                BeanUtils.setUppercasedBeanProperty(uploaded_file_name, file, prefix, bean_properties, bean);
            }
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    /**
     * See {@link Request#getBody()}.
     *
     * @since 2.0
     */
    public String body() {
        return request_.getBody();
    }

    /**
     * See {@link Request#getBodyAsBytes()}.
     *
     * @since 2.0
     */
    public byte[] bodyAsBytes() {
        return request_.getBodyAsBytes();
    }

    /**
     * Retrieves the list of uploaded file names.
     *
     * @return the list of uploaded file names
     * @see #hasFile(String)
     * @see #isFileEmpty(String)
     * @see #file(String)
     * @see #files(String)
     * @since 2.0
     */
    public Set<String> fileNames() {
        return files().keySet();
    }

    /**
     * Checks if an uploaded file wasn't sent or if it is empty.
     *
     * @param name the name of the file, as declared in the submission
     * @see #fileNames()
     * @see #hasFile(String)
     * @see #file(String)
     * @see #files(String)
     * @since 2.0
     */
    public boolean isFileEmpty(String name) {
        try (final var file = file(name)) {
            return null == file ||
                null == file.getFile() ||
                0 == file.getFile().length();
        }
    }

    /**
     * See {@link Request#getFiles()}.
     *
     * @since 2.0
     */
    public Map<String, UploadedFile[]> files() {
        if (request_.getFiles() == null) {
            return Collections.emptyMap();
        }

        return request_.getFiles();
    }

    /**
     * See {@link Request#hasFile}.
     *
     * @since 2.0
     */
    public boolean hasFile(String name) {
        return request_.hasFile(name);
    }

    /**
     * See {@link Request#getFile}.
     *
     * @since 2.0
     */
    public UploadedFile file(String name) {
        return request_.getFile(name);
    }

    /**
     * See {@link Request#getFiles}.
     *
     * @since 2.0
     */
    public UploadedFile[] files(String name) {
        return request_.getFiles(name);
    }

    /**
     * See {@link Request#getServerRootUrl}.
     *
     * @since 2.0
     */
    public String serverRootUrl(int port) {
        return request_.getServerRootUrl(port);
    }

    /**
     * See {@link Request#hasCookie}.
     *
     * @since 2.0
     */
    public boolean hasCookie(String name) {
        return request_.hasCookie(name);
    }

    /**
     * See {@link Request#getCookie}.
     *
     * @since 2.0
     */
    public Cookie cookie(String name) {
        return request_.getCookie(name);
    }

    /**
     * See {@link Request#getCookies()}.
     *
     * @since 2.0
     */
    public Cookie[] cookies() {
        return request_.getCookies();
    }

    /**
     * Retrieves the value of a cookie.
     *
     * @param name the name of the cookie
     * @return the value of the cookie; or
     * <p>{@code null} if no such cookie is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public String cookieValue(String name) {
        var cookie = cookie(name);
        String value = null;
        if (cookie != null) {
            value = cookie.getValue();
        }

        return value;
    }

    /**
     * Retrieves all current cookies names with their values.
     *
     * @return a new map of all the current cookies names with their values
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public Map<String, String> cookieValues() {
        Map<String, String> result = new HashMap<>();

        for (var entry : request_.getCookies()) {
            result.put(entry.getName(), entry.getValue());
        }

        return result;
    }

    /**
     * Retrieves the value of a named cookie, using a default value as
     * fallback.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public String cookieValue(String name, String defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Retrieves the value of a named cookie and converts it to a boolean.
     *
     * @param name the name of the cookie
     * @return the converted cookie value; or
     * <p>{@code false} if no cookie value is present or if the cookie
     * value is not a valid boolean
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public boolean cookieBoolean(String name) {
        return cookieBoolean(name, DEFAULT_BOOLEAN);
    }

    /**
     * Retrieves the value of a named cookie and converts it to a boolean,
     * using a default value if no input value is present.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the converted cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public boolean cookieBoolean(String name, boolean defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }

        return StringUtils.convertToBoolean(value);
    }

    /**
     * Retrieves the value of a named cookie and converts it to an integer.
     *
     * @param name the name of the cookie
     * @return the converted cookie value; or
     * <p>{@code 0} if no cookie value is present or if the cookie value
     * is not a valid integer
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public int cookieInt(String name) {
        return cookieInt(name, DEFAULT_INTEGER);
    }

    /**
     * Retrieves the value of a named cookie and converts it to an integer,
     * using a default value if no input value is present.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the converted cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public int cookieInt(String name, int defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a named cookie and converts it to a long.
     *
     * @param name the name of the cookie
     * @return the converted cookie value; or
     * <p>{@code 0L} if no cookie value is present or if the cookie value
     * is not a valid long
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public long cookieLong(String name) {
        return cookieLong(name, DEFAULT_LONG);
    }

    /**
     * Retrieves the value of a named cookie and converts it to a long, using
     * a default value if no input value is present.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the converted cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public long cookieLong(String name, long defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a named cookie and converts it to a double.
     *
     * @param name the name of the cookie
     * @return the converted cookie value; or
     * <p>{@code 0.0d} if no cookie value is present or if the cookie
     * value is not a valid double
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public double cookieDouble(String name) {
        return cookieDouble(name, DEFAULT_DOUBLE);
    }

    /**
     * Retrieves the value of a named cookie and converts it to a double,
     * using a default value if no input value is present.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the converted cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public double cookieDouble(String name, double defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Retrieves the value of a named cookie and converts it to a float.
     *
     * @param name the name of the cookie
     * @return the converted cookie value; or
     * <p>{@code 0.0}f if no cookie value is present or if the cookie
     * value is not a valid float
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public float cookieFloat(String name) {
        return cookieFloat(name, DEFAULT_FLOAT);
    }

    /**
     * Retrieves the value of a named cookie and converts it to a float, using
     * a default value if no input value is present.
     *
     * @param name         the name of the cookie
     * @param defaultValue the default value that will be used when no cookie
     *                     value is present
     * @return the converted cookie value; or
     * <p>the default value if no cookie value is present
     * @see #hasCookie(String)
     * @see #cookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @see #cookie(Cookie)
     * @since 2.0
     */
    public float cookieFloat(String name, float defaultValue) {
        var value = cookieValue(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * See {@link Request#getAttribute}.
     *
     * @since 2.0
     */
    public Object attribute(String name) {
        return request_.getAttribute(name);
    }

    /**
     * See {@link Request#hasAttribute}.
     *
     * @since 2.0
     */
    public boolean hasAttribute(String name) {
        return request_.hasAttribute(name);
    }

    /**
     * See {@link Request#getAttributeNames}.
     *
     * @since 2.0
     */
    public List<String> attributeNames() {
        return Collections.list(request_.getAttributeNames());
    }

    /**
     * See {@link Request#removeAttribute}.
     *
     * @since 2.0
     */
    public void removeAttribute(String name) {
        request_.removeAttribute(name);
    }

    /**
     * See {@link Request#setAttribute}.
     *
     * @since 2.0
     */
    public void attribute(String name, Object object) {
        request_.setAttribute(name, object);
    }

    /**
     * See {@link Request#getCharacterEncoding}.
     *
     * @since 2.0
     */
    public String characterEncoding() {
        return request_.getCharacterEncoding();
    }

    /**
     * See {@link Request#getContentType}.
     *
     * @since 2.0
     */
    public String contentType() {
        return request_.getContentType();
    }

    /**
     * See {@link Request#getDateHeader}.
     *
     * @since 2.0
     */
    public long headerDate(String name) {
        return request_.getDateHeader(name);
    }

    /**
     * See {@link Request#getHeader}.
     *
     * @since 2.0
     */
    public String header(String name) {
        return request_.getHeader(name);
    }

    /**
     * See {@link Request#getHeaderNames}.
     *
     * @since 2.0
     */
    public List<String> headerNames() {
        return Collections.list(request_.getHeaderNames());
    }

    /**
     * See {@link Request#getHeaders}.
     *
     * @since 2.0
     */
    public List<String> headers(String name) {
        return Collections.list(request_.getHeaders(name));
    }

    /**
     * See {@link Request#getIntHeader}.
     *
     * @since 2.0
     */
    public int headerInt(String name) {
        return request_.getIntHeader(name);
    }

    /**
     * See {@link Request#getLocale}.
     *
     * @since 2.0
     */
    public Locale locale() {
        return request_.getLocale();
    }

    /**
     * See {@link Request#getLocales}.
     *
     * @since 2.0
     */
    public List<Locale> locales() {
        return Collections.list(request_.getLocales());
    }

    /**
     * See {@link Request#getProtocol}.
     *
     * @since 2.0
     */
    public String protocol() {
        return request_.getProtocol();
    }

    /**
     * See {@link Request#getRemoteAddr}.
     *
     * @since 2.0
     */
    public String remoteAddr() {
        return request_.getRemoteAddr();
    }

    /**
     * See {@link Request#getRemoteUser}.
     *
     * @since 2.0
     */
    public String remoteUser() {
        return request_.getRemoteUser();
    }

    /**
     * See {@link Request#getRemoteHost}.
     *
     * @since 2.0
     */
    public String remoteHost() {
        return request_.getRemoteHost();
    }

    /**
     * See {@link Request#getRequestDispatcher}.
     *
     * @since 2.0
     */
    public RequestDispatcher requestDispatcher(String url) {
        return request_.getRequestDispatcher(url);
    }

    /**
     * See {@link Request#getSession}.
     *
     * @since 2.0
     */
    public HttpSession session() {
        return request_.getSession();
    }

    /**
     * See {@link Request#getSession(boolean)}.
     *
     * @since 2.0
     */
    public HttpSession session(boolean create) {
        return request_.getSession(create);
    }

    /**
     * See {@link Request#getServerPort}.
     *
     * @since 2.0
     */
    public int serverPort() {
        return request_.getServerPort();
    }

    /**
     * See {@link Request#getScheme}.
     *
     * @since 2.0
     */
    public String scheme() {
        return request_.getScheme();
    }

    /**
     * See {@link Request#getServerName}.
     *
     * @since 2.0
     */
    public String serverName() {
        return request_.getServerName();
    }

    /**
     * See {@link Request#getContextPath}.
     *
     * @since 2.0
     */
    public String contextPath() {
        return request_.getContextPath();
    }

    /**
     * See {@link Request#isSecure}.
     *
     * @since 2.0
     */
    public boolean secure() {
        return request_.isSecure();
    }

    /**
     * Enables or disables the response text buffer. By default, it is
     * enabled.
     * <p>Disabling an enabled text buffer, flushes the already buffered
     * content first.
     * <p>If the text buffer is disabled, text content will be sent
     * immediately to the client, this can decrease performance. Unless you
     * need to stream content in real time, it's best to leave the text buffer
     * enabled. It will be flushed and sent in one go at the end of the
     * request.
     *
     * @param enabled {@code true} to enable the text buffer; or
     *                <p>{@code false} to disable it
     * @see #textBufferEnabled()
     * @see #flush()
     * @see #clearBuffer()
     * @since 2.0
     */
    public void enableTextBuffer(boolean enabled) {
        response_.enableTextBuffer(enabled);
    }

    /**
     * Indicates whether the response text buffer is enabled or disabled.
     *
     * @return {@code true} if the text buffer is enabled; or
     * <p>{@code false} if it is disabled
     * @see #enableTextBuffer(boolean)
     * @see #flush()
     * @see #clearBuffer()
     * @since 1.0
     */
    public boolean textBufferEnabled() {
        return response_.isTextBufferEnabled();
    }

    /**
     * See {@link Response#clearBuffer()}.
     *
     * @since 2.0
     */
    public void clearBuffer() {
        response_.clearBuffer();
    }

    /**
     * See {@link Response#flush()}.
     *
     * @since 2.0
     */
    public void flush() {
        response_.flush();
    }

    /**
     * See {@link Response#getOutputStream()}.
     *
     * @since 2.0
     */
    public OutputStream outputStream() {
        return response_.getOutputStream();
    }

    /**
     * See {@link Response#setContentType(String)}.
     *
     * @since 2.0
     */
    public void contentType(String contentType) {
        response_.setContentType(contentType);
    }

    /**
     * See {@link Response#setLocale(Locale)}.
     *
     * @since 2.0
     */
    public void locale(Locale locale) {
        response_.setLocale(locale);
    }

    /**
     * See {@link Response#setContentLength(int)}.
     *
     * @since 2.0
     */
    public void contentLength(int length) {
        response_.setContentLength(length);
    }

    /**
     * See {@link Response#addCookie(Cookie)}.
     *
     * @since 2.0
     */
    public void cookie(Cookie cookie) {
        response_.addCookie(cookie);
    }

    /**
     * Adds the <code>Cookie</code> created by a <code>CookieBuilder</code> to the response.
     * This method can be called multiple times to set more than one cookie.
     *
     * @param builder the <code>CookieBuilder</code> to use for building the <code>Cookie</code>
     * @since 2.0
     */
    public void cookie(CookieBuilder builder) {
        response_.addCookie(builder.cookie());
    }

    /**
     * Removes a cookie.
     *
     * @param name name of the cookie
     * @since 2.0
     */
    public void removeCookie(String name) {
        removeCookie(null, name);
    }

    /**
     * Removes a cookie with given path and name.
     *
     * @param path path of the cookie
     * @param name name of the cookie
     * @since 2.0
     */
    public void removeCookie(String path, String name) {
        var cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie(cookie);
    }

    /**
     * See {@link Response#addHeader(String, String)}.
     *
     * @since 2.0
     */
    public void header(String name, String value) {
        response_.addHeader(name, value);
    }

    /**
     * See {@link Response#addDateHeader(String, long)}.
     *
     * @since 2.0
     */
    public void header(String name, long date) {
        response_.addDateHeader(name, date);
    }

    /**
     * See {@link Response#addIntHeader(String, int)}.
     *
     * @since 2.0
     */
    public void header(String name, int integer) {
        response_.addIntHeader(name, integer);
    }

    /**
     * See {@link Response#setStatus(int)}.
     *
     * @since 2.0
     */
    public void status(int statusCode) {
        response_.setStatus(statusCode);
    }

}
