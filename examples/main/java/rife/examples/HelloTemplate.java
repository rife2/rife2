/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;

public class HelloTemplate extends Site {
    Route templateHello = get("/templateHello", c -> c.print("Hello World Template"));
    Route template = get("/template", c-> {
        var t = c.template("HelloTemplate");
        t.setValueEncoded("message", "Bonjour");
        c.print(t);
    });

    public static void main(String[] args) {
        new Server().start(new HelloTemplate());
    }
}
