/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.engine.annotations.Parameter;
import rife.engine.exceptions.EngineException;
import rife.tools.*;
import rife.tools.exceptions.ConversionException;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;

class RouteClass implements Route {
    private final Router router_;
    private final RequestMethod[] methods_;
    private String path_;
    private final PathInfoHandling pathInfoHandling_;
    private final Class<? extends Element> elementClass_;
    private List<Field> fields_ = null;

    RouteClass(Router router, Class<? extends Element> elementClass) {
        this(router, null, null, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, Class<? extends Element> elementClass) {
        this(router, methods, null, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, String path, Class<? extends Element> elementClass) {
        this(router, methods, path, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        this(router, methods, null, pathInfoHandling, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, String path, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        router_ = router;
        methods_ = methods;
        elementClass_ = elementClass;
        if (path == null) {
            path = defaultElementPath();
        }
        path_ = path;
        if (pathInfoHandling == null) {
            pathInfoHandling = PathInfoHandling.NONE;
        }
        pathInfoHandling_ = pathInfoHandling;
    }

    @Override
    public Router router() {
        return router_;
    }

    @Override
    public RequestMethod[] methods() {
        return methods_;
    }

    @Override
    public String path() {
        return path_;
    }

    @Override
    public PathInfoHandling pathInfoHandling() {
        return pathInfoHandling_;
    }

    private static boolean shouldProcessInFlow(FlowDirection flow) {
        return flow == FlowDirection.IN || flow == FlowDirection.IN_OUT;
    }

    private static boolean shouldProcessOutFlow(FlowDirection flow) {
        return flow == FlowDirection.OUT || flow == FlowDirection.IN_OUT;
    }

    private List<Field> getAnnotatedFields() {
        if (fields_ != null) {
            return fields_;
        }

        List<Field> fields = new ArrayList<>();
        try {
            Class klass = elementClass_;
            while (klass != null && klass != Element.class) {
                for (var field : klass.getDeclaredFields()) {
                    field.setAccessible(true);

                    if (Modifier.isStatic(field.getModifiers()) ||
                        Modifier.isFinal(field.getModifiers()) ||
                        Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }

                    if (field.isAnnotationPresent(ActiveSite.class) ||
                        field.isAnnotationPresent(Body.class) ||
                        field.isAnnotationPresent(Cookie.class) ||
                        field.isAnnotationPresent(FileUpload.class) ||
                        field.isAnnotationPresent(Header.class) ||
                        field.isAnnotationPresent(Parameter.class) ||
                        field.isAnnotationPresent(ParametersBean.class) ||
                        field.isAnnotationPresent(PathInfo.class) ||
                        field.isAnnotationPresent(Property.class) ||
                        field.isAnnotationPresent(RequestAttribute.class) ||
                        field.isAnnotationPresent(SessionAttribute.class)) {
                        fields.add(field);
                    }
                }

                klass = klass.getSuperclass();
            }
        } catch (Exception e) {
            throw new EngineException(e);
        }

        fields_ = fields;
        return fields;
    }

    static Map<String, String[]> getAnnotatedOutParameters(Context context) {
        try {
            var parameters = new LinkedHashMap<String, String[]>();

            if (context.processedRoute() instanceof RouteClass route) {
                for (var field : route.getAnnotatedFields()) {
                    field.setAccessible(true);

                    if (Modifier.isStatic(field.getModifiers()) ||
                        Modifier.isFinal(field.getModifiers()) ||
                        Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }

                    var name = field.getName();
                    var value = field.get(context.processedElement());

                    if (value != null) {
                        if (field.isAnnotationPresent(Parameter.class) &&
                            shouldProcessOutFlow(field.getAnnotation(Parameter.class).flow())) {
                            var annotation_name = field.getAnnotation(Parameter.class).value();
                            if (annotation_name != null && !annotation_name.isEmpty()) {
                                name = annotation_name;
                            }

                            parameters.put(name, new String[]{Convert.toString(value)});
                        } else if (field.isAnnotationPresent(ParametersBean.class) &&
                                   shouldProcessOutFlow(field.getAnnotation(ParametersBean.class).flow())) {
                            var annotation_prefix = field.getAnnotation(ParametersBean.class).prefix();

                            BeanUtils.processPropertyValues(value, null, null, annotation_prefix, (propertyName, descriptor, propertyValue, constrainedProperty) -> {
                                if (propertyValue != null) {
                                    parameters.put(propertyName, ArrayUtils.createStringArray(propertyValue, constrainedProperty));
                                }
                            });
                        }
                    }
                }
            }

            return parameters;
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    Set<String> getAnnotatedInParameters() {
        try {
            var parameters = new HashSet<String>();

            for (var field : getAnnotatedFields()) {
                field.setAccessible(true);

                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                var name = field.getName();

                if (field.isAnnotationPresent(Parameter.class) &&
                    shouldProcessInFlow(field.getAnnotation(Parameter.class).flow())) {
                    var annotation_name = field.getAnnotation(Parameter.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }

                    parameters.add(name);
                } else if (field.isAnnotationPresent(ParametersBean.class) &&
                           shouldProcessInFlow(field.getAnnotation(ParametersBean.class).flow())) {
                    var type = field.getType();
                    var annotation_prefix = field.getAnnotation(ParametersBean.class).prefix();

                    parameters.addAll(BeanUtils.getPropertyNames(type, null, null, annotation_prefix));
                }
            }

            return parameters;
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public Element obtainElementInstance(Context context) {
        try {
            return elementClass_.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void prepareElementInstance(Element element, Context context) {
        try {
            for (var field : getAnnotatedFields()) {
                var name = field.getName();
                var type = field.getType();

                if (field.isAnnotationPresent(ActiveSite.class)) {
                    if (Site.class.isAssignableFrom(field.getType())) {
                        field.set(element, context.site());
                    }
                } else if (field.isAnnotationPresent(Parameter.class) &&
                           shouldProcessInFlow(field.getAnnotation(Parameter.class).flow())) {
                    var params = context.parameters();
                    var annotation_name = field.getAnnotation(Parameter.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var values = params.get(name);
                    if (values != null && values.length > 0) {
                        Object value;
                        try {
                            value = Convert.toType(values[0], type);
                        } catch (ConversionException e) {
                            value = Convert.getDefaultValue(type);
                        }
                        field.set(element, value);
                    }
                } else if (field.isAnnotationPresent(ParametersBean.class) &&
                           shouldProcessInFlow(field.getAnnotation(ParametersBean.class).flow())) {
                    var annotation_prefix = field.getAnnotation(ParametersBean.class).prefix();
                    Object bean = field.get(element);
                    if (bean == null) {
                        field.set(element, context.parametersBean(type, annotation_prefix));
                    } else {
                        context.parametersBean(bean, annotation_prefix);
                    }
                } else if (field.isAnnotationPresent(Property.class)) {
                    var properties = context.properties();
                    var annotation_name = field.getAnnotation(Property.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var values = properties.get(name);
                    if (values != null) {
                        Object value;
                        try {
                            value = Convert.toType(values, type);
                        } catch (ConversionException e) {
                            value = Convert.getDefaultValue(type);
                        }
                        field.set(element, value);
                    }
                } else if (field.isAnnotationPresent(Header.class) &&
                           shouldProcessInFlow(field.getAnnotation(Header.class).flow())) {
                    var annotation_name = field.getAnnotation(Header.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var header = context.header(name);
                    if (header != null) {
                        Object value;
                        try {
                            value = Convert.toType(header, type);
                        } catch (ConversionException e) {
                            value = Convert.getDefaultValue(type);
                        }
                        field.set(element, value);
                    }
                } else if (field.isAnnotationPresent(Body.class) &&
                           shouldProcessInFlow(field.getAnnotation(Body.class).flow())) {
                    var body = context.body();
                    Object value;
                    try {
                        value = Convert.toType(body, type);
                    } catch (ConversionException e) {
                        value = Convert.getDefaultValue(type);
                    }
                    field.set(element, value);
                } else if (field.isAnnotationPresent(PathInfo.class) &&
                           pathInfoHandling_.type() != PathInfoType.NONE) {
                    var path_info = context.pathInfo();
                    Object value;
                    try {
                        value = Convert.toType(path_info, type);
                    } catch (ConversionException e) {
                        value = Convert.getDefaultValue(type);
                    }
                    field.set(element, value);
                } else if (field.isAnnotationPresent(FileUpload.class)) {
                    var annotation_name = field.getAnnotation(FileUpload.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }

                    var uploaded_file = context.file(name);
                    if (uploaded_file != null) {
                        Object value;
                        if (UploadedFile.class.isAssignableFrom(type)) {
                            value = uploaded_file;
                        } else if (File.class.isAssignableFrom(type)) {
                            value = uploaded_file.getFile();
                        } else {
                            try {
                                value = Convert.toType(uploaded_file.getFile().getAbsolutePath(), type);
                            } catch (ConversionException e) {
                                value = Convert.getDefaultValue(type);
                            }
                        }
                        field.set(element, value);
                    }
                } else if (field.isAnnotationPresent(Cookie.class) &&
                           shouldProcessInFlow(field.getAnnotation(Cookie.class).flow())) {
                    var annotation_name = field.getAnnotation(Cookie.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    if (context.hasCookie(name)) {
                        String cookie_value = context.cookieValue(name);
                        if (cookie_value != null) {
                            Object value;
                            try {
                                value = Convert.toType(cookie_value, type);
                            } catch (ConversionException e) {
                                value = Convert.getDefaultValue(type);
                            }
                            field.set(element, value);
                        }
                    }
                } else if (field.isAnnotationPresent(RequestAttribute.class) &&
                           shouldProcessInFlow(field.getAnnotation(RequestAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(RequestAttribute.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var value = context.attribute(name);
                    if (value != null) {
                        try {
                            value = Convert.toType(value, type);
                        } catch (ConversionException e) {
                            value = Convert.getDefaultValue(type);
                        }
                        field.set(element, value);
                    }
                } else if (field.isAnnotationPresent(SessionAttribute.class) &&
                           shouldProcessInFlow(field.getAnnotation(SessionAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(SessionAttribute.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var session = context.session(false);
                    if (session != null) {
                        var value = session.attribute(name);
                        if (value != null) {
                            try {
                                value = Convert.toType(value, type);
                            } catch (ConversionException e) {
                                value = Convert.getDefaultValue(type);
                            }
                            field.set(element, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void finalizeElementInstance(Element element, Context context) {
        try {
            for (var field : getAnnotatedFields()) {
                field.setAccessible(true);

                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                var name = field.getName();
                var value = field.get(element);

                if (field.isAnnotationPresent(Header.class) &&
                    shouldProcessOutFlow(field.getAnnotation(Header.class).flow())) {
                    var annotation_name = field.getAnnotation(Header.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    context.addHeader(name, Convert.toString(value));
                } else if (field.isAnnotationPresent(Body.class) &&
                           shouldProcessOutFlow(field.getAnnotation(Body.class).flow())) {
                    context.print(value);
                } else if (field.isAnnotationPresent(Cookie.class) &&
                           shouldProcessOutFlow(field.getAnnotation(Cookie.class).flow())) {
                    var annotation_name = field.getAnnotation(Cookie.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var builder = new CookieBuilder(name, Convert.toString(value));
                    context.addCookie(builder);
                } else if (field.isAnnotationPresent(RequestAttribute.class) &&
                           shouldProcessOutFlow(field.getAnnotation(RequestAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(RequestAttribute.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    context.setAttribute(name, value);
                } else if (field.isAnnotationPresent(SessionAttribute.class) &&
                           shouldProcessOutFlow(field.getAnnotation(SessionAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(SessionAttribute.class).value();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var session = context.request().getSession();
                    session.setAttribute(name, value);
                }
            }
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public String defaultElementId() {
        return StringUtils.uncapitalize(ClassUtils.shortenClassName(elementClass_));
    }

    @Override
    public Class getElementClass() {
        return elementClass_;
    }

    private String defaultElementPath() {
        return "/" + defaultElementId();
    }

    void prefixPathWith(String prefix) {
        path_ = prefix + path_;
    }
}
