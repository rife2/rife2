/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;

import java.util.HashMap;
import java.util.Map;

public abstract class Site {
    private final Map<String, Route> routes_ = new HashMap<>();

    public abstract void setup();

    public Route get(String path, Class<? extends Element> element) {
        var route = new RouteClass(RequestMethod.GET, path, element);
        routes_.put(path, route);
        return route;
    }

    public Route get(String path, Element element) {
        var route = new RouteInstance(RequestMethod.GET, path, element);
        routes_.put(path, route);
        return route;
    }

    public boolean process(String gateUrl, String elementUrl, Request request, Response response) {
        // ensure a valid element url
        if (null == elementUrl ||
            0 == elementUrl.length()) {
            elementUrl = "/";
        }

        // strip away the optional path parameters
        int path_parameters_index = elementUrl.indexOf(";");
        if (path_parameters_index != -1) {
            elementUrl = elementUrl.substring(0, path_parameters_index);
        }

        // Set up the element request and process it.
        Route route = findRouteForRequest(request, elementUrl);

        // If no element was found, don't continue executing the gate logic.
        // This could allow a next filter in the chain to be executed.
        if (null == route) {
            return false;
        }

        var context = new Context(request, response);
        route.process(context);
        response.close();

        return true;
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

        Route route = null;

        if (null == pathInfo) {
            route = routes_.get(url);
            if (route != null && request.getMethod() == route.method()) {
                return route;
            }

            if (url.length() > 0 &&
                '/' == url.charAt(url.length() - 1)) {
                String stripped_url = url.substring(0, url.length() - 1);
                // if the url contains a dot in the last part, it shouldn't be
                // seen as simulating a directory
                if (stripped_url.lastIndexOf('.') <= stripped_url.lastIndexOf('/')) {
                    route = routes_.get(stripped_url);
                    if (route != null) {
                        return route;
                    }
                }
            }
        }

        return route;
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
    public Route findRouteForRequest(Request request, String elementUrl) {
        // obtain the element info that mapped to the requested path info
        Route route = null;
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
//                // construct the element path info
//                element_path_info = elementUrl.substring(element_url_buffer.length());
//                // always ensure that the path info starts with a slash
//                // this can not be present if the concerned element is
//                // an arrival for instance
//                if (!element_path_info.startsWith("/")) {
//                    element_path_info = "/" + element_path_info;
//                }
//            }
//        }

        return route;
    }
}
