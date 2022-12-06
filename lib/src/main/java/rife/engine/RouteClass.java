/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.engine.exceptions.EngineException;
import rife.tools.ClassUtils;
import rife.tools.Convert;
import rife.tools.StringUtils;
import rife.tools.exceptions.ConversionException;

import java.io.File;
import java.lang.reflect.Modifier;

public class RouteClass implements Route {
    private final Router router_;
    private final RequestMethod method_;
    private String path_;
    private final PathInfoHandling pathInfoHandling_;
    private final Class<? extends Element> elementClass_;

    public RouteClass(Router router, Class<? extends Element> elementClass) {
        this(router, null, null, null, elementClass);
    }

    public RouteClass(Router router, RequestMethod method, Class<? extends Element> elementClass) {
        this(router, method, null, null, elementClass);
    }

    public RouteClass(Router router, RequestMethod method, String path, Class<? extends Element> elementClass) {
        this(router, method, path, null, elementClass);
    }

    public RouteClass(Router router, RequestMethod method, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        this(router, method, null, pathInfoHandling, elementClass);
    }

    public RouteClass(Router router, RequestMethod method, String path, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        router_ = router;
        method_ = method;
        elementClass_ = elementClass;
        if (path == null) {
            path = getDefaultElementPath();
        }
        path_ = path;
        if (pathInfoHandling == null) {
            // TODO : instead this should detect whether a PathInfo annotation is present and handle it
            pathInfoHandling = PathInfoHandling.NONE;
        }
        pathInfoHandling_ = pathInfoHandling;
    }

    @Override
    public Router router() {
        return router_;
    }

    @Override
    public RequestMethod method() {
        return method_;
    }

    @Override
    public String path() {
        return path_;
    }

    @Override
    public PathInfoHandling pathInfoHandling() {
        return pathInfoHandling_;
    }

    private boolean shouldProcessInFlow(FlowDirection flow) {
        return flow == FlowDirection.IN || flow == FlowDirection.IN_OUT;
    }

    @Override
    public Element getElementInstance(Context context) {
        try {
            var element = elementClass_.getDeclaredConstructor().newInstance();

            // auto assign annotated parameters
            for (var field : element.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                var name = field.getName();
                var type = field.getType();

                if (field.isAnnotationPresent(Parameter.class)) {
                    var params = context.parameters();
                    var annotation_name = field.getAnnotation(Parameter.class).name();
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
                } else if (field.isAnnotationPresent(Header.class) &&
                           shouldProcessInFlow(field.getAnnotation(Header.class).flow())) {
                    var annotation_name = field.getAnnotation(Header.class).name();
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
                    var annotation_name = field.getAnnotation(FileUpload.class).name();
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
                    var annotation_name = field.getAnnotation(Cookie.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var cookie = context.cookie(name);
                    if (cookie != null) {
                        if (cookie.getValue() != null) {
                            Object value;
                            try {
                                value = Convert.toType(cookie.getValue(), type);
                            } catch (ConversionException e) {
                                value = Convert.getDefaultValue(type);
                            }
                            field.set(element, value);
                        }
                    }
                } else if (field.isAnnotationPresent(RequestAttribute.class) &&
                           shouldProcessInFlow(field.getAnnotation(RequestAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(RequestAttribute.class).name();
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
                    var annotation_name = field.getAnnotation(SessionAttribute.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var session = context.session(false);
                    if (session != null) {
                        var value = session.getAttribute(name);
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

            return element;
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    private boolean shouldProcessOutFlow(FlowDirection flow) {
        return flow == FlowDirection.OUT || flow == FlowDirection.IN_OUT;
    }

    @Override
    public void finalizeElementInstance(Element element, Context context) {
        try {
            for (var field : element.getClass().getDeclaredFields()) {
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
                    var annotation_name = field.getAnnotation(Header.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    context.addHeader(name, String.valueOf(value));
                } else if (field.isAnnotationPresent(Body.class) &&
                           shouldProcessOutFlow(field.getAnnotation(Body.class).flow())) {
                    context.print(value);
                } else if (field.isAnnotationPresent(Cookie.class) &&
                           shouldProcessOutFlow(field.getAnnotation(Cookie.class).flow())) {
                    var annotation_name = field.getAnnotation(Cookie.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    var builder = new CookieBuilder(name, String.valueOf(value));
                    context.addCookie(builder.cookie());
                } else if (field.isAnnotationPresent(RequestAttribute.class) &&
                           shouldProcessOutFlow(field.getAnnotation(RequestAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(RequestAttribute.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }
                    context.setAttribute(name, value);
                } else if (field.isAnnotationPresent(SessionAttribute.class) &&
                           shouldProcessOutFlow(field.getAnnotation(SessionAttribute.class).flow())) {
                    var annotation_name = field.getAnnotation(SessionAttribute.class).name();
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
    public String getDefaultElementId() {
        return StringUtils.uncapitalize(ClassUtils.shortenClassName(elementClass_));
    }

    @Override
    public String getDefaultElementPath() {
        return "/" + getDefaultElementId();
    }

    void prefixPathWith(String prefix) {
        path_ = prefix + path_;
    }

    public Class<? extends Element> elementClass() {
        return elementClass_;
    }
}
