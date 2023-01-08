/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.RouterAlreadyDeployedException;
import rife.ioc.HierarchicalProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Router {
    final HierarchicalProperties properties_ = new HierarchicalProperties();
    final List<Route> before_ = new ArrayList<>();
    final List<Route> after_ = new ArrayList<>();
    final Map<String, List<Route>> routes_ = new HashMap<>();
    final Map<String, List<Route>> pathInfoRoutes_ = new HashMap<>();
    final Map<String, Route> fallbackRoutes_ = new HashMap<>();
    final List<Router> groups_ = new ArrayList<>();
    Route exceptionRoute_ = null;
    Router parent_ = null;
    boolean deployed_ = false;

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

        deployed_ = true;
    }

    void ensurePreDeployment() {
        if (deployed_) {
            throw new RouterAlreadyDeployedException(this);
        }
    }

    @SafeVarargs
    public final void before(Class<? extends Element>... elementClasses) {
        ensurePreDeployment();
        for (var klass : elementClasses) {
            before_.add(new RouteClass(this, klass));
        }
    }

    public final void before(Element... elements) {
        ensurePreDeployment();
        for (var element : elements) {
            before_.add(new RouteInstance(this, element));
        }
    }

    @SafeVarargs
    public final void after(Class<? extends Element>... elementClasses) {
        ensurePreDeployment();
        for (var klass : elementClasses) {
            after_.add(new RouteClass(this, klass));
        }
    }

    public final void after(Element... elements) {
        ensurePreDeployment();
        for (var element : elements) {
            after_.add(new RouteInstance(this, element));
        }
    }

    public final <T extends Router> T group(T router) {
        ensurePreDeployment();
        return group("", router);
    }

    public final <T extends Router> T group(String path, T router) {
        ensurePreDeployment();
        router.properties_.setParent(this.properties_);
        router.parent_ = this;
        groups_.add(router);

        router.setup();

        // pull in routes
        for (var e : router.routes_.entrySet()) {
            var routes = routes_.computeIfAbsent(path + e.getKey(), k -> new ArrayList<>());
            if (path.isEmpty()) {
                routes.addAll(e.getValue());
            } else {
                for (var r : e.getValue()) {
                    if (r instanceof RouteInstance route) {
                        route.prefixPathWith(path);
                        routes.add(route);
                    } else if (r instanceof RouteClass route) {
                        route.prefixPathWith(path);
                        routes.add(route);
                    }
                }
            }
        }

        // pull in path info routes
        for (var e : router.pathInfoRoutes_.entrySet()) {
            var routes = pathInfoRoutes_.computeIfAbsent(path + e.getKey(), k -> new ArrayList<>());
            if (path.isEmpty()) {
                routes.addAll(e.getValue());
            } else {
                for (var r : e.getValue()) {
                    if (r instanceof RouteInstance route) {
                        route.prefixPathWith(path);
                        routes.add(route);
                    } else if (r instanceof RouteClass route) {
                        route.prefixPathWith(path);
                        routes.add(route);
                    }
                }
            }
        }

        // pull in fallback routes
        if (path.isEmpty()) {
            for (var e : router.fallbackRoutes_.entrySet()) {
                fallbackRoutes_.putIfAbsent(e.getKey(), e.getValue());
            }
        } else {
            for (var e : router.fallbackRoutes_.entrySet()) {
                var r = e.getValue();
                if (r instanceof RouteInstance route) {
                    route.prefixPathWith(path);
                    fallbackRoutes_.putIfAbsent(route.path(), route);
                } else if (r instanceof RouteClass route) {
                    route.prefixPathWith(path);
                    fallbackRoutes_.putIfAbsent(route.path(), route);
                }
            }
        }

        return router;
    }

    public final Route get(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, elementClass));
    }

    public final Route get(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, pathInfo, elementClass));
    }

    public final Route get(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, path, elementClass));
    }

    public final Route get(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, path, pathInfo, elementClass));
    }

    public final Route get(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET}, path, element));
    }

    public final Route get(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET}, path, pathInfo, element));
    }

    public final Route post(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, elementClass));
    }

    public final Route post(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, pathInfo, elementClass));
    }

    public final Route post(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, path, elementClass));
    }

    public final Route post(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, path, pathInfo, elementClass));
    }

    public final Route post(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.POST}, path, element));
    }

    public final Route post(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.POST}, path, pathInfo, element));
    }

    public final Route getPost(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, elementClass));
    }

    public final Route getPost(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, pathInfo, elementClass));
    }

    public final Route getPost(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, elementClass));
    }

    public final Route getPost(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, pathInfo, elementClass));
    }

    public final Route getPost(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, element));
    }

    public final Route getPost(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, pathInfo, element));
    }

    public final Route put(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, elementClass));
    }

    public final Route put(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, pathInfo, elementClass));
    }

    public final Route put(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, path, elementClass));
    }

    public final Route put(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, path, pathInfo, elementClass));
    }

    public final Route put(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PUT}, path, element));
    }

    public final Route put(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PUT}, path, pathInfo, element));
    }

    public final Route delete(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, elementClass));
    }

    public final Route delete(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, pathInfo, elementClass));
    }

    public final Route delete(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, path, elementClass));
    }

    public final Route delete(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, path, pathInfo, elementClass));
    }

    public final Route delete(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.DELETE}, path, element));
    }

    public final Route delete(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.DELETE}, path, pathInfo, element));
    }

    public final Route patch(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, elementClass));
    }

    public final Route patch(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, pathInfo, elementClass));
    }

    public final Route patch(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, path, elementClass));
    }

    public final Route patch(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, path, pathInfo, elementClass));
    }

    public final Route patch(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PATCH}, path, element));
    }

    public final Route patch(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PATCH}, path, pathInfo, element));
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

    final Route registerRoute(Route route) {
        ensurePreDeployment();
        switch (route.pathInfoHandling().type()) {
            case NONE -> {
                var routes = routes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
            case CAPTURE, MAP -> {
                var routes = pathInfoRoutes_.computeIfAbsent(route.path(), k -> new ArrayList<>());
                routes.add(route);
            }
        }
        return route;
    }

    public final Route exception(Class<? extends Element> elementClass) {
        ensurePreDeployment();
        exceptionRoute_ = new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, elementClass);
        return exceptionRoute_;
    }

    public final Route exception(Element element) {
        ensurePreDeployment();
        exceptionRoute_ = new RouteInstance(this, element);
        return exceptionRoute_;
    }

    public Route getExceptionRoute() {
        if (exceptionRoute_ != null) {
            return exceptionRoute_;
        }
        if (parent_ != null) {
            return parent_.exceptionRoute_;
        }
        return null;
    }

    public final Route fallback(Class<? extends Element> elementClass) {
        return registerFallback(new RouteClass(this, null, "", elementClass));
    }

    public final Route fallback(Element element) {
        return registerFallback(new RouteInstance(this, null, "", element));
    }

    final Route registerFallback(Route route) {
        ensurePreDeployment();
        fallbackRoutes_.put("", route);
        return route;
    }

    public HierarchicalProperties properties() {
        return properties_;
    }

    public Site site() {
        Router router = this;
        while (router.parent_ != null) {
            router = router.parent_;
        }
        return (Site) router;
    }

    Route resolveRoute(String path) {
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
                Class klass = router.getClass();
                Field field = null;
                while (field == null && klass != Site.class && klass != Router.class) {
                    try {
                        field = klass.getDeclaredField(token);
                        field.setAccessible(true);

                        if (Modifier.isStatic(field.getModifiers()) ||
                            Modifier.isTransient(field.getModifiers()) ||
                            (!Route.class.isAssignableFrom(field.getType()) && !Router.class.isAssignableFrom(field.getType()))) {
                            field = null;
                        }
                    } catch (NoSuchFieldException ignored) {
                    }

                    klass = klass.getSuperclass();
                }

                if (field == null) {
                    return null;
                }

                try {
                    if (Route.class.isAssignableFrom(field.getType())) {
                        if (route != null) {
                            return null;
                        }
                        route = (Route) field.get(router);
                    } else if (Router.class.isAssignableFrom(field.getType())) {
                        router = (Router) field.get(router);
                    }
                } catch (IllegalAccessException e) {
                    return null;
                }
            }
        }

        return route;
    }
}
