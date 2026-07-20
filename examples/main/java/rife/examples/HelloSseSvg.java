/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.template.TemplateFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloSseSvg extends Site {
    final SseBroadcaster broadcaster = new SseBroadcaster();
    final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "sse-svg-clock");
        thread.setDaemon(true);
        return thread;
    });

    Route clock = get("/clock", c -> c.sse(broadcaster));
    Route page = get("/", c -> {
        var t = c.template("HelloSseSvg");
        var svg = c.templateSvg("HelloSvg");
        HelloSvg.fillClock(svg);
        t.setValue("clock", svg.getContent());
        c.print(t);
    });

    public void setup() {
        // repaint the whole clock every second, htmx swaps the SVG in place
        ticker.scheduleAtFixedRate(() -> {
            var svg = TemplateFactory.SVG.get("HelloSvg");
            HelloSvg.fillClock(svg);
            broadcaster.send(new ServerSentEvent().name("clock").template(svg));
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void destroy() {
        ticker.shutdownNow();
        broadcaster.close();
    }

    public static void main(String[] args) {
        new Server().start(new HelloSseSvg());
    }
}
