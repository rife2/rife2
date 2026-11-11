/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.Config;
import rife.config.exceptions.ConfigErrorException;
import rife.continuations.ContinuationManager;
import rife.engine.exceptions.EngineException;
import rife.tools.StringUtils;
import rife.workflow.Workflow;

import java.io.File;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * The main site of your web application, which is also the
 * top-level router.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Site extends Router {
    /**
     * A unique random number that's generated only once per web application deployment.
     *
     * @since 1.0
     */
    public final int RND = new Random().nextInt();

    final SecureRandom csrfRandom_ = new SecureRandom();

    final ContinuationManager continuationManager_ = new ContinuationManager(new EngineContinuationConfigRuntime(this));

    private Config config_ = new Config();

    /**
     * Looks up the information of the element that is responsible for handling
     * a certain URL and path info.
     *
     * @param url      the URL that should be looked up
     * @param pathInfo the path info that should be taken into account
     * @return the corresponding element information; or
     * <p>{@code null} if the URL and path info aren't registered in this site
     * @since 1.0
     */
    private Route resolveUrl(RequestMethod method, String url, String pathInfo)
    throws EngineException {
        if (null == url) throw new IllegalArgumentException("url can't be null;");

        if (url.isEmpty()) {
            url = "/";
        }

        if (null == pathInfo) {
            var routes = routes_.get(url);
            if (routes != null && !routes.isEmpty()) {
                for (var route : routes) {
                    if (routeHandlesMethod(route, method)) {
                        return route;
                    }
                }
            }
        }

        return resolvePathInfoUrl(method, url, pathInfo);
    }

    private static boolean routeHandlesMethod(Route route, RequestMethod method) {
        if (route.methods() == null) {
            return true;
        }
        for (var m : route.methods()) {
            if (m == method) {
                return true;
            }
        }
        return false;
    }

    private Route resolvePathInfoUrl(RequestMethod method, String url, String pathInfo)
    throws EngineException {
        var routes = pathInfoRoutes_.get(url);
        if (null == routes ||
            routes.isEmpty()) {
            return null;
        }

        // if a path info was provided, check the path info mappings for the first that matches
        if (pathInfo != null) {
            var path_info = StringUtils.stripFromFront(pathInfo, "/");
            for (var route : routes) {
                if (route.pathInfoHandling().type() == PathInfoType.MAP && routeHandlesMethod(route, method)) {
                    for (var mapping : route.pathInfoHandling().mappings()) {
                        var matcher = mapping.regexp().matcher(path_info);
                        if (matcher.matches()) {
                            return route;
                        }
                    }
                }
            }
        }

        // return the first route that handles the url and doesn't have  any path info mappings
        for (var route : routes) {
            if (route.pathInfoHandling().type() == PathInfoType.CAPTURE && routeHandlesMethod(route, method)) {
                return route;
            }
        }

        return null;
    }

    private Route resolveFallback(String url) {
        String best_match = null;
        if (url.isEmpty()) {
            url = "/";
        }

        for (var fallback_url : fallbackRoutes_.keySet()) {
            if (url.startsWith(fallback_url) &&
                (null == best_match || fallback_url.length() > best_match.length())) {
                best_match = fallback_url;
            }
        }

        if (best_match != null) {
            return fallbackRoutes_.get(best_match);
        } else {
            return null;
        }
    }

    private Route resolveRoute(RequestMethod method, String elementUrl, StringBuilder elementUrlBuffer) {
        Route route;
        elementUrlBuffer.setLength(0);
        elementUrlBuffer.append(elementUrl);
        var element_url_location = -1;
        String path_info = null;
        do {
            // if a slash was found in the url, it was stripped away
            // and thus the only urls that should match then are path info
            // urls
            if (element_url_location > -1) {
                path_info = elementUrl.substring(element_url_location);
            }
            route = resolveUrl(method, elementUrlBuffer.toString(), path_info);

            if (route != null) {
                break;
            }

            element_url_location = elementUrlBuffer.lastIndexOf("/");
            if (-1 == element_url_location) {
                break;
            }
            elementUrlBuffer.setLength(element_url_location);
        }
        while (true);

        return route;
    }

    /**
     * Looks for an element that corresponds to a particular request URL.
     * <p>
     * This method will determine the best element match by stepping up the path
     * segments. It will also look for fallback elements and figure out the
     * correct path info.
     * <p>
     *
     * @param elementUrl the URL that will be used to search for the element
     * @return an instance of {@code Route} when an element match
     * was found; or
     * <p>{@code null} if no suitable element could be found.
     * @since 1.0
     */
    RouteMatch findRouteForRequest(Request request, String elementUrl) {
        // obtain the element info that mapped to the requested path info
        var element_url_buffer = new StringBuilder();
        var element_path_info = "";
        var route = resolveRoute(request.getMethod(), elementUrl, element_url_buffer);
        // GET routes answer HEAD, only when no route at all handles HEAD itself
        if (null == route && request.getMethod() == RequestMethod.HEAD) {
            route = resolveRoute(RequestMethod.GET, elementUrl, element_url_buffer);
        }

        // no target element, get the fallback element
        if (null == route) {
            route = resolveFallback(elementUrl);
            if (null == route) {
                return null;
            }
        }
        // otherwise get the target element's path info
        else {
            // only accept path info if the element accepts it
            if (route.pathInfoHandling() == PathInfoHandling.NONE &&
                elementUrl.length() != element_url_buffer.length()) {
                // check for a fallback element
                route = resolveFallback(elementUrl);
                if (null == route) {
                    return null;
                }
            } else if (route.pathInfoHandling() != PathInfoHandling.NONE) {
                // construct the element path info
                element_path_info = elementUrl.substring(element_url_buffer.length());
                element_path_info = StringUtils.stripFromFront(element_path_info, "/");
            }
        }

        return new RouteMatch(route, element_path_info);
    }

    /**
     * Creates a new workflow instance with a default executor.
     * <p>Note that the site doesn't keep a reference to it, so make sure it's
     * not garbage collected unexpectedly.
     *
     * @return the new workflow instance
     * @since 1.0
     */
    public Workflow createWorkflow() {
        return new Workflow(properties_);
    }

    /**
     * Creates a new workflow instance with a provided executor.
     * <p>Note that the site doesn't keep a reference to it, so make sure it's
     * not garbage collected unexpectedly.
     *
     * @param executor the executor to use for running the work
     * @return the new workflow instance
     * @since 1.0
     */
    public Workflow createWorkflow(ExecutorService executor) {
        return new Workflow(executor, properties_);
    }

    /**
     * Returns this site's config instance.
     *
     * @return this site's config instance.
     * @since 1.6.0
     */
    public Config config() {
        return config_;
    }

    /**
     * Looks for a named resource in the classpath, parses it as an
     * XML {@link Config} file and sets it as this site's config
     * instance.
     *
     * @param name the name of the resource to parse
     * @throws EngineException when an error occurred during the parsing, or
     *                         if the resource couldn't be found
     * @since 1.6.0
     */
    public void loadConfig(String name) {
        try {
            config_ = Config.fromXmlResource(name, properties());
        } catch (ConfigErrorException e) {
            throw new EngineException(e);
        }
    }

    /**
     * Parses the provided file as an XML {@link Config} file and
     * sets it as this site's config instance.
     *
     * @param file the file to parse
     * @throws EngineException when an error occurred during the parsing, or
     *                         if the resource couldn't be found
     * @since 1.6.0
     */
    public void loadConfig(File file) {
        try {
            config_ = Config.fromXmlFile(file, properties());
        } catch (ConfigErrorException e) {
            throw new EngineException(e);
        }
    }
}
