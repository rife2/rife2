/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import rife.engine.Route;
import rife.engine.SseBroadcaster;
import rife.engine.ServerSentEvent;
import rife.engine.Site;
import rife.test.MockConversation;
import rifeworkflowtests.CountdownWork;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSseWorkflowBridge {
    enum TestTypes {
        START, TICK, DONE, IGNORED
    }

    private static MockConversation createSseConversation(SseBroadcaster broadcaster) {
        return new MockConversation(new Site() {
            public void setup() {
                get("/stream", c -> c.sse(broadcaster));
            }
        });
    }

    @Test
    void testDefaultConversion() {
        var broadcaster = new SseBroadcaster();
        var m = createSseConversation(broadcaster);
        var response = m.doRequest("/stream");
        assertEquals(1, broadcaster.connectionCount());

        var workflow = new Workflow();
        workflow.addListener(new SseWorkflowBridge(broadcaster));

        workflow.trigger(TestTypes.TICK, 42);
        workflow.inform(TestTypes.DONE);

        var events = response.getEvents();
        assertEquals(2, events.size());
        assertEquals("TICK", events.get(0).getName());
        assertEquals("42", events.get(0).getData());
        assertEquals("DONE", events.get(1).getName());
        assertNull(events.get(1).getData());
    }

    public static class Progress {
        private String step_ = null;
        private int percent_ = 0;

        public void setStep(String step) {
            step_ = step;
        }

        public String getStep() {
            return step_;
        }

        public void setPercent(int percent) {
            percent_ = percent;
        }

        public int getPercent() {
            return percent_;
        }
    }

    @Test
    void testJsonConversion() {
        var broadcaster = new SseBroadcaster();
        var m = createSseConversation(broadcaster);
        var response = m.doRequest("/stream");

        var workflow = new Workflow();
        workflow.addListener(SseWorkflowBridge.json(broadcaster));

        var progress = new Progress();
        progress.setStep("packaging");
        progress.setPercent(80);
        workflow.trigger(TestTypes.TICK, progress);
        workflow.inform(TestTypes.DONE);

        var events = response.getEvents();
        assertEquals(2, events.size());
        assertEquals("TICK", events.get(0).getName());
        assertEquals("packaging", events.get(0).getDataAsJsonObject().getString("step"));
        assertEquals(80, events.get(0).getDataAsJsonObject().getInt("percent"));
        assertEquals(80, events.get(0).getDataAsBean(Progress.class).getPercent());
        assertEquals("DONE", events.get(1).getName());
        assertNull(events.get(1).getData());
    }

    @Test
    void testCustomConverter() {
        var broadcaster = new SseBroadcaster();
        var m = createSseConversation(broadcaster);
        var response = m.doRequest("/stream");

        var workflow = new Workflow();
        workflow.addListener(new SseWorkflowBridge(broadcaster, event -> {
            if (event.getType() == TestTypes.IGNORED) {
                return null;
            }
            return new ServerSentEvent()
                .name("workflow")
                .data(event.getType() + ":" + event.getData());
        }));

        workflow.trigger(TestTypes.IGNORED, "hidden");
        workflow.trigger(TestTypes.TICK, 7);

        var events = response.getEvents();
        assertEquals(1, events.size());
        assertEquals("workflow", events.get(0).getName());
        assertEquals("TICK:7", events.get(0).getData());
    }

    static class BlockConverterSite extends Site {
        final SseBroadcaster broadcaster = new SseBroadcaster();
        Route events = get("/events", c -> c.sse(broadcaster));
    }

    @Test
    void testTemplateBlockConverter() {
        var site = new BlockConverterSite();
        var broadcaster = site.broadcaster;
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        var workflow = new Workflow();
        workflow.addListener(new SseWorkflowBridge(broadcaster, event -> {
            var t = rife.template.TemplateFactory.HTML.get("sse_blocks");
            t.setValue("symbol", "TICK");
            t.setValue("price", event.getData());
            return new ServerSentEvent().name("price").templateBlock(t, "price_row");
        }));

        workflow.trigger(TestTypes.TICK, 42);

        var events = response.getEvents();
        assertEquals(1, events.size());
        assertEquals("price", events.get(0).getName());
        assertEquals("<p>TICK: 42 <a href=\"http://localhost/events\">watch</a></p>", events.get(0).getData());
        assertEquals("42", events.get(0).getTemplate().getValue("price"));
    }

    // the Work implementation deliberately lives in the rifeworkflowtests
    // package, classes inside rife.* are excluded from bytecode instrumentation
    // by the continuations agent
    @Test
    @Timeout(60)
    void testWorkEventsBroadcast()
    throws Throwable {
        var broadcaster = new SseBroadcaster();
        var m = createSseConversation(broadcaster);
        var response = m.doRequest("/stream");

        var workflow = new Workflow();
        workflow.addListener(new SseWorkflowBridge(broadcaster));

        workflow.start(new CountdownWork());
        workflow.waitForPausedWork();
        workflow.trigger(CountdownWork.Types.START, 3);
        workflow.waitForNoWork();

        var events = response.getEvents();
        assertEquals(5, events.size());
        assertEquals("START", events.get(0).getName());
        assertEquals("3", events.get(0).getData());
        assertEquals("TICK", events.get(1).getName());
        assertEquals("3", events.get(1).getData());
        assertEquals("TICK", events.get(2).getName());
        assertEquals("2", events.get(2).getData());
        assertEquals("TICK", events.get(3).getName());
        assertEquals("1", events.get(3).getData());
        assertEquals("DONE", events.get(4).getName());
        assertNull(events.get(4).getData());
    }

    @Test
    void testInvalidArguments() {
        var broadcaster = new SseBroadcaster();
        assertThrows(IllegalArgumentException.class, () -> new SseWorkflowBridge(null));
        assertThrows(IllegalArgumentException.class, () -> new SseWorkflowBridge(broadcaster, null));
        assertThrows(IllegalArgumentException.class, () -> new SseWorkflowBridge(null, event -> null));
    }
}
