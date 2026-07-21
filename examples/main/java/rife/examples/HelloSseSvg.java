/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.template.Template;
import rife.template.TemplateFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;

public class HelloSseSvg extends Site {
    static final int MAX_SPEED = 64;

    final SseBroadcaster broadcaster = new SseBroadcaster();
    final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(runnable -> {
        var thread = new Thread(runnable, "sse-svg-clock");
        thread.setDaemon(true);
        return thread;
    });

    // the time that every viewer sees is owned by the server, it only
    // matches the wall clock until somebody changes the speed
    long displayedTime = System.currentTimeMillis();
    int speed = 1;

    Route clock = get("/clock", c -> c.sse(broadcaster));
    Route faster = post("/faster", c -> changeSpeed(s -> s == 0 ? 1 : Math.max(-MAX_SPEED, Math.min(MAX_SPEED, s * 2))));
    Route slower = post("/slower", c -> changeSpeed(s -> s / 2));
    Route backwards = post("/backwards", c -> changeSpeed(s -> s == 0 ? -1 : -s));
    Route stop = post("/stop", c -> changeSpeed(s -> 0));
    Route now = post("/now", c -> {
        synchronized (this) {
            displayedTime = System.currentTimeMillis();
        }
        changeSpeed(s -> 1);
    });
    Route page = get("/", c -> {
        var t = c.template("HelloSseSvg");
        t.setValue("clock", renderClock());
        fillSpeed(t);
        t.setBlock("speed_row", "speed_row");
        c.print(t);
    });

    synchronized String renderClock() {
        var svg = TemplateFactory.SVG.get("HelloSvg");
        HelloSvg.fillClock(svg, displayedTime);
        return svg.getContent();
    }

    synchronized void fillSpeed(Template t) {
        if (speed == 0) {
            t.setValue("speed", "stopped");
        } else if (speed == 1) {
            t.setValue("speed", "real time");
        } else if (speed < 0) {
            t.setValue("speed", Math.abs(speed) + "x backwards");
        } else {
            t.setValue("speed", speed + "x faster");
        }
    }

    void changeSpeed(IntUnaryOperator change) {
        synchronized (this) {
            speed = change.applyAsInt(speed);
        }
        // every viewer sees the change immediately, no matter which browser
        // or device it was made from
        broadcast();
    }

    void broadcast() {
        var svg = TemplateFactory.SVG.get("HelloSvg");
        synchronized (this) {
            HelloSvg.fillClock(svg, displayedTime);
        }
        broadcaster.send(new ServerSentEvent().name("clock").template(svg));

        var t = TemplateFactory.HTML.get("HelloSseSvg");
        fillSpeed(t);
        broadcaster.send(new ServerSentEvent().name("speed").templateBlock(t, "speed_row"));
    }

    public void setup() {
        ticker.scheduleAtFixedRate(() -> {
            synchronized (this) {
                displayedTime += 1000L * speed;
            }
            broadcast();
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
