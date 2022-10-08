/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;
import rife.tools.StringUtils;

import java.util.*;

public class Site {
    record RouteMatch(Route route, String pathInfo) {
    }

    private final Map<String, List<Route>> routes_ = new HashMap<>();
    private final Map<String, List<Route>> pathInfoRoutes_ = new HashMap<>();

    public void setup() {
    }

    public Route get(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.GET));
    }

    public Route get(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.GET, pathInfo));
    }

    public Route get(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.GET, path));
    }

    public Route get(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.GET, path, pathInfo));
    }

    public Route get(String path, Element element) {
        return registerRoute(new RouteInstance(element, RequestMethod.GET, path));
    }

    public Route get(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(element, RequestMethod.GET, path, pathInfo));
    }

    public Route post(Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.POST));
    }

    public Route post(PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.POST, pathInfo));
    }

    public Route post(String path, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.POST, path));
    }

    public Route post(String path, PathInfoHandling pathInfo, Class<? extends Element> elementClass) {
        return registerRoute(new RouteClass(elementClass, RequestMethod.POST, path, pathInfo));
    }

    public Route post(String path, Element element) {
        return registerRoute(new RouteInstance(element, RequestMethod.POST, path));
    }

    public Route post(String path, PathInfoHandling pathInfo, Element element) {
        return registerRoute(new RouteInstance(element, RequestMethod.POST, path, pathInfo));
    }

    public Route registerRoute(Route route) {
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

    /**
     * Looks up the information of the element that is responsible for handling
     * a certain URL and path info.
     *
     * @param url      the URL that should be looked up
     * @param pathInfo the path info that should be taken into account
     * @return the corresponding element information; or
     * <p><code>null</code> if the URL and path info aren't registered in this site
     * @since 1.4
     */
    public Route resolveUrl(Request request, String url, String pathInfo)
    throws EngineException {
        if (null == url) throw new IllegalArgumentException("url can't be null;");

        if (0 == url.length()) {
            url = "/";
        }

        if (null == pathInfo) {
            List<Route> routes = routes_.get(url);
            if (routes != null && !routes.isEmpty()) {
                for (Route route : routes) {
                    if (request.getMethod() == route.method()) {
                        return route;
                    }
                }
            }

            if ('/' == url.charAt(url.length() - 1)) {
                String stripped_url = url.substring(0, url.length() - 1);
                // if the url contains a dot in the last part, it shouldn't be
                // seen as simulating a directory
                if (stripped_url.lastIndexOf('.') <= stripped_url.lastIndexOf('/')) {
                    if (routes != null && !routes.isEmpty()) {
                        for (Route route : routes) {
                            if (request.getMethod() == route.method()) {
                                return route;
                            }
                        }
                    }
                }
            }
        }

        return resolvePathInfoUrl(request, url, pathInfo);
    }

    private Route resolvePathInfoUrl(Request request, String url, String pathinfo)
    throws EngineException {
        List<Route> routes = pathInfoRoutes_.get(url);
        if (null == routes ||
            0 == routes.size()) {
            return null;
        }

        // TODO
//        // if a path info was provided, check the path info mappings
//        // for the first that matches
//        if (pathinfo != null)
//        {
//            for (Route route : routes)
//            {
//                if (element.hasPathInfoMappings() && request.getMethod() == route.method())
//                {
//                    for (PathInfoMapping mapping : element.getPathInfoMappings())
//                    {
//                        Matcher matcher = mapping.getRegexp().matcher(pathinfo);
//                        if (matcher.matches())
//                        {
//                            return element;
//                        }
//                    }
//                }
//            }
//        }

        // return the first route that handles the url and doesn't have
        // any path info mappings
        for (Route route : routes) {
            if (request.getMethod() == route.method()) {
                return route;
            }

//            if (!element.hasPathInfoMappings() ||
//                PathInfoMode.LOOSE.equals(element.getPathInfoMode()))
//            {
//            return route;
//            }
        }

        return null;
    }


    /**
     * Looks for an element that corresponds to a particular request URL.
     * <p>
     * This method will determine the best element match by stepping up the path
     * segments. It will also look for fallback elements, cater for trailing
     * slashes, and figure out the correct path info.
     * <p>
     *
     * @param elementUrl the URL that will be used to search for the element
     * @return an instance of <code>Route</code> when an element match
     * was found; or
     * <p><code>null</code> if no suitable element could be found.
     * @since 1.6
     */
    public RouteMatch findRouteForRequest(Request request, String elementUrl) {
        // obtain the element info that mapped to the requested path info
        Route route;
        StringBuilder element_url_buffer = new StringBuilder(elementUrl);
        int element_url_location = -1;
        String element_path_info = "";
        String path_info = null;
        do {
            // if a slash was found in the url, it was stripped away
            // and thus the only urls that should match then are path info
            // urls
            if (element_url_location > -1) {
                path_info = elementUrl.substring(element_url_location);
            }
            route = resolveUrl(request, element_url_buffer.toString(), path_info);

            if (route != null) {
                break;
            }

            element_url_location = element_url_buffer.lastIndexOf("/");
            if (-1 == element_url_location) {
                break;
            }
            element_url_buffer.setLength(element_url_location);
        }
        while (true);

        // no target element, get the fallback element
//        if (null == route) {
//            route = searchFallback(elementUrl);
//            if (null == route) {
//                return null;
//            }
//        }
//        // otherwise get the target element's path info
//        else {
//            // only accept pathinfo if the element accepts it
//            if (!route.isPathInfoUsed() &&
//                elementUrl.length() != element_url_buffer.length()) {
//                // check for a fallback element
//                route = searchFallback(elementUrl);
//                if (null == route) {
//                    return null;
//                }
//            } else if (route.isPathInfoUsed()) {
        if (route.pathInfoHandling() != PathInfoHandling.NONE) {
            // construct the element path info
            element_path_info = elementUrl.substring(element_url_buffer.length());
            element_path_info = StringUtils.stripFromFront(element_path_info, "/");
        }
//        }

        return new RouteMatch(route, element_path_info);
    }
}
