/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public interface Route {
    Router router();

    RequestMethod method();

    String path();

    PathInfoHandling pathInfoHandling();

    default boolean handlesMethod(RequestMethod method) {
        return method() == null || method() == method;
    }

    Element getElementInstance(Context context);

    void finalizeElementInstance(Element element, Context context);

    String getDefaultElementId();

    String getDefaultElementPath();
}
