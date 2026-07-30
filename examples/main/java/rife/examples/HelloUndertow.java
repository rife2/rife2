/*
 * Copyright 2026 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.Site;
import rife.engine.UndertowServer;

public class HelloUndertow extends Site {
    public static void main(String[] args) {
        new UndertowServer().start(new HelloUndertow());
    }

    public void setup() {
        get("/hello", c -> c.print("Hello Undertow"));
    }
}
