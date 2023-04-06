/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine.exceptions;

import rife.engine.Element;
import rife.engine.Route;

import java.io.Serial;

public class AnnotatedElementInstanceFieldException extends EngineException {
    @Serial
    private static final long serialVersionUID = -4207383096387239120L;

    private Route route_ = null;
    private Element element_ = null;
    private String field_ = null;

    public AnnotatedElementInstanceFieldException(Route route, Element element, String field) {
        super("The route '" + route.path() + "' uses an instance-based element '" + element.getClass().getName() + "' with an annotated field '" + field + "'. Use a class-based element for this route instead, otherwise the same field will be shared across multiple requests.");

        route_ = route;
        element_ = element;
        field_ = field;
    }

    public Route getRoute() {
        return route_;
    }

    public Element getElement() {
        return element_;
    }

    public String getField() {
        return field_;
    }
}
