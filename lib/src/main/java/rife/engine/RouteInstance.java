/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.tools.StringUtils;

public record RouteInstance(Element element, RequestMethod method, String path) implements Route {
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
