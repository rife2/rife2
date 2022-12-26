/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloGroup extends Site {
    Route welcome = get("/welcome", c -> c.print("Hello World"));
    Router group = group("/group", new Router() {
        public void setup() {
            before(c -> c.print("before "));
            get("/hello", c -> c.print("hello inside"));
            get("/bonjour", c -> c.print("bonjour inside"));
            after(c -> c.print(" after"));
        }
    });

    public static void main(String[] args) {
        new Server().start(new HelloGroup());
    }
}