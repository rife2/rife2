/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.engine.annotations.Cookie;
import rife.engine.annotations.FlowDirection;

public class HelloCookieFlow extends Site {
    public static class MyCounter implements Element {
        @Cookie(flow = FlowDirection.IN_OUT)
        int counter = 1;

        public void process(Context c) {
            c.print("counter " + (counter++));
        }
    }

    Route counter = get("/counter", MyCounter.class);

    public static void main(String[] args) {
        new Server().start(new HelloCookieFlow());
    }
}
