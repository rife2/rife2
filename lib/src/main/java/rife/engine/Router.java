/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.RouterAlreadyDeployedException;
import rife.ioc.HierarchicalProperties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * Provides the routing features of the RIFE2 web engine.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
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

    /**
     * The {@code setup()} method will be called by RIFE2 when the
     * web application starts up. Lifecycle-wise it is equivalent to
     * the constructor of the {@code Router}, but it can be used as
     * an overloaded method in an anonymous inner class, which is
     * very convenient when setting up groups.
     *
     * @since 1.0
     */
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

    /**
     * Adds class elements that will be processed in order before any
     * routes in this router.
     *
     * @param elementClasses the element classes that should be processed before any routes.
     * @since 1.0
     */
    @SafeVarargs
    public final void before(Class<? extends Element>... elementClasses) {
        ensurePreDeployment();
        for (var klass : elementClasses) {
            before_.add(new RouteClass(this, klass));
        }
    }

    /**
     * Adds element suppliers that will be processed in order before any
     * routes in this router.
     *
     * @param elementSuppliers the element suppliers that should be processed before any routes.
     * @since 1.1
     */
    @SafeVarargs
    public final void before(Supplier<? extends Element>... elementSuppliers) {
        ensurePreDeployment();
        for (var supplier : elementSuppliers) {
            before_.add(new RouteSupplier(this, supplier));
        }
    }

    /**
     * Adds lambda elements that will be processed in order before any
     * routes in this router.
     *
     * @param elements the lambda elements that should be processed before any routes.
     * @since 1.0
     */
    public final void before(Element... elements) {
        ensurePreDeployment();
        for (var element : elements) {
            before_.add(new RouteInstance(this, element));
        }
    }

    /**
     * Adds class elements that will be processed in order after any
     * routes in this router.
     *
     * @param elementClasses the element classes that should be processed after any routes.
     * @since 1.0
     */
    @SafeVarargs
    public final void after(Class<? extends Element>... elementClasses) {
        ensurePreDeployment();
        for (var klass : elementClasses) {
            after_.add(new RouteClass(this, klass));
        }
    }


    /**
     * Adds element suppliers that will be processed in order after any
     * routes in this router.
     *
     * @param elementSuppliers the element suppliers that should be processed after any routes.
     * @since 1.1
     */
    @SafeVarargs
    public final void after(Supplier<? extends Element>... elementSuppliers) {
        ensurePreDeployment();
        for (var supplier : elementSuppliers) {
            after_.add(new RouteSupplier(this, supplier));
        }
    }

    /**
     * Adds lambda elements that will be processed in order after any
     * routes in this router.
     *
     * @param elements the lambda elements that should be processed after any routes.
     * @since 1.0
     */
    public final void after(Element... elements) {
        ensurePreDeployment();
        for (var element : elements) {
            after_.add(new RouteInstance(this, element));
        }
    }

    /**
     * Adds another router as a group to this router.
     *
     * @param router the router to add
     * @return the router that was added
     * @since 1.0
     */
    public final <T extends Router> T group(T router) {
        ensurePreDeployment();
        return group("", router);
    }

    /**
     * Adds another router as a group with a path to this router.
     *
     * @param path   the group's path
     * @param router the router to add
     * @return the router that was added
     * @since 1.0
     */
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
                    } else if (r instanceof RouteAnnotated route) {
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
                    } else if (r instanceof RouteAnnotated route) {
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
                } else if (r instanceof RouteAnnotated route) {
                    route.prefixPathWith(path);
                    fallbackRoutes_.putIfAbsent(route.path(), route);
                }
            }
        }

        return router;
    }

    /**
     * Registers a class element as a route for the GET method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route get(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, elementClass));
    }

    /**
     * Registers a class element as a route for the GET method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route get(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the GET method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route get(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the GET method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route get(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the GET method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route get(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route get(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route get(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route get(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the GET method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route get(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET}, path, element));
    }

    /**
     * Registers a lambda element as a route for the GET method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route get(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for the POST method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route post(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, elementClass));
    }

    /**
     * Registers a class element as a route for the POST method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route post(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the POST method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route post(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the POST method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route post(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.POST}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the POST method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route post(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.POST}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the POST method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route post(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.POST}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the POST method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route post(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.POST}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the POST method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route post(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.POST}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the POST method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route post(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.POST}, path, element));
    }

    /**
     * Registers a lambda element as a route for the POST method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route post(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.POST}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for the GET and POST method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route getPost(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, elementClass));
    }

    /**
     * Registers a class element as a route for the GET and POST method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route getPost(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the GET and POST method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route getPost(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the GET and POST method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route getPost(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the GET and POST method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route getPost(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET and POST method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route getPost(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET and POST method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route getPost(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the GET and POST method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route getPost(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the GET and POST method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route getPost(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, element));
    }

    /**
     * Registers a lambda element as a route for the GET and POST method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route getPost(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.GET, RequestMethod.POST}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for the PUT method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route put(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, elementClass));
    }

    /**
     * Registers a class element as a route for the PUT method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route put(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the PUT method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route put(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the PUT method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route put(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PUT}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the PUT method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route put(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PUT}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PUT method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route put(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PUT}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PUT method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route put(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PUT}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PUT method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route put(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PUT}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the PUT method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route put(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PUT}, path, element));
    }

    /**
     * Registers a lambda element as a route for the PUT method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route put(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PUT}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for the DELETE method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route delete(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, elementClass));
    }

    /**
     * Registers a class element as a route for the DELETE method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route delete(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the DELETE method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route delete(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the DELETE method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route delete(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.DELETE}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the DELETE method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route delete(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.DELETE}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the DELETE method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route delete(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.DELETE}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the DELETE method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route delete(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.DELETE}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the DELETE method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route delete(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.DELETE}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the DELETE method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route delete(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.DELETE}, path, element));
    }

    /**
     * Registers a lambda element as a route for the DELETE method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route delete(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.DELETE}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for the PATCH method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route patch(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, elementClass));
    }

    /**
     * Registers a class element as a route for the PATCH method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route patch(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for the PATCH method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route patch(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, path, elementClass));
    }

    /**
     * Registers a class element as a route for the PATCH method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route patch(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, new RequestMethod[]{RequestMethod.PATCH}, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for the PATCH method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route patch(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PATCH}, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PATCH method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route patch(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PATCH}, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PATCH method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route patch(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PATCH}, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for the PATCH method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route patch(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, new RequestMethod[]{RequestMethod.PATCH}, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for the PATCH method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route patch(String path, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PATCH}, path, element));
    }

    /**
     * Registers a lambda element as a route for the PATCH method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
    public final Route patch(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(this, new RequestMethod[]{RequestMethod.PATCH}, path, pathInfo, element));
    }

    /**
     * Registers a class element as a route for any HTTP method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route route(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, elementClass));
    }

    /**
     * Registers a class element as a route for any HTTP method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route route(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, pathInfo, elementClass));
    }

    /**
     * Registers a class element as a route for any HTTP method with a specific path.
     *
     * @param path         the path of the route
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route route(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, path, elementClass));
    }

    /**
     * Registers a class element as a route for any HTTP method with a specific path
     * and pathinfo handling.
     *
     * @param path         the path of the route
     * @param pathInfo     the pathinfo handling to use
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route route(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(this, null, path, pathInfo, elementClass));
    }

    /**
     * Registers an element supplier as a route for any HTTP method,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.0
     */
    public final Route route(Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, null, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for any HTTP method with a pathinfo handling,
     * the path will be derived from the uncapitalized shortened class name.
     *
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.0
     */
    public final Route route(PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, null, pathInfo, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for any HTTP method with a specific path.
     *
     * @param path            the path of the route
     * @param elementSupplier the element to register a route for
     * @since 1.0
     */
    public final Route route(String path, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, null, path, elementSupplier));
    }

    /**
     * Registers an element supplier as a route for any HTTP method with a specific path
     * and pathinfo handling.
     *
     * @param path            the path of the route
     * @param pathInfo        the pathinfo handling to use
     * @param elementSupplier the element to register a route for
     * @since 1.0
     */
    public final Route route(String path, PathInfoHandling pathInfo, Supplier<? extends Element> elementSupplier) {
        return registerRoute(new RouteSupplier(this, null, path, pathInfo, elementSupplier));
    }

    /**
     * Registers a lambda element as a route for any HTTP method with a specific path.
     *
     * @param path    the path of the route
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route route(String path, Element element) {
        return registerRoute(new RouteInstance(this, null, path, element));
    }

    /**
     * Registers a lambda element as a route for any HTTP method with a specific path
     * and pathinfo handling.
     *
     * @param path     the path of the route
     * @param pathInfo the pathinfo handling to use
     * @param element  the element to register a route for
     * @since 1.0
     */
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

    /**
     * Registers a class element as the route for handling exceptions
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route exception(Class<? extends Element> elementClass) {
        ensurePreDeployment();
        exceptionRoute_ = new RouteClass(this, new RequestMethod[]{RequestMethod.GET}, elementClass);
        return exceptionRoute_;
    }

    /**
     * Registers an element supplier as the route for handling exceptions
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route exception(Supplier<? extends Element> elementSupplier) {
        ensurePreDeployment();
        exceptionRoute_ = new RouteSupplier(this, new RequestMethod[]{RequestMethod.GET}, elementSupplier);
        return exceptionRoute_;
    }

    /**
     * Registers a lambda element as the route for handling exceptions
     *
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route exception(Element element) {
        ensurePreDeployment();
        exceptionRoute_ = new RouteInstance(this, element);
        return exceptionRoute_;
    }

    Route getExceptionRoute() {
        if (exceptionRoute_ != null) {
            return exceptionRoute_;
        }
        if (parent_ != null) {
            return parent_.exceptionRoute_;
        }
        return null;
    }

    /**
     * Registers a class element as the route for handling requests that don't match any other routes.
     *
     * @param elementClass the element to register a route for
     * @since 1.0
     */
    public final Route fallback(Class<? extends Element> elementClass) {
        return registerFallback(new RouteClass(this, null, "", elementClass));
    }

    /**
     * Registers an element supplier as the route for handling requests that don't match any other routes.
     *
     * @param elementSupplier the element to register a route for
     * @since 1.1
     */
    public final Route fallback(Supplier<? extends Element> elementSupplier) {
        return registerFallback(new RouteSupplier(this, null, "", elementSupplier));
    }

    /**
     * Registers a lambda element as the route for handling requests that don't match any other routes.
     *
     * @param element the element to register a route for
     * @since 1.0
     */
    public final Route fallback(Element element) {
        return registerFallback(new RouteInstance(this, null, "", element));
    }

    final Route registerFallback(Route route) {
        ensurePreDeployment();
        fallbackRoutes_.put("", route);
        return route;
    }

    /**
     * Retrieves the hierarchical properties for this router.
     *
     * @return this router's collection of hierarchical properties
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Retrieves the top-level router, aka. the site for this router.
     *
     * @return this router's {@code site}
     * @since 1.0
     */
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
