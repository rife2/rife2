/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
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
        new TomcatServer().addWebapp("examples/test/resources/tomcat-webapp").start();
    }
}
