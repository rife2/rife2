/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public interface Route {
    RequestMethod method();

    String path();

    PathInfoHandling pathInfoHandling();

    Element getElementInstance(Context context);

    String getDefaultElementId();

    String getDefaultElementPath();
}
