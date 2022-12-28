/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.Parameter;

public class HelloFormContinuation extends Site {
    public static class MyForm implements Element {
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
    Route form = route("/form", MyForm.class);

    public static void main(String[] args) {
        new Server().start(new HelloFormContinuation());
    }
}
