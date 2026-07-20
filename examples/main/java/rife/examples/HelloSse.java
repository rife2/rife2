/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.template.TemplateFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloSse extends Site {
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    final SseBroadcaster broadcaster = new SseBroadcaster();
    final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "sse-clock");
        thread.setDaemon(true);
        return thread;
    });

    Route clock = get("/clock", c -> c.sse(broadcaster));
    Route page = get("/", c -> {
        var t = c.template("HelloSse");
        t.setValueEncoded("time", TIME_FORMAT.format(LocalTime.now()));
        t.setBlock("clock_row", "clock_row");
        c.print(t);
    });

    public void setup() {
        ticker.scheduleAtFixedRate(() -> {
            var t = TemplateFactory.HTML.get("HelloSse");
            t.setValueEncoded("time", TIME_FORMAT.format(LocalTime.now()));
            broadcaster.send(new ServerSentEvent().name("clock").templateBlock(t, "clock_row"));
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void destroy() {
        ticker.shutdownNow();
        broadcaster.close();
    }

    public static void main(String[] args) {
        new Server().start(new HelloSse());
    }
}
