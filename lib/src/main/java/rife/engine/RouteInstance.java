/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

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
    public Element obtainElementInstance(Context context) {
        return element_;
    }

    @Override
    public void finalizeElementInstance(Element element, Context context) {
        // no-op
    }

    void prefixPathWith(String prefix) {
        path_ = prefix + path_;
    }
}
