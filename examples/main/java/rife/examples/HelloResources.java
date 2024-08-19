/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.Route;
import rife.engine.Server;
import rife.engine.Site;
import rife.resources.MemoryResources;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderGroup;
import rife.template.TemplateFactory;

public class HelloResources extends Site {
    Route hello = get("/hello", c -> {
        var t = c.template("hello");
        t.setValue("name", "John");
        c.print(t);
    });
    Route bye = get("/bye", c -> {
        var t = c.template("bye");
        t.setValue("name", "Jim");
        c.print(t);
    });

    public void setup() {
        var resources = new MemoryResources();

        resources.addResource("hello.html", """
            Hello <em><!--v name/--></em><br>
            <a href="{{v route:bye/}}">Bye</a>""");

        resources.addResource("bye.html", """
            Bye <em><!--v name/--></em><br>
            <a href="{{v route:hello/}}">Hello</a>""");

        TemplateFactory.HTML.setResourceFinder(new ResourceFinderGroup()
            .add(ResourceFinderClasspath.instance())
            .add(resources));
    }

    public static void main(String[] args) {
        new Server().start(new HelloResources());
    }
}
