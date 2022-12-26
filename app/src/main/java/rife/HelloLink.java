/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloLink extends Site {
    Route target = get("/target", c -> c.print("Hello World Link"));
    Route link = get("/link", c-> c.print("<a href='" + c.urlFor(target) + "'>Hello</a>"));

    public static void main(String[] args) {
        new Server().start(new HelloLink());
    }
}
