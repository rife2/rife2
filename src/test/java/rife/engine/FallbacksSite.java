/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class FallbacksSite extends Site {
    @Override
    public void setup() {
        fallback(c -> c.print("fallback1"));
        get("/one", c -> c.print(c.route().path()));
        get("/two", PathInfoHandling.CAPTURE, c -> c.print(c.route().path()));
        group("/prefix1", new Router() {
            @Override
            public void setup() {
                get("/three", c -> c.print(c.route().path()));
                group("/prefix2", new Router() {
                    @Override
                    public void setup() {
                        get("/four", c -> c.print(c.route().path()));
                        fallback(c -> c.print("fallback2"));
                        group(new Router() {
                            @Override
                            public void setup() {
                                fallback(c -> c.print("fallback3"));
                                group("/prefix3", new Router() {
                                    @Override
                                    public void setup() {
                                        fallback(c -> c.print("fallback4"));
                                        get("/five", c -> c.print(c.route().path()));
                                        get("/five", PathInfoHandling.CAPTURE, c -> c.print(c.route().path()));
                                    }
                                });
                                get("/six", c -> c.print(c.route().path()));
                            }
                        });
                    }
                });
            }
        });
        group(new Router() {
            @Override
            public void setup() {
                get("/seven", c -> c.print(c.route().path()));
            }
        });
    }
}
