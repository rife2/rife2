/*
 * Copyright 2023 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.Site;
import rife.engine.TomcatServer;

public class HelloTomcat extends Site {
    public void setup() {
        get("/hello", c -> c.print("Hello Tomcat"));
    }

    public static void main(String[] args) {
        new TomcatServer().start(new HelloTomcat());
    }
}
