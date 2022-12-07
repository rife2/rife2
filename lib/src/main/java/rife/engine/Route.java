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

    String defaultElementId();

    String defaultElementPath();

    Element obtainElementInstance(Context context);

    void finalizeElementInstance(Element element, Context context);
}
