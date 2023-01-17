/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;

public class HelloFormContinuations extends Site {
    public static class Form implements Element {
        @Parameter String name;
        public void process(Context c) {
            var t = c.template("HelloForm");
            t.setBlock("content", "form");
            c.print(t);

            c.pause();

            t.setValueEncoded("name", name);
            t.setBlock("content", "greeting");
            c.print(t);
        }
    }
    Route form = getPost("/form", Form.class);

    public static void main(String[] args) {
        new Server().start(new HelloFormContinuations());
    }
}
