/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.continuations.ContinuationConfigRuntime;
import rife.continuations.ContinuationContext;
import rife.continuations.exceptions.*;
import rife.engine.exceptions.*;
import rife.ioc.HierarchicalProperties;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.template.exceptions.TemplateException;
import rife.tools.*;
import rife.tools.exceptions.BeanUtilsException;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class provides the context for the current HTTP request.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Context {
    public static final boolean DEFAULT_BOOLEAN = false;
    public static final int DEFAULT_INTEGER = 0;
    public static final long DEFAULT_LONG = 0L;
    public static final double DEFAULT_DOUBLE = 0.0d;
    public static final float DEFAULT_FLOAT = 0.0f;

    /**
     * Status code (304) indicating that a conditional GET operation found that the resource was available and not modified.
     */
    public static final int SC_NOT_MODIFIED = 304;


    /**
     * Status code (500) indicating an error inside the HTTP server which prevented it from fulfilling the request.
     */
    public static final int SC_INTERNAL_SERVER_ERROR = 500;

    private final String gateUrl_;
    private final Site site_;
    private final Request request_;
    private final Response response_;
    private final RouteMatch routeMatch_;
    private final Map<String, String[]> parametersIn_;
    private Map<String, String[]> parametersOut_;
    private Throwable engineException_;

    private Route processedRoute_ = null;
    private Element processedElement_ = null;

    Context(String gateUrl, Site site, Request request, Response response, RouteMatch routeMatch) {
        gateUrl_ = gateUrl;
        site_ = site;
        request_ = request;
        response_ = response;
        routeMatch_ = routeMatch;

        var params = new LinkedHashMap<>(request_.getParameters());
        if (routeMatch_ != null) {
            if (routeMatch_.route().pathInfoHandling().type() == PathInfoType.MAP) {
                for (var mapping : routeMatch_.route().pathInfoHandling().mappings()) {
                    var matcher = mapping.regexp().matcher(pathInfo());
                    if (matcher.matches()) {
                        var i = 1;
                        for (var param : mapping.parameters()) {
                            params.put(param, new String[]{matcher.group(i++)});
                        }
                        break;
                    }
                }
            }
        }
        parametersIn_ = params;
        parametersOut_ = null;
    }

    void process() {
        if (routeMatch_ == null) {
            return;
        }

        var route = routeMatch_.route();

        try {
            for (var before_route : route.router().before_) {
                processElement(before_route);
            }

            processElement(route);

            for (var after_route : route.router().after_) {
                processElement(after_route);
            }
        } catch (RespondException ignored) {
            // processing is over, just send the current response
        } catch (PauseException e) {
            handlePause(e);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    void processElement(Route route)
    throws Exception {
        parametersOut_ = null;

        // try to set up a continuation context and try to
        // retrieve the element instance that belongs to it
        var element = setupContinuationContext(route);

        // if not element can be obtained from a continuation,
        // get a new instance from the route
        if (element == null) {
            element = route.obtainElementInstance(this);
        }

        route.prepareElementInstance(element, this);

        processedRoute_ = route;
        processedElement_ = element;
        response_.setLastElement(element);

        // process the element with this context
        try {
            element.process(this);
        } catch (NextException ignored) {
            // this element is done processing
            // move on to the next one
        } finally {
            route.finalizeElementInstance(element, this);
            ContinuationContext.clearActiveContext();
        }
    }

    private Element setupContinuationContext(Route route)
    throws Exception {
        // continuations are only supported on element class routes, not element instance routes
        if (route instanceof RouteInstance) {
            return null;
        }

        Element element = null;
        ContinuationContext continuation_context = null;

        // resume a continuation context if it can be found
        var resume_id = parameter(SpecialParameters.CONT_ID);
        continuation_context = site_.continuationManager_.resumeContext(resume_id);

        // if a continuation context can be resumed, activate it
        // when its continuable is the same type as the element that should be processed,
        // process that continuable instead
        if (continuation_context != null) {
            ContinuationContext.setActiveContext(continuation_context);

            if (continuation_context.getContinuable() != null &&
                route.getElementClass() == continuation_context.getContinuable().getClass()) {
                removeGeneratedTemplateValues(continuation_context);

                element = (Element) continuation_context.getContinuable();
            }
        }

        ContinuationConfigRuntime.setActiveConfigRuntime(site_.continuationManager_.getConfigRuntime());

        return element;
    }

    private void removeGeneratedTemplateValues(ContinuationContext continuationContext)
    throws Exception {
        var local_stack = continuationContext.getLocalStack();
        for (int i = 0; i < local_stack.getReferenceStackSize(); ++i) {
            var reference = local_stack.getReference(i);
            if (reference instanceof Template t) {
                t.removeGeneratedValues();
            }
        }
        var local_vars = continuationContext.getLocalVars();
        for (int i = 0; i < local_vars.getReferenceStackSize(); ++i) {
            var reference = local_vars.getReference(i);
            if (reference instanceof Template t) {
                t.removeGeneratedValues();
            }
        }

        var continuable = continuationContext.getContinuable();
        Class klass = continuable.getClass();
        while (klass != null && klass != Element.class) {
            for (var field : klass.getDeclaredFields()) {
                field.setAccessible(true);

                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                if (Template.class.isAssignableFrom(field.getType())) {
                    var t = (Template) field.get(continuable);
                    t.removeGeneratedValues();
                }
            }

            klass = klass.getSuperclass();
        }
    }

    private void handlePause(PauseException e) {
        // register context
        var continuation_context = e.getContext();
        site_.continuationManager_.addContext(continuation_context);
    }

    Route processedRoute() {
        return processedRoute_;
    }

    Element processedElement() {
        return processedElement_;
    }

    void engineException(Throwable exception) {
        engineException_ = exception;
    }

    /**
     * The active site this context is executing in.
     *
     * @return the currently active site
     * @since 1.0
     */
    public Site site() {
        return site_;
    }

    /**
     * The request of this context.
     * <p>
     * Most request methods have direct counterparts in the context for
     * easier and more convenient usage.
     *
     * @return this context's request
     * @since 1.0
     */
    public Request request() {
        return request_;
    }

    /**
     * The response of this context.
     * <p>
     * Most response methods have direct counterparts in the context for
     * easier and more convenient usage.
     *
     * @return this context's response
     * @since 1.0
     */
    public Response response() {
        return response_;
    }

    /**
     * The pathinfo that was captured or matched.
     * <p>
     * The slash separated between the route path in the pathinfo will
     * have been stripped already.
     *
     * @return the captured or matching pathinfo; or
     * {@code null} if no pathinfo was captured or matched
     * @since 1.0
     */
    public String pathInfo() {
        if (routeMatch_ != null && routeMatch_.pathInfo() != null) {
            return routeMatch_.pathInfo();
        }

        return null;
    }

    /**
     * The route of this context.
     *
     * @return this context's route; or
     * {@code null} if the context doesn't belong to a specific route
     * @since 1.0
     */
    public Route route() {
        if (routeMatch_ == null) {
            return null;
        }
        return routeMatch_.route();
    }

    /**
     * The router in which this context's route was defined.
     *
     * @return this context's route's router; or
     * {@code null} if the context doesn't belong to a specific route
     * @since 1.0
     */
    public Router router() {
        var route = route();
        if (route == null) {
            return null;
        }
        return route.router();
    }

    /**
     * The hierarchical properties accessible to this context.
     *
     * @return this context's hierarchical properties; or
     * {@code null} if those properties can't be found
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        var router = router();
        if (router == null) {
            return null;
        }
        return router.properties();
    }

    /**
     * Retrieve a property from this context's {@code HierarchicalProperties}.
     *
     * @return the requested property; or {@code null} if it doesn't exist
     * @since 1.0
     */
    public Object property(String name) {
        var properties = properties();
        if (null == properties) {
            return null;
        }
        return properties.getValue(name);
    }

    /**
     * Checks for the existence of a property in this context's {@code HierarchicalProperties}.
     *
     * @return {@code true} if the property exists; or {@code false} otherwise
     * @since 1.0
     */
    public boolean hasProperty(String name) {
        var properties = properties();
        if (null == properties) {
            return false;
        }
        return properties.contains(name);
    }

    /**
     * Returns a collection of the names in this context's {@code HierarchicalProperties}.
     *
     * @return the requested collection of names
     * @since 1.0
     */
    public Collection<String> propertyNames() {
        var properties = properties();
        if (null == properties) {
            return Collections.emptyList();
        }
        return properties.getNames();
    }

    /**
     * Pauses the execution of the element and creates a new continuation.
     * <p>The next request will resume exactly at the same location with a
     * completely restored call stack and variable stack.
     *
     * @since 1.0
     */
    public final void pause() {
        // this is deliberately empty since the continuation support
        // rewrites method calls to pause
        throw new ContinuationsNotActiveException();
    }

    /**
     * Indicates whether a continuation identifier is available
     *
     * @return {@code true} when a continuation identifier is available; or
     * <p>{@code false} otherwise
     * @since 1.0
     */
    public boolean hasContinuationId() {
        return ContinuationContext.getActiveContextId() != null;
    }

    /**
     * Returns the unique identifier of the current continuation.
     *
     * @return the unique identifier of the current continuation; or
     * <p>{@code null} if no continuation is active
     * @since 1.0
     */
    public String continuationId() {
        return ContinuationContext.getActiveContextId();
    }

    /**
     * Prints the string representation of an object to the request text
     * output. The string representation will be created through a
     * {@code String.valueOf(value)} call.
     *
     * @param object the object that will be output
     * @throws EngineException if an error occurs during the output of the content
     * @see #print(Template)
     * @since 1.0
     */
    public void print(Object object)
    throws EngineException {
        response_.print(object);
    }

    /**
     * Prints a template to the response.
     * <p>If no response content type was set yet, the template's content
     * type will be used.
     * <p>Printing a template will automatically process all the filtered
     * tags.
     * <p>Printing a template instead of the string representation of its
     * content has many advantages, the biggest one being that the out-of-container
     * testing API will have access to this template instance, allowing you
     * to assert of value content, instead of having to parse a response.
     *
     * @param template the template to print
     * @throws TemplateException if an error occurs while processing the template
     * @throws EngineException   if an error occurs during the output of the content
     */
    public void print(Template template)
    throws TemplateException {
        new EngineTemplateProcessor(this, template).processTemplate();

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
    }

    /**
     * Instantiates an HTML template with the name corresponding to the
     * identifier of this route's element.
     * <p>For lambda elements, this will be the path of the route and
     * for class elements this will be the shortened uncapitalized name
     * of the class.
     *
     * @return the HTML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #template(String)
     * @see #template(String, String)
     * @since 1.0
     */
    public Template template()
    throws TemplateException {
        if (routeMatch_ == null) {
            return null;
        }
        return template(routeMatch_.route().defaultElementId(), null);
    }

    /**
     * Instantiates an HTML template with a given name.
     *
     * @param name the template name
     * @return the HTML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #template
     * @see #template(String, String)
     * @since 1.0
     */
    public Template template(String name)
    throws TemplateException {
        return template(name, null);
    }

    /**
     * Instantiates an HTML template with a given name and encoding.
     *
     * @param name     the template name
     * @param encoding the template's encoding
     * @return the HTML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #template(String)
     * @see #template(String, String)
     * @since 1.0
     */
    public Template template(String name, String encoding)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.HTML.get(name, encoding);
    }

    /**
     * Instantiates a TXT template with the name corresponding to the
     * identifier of this route's element.
     * <p>For lambda elements, this will be the path of the route and
     * for class elements this will be the shortened uncapitalized name
     * of the class.
     *
     * @return the TXT template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateTxt(String)
     * @see #templateTxt(String, String)
     * @since 1.0
     */
    public Template templateTxt()
    throws TemplateException {
        return templateTxt(routeMatch_.route().defaultElementId(), null);
    }

    /**
     * Instantiates a TXT template with a given name.
     *
     * @param name the template name
     * @return the TXT template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateTxt
     * @see #templateTxt(String, String)
     * @since 1.0
     */
    public Template templateTxt(String name)
    throws TemplateException {
        return templateTxt(name, null);
    }

    /**
     * Instantiates a TXT template with a given name and encoding.
     *
     * @param name     the template name
     * @param encoding the template's encoding
     * @return the TXT template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateTxt(String)
     * @see #templateTxt(String, String)
     * @since 1.0
     */
    public Template templateTxt(String name, String encoding)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.TXT.get(name, encoding);
    }

    /**
     * Instantiates an XML template with the name corresponding to the
     * identifier of this route's element.
     * <p>For lambda elements, this will be the path of the route and
     * for class elements this will be the shortened uncapitalized name
     * of the class.
     *
     * @return the XML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateXml(String)
     * @see #templateXml(String, String)
     * @since 1.0
     */
    public Template templateXml()
    throws TemplateException {
        return templateXml(routeMatch_.route().defaultElementId(), null);
    }

    /**
     * Instantiates an XML template with a given name.
     *
     * @param name the template name
     * @return the XML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateXml
     * @see #templateXml(String, String)
     * @since 1.0
     */
    public Template templateXml(String name)
    throws TemplateException {
        return templateXml(name, null);
    }

    /**
     * Instantiates an XML template with a given name and encoding.
     *
     * @param name     the template name
     * @param encoding the template's encoding
     * @return the XML template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateXml(String)
     * @see #templateXml(String, String)
     * @since 1.0
     */
    public Template templateXml(String name, String encoding)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.XML.get(name, encoding);
    }

    /**
     * Instantiates an JSON template with the name corresponding to the
     * identifier of this route's element.
     * <p>For lambda elements, this will be the path of the route and
     * for class elements this will be the shortened uncapitalized name
     * of the class.
     *
     * @return the JSON template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateJson(String)
     * @see #templateJson(String, String)
     * @since 1.0
     */
    public Template templateJson()
    throws TemplateException {
        return templateJson(routeMatch_.route().defaultElementId(), null);
    }

    /**
     * Instantiates an JSON template with a given name.
     *
     * @param name the template name
     * @return the JSON template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateJson
     * @see #templateJson(String, String)
     * @since 1.0
     */
    public Template templateJson(String name)
    throws TemplateException {
        return templateJson(name, null);
    }

    /**
     * Instantiates an JSON template with a given name and encoding.
     *
     * @param name     the template name
     * @param encoding the template's encoding
     * @return the JSON template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateJson(String)
     * @see #templateJson(String, String)
     * @since 1.0
     */
    public Template templateJson(String name, String encoding)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.JSON.get(name, encoding);
    }

    /**
     * Instantiates an SVG template with the name corresponding to the
     * identifier of this route's element.
     * <p>For lambda elements, this will be the path of the route and
     * for class elements this will be the shortened uncapitalized name
     * of the class.
     *
     * @return the SVG template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateSvg(String)
     * @see #templateSvg(String, String)
     * @since 1.0
     */
    public Template templateSvg()
    throws TemplateException {
        return templateSvg(routeMatch_.route().defaultElementId(), null);
    }

    /**
     * Instantiates an SVG template with a given name.
     *
     * @param name the template name
     * @return the SVG template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateSvg
     * @see #templateSvg(String, String)
     * @since 1.0
     */
    public Template templateSvg(String name)
    throws TemplateException {
        return templateSvg(name, null);
    }

    /**
     * Instantiates an SVG template with a given name and encoding.
     *
     * @param name     the template name
     * @param encoding the template's encoding
     * @return the SVG template if it was found.
     * @throws TemplateException when an error occurred instantiating the template
     * @see #templateSvg(String)
     * @see #templateSvg(String, String)
     * @since 1.0
     */
    public Template templateSvg(String name, String encoding)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        return TemplateFactory.SVG.get(name, encoding);
    }

    /**
     * Retrieves the URL portion that corresponds to the entrance gate
     * of the RIFE2 web engine, this is usually equivalent with the HTTP request
     * context path.
     *
     * @return the URL of the web engine gate
     * @since 1.0
     */
    public String gateUrl() {
        return gateUrl_;
    }

    /**
     * Returns the root URL of the server that is running this web
     * applications.
     * <p>This includes the protocol, the server name and the server port, for
     * example: {@code http://www.somehost.com:8080}.
     *
     * @param port the server port to use, or
     *             {@code -1} to use the sane port as the active request
     * @return the server's root url
     * @since 1.0
     */
    public String serverRootUrl(int port) {
        return request_.getServerRootUrl(port);
    }

    /**
     * Returns the root URL of this web application.
     * <p>This includes the protocol, the server name and the server port, and
     * the gate URL.
     *
     * @return this web application's root URL
     * @since 1.0
     */
    public String webappRootUrl() {
        return webappRootUrl(-1);
    }

    /**
     * Returns the root URL of this web application.
     * <p>This includes the protocol, the server name and the server port, and
     * the gate URL.
     *
     * @param port the server port to use, or
     *             {@code -1} to use the sane port as the active request
     * @return this web application's root URL
     * @since 1.0
     */
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

    /**
     * Start building a URL towards a particular route.
     * <p>
     * The result is a {@see UrlBuilder} instance that can be used for
     * further customization of the URL, if needed.
     *
     * @param route the target route
     * @return an instance of {@see UrlBuilder}
     * @since 1.0
     */
    public UrlBuilder urlFor(Route route) {
        return new UrlBuilder(this, route);
    }

    /**
     * Retrieves the exception that was triggered during the RIFE2 web
     * engine execution.
     *
     * @return the triggered exception; or
     * <p>{@code null} if no exception was triggered
     * @since 1.0
     */
    public Throwable engineException() {
        return engineException_;
    }

    /**
     * Sets a select box option, a radio button or a checkbox to selected or
     * checked.
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

        var form_builder = template.getFormBuilder();
        if (null == form_builder) {
            return Collections.emptyList();
        }

        return form_builder.selectParameter(template, name, values);
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

        var form_builder = template.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanInstance.getClass(), prefix);
            form_builder.generateForm(template, beanInstance, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
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

        var form_builder = template.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
            form_builder.generateForm(template, beanClass, null, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
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

        var form_builder = template.getFormBuilder();
        if (null == form_builder) {
            return;
        }
        try {
            form_builder.removeForm(template, beanClass, prefix);
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    /**
     * Interrupts the execution in RIFE2 completely and defers it to the
     * servlet container.
     * <p>If RIFE2 is being run as a filter, it will execute the next filter in
     * the chain.
     * <p>If RIFE2 is being run as a servlet, the status code {@code 404: Not
     * Found} will be sent to the client.
     *
     * @throws DeferException an exception that is used to immediately interrupt the execution, don't
     *                        catch this exception
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
     * @param url the URL to which the request will be redirected, {@code String.valueOf()}
     *            will be called with this object, so a variety of types can be used
     * @throws RedirectException an exception that is used to immediately interrupt the execution, don't
     *                           catch this exception
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
     * @throws RedirectException an exception that is used to immediately interrupt the execution, don't
     *                           catch this exception
     * @since 1.0
     */
    public void redirect(Route route)
    throws RedirectException {
        throw new RedirectException(urlFor(route));
    }

    /**
     * Interrupts the execution in this element, stops processing any other
     * element, and sends the current response directly to the client.
     *
     * @throws RespondException an exception that is used to immediately interrupt the execution, don't
     *                          catch this exception
     * @since 1.0
     */
    public void respond() {
        throw new RespondException();
    }

    /**
     * Interrupts the execution in this element, moving processing on to the
     * next element in the before/route/after chain
     *
     * @throws NextException an exception that is used to immediately interrupt the execution, don't
     *                       catch this exception
     * @since 1.0
     */
    public void next() {
        throw new NextException();
    }

    /**
     * Sets up the current response to prevent all caching of the response by
     * the client.
     *
     * @since 1.0
     */
    public void preventCaching() {
        ServletUtils.preventCaching(response_);
    }

    /**
     * Returns the HTTP {@code RequestMethod} with which this context's request was made.
     *
     * @return the {@code RequestMethod} of this context's request
     * @since 1.0
     */
    public RequestMethod method() {
        return request_.getMethod();
    }

    /**
     * Retrieves the parameters that were sent to this context.
     *
     * @return a {@code Map} with all the parameter names and values
     * @since 1.0
     */
    public Map<String, String[]> parameters() {
        return parametersIn_;
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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
     * @since 1.0
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

            for (var parameter_name : parameterNames()) {
                parameter_values = parameterValues(parameter_name);
                if (null == empty_bean &&
                    (null == parameter_values ||
                     0 == parameter_values[0].length())) {
                    empty_bean = getNewBeanInstance(bean.getClass());
                }

                BeanUtils.setUppercasedBeanProperty(parameter_name, parameter_values, prefix, bean_properties, bean, empty_bean);
            }

            for (var uploaded_file_name : fileNames()) {
                var file = file(uploaded_file_name);
                BeanUtils.setUppercasedBeanProperty(uploaded_file_name, file, prefix, bean_properties, bean);
            }
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    /**
     * Retrieves the body of this context's request as a string.
     *
     * @return the string of the request body
     * @see #bodyAsBytes()
     * @since 1.0
     */
    public String body() {
        return request_.getBody();
    }

    /**
     * Retrieves the body of this context's request as a byte array.
     *
     * @return the byte array of the request body
     * @see #body()
     * @since 1.0
     */
    public byte[] bodyAsBytes() {
        return request_.getBodyAsBytes();
    }

    /**
     * Retrieves the list of uploaded file names.
     *
     * @return the set of uploaded file names
     * @see #hasFile(String)
     * @see #isFileEmpty(String)
     * @see #file(String)
     * @see #files(String)
     * @since 1.0
     */
    public Set<String> fileNames() {
        return files().keySet();
    }

    /**
     * Checks if an uploaded file wasn't sent or if it is empty.
     *
     * @param name the name of the file
     * @see #fileNames()
     * @see #hasFile(String)
     * @see #file(String)
     * @see #files(String)
     * @since 1.0
     */
    public boolean isFileEmpty(String name) {
        try (final var file = file(name)) {
            return null == file ||
                   null == file.getFile() ||
                   0 == file.getFile().length();
        }
    }

    /**
     * Retrieves the files that were uploaded in this context.
     *
     * @return a {@code Map} with all the uploaded files
     * @see #hasFile(String)
     * @see #file(String)
     * @see #files(String)
     * @since 1.0
     */
    public Map<String, UploadedFile[]> files() {
        if (request_.getFiles() == null) {
            return Collections.emptyMap();
        }

        return request_.getFiles();
    }

    /**
     * Checks if a particular file has been uploaded in this context.
     *
     * @param name the name of the file
     * @return {@code true} if the file was uploaded; or
     * <p>{@code false} otherwise
     * @see #files()
     * @see #file(String)
     * @see #files(String)
     * @since 1.0
     */
    public boolean hasFile(String name) {
        return request_.hasFile(name);
    }

    /**
     * Retrieves an uploaded file.
     *
     * @param name the name of the file
     * @return the uploaded file; or
     * <p>{@code null} if no file was uploaded
     * @see #files()
     * @see #hasFile(String)
     * @see #files(String)
     * @since 1.0
     */
    public UploadedFile file(String name) {
        return request_.getFile(name);
    }

    /**
     * Retrieves all files that have been uploaded for a particular name.
     *
     * @param name the name of the file
     * @return the uploaded files; or
     * <p>{@code null} if no files were uploaded for that name
     * @see #files()
     * @see #hasFile(String)
     * @see #file(String)
     * @since 1.0
     */
    public UploadedFile[] files(String name) {
        return request_.getFiles(name);
    }

    /**
     * Checks whether a cookie is present.
     *
     * @param name the name of the cookie
     * @return {@code true} if the cookie was present; or
     * <p>{@code false} otherwise
     * @see #cookieValue(String)
     * @see #cookieValues()
     * @since 1.0
     */
    public boolean hasCookie(String name) {
        return request_.hasCookie(name);
    }

    /**
     * Retrieves the names of the cookies.
     *
     * @return a list of strings with the cookie names
     * @see #cookieValue(String)
     * @see #hasCookie(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
     */
    public List<String> cookieNames() {
        var names = new ArrayList<String>();
        for (var cookie : request_.getCookies()) {
            names.add(cookie.getName());
        }
        return names;
    }

    /**
     * Retrieves the value of a cookie.
     *
     * @param name the name of the cookie
     * @return the value of the cookie; or
     * <p>{@code null} if no such cookie is present
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
     */
    public String cookieValue(String name) {
        if (!request_.hasCookie(name)) {
            return null;
        }
        return request_.getCookie(name).getValue();
    }

    /**
     * Retrieves all current cookies names with their values.
     *
     * @return a new map of all the current cookies names with their values
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * @see #cookieNames()
     * @see #hasCookie(String)
     * @see #cookieValue(String)
     * @see #cookieValue(String, String)
     * @see #cookieValues()
     * @since 1.0
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
     * Returns the value of the named attribute.
     *
     * @param name the name of the attribute
     * @return an Object containing the value of the attribute; or
     * <p>{@code null} if the attribute does not exist
     * @see #hasAttribute
     * @see #attributeNames
     * @see #removeAttribute
     * @see #setAttribute
     * @since 1.0
     */
    public Object attribute(String name) {
        return request_.getAttribute(name);
    }

    /**
     * Checks if a request attribute exists.
     *
     * @param name a {@code String} specifying the name of the attribute
     *             <p>{@code false} otherwise
     * @see #attribute
     * @see #attributeNames
     * @see #removeAttribute
     * @see #setAttribute
     * @since 1.0
     */
    public boolean hasAttribute(String name) {
        return request_.hasAttribute(name);
    }

    /**
     * Returns a list of attribute names available to this request.
     *
     * @return a list of strings containing the names of the request's attributes; or
     * <p>an empty list if not attributes are present
     * @see #attribute
     * @see #hasAttribute
     * @see #removeAttribute
     * @see #setAttribute
     * @since 1.0
     */
    public List<String> attributeNames() {
        return Collections.list(request_.getAttributeNames());
    }

    /**
     * Removes an attribute from this request. This method is not generally needed as
     * attributes only persist as long as the request is being handled.
     *
     * @param name the name of the attribute to remove
     * @see #attribute
     * @see #hasAttribute
     * @see #attributeNames
     * @see #setAttribute
     * @since 1.0
     */
    public void removeAttribute(String name) {
        request_.removeAttribute(name);
    }

    /**
     * Stores an attribute in this request. Attributes are reset between requests.
     * <p>Attribute names should follow the same conventions as package names.
     * If the object passed in is {@code null}, the effect is the same as calling
     * {@code removeAttribute}.
     *
     * @param name   the name of the attribute to set
     * @param object the object to store
     * @see #attribute
     * @see #hasAttribute
     * @see #attributeNames
     * @see #removeAttribute
     * @since 1.0
     */
    public void setAttribute(String name, Object object) {
        request_.setAttribute(name, object);
    }

    /**
     * Returns the name of the character encoding used in the body of this request.
     *
     * @return a {@code String} containing the name of the character encoding; or
     * <p>{@code null} if the request does not specify a character encoding
     * @since 1.0
     */
    public String characterEncoding() {
        return request_.getCharacterEncoding();
    }

    /**
     * Returns the MIME type of the body of the request, or null if the type is not known.
     *
     * @return a {@code String} containing the name of the MIME type of the request; or
     * {@code null} if the type is not known
     * @since 1.0
     */
    public String contentType() {
        return request_.getContentType();
    }

    /**
     * Returns the value of the specified request header as a String.
     *
     * @param name the case-insensitive name of the header
     * @return a {@code String} with the value of the requested header; or
     * {@code null} if the request does not have a header of that name
     * @see #headerDate
     * @see #headerInt
     * @see #headerNames
     * @see #headers
     * @since 1.0
     */
    public String header(String name) {
        return request_.getHeader(name);
    }

    /**
     * Returns the value of the specified request header as a long value that
     * represents a date. Use this method with headers that contain dates,
     * such as If-Modified-Since.
     *
     * @param name the case-insensitive name of the header
     * @return a long value representing the date specified in the header expressed
     * as the number of milliseconds since January 1, 1970 GMT; or
     * <p>{@code -1} if the named header was not included with the request
     * @throws IllegalArgumentException if the header can't be converted to a date
     * @see #header
     * @see #headerInt
     * @see #headerNames
     * @see #headers
     * @since 1.0
     */
    public long headerDate(String name) {
        return request_.getDateHeader(name);
    }

    /**
     * Returns the value of the specified request header as an int.
     *
     * @param name the case-insensitive name of the header
     * @return an integer expressing the value of the request header; or
     * <p>{@code -1} if the request doesn't have a header of this name
     * @throws NumberFormatException if the header value can't be converted to an int
     * @see #header
     * @see #headerDate
     * @see #headerNames
     * @see #headers
     * @since 1.0
     */
    public int headerInt(String name) {
        return request_.getIntHeader(name);
    }

    /**
     * Returns a list of all the header names in the request.
     *
     * @return a list of all the header names sent with this request;
     * <p>if the request has no headers, an empty list will be returned
     * @see #header
     * @see #headerDate
     * @see #headerInt
     * @see #headers
     * @since 1.0
     */
    public List<String> headerNames() {
        return Collections.list(request_.getHeaderNames());
    }

    /**
     * Returns all the values of the specified request header as a list
     * of {@code String} objects.
     * <p>Some headers, such as {@code Accept-Language} can be sent by clients
     * as several headers each with a different value rather than sending the
     * header as a comma separated list.
     *
     * @param name the case-insensitive name of the header
     * @return a list containing the values of the requested header;
     * <p>itf the request does not have any headers of that name an empty list will be returned
     * @see #header
     * @see #headerDate
     * @see #headerInt
     * @see #headerNames
     * @since 1.0
     */
    public List<String> headers(String name) {
        return Collections.list(request_.getHeaders(name));
    }

    /**
     * Returns the preferred {@code Locale} that the client will accept content in,
     * based on the {@code Accept-Language} header.
     * <p>If the client request doesn't provide an Accept-Language header, this method
     * returns the default locale for the server.
     *
     * @return the preferred Locale for the client
     * @since 1.0
     */
    public Locale locale() {
        return request_.getLocale();
    }

    /**
     * Returns a list of {@code Locale} objects indicating, in decreasing order
     * starting with the preferred locale, the locales that are acceptable to the
     * client based on the {@code Accept-Language}} header.
     * <p>If the client request doesn't provide an Accept-Language header, this method
     * returns a list containing one Locale, the default locale for the server.
     *
     * @return a list of preferred Locale objects for the client
     * @since 1.0
     */
    public List<Locale> locales() {
        return Collections.list(request_.getLocales());
    }

    /**
     * Returns the Internet Protocol (IP) address of the client or last proxy
     * that sent the request.
     *
     * @return a String containing the IP address of the client that sent the request
     * @since 1.0
     */
    public String remoteAddr() {
        return request_.getRemoteAddr();
    }

    /**
     * Returns the login of the user making this request.
     * <p>Whether the username is sent with each subsequent request depends on the
     * browser and type of authentication.
     *
     * @return a String specifying the login of the user making this request; or
     * {@code null} if the user login is not known
     * @since 1.0
     */
    public String remoteUser() {
        return request_.getRemoteUser();
    }

    /**
     * Returns the fully qualified name of the client or the last proxy that
     * sent the request. If the engine cannot or chooses not to resolve the hostname
     * (to improve performance), this method returns the dotted-string form of the IP
     * address.
     *
     * @return a String containing the fully qualified name of the client
     * @since 1.0
     */
    public String remoteHost() {
        return request_.getRemoteHost();
    }

    /**
     * Returns the current session associated with this request, or if the request does not have a session, creates one.
     *
     * @return the {@code Session} associated with this request
     * @see #session(boolean)
     * @since 1.0
     */
    public Session session() {
        return session(true);
    }

    /**
     * Returns the current {@code Session} associated with this request or, if there is no current session and
     * {@code create} is true, returns a new session.
     *
     * <p>
     * If {@code create} is {@code false} and the request has no valid {@code Session}, this method
     * returns {@code null}.
     *
     * @param create {@code true} to create a new session for this request if necessary; {@code false} to return
     *               {@code null} if there's no current session
     * @return the {@code Session} associated with this request or {@code null} if {@code create} is
     * {@code false} and the request has no valid session
     * @since 1.0
     */
    public Session session(boolean create) {
        var session = request_.getSession(create);
        if (session == null) {
            return null;
        }
        return new Session(session);
    }

    /**
     * Returns the port number to which the request was sent.
     *
     * @return an integer specifying the port number
     * @since 1.0
     */
    public int serverPort() {
        return request_.getServerPort();
    }

    /**
     * Returns the name and version of the protocol the request uses in the
     * form protocol/majorVersion.minorVersion, for example, HTTP/1.1.
     *
     * @return a String containing the protocol name and version number
     * @since 1.0
     */
    public String protocol() {
        return request_.getProtocol();
    }

    /**
     * Returns the name of the scheme used to make this request.
     *
     * @return the name of the scheme used to make this request
     * @since 1.0
     */
    public String scheme() {
        return request_.getScheme();
    }

    /**
     * Returns the host name of the server to which the request was sent.
     *
     * @return the name of the server
     * @since 1.0
     */
    public String serverName() {
        return request_.getServerName();
    }

    /**
     * Returns the portion of the request URI that indicates the context of the request.
     * The context path always comes first in a request URI. The path starts with a "/"
     * character but does not end with a "/" character. For servlets in the default (root)
     * context, this method returns "". The container does not decode this string.
     *
     * @return the portion of the request URI that indicates the context of the request
     * @since 1.0
     */
    public String contextPath() {
        return request_.getContextPath();
    }

    /**
     * Returns a boolean indicating whether this request was made using a secure channel,
     * such as HTTPS.
     *
     * @return a boolean indicating whether the request was made using a secure channel
     * @since 1.0
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
     * @since 1.0
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
     * Clears the text buffer is it's enabled.
     * <p>If the text buffer is disabled, this method does nothing.
     *
     * @since 1.0
     */
    public void clearBuffer() {
        response_.clearBuffer();
    }

    /**
     * Forces all the streamed content to be output to the client.
     * <p>If the text buffer is enabled, this will flush its content to the
     * output stream first.
     *
     * @since 1.0
     */
    public void flush() {
        response_.flush();
    }

    /**
     * Returns an output stream suitable for writing binary data in the response.
     * <p>When possible and applicable, this output stream will automatically handle
     * gzip encoding.
     *
     * @return an {@code OutputStream} for binary data output
     * @since 1.0
     */
    public OutputStream outputStream()
    throws EngineException {
        return response_.getOutputStream();
    }

    /**
     * Sets the content type of the response being sent to the client.
     *
     * @param contentType the MIME type of the content
     * @since 1.0
     */
    public void setContentType(String contentType) {
        response_.setContentType(contentType);
    }

    /**
     * Sets the length of the content body in the response.
     *
     * @param length an integer specifying the length of the content being
     *               returned to the client
     * @since 1.0
     */
    public void setContentLength(int length) {
        response_.setContentLength(length);
    }

    /**
     * Sets the locale of the response.
     *
     * @param locale the locale of the response
     * @since 1.0
     */
    public void setLocale(Locale locale) {
        response_.setLocale(locale);
    }

    Map<String, String[]> parametersOut() {
        if (parametersOut_ == null) {
            return Collections.emptyMap();
        }
        return parametersOut_;
    }

    /**
     * Sets a named parameter value for URL generation with {@link #urlFor}
     * or the `route:` filtered template value tags.
     * <p>
     * The parameter value will be converted to a string array.
     * <p>
     * Setting parameters acts on a different collection than incoming request
     * parameters and is used for RIFE2 state-related features like URL generation.
     *
     * @param name the name of the parameter to set
     * @param value the value of the parameter
     * @see #urlFor
     * @see #setParametersBean(Object)
     * @see #setParametersBean(Object, String)
     * @see #removeParameter
     * @since 1.0
     */
    public void setParameter(String name, Object value) {
        if (parametersOut_ == null) {
            parametersOut_ = new HashMap<>();
        }
        parametersOut_.put(name, ArrayUtils.createStringArray(value, null));
    }

    /**
     * Sets named parameters values from bean properties for URL generation
     * with {@link #urlFor} or the `route:` filtered template value tags.
     *
     * @param bean the bean whose properties will be set as parameters
     * @see #urlFor
     * @see #setParameter
     * @see #setParametersBean(Object, String)
     * @see #removeParameter
     * @since 1.0
     */
   public void setParametersBean(Object bean) {
        setParametersBean(bean, null);
    }


    /**
     * Sets named parameters values from bean properties for URL generation
     * with {@link #urlFor} or the `route:` filtered template value tags.
     *
     * @param bean the bean whose properties will be set as parameters
     * @param prefix the prefix that will be added to the parameter names
     * @see #urlFor
     * @see #setParameter
     * @see #setParametersBean(Object)
     * @see #removeParameter
     * @since 1.0
     */
    public void setParametersBean(Object bean, String prefix) {
        if (parametersOut_ == null) {
            parametersOut_ = new HashMap<>();
        }

        try {
            BeanUtils.processPropertyValues(bean, null, null, prefix, (propertyName, descriptor, propertyValue, constrainedProperty) -> {
                if (propertyValue != null) {
                    parametersOut_.put(propertyName, ArrayUtils.createStringArray(propertyValue, constrainedProperty));
                }
            });
        } catch (BeanUtilsException e) {
            throw new EngineException(e);
        }
    }

    /**
     * Removes a name parameter that was previously set.
     *
     * @param name the name of the parameter to remove
     * @see #urlFor
     * @see #setParameter
     * @see #setParametersBean(Object)
     * @see #setParametersBean(Object, String)
     * @since 1.0
     */
    public void removeParameter(String name) {
        if (parametersOut_ == null) {
            return;
        }

        parametersOut_.remove(name);
    }

    /**
     * Adds the {@code Cookie} created by a {@code CookieBuilder} to the response.
     * This method can be called multiple times to set more than one cookie.
     *
     * @param builder the {@code CookieBuilder} to use for building the {@code Cookie}
     * @since 1.0
     */
    public void addCookie(CookieBuilder builder) {
        response_.addCookie(builder.cookie());
    }

    /**
     * Removes a cookie.
     *
     * @param name name of the cookie
     * @since 1.0
     */
    public void removeCookie(String name) {
        removeCookie(null, name);
    }

    /**
     * Removes a cookie with given path and name.
     *
     * @param path path of the cookie
     * @param name name of the cookie
     * @since 1.0
     */
    public void removeCookie(String path, String name) {
        addCookie(new CookieBuilder(name, "").path(path).maxAge(0));
    }

    /**
     * Sets a response header with the given name and value.
     * <p>If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header
     * @param value the additional header value
     * @since 1.0
     */
    public void setHeader(String name, String value) {
        response_.setHeader(name, value);
    }

    /**
     * Adds a response header with the given name and value.
     * <p>This method allows response headers to have multiple values.
     *
     * @param name the name of the header
     * @param value the additional header value
     * @since 1.0
     */
    public void addHeader(String name, String value) {
        response_.addHeader(name, value);
    }

    /**
     * Adds a response header with the given name and date-value. The date is specified in terms of milliseconds since the epoch.
     * <p>If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header to set
     * @param date the additional date value
     * @since 1.0
     */
    public void setDateHeader(String name, long date) {
        response_.setDateHeader(name, date);
    }

    /**
     * Adds a response header with the given name and date-value. The date is specified in terms of milliseconds since the epoch.
     * <p>This method allows response headers to have multiple values.
     *
     * @param name the name of the header to set
     * @param date the additional date value
     * @since 1.0
     */
    public void addDateHeader(String name, long date) {
        response_.addDateHeader(name, date);
    }

    /**
     * Adds a response header with the given name and integer value.
     * <p>If the header had already been set, the new value overwrites the previous one.
     *
     * @param name the name of the header
     * @param value the assigned integer value
     *
     * @since 1.0
     */
    public void setHeader(String name, int value) {
        response_.setIntHeader(name, value);
    }

    /**
     * Adds a response header with the given name and integer value.
     * <p>This method allows response headers to have multiple values.
     *
     * @param name the name of the header
     * @param value the assigned integer value
     *
     * @since 1.0
     */
    public void addHeader(String name, int value) {
        response_.addIntHeader(name, value);
    }

    /**
     * Sets the status code for the response.
     *
     * @param statusCode the status code
     * @since 1.0
     */
    public void setStatus(int statusCode) {
        response_.setStatus(statusCode);
    }

}
