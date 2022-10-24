/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.Body;
import rife.engine.annotations.FileUpload;
import rife.engine.annotations.Parameter;
import rife.engine.annotations.PathInfo;
import rife.engine.exceptions.EngineException;
import rife.tools.ClassUtils;
import rife.tools.Convert;
import rife.tools.StringUtils;
import rife.tools.exceptions.ConversionException;

import java.io.File;
import java.lang.reflect.Modifier;

record RouteClass(Router router, RequestMethod method, String path, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass
) implements Route {
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
        this.router = router;
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
                if (field.isAnnotationPresent(FileUpload.class)) {
                    var annotation_name = field.getAnnotation(FileUpload.class).name();
                    if (annotation_name != null && !annotation_name.isEmpty()) {
                        name = annotation_name;
                    }

                    var uploaded_file = context.request().getFile(name);
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
