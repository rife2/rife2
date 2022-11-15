/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class GroupExceptionSite extends Site {
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        get("/two", c -> {
            throw new RuntimeException(c.route().path());
        });
        exception(c -> c.print("1: " + c.engineException()));
        group(new Router() {
            public void setup() {
                get("/three", c -> c.print(c.route().path()));
                group("/prefix2", new Router() {
                    public void setup() {
                        get("/five", c -> {
                            throw new RuntimeException(c.route().path());
                        });
                        exception(c -> c.print("2: " + c.engineException()));
                        group(new Router() {
                            public void setup() {
                                get("/seven", c -> c.print(c.route().path()));
                                get("/eight", c -> {
                                    throw new RuntimeException(c.route().path());
                                });
                                exception(c -> c.print("3: " + c.engineException()));
                            }
                        });
                        get("/six", c -> c.print(c.route().path()));
                    }
                });
                get("/four", c -> {
                    throw new RuntimeException(c.route().path());
                });
            }
        });
    }
}
