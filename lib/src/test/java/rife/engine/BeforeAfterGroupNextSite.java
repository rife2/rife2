/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class BeforeAfterGroupNextSite extends Site {
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        group(new Router() {
            public void setup() {
                get("/three", c -> {
                    if (c.parameterBoolean("next3")) {
                        c.next();
                    }
                    c.print(c.route().path());
                });
                after(c -> {
                    if (c.parameterBoolean("next5")) {
                        c.next();
                    }
                    c.print("after1");
                }, c -> {
                    if (c.parameterBoolean("next6")) {
                        c.next();
                    }
                    c.print("after2");
                });
                get("/four", c -> {
                    if (c.parameterBoolean("next4")) {
                        c.next();
                    }
                    c.print(c.route().path());
                });
                before(c -> {
                    if (c.parameterBoolean("next1")) {
                        c.next();
                    }
                    c.print("before1");
                }, c -> {
                    if (c.parameterBoolean("next2")) {
                        c.next();
                    }
                    c.print("before2");
                });
            }
        });
        get("/two", c -> c.print(c.route().path()));
    }
}
