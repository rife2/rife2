/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;

public class HelloForm extends Site {
    Route form = route("/form", c -> {
        var t = c.template("HelloForm");
        switch (c.method()) {
            case GET -> t.setBlock("content", "form");
            case POST -> t.setBlock("content", "text");
        }
        c.print(t);
    });

    public static void main(String[] args) {
        new Server().start(new HelloForm());
    }
}
