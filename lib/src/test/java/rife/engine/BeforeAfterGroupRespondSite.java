/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class BeforeAfterGroupRespondSite extends Site {
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        group(new Router() {
            public void setup() {
                get("/three", c -> {
                    c.print(c.route().path());
                    if (c.parameterBoolean("respond3")) {
                        c.respond();
                    }
                });
                after(c -> {
                    c.print("after1");
                    if (c.parameterBoolean("respond5")) {
                        c.respond();
                    }
                }, c -> {
                    c.print("after2");
                    if (c.parameterBoolean("respond6")) {
                        c.respond();
                    }
                });
                get("/four", c -> {
                    c.print(c.route().path());
                    if (c.parameterBoolean("respond4")) {
                        c.respond();
                    }
                });
                before(c -> {
                    c.print("before1");
                    if (c.parameterBoolean("respond1")) {
                        c.respond();
                    }
                }, c -> {
                    c.print("before2");
                    if (c.parameterBoolean("respond2")) {
                        c.respond();
                    }
                });
            }
        });
        get("/two", c -> c.print(c.route().path()));
    }
}
