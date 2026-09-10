/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;

import java.util.List;

public class HelloHtmx extends Site {
    static final List<String> LANGUAGES = List.of("Java", "Kotlin", "Scala", "Groovy", "Clojure");

    Route languages = get("/", c -> {
        var t = c.template("HelloHtmx");
        var q = c.parameter("q", "").toLowerCase();
        for (var name : LANGUAGES) {
            if (name.toLowerCase().contains(q)) {
                t.setValueEncoded("name", name);
                t.appendBlock("rows", "row");
            }
        }
        // a browser gets the whole page, htmx gets just the "list" block
        c.printHtmxFragment(t, "list");
    });

    public static void main(String[] args) {
        new Server().start(new HelloHtmx());
    }
}
