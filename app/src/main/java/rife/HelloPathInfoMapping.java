/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;

public class HelloPathInfoMapping extends Site {
    public void setup() {
        get("/mapping", PathInfoHandling.MAP(
            m -> m.p("first"),
            m -> m.p("first").s().p("last")), HelloPerson.class);
    }

    public static void main(String[] args) {
        new Server().start(new HelloPathInfoMapping());
    }

    public static class HelloPerson implements Element {
        @Parameter String first;
        @Parameter String last;
        public void process(Context c) {
            c.addCookie(new CookieBuilder("name", "value"));
            c.print(first);
            if (last != null) {
                c.print(" " + last);
            }
        }
    }
}