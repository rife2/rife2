/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.exceptions.EngineException;

public record RouteInstance(RequestMethod method, String path, Element element) implements Route {
    @Override
    public void process(Context context) throws EngineException {
        element.process(context);
    }
}
