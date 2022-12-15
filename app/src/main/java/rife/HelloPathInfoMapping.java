/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloPathInfoMapping extends Site {
    public void setup() {
        get("/hello",
            PathInfoHandling.MAP(m -> m.p("first").s().p("last")),
            c -> c.print(c.parameter("first") + " " + c.parameter("last")));
    }

    public static void main(String[] args) {
        new Server().start(new HelloPathInfoMapping());
    }
}