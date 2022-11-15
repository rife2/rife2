/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class BeforeAfterGroupsMultiLevelSite extends Site {
    public void setup() {
        get("/one", c -> c.print(c.route().path()));
        get("/two", c -> c.print(c.route().path()));
        group("/prefix1", new Router() {
            public void setup() {
                get("/three", c -> c.print(c.route().path()));
                before(c -> c.print("before1"));
                group("/prefix2", new Router() {
                    public void setup() {
                        after(c -> c.print("after1"), c -> c.print("after2"));
                        get("/five", c -> c.print(c.route().path()));
                        group(new Router() {
                            public void setup() {
                                before(c -> c.print("before3"), c -> c.print("before4"));
                                group("/prefix3", new Router() {
                                    public void setup() {
                                        after(c -> c.print("after3"));
                                        get("/eleven", c -> c.print(c.route().path()));
                                        after(c -> c.print("after4"));
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
                before(c -> c.print("before2"));
                get("/four", c -> c.print(c.route().path()));
            }
        });
        group(new Router() {
            public void setup() {
                get("/seven", c -> c.print(c.route().path()));
                before(c -> c.print("before5"));
                get("/eight", c -> c.print(c.route().path()));
            }
        });
    }
}
