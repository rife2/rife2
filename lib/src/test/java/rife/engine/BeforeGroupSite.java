/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class BeforeGroupSite extends Site {
    public void setup() {
        get("/one", c -> {
            c.print(c.route().path());
        });
        group(new Router() {
            public void setup() {
                get("/three", c -> {
                    c.print(c.route().path());
                });
                before(c -> {
                    c.print("before1");
                }, c -> {
                    c.print("before2");
                });
                get("/four", c -> {
                    c.print(c.route().path());
                });
            }
        });
        get("/two", c -> {
            c.print(c.route().path());
        });
    }
}
