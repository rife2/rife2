/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.Route;
import rife.engine.Site;
import rife.server.Server;

public class HelloTemplate extends Site {
    Route hello = get("/hello", c -> c.print("Hello World"));
    Route link = get("/link", c-> c.print(c.template("HelloTemplate")));

    public static void main(String[] args) {
        new Server().start(new HelloTemplate());
    }
}
