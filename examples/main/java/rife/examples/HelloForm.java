/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.engine.annotations.Parameter;
import rife.engine.elements.CsrfProtected;

import static rife.engine.RequestMethod.GET;

public class HelloForm extends Site {
    public static class MyForm implements Element {
        @Parameter String name;

        public void process(Context c) {
            var t = c.template("HelloForm");

            if (c.method() == GET) {
                // the route:inputs: tag adds the CSRF token automatically,
                // since the form submits to a route that CsrfProtected guards
                t.setBlock("content", "form");
            } else {
                t.setValueEncoded("name", name);
                t.setBlock("content", "greeting");
            }

            c.print(t);
        }
    }

    Route form = getPost("/form", MyForm::new);

    public void setup() {
        // the submission of the form is verified, a refused request gets a
        // 403 straight from the element
        before(new CsrfProtected());
    }

    public static void main(String[] args) {
        new Server().start(new HelloForm());
    }
}
