/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.engine.exceptions.AnnotatedElementInstanceFieldException;
import rife.engine.exceptions.EngineException;
import rife.tools.StringUtils;

import java.lang.reflect.Modifier;

public class RouteInstance implements Route {
    private final Router router_;
    private final RequestMethod method_;
    private String path_;
    private final PathInfoHandling pathInfoHandling_;
    private final Element element_;

    public RouteInstance(Router router, Element element) {
        this(router, null, null, element);
    }

    public RouteInstance(Router router, RequestMethod method, String path, Element element) {
        this(router, method, path, PathInfoHandling.NONE, element);
    }

    public RouteInstance(Router router, RequestMethod method, String path, PathInfoHandling pathInfoHandling, Element element) {
        router_ = router;
        method_ = method;
        path_ = path;
        pathInfoHandling_ = pathInfoHandling;
        element_ = element;
        preventAnnotatedFields();
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

    @Override
    public String defaultElementId() {
        return StringUtils.stripFromFront(path_, "/");
    }

    @Override
    public Class getElementClass() {
        return element_.getClass();
    }

    private void preventAnnotatedFields() {
        try {
            Class klass = element_.getClass();
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
                        field.isAnnotationPresent(PathInfo.class) ||
                        field.isAnnotationPresent(Property.class) ||
                        field.isAnnotationPresent(RequestAttribute.class) ||
                        field.isAnnotationPresent(SessionAttribute.class)) {
                        throw new AnnotatedElementInstanceFieldException(this, element_, field.getName());
                    }
                }

                klass = klass.getSuperclass();
            }
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    @Override
    public Element obtainElementInstance(Context context) {
        return element_;
    }

    @Override
    public void prepareElementInstance(Element element, Context context) {
        // no-op
    }

    @Override
    public void finalizeElementInstance(Element element, Context context) {
        // no-op
    }

    void prefixPathWith(String prefix) {
        path_ = prefix + path_;
    }
}
