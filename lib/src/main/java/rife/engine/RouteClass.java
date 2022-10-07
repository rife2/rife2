/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.Body;
import rife.engine.annotations.Parameter;
import rife.engine.annotations.PathInfo;
import rife.engine.exceptions.EngineException;
import rife.tools.ClassUtils;
import rife.tools.Convert;
import rife.tools.StringUtils;
import rife.tools.exceptions.ConversionException;

import java.lang.reflect.Modifier;

public record RouteClass(Class<? extends Element> elementClass, RequestMethod method, String path,
                         PathInfoHandling pathInfoHandling) implements Route {
    public RouteClass(Class<? extends Element> elementClass, RequestMethod method) {
        this(elementClass, method, null, null);
    }

    public RouteClass(Class<? extends Element> elementClass, RequestMethod method, String path) {
        this(elementClass, method, path, null);
    }

    public RouteClass(Class<? extends Element> elementClass, RequestMethod method, PathInfoHandling pathInfoHandling) {
        this(elementClass, method, null, pathInfoHandling);
    }

    public RouteClass(Class<? extends Element> elementClass, RequestMethod method, String path, PathInfoHandling pathInfoHandling) {
        this.method = method;
        this.elementClass = elementClass;
        if (path == null) {
            path = getDefaultElementPath();
        }
        this.path = path;
        if (pathInfoHandling == null) {
            // TODO : instead this should detect whether a PathInfo annotation is present and handle it
            pathInfoHandling = PathInfoHandling.NONE;
        }
        this.pathInfoHandling = pathInfoHandling;
    }

    @Override
    public Element getElementInstance(Context context) {
        try {
            var element = elementClass().getDeclaredConstructor().newInstance();

            // auto assign annotated parameters
            var params = context.request().getParameters();
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
                }
                if (field.isAnnotationPresent(Body.class)) {
                    var body = context.request().getBody();
                    Object value;
                    try {
                        value = Convert.toType(body, type);
                    } catch (ConversionException e) {
                        value = Convert.getDefaultValue(type);
                    }
                    field.set(element, value);
                }
                if (field.isAnnotationPresent(PathInfo.class)) {
                    var path_info = context.pathInfo();
                    Object value;
                    try {
                        value = Convert.toType(path_info, type);
                    } catch (ConversionException e) {
                        value = Convert.getDefaultValue(type);
                    }
                    field.set(element, value);
                }
            }

            return element;
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public String getDefaultElementId() {
        return StringUtils.uncapitalize(ClassUtils.shortenClassName(elementClass));
    }

    @Override
    public String getDefaultElementPath() {
        return "/" + getDefaultElementId();
    }
}
