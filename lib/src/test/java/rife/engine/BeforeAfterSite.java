/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

public class BeforeAfterSite extends Site {
    public void setup() {
        after(c -> c.print("after1"), c -> c.print("after2"));
        get("/one", c -> c.print(c.route().path()));
        before(c -> c.print("before1"), c -> c.print("before2"));
        get("/two", c -> c.print(c.route().path()));
    }
}
