/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class UrlBuilder {
    private final String rootUrl_;
    private final Route route_;
    private String pathInfo_;
    private final Map<String, String[]> parameters_ = new HashMap<>();
    private String fragment_;

    public UrlBuilder(String rootUrl, Route route) {
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
        final StringBuilder url = new StringBuilder(rootUrl_);

        url.append(StringUtils.stripFromFront(route_.path(), "/"));

        if (pathInfo_ != null) {
            if (url.charAt(url.length() - 1) != '/') {
                url.append("/");
            }
            url.append(pathInfo_);
        }

        if (parameters_.size() > 0) {
            StringBuilder query_parameters = new StringBuilder("?");

            for (var parameter_entry : parameters_.entrySet()) {
                String parameter_name = parameter_entry.getKey();
                String[] parameter_values = parameter_entry.getValue();
                if (null == parameter_values) {
                    continue;
                }

                if (query_parameters.length() > 1) {
                    query_parameters.append("&");
                }

                for (int i = 0; i < parameter_values.length; i++) {
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
