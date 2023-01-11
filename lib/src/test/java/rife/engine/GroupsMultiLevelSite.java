/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class GroupsMultiLevelSite extends Site {
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        get("/two", c -> c.print(c.route().path()));
        group("/prefix1", new Router() {
            public void setup() {
                get("/three", c -> c.print(c.route().path()));
                group("/prefix2", new Router() {
                    public void setup() {
                        get("/five", c -> c.print(c.route().path()));
                        group(new Router() {
                            public void setup() {
                                group("/prefix3", new Router() {
                                    public void setup() {
                                        get("/eleven", c -> c.print(c.route().path()));
                                        get("/twelve", c -> c.print(c.route().path()));
                                    }
                                });
                                get("/nine", c -> c.print(c.route().path()));
                                get("/ten", c -> c.print(c.route().path()));
                            }
                        });
                        get("/six", c -> c.print(c.route().path()));
                    }
                });
                get("/four", c -> c.print(c.route().path()));
            }
        });
        group(new Router() {
            public void setup() {
                get("/seven", c -> c.print(c.route().path()));
                get("/eight", c -> c.print(c.route().path()));
            }
        });
    }
}
