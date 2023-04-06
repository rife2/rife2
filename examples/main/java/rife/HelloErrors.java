/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.Server;
import rife.engine.Site;

public class HelloErrors extends Site {
    public void setup() {
        get("/error", c -> { throw new Exception("the error"); });
        exception(c -> c.print("Oh no: " + c.engineException().getCause().getMessage()));
        fallback(c -> c.print("It's not here!"));
    }

    public static void main(String[] args) {
        new Server().start(new HelloErrors());
    }
}
