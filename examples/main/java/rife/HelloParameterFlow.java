/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.engine.*;
import rife.engine.annotations.ActiveSite;
import rife.engine.annotations.FlowDirection;
import rife.engine.annotations.Parameter;

public class HelloParameterFlow extends Site {
    public static class StartCounter implements Element {
        @ActiveSite HelloParameterFlow site;

        @Parameter(flow = FlowDirection.OUT)
        int counter;

        public StartCounter(int counter) { this.counter = counter; }

        public void process(Context c) {
            c.print("<a href='" + c.urlFor(site.count) + "'>start " + counter + "</a>");
        }
    }

    public static class ContinueCounter implements Element {
        @ActiveSite HelloParameterFlow site;

        @Parameter(flow = FlowDirection.IN_OUT)
        int counter;

        public void process(Context c) {
            c.print("counter " + (counter++) + "<br>");
            c.print("<a href='" + c.urlFor(site.count) + "'>add</a><br>");
            c.print("<a href='" + c.urlFor(site.start) + "'>restart 1</a><br>");
            c.print("<a href='" + c.urlFor(site.start10) + "'>restart 10</a>");
        }
    }

    Route start = get("/start", () -> new StartCounter(1));
    Route start10 = get("/start10", () -> new StartCounter(10));
    Route count = get("/count", ContinueCounter.class);

    public static void main(String[] args) {
        new Server().start(new HelloParameterFlow());
    }
}
