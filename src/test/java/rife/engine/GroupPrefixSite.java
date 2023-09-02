/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class GroupPrefixSite extends Site {
    @Override
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        get("/two", c -> c.print(c.route().path()));
        group("/group", new Router() {
            @Override
            public void setup() {
                get("/three", c -> c.print(c.route().path()));
                get("/four", c -> c.print(c.route().path()));
            }
        });
    }
}
