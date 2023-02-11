/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;

import java.util.function.Supplier;

class RouteSupplier extends RouteAnnotated {
    private final Supplier<? extends Element> elementSupplier_;

    RouteSupplier(Router router, Supplier<? extends Element> elementSupplier) {
        this(router, null, null, null, elementSupplier);
    }

    RouteSupplier(Router router, RequestMethod[] methods, Supplier<? extends Element> elementSupplier) {
        this(router, methods, null, null, elementSupplier);
    }

    RouteSupplier(Router router, RequestMethod[] methods, String path, Supplier<? extends Element> elementSupplier) {
        this(router, methods, path, null, elementSupplier);
    }

    RouteSupplier(Router router, RequestMethod[] methods, PathInfoHandling pathInfoHandling, Supplier<? extends Element> elementSupplier) {
        this(router, methods, null, pathInfoHandling, elementSupplier);
    }

    RouteSupplier(Router router, RequestMethod[] methods, String path, PathInfoHandling pathInfoHandling, Supplier<? extends Element> elementSupplier) {
        super(router, methods, path, pathInfoHandling, elementSupplier.get().getClass());
        elementSupplier_ = elementSupplier;
    }

    @Override
    public Element obtainElementInstance(Context context) {
        try {
            return elementSupplier_.get();
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }
}
