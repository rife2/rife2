/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

record RouteInstance(Router router, RequestMethod method, String path, PathInfoHandling pathInfoHandling, Element element) implements Route {
    public RouteInstance(Router router, Element element) {
        this(router, null, null, element);
    }

    public RouteInstance(Router router, RequestMethod method, String path, Element element) {
        this(router, method, path, PathInfoHandling.NONE, element);
    }

    @Override
    public Element getElementInstance(Context context) {
        return element;
    }

    @Override
    public String getDefaultElementId() {
        return StringUtils.stripFromFront(path, "/");
    }

    @Override
    public String getDefaultElementPath() {
        return path;
    }
}
