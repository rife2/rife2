/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import java.lang.reflect.Modifier;
import java.util.*;

public class Router {
    final List<Route> before_ = new ArrayList<>();
    final List<Route> after_ = new ArrayList<>();
    final Map<String, List<Route>> routes_ = new HashMap<>();
    final Map<String, List<Route>> pathInfoRoutes_ = new HashMap<>();
    final List<Router> groups_ = new ArrayList<>();

    Router parent_ = null;

    public void setup() {
    }

    final void deploy() {
        if (parent_ != null) {
            before_.addAll(0, parent_.before_);
            after_.addAll(parent_.after_);
        }

        for (var router : groups_) {
            router.deploy();
        }
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
        router.parent_ = this;
        groups_.add(router);

        router.setup();

        for (var e : router.routes_.entrySet()) {
            var routes = routes_.computeIfAbsent(path + e.getKey(), k -> new ArrayList<>());
            if (path.isEmpty()) {
                routes.addAll(e.getValue());
            } else {
                for (var r : e.getValue()) {
                    if (r instanceof RouteInstance route) {
                        routes.add(new RouteInstance(r.router(), r.method(), path + r.path(), r.pathInfoHandling(), route.element()));
                    } else if (r instanceof RouteClass route) {
                        routes.add(new RouteClass(r.router(), r.method(), path + r.path(), r.pathInfoHandling(), route.elementClass()));
                    }
                }
            }
        }
        for (var e : router.pathInfoRoutes_.entrySet()) {
            var routes = pathInfoRoutes_.computeIfAbsent(path + e.getKey(), k -> new ArrayList<>());
            if (path.isEmpty()) {
                routes.addAll(e.getValue());
            } else {
                for (var r : e.getValue()) {
                    if (r instanceof RouteInstance route) {
                        routes.add(new RouteInstance(r.router(), r.method(), path + r.path(), r.pathInfoHandling(), route.element()));
                    } else if (r instanceof RouteClass route) {
                        routes.add(new RouteClass(r.router(), r.method(), path + r.path(), r.pathInfoHandling(), route.elementClass()));
                    }
                }
            }
        }

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

    public final Route route(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, elementClass));
    }

    public final Route route(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, pathInfo, elementClass));
    }

    public final Route route(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, path, elementClass));
    }

    public final Route route(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, path, pathInfo, elementClass));
    }

    public final Route route(String path, Element element) {
        return registerRoute(new RouteInstance(this, null, path, element));
    }

    public final Route route(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, null, path, pathInfo, element));
    }

    public final Route registerRoute(Route route) {
        switch (route.pathInfoHandling()) {
            case NONE -> {
                var routes = routes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
            case CAPTURE -> {
                var routes = pathInfoRoutes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
        }
        return route;
    }

    public Site site() {
        Router router = this;
        while (router.parent_ != null) {
            router = router.parent_;
        }
        return (Site) router;
    }

    public Route resolveRoute(String path) {
        if (null == path || path.isEmpty()) {
            return null;
        }

        Route route = null;
        Router router = null;
        // if this is an absolute path, start resolving from the top level site
        if (path.startsWith(".")) {
            router = site();
        } else {
            router = this;
        }

        var route_tok = new StringTokenizer(path, ".^", true);
        String token = null;
        while (route_tok.hasMoreTokens()) {
            token = route_tok.nextToken();
            if (token.equals(".")) {
                // do nothing
            } else if (token.equals("^")) {
                if (route != null) {
                   route = null;
                } else {
                    if (router.parent_ == null) {
                        return null;
                    }

                    router = router.parent_;
                }
            } else {
                try {
                    var field = router.getClass().getDeclaredField(token);
                    field.setAccessible(true);

                    if (!Modifier.isStatic(field.getModifiers()) &&
                        !Modifier.isTransient(field.getModifiers())) {
                        if (Route.class.isAssignableFrom(field.getType())) {
                            if (route != null) {
                                return null;
                            }
                            route = (Route) field.get(router);
                        } else if (Router.class.isAssignableFrom(field.getType())) {
                            router = (Router) field.get(router);
                        }
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    return null;
                }
            }
        }

        return route;
    }
}
