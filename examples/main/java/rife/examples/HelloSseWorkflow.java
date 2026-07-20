/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.engine.*;
import rife.template.TemplateFactory;
import rife.workflow.SseWorkflowBridge;
import rife.workflow.Work;
import rife.workflow.Workflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static rife.examples.HelloSseWorkflow.LaunchType.*;

public class HelloSseWorkflow extends Site {
    enum LaunchType {
        LAUNCH, TICK, LIFTOFF
    }

    final SseBroadcaster broadcaster = new SseBroadcaster();
    final ExecutorService workExecutor = Executors.newCachedThreadPool();
    final Workflow workflow = createWorkflow(workExecutor);

    Route events = get("/events", c -> c.sse(broadcaster));
    Route launch = post("/launch", c -> {
        workflow.trigger(LAUNCH);
        c.print("launch requested");
    });
    Route page = get("/", c -> {
        var t = c.template("HelloSseWorkflow");
        t.setValueEncoded("status", "Standing by");
        t.setBlock("status_row", "status_row");
        c.print(t);
    });

    public class CountdownWork implements Work {
        public void execute(Workflow workflow) {
            while (true) {
                pauseForEvent(LAUNCH);
                for (var i = 5; i > 0; i--) {
                    workflow.trigger(TICK, i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                workflow.trigger(LIFTOFF);
            }
        }
    }

    public void setup() {
        workflow.start(new CountdownWork());
        workflow.addListener(new SseWorkflowBridge(broadcaster, event -> {
            var t = TemplateFactory.HTML.get("HelloSseWorkflow");
            t.setValueEncoded("status", switch ((LaunchType) event.getType()) {
                case LAUNCH -> "Launch sequence initiated";
                case TICK -> "T-minus " + event.getData();
                case LIFTOFF -> "Liftoff!";
            });
            return new ServerSentEvent().name("countdown").templateBlock(t, "status_row");
        }));
    }

    public void destroy() {
        workExecutor.shutdownNow();
        broadcaster.close();
    }

    public static void main(String[] args) {
        new Server().start(new HelloSseWorkflow());
    }
}
