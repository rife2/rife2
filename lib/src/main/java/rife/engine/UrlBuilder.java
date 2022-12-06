/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

import java.util.*;

public class UrlBuilder {
    private final Context context_;
    private final String rootUrl_;
    private final Route route_;
    private String pathInfo_;
    private final Map<String, String[]> parameters_ = new LinkedHashMap<>();
    private String fragment_;

    public UrlBuilder(Context context, Route route) {
        context_ = context;
        rootUrl_ = context.webappRootUrl();
        route_ = route;
    }

    public UrlBuilder(String rootUrl, Route route) {
        context_ = null;
        rootUrl_ = rootUrl;
        route_ = route;
    }

    public UrlBuilder pathInfo(String pathInfo) {
        pathInfo_ = StringUtils.stripFromFront(pathInfo, "/");
        return this;
    }

    public UrlBuilder param(String name, String value) {
        parameters_.put(name, new String[]{value});
        return this;
    }

    public UrlBuilder param(String name, String[] value) {
        parameters_.put(name, value);
        return this;
    }

    public UrlBuilder params(Map<String, String[]> parameters) {
        parameters_.putAll(parameters);
        return this;
    }

    public UrlBuilder fragment(String fragment) {
        fragment_ = StringUtils.stripFromFront(fragment, "#");
        return this;
    }

    public String toString() {
        final var url = new StringBuilder(rootUrl_);

        url.append(StringUtils.stripFromFront(route_.path(), "/"));

        var parameters = new LinkedHashMap<>(parameters_);

        if (pathInfo_ != null) {
            if (url.charAt(url.length() - 1) != '/') {
                url.append("/");
            }
            url.append(pathInfo_);
        } else if (route_.pathInfoHandling().type() == PathInfoType.MAP) {
            var mapping = route_.pathInfoHandling().mapping();
            if (parameters.keySet().containsAll(mapping.parameters())) {
                var parameters_it = mapping.parameters().iterator();

                var builder = new StringBuilder();
                String parameter_name;
                String[] parameter_value;
                for (var segment : mapping.segments()) {
                    if (segment.isRegexp()) {
                        if (!parameters_it.hasNext()) {
                            continue;
                        }

                        parameter_name = parameters_it.next();

                        // ensure that the parameter has at least one value
                        parameter_value = parameters.get(parameter_name);
                        if (null == parameter_value ||
                            parameter_value.length < 1) {
                            continue;
                        }

                        // ensure that the parameter value corresponds to the
                        // regexp pattern for it
                        var matcher = segment.pattern().matcher(parameter_value[0]);
                        if (!matcher.matches()) {
                            continue;
                        }

                        // add the url-encoded parameter value to the path info
                        builder.append(StringUtils.encodeUrl(parameter_value[0]));
                        parameters.remove(parameter_name);
                    } else {
                        builder.append(segment.text());
                    }
                }

                // append the new path info
                var path_info = builder.toString();
                if (!path_info.isEmpty()) {
                    if (path_info.charAt(0) != '/' && url.charAt(url.length() - 1) != '/') {
                        url.append("/");
                    }
                    url.append(path_info);
                }
            }
        }

        if (parameters.size() > 0) {
            var query_parameters = new StringBuilder("?");

            for (var parameter_entry : parameters.entrySet()) {
                var parameter_name = parameter_entry.getKey();
                var parameter_values = parameter_entry.getValue();
                if (null == parameter_values) {
                    continue;
                }

                if (query_parameters.length() > 1) {
                    query_parameters.append("&");
                }

                for (var i = 0; i < parameter_values.length; i++) {
                    query_parameters.append(StringUtils.encodeUrl(parameter_name));
                    query_parameters.append("=");
                    query_parameters.append(StringUtils.encodeUrl(parameter_values[i]));
                    if (i + 1 < parameter_values.length) {
                        query_parameters.append("&");
                    }
                }
            }

            url.append(query_parameters);
        }

        if (fragment_ != null) {
            url.append("#");
            url.append(StringUtils.encodeUrl(fragment_));
        }

        return url.toString();
    }
}
