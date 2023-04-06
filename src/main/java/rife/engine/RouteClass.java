/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;

class RouteClass extends RouteAnnotated {
    RouteClass(Router router, Class<? extends Element> elementClass) {
        this(router, null, null, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, Class<? extends Element> elementClass) {
        this(router, methods, null, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, String path, Class<? extends Element> elementClass) {
        this(router, methods, path, null, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        this(router, methods, null, pathInfoHandling, elementClass);
    }

    RouteClass(Router router, RequestMethod[] methods, String path, PathInfoHandling pathInfoHandling, Class<? extends Element> elementClass) {
        super(router, methods, path, pathInfoHandling, elementClass);
    }

    @Override
    public Element obtainElementInstance(Context context) {
        try {
            return elementClass_.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }
}
