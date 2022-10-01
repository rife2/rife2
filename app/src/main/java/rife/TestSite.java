/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.Context;
import rife.engine.Element;
import rife.engine.Site;
import rife.engine.annotations.Parameter;
import rife.server.Server;

public class TestSite extends Site {
    public static void main(String[] args) {
        new Server().port(4242).start(new TestSite());
    }

    public void setup() {
        get("/inlined", (Context context) -> {
            context.print("test");
        });
        get("/parameters", Checkout.class);
    }

    public static class Checkout implements Element {
        @Parameter String one;
        @Parameter int two;

        @Override
        public void process(Context context) {
            context.print("test2 " + one + " " + two);
        }
    }
}
