/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.util.*;

public class Router {
    protected final List<Route> before_ = new ArrayList<>();
    protected final List<Route> after_ = new ArrayList<>();
    protected final Map<String, List<Route>> routes_ = new HashMap<>();
    protected final Map<String, List<Route>> pathInfoRoutes_ = new HashMap<>();
    protected final List<Router> groups_ = new ArrayList<>();

    public void setup() {
    }

    @SafeVarargs
    public final void before(Class<? extends Element>... elementClasses) {
        for (var klass : elementClasses) {
            before_.add(new RouteClass(this, klass));
        }
    }

    public final void before(Element... elements) {
        for (var element : elements) {
            before_.add(new RouteInstance(this, element));
        }
    }

    @SafeVarargs
    public final void after(Class<? extends Element>... elementClasses) {
        for (var klass : elementClasses) {
            after_.add(new RouteClass(this, klass));
        }
    }

    public final void after(Element... elements) {
        for (var element : elements) {
            after_.add(new RouteInstance(this, element));
        }
    }

    public final <T extends Router> T group(T router) {
        return group("", router);
    }

    public final <T extends Router> T group(String path, T router) {
        router.setup();
        groups_.add(router);

        // TODO : consolidate routes

        return router;
    }

    public final Route get(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.GET, elementClass));
    }

    public final Route get(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.GET, pathInfo, elementClass));
    }

    public final Route get(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.GET, path, elementClass));
    }

    public final Route get(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.GET, path, pathInfo, elementClass));
    }

    public final Route get(String path, Element element) {
        return registerRoute(new RouteInstance(this, RequestMethod.GET, path, element));
    }

    public final Route get(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, RequestMethod.GET, path, pathInfo, element));
    }

    public final Route post(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.POST, elementClass));
    }

    public final Route post(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.POST, pathInfo, elementClass));
    }

    public final Route post(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.POST, path, elementClass));
    }

    public final Route post(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, RequestMethod.POST, path, pathInfo, elementClass));
    }

    public final Route post(String path, Element element) {
        return registerRoute(new RouteInstance(this, RequestMethod.POST, path, element));
    }

    public final Route post(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, RequestMethod.POST, path, pathInfo, element));
    }

    public final Route registerRoute(Route route) {
        switch (route.pathInfoHandling()) {
            case NONE -> {
                List<Route> routes = routes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
            case CAPTURE -> {
                List<Route> routes = pathInfoRoutes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
        }
        return route;
    }

}
