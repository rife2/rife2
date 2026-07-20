/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import org.junit.jupiter.api.Test;
import rife.database.Datasource;
import rife.engine.Route;
import rife.engine.ServerSentEvent;
import rife.engine.Site;
import rife.engine.SseBroadcaster;
import rife.template.TemplateFactory;
import rife.test.MockConversation;

import static org.junit.jupiter.api.Assertions.*;

public class TestSseGqmBridge {
    public static class Product {
        private int id_ = -1;
        private String name_ = null;

        public void setId(int id) {
            id_ = id;
        }

        public int getId() {
            return id_;
        }

        public void setName(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }

        public String toString() {
            return "Product:" + name_;
        }
    }

    static class EventsSite extends Site {
        final SseBroadcaster broadcaster = new SseBroadcaster();
        Route events = get("/events", c -> c.sse(broadcaster));
    }

    private static GenericQueryManager<Product> createManager(String db) {
        var datasource = new Datasource("org.h2.Driver", "jdbc:h2:mem:" + db, "sa", "", 5);
        return GenericQueryManagerFactory.instance(datasource, Product.class);
    }

    @Test
    void testDefaultConversion() {
        var site = new EventsSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");
        assertEquals(1, site.broadcaster.connectionCount());

        var manager = createManager("sse_gqm_default");
        manager.install();
        try {
            manager.addListener(new SseGqmBridge<>(site.broadcaster));

            var product = new Product();
            product.setName("ACME");
            var id = manager.save(product);

            product.setName("ACME2");
            manager.save(product);

            // restorations are reads and aren't broadcast
            assertNotNull(manager.restore(id));

            manager.delete(id);

            var events = response.getEvents();
            assertEquals(3, events.size());
            assertEquals("inserted", events.get(0).getName());
            assertEquals("Product:ACME", events.get(0).getData());
            assertEquals("updated", events.get(1).getName());
            assertEquals("Product:ACME2", events.get(1).getData());
            assertEquals("deleted", events.get(2).getName());
            assertEquals(String.valueOf(id), events.get(2).getData());
        } finally {
            manager.remove();
        }
    }

    @Test
    void testCustomConverters() {
        var site = new EventsSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        var manager = createManager("sse_gqm_custom");
        manager.install();
        try {
            manager.addListener(new SseGqmBridge<Product>(site.broadcaster)
                .onInserted(product -> {
                    var t = TemplateFactory.HTML.get("sse_blocks");
                    t.setValue("symbol", t.getEncoder().encode(product.getName()));
                    t.setValue("price", product.getId());
                    return new ServerSentEvent().name("product").templateBlock(t, "price_row");
                })
                .onUpdated(product -> null)
                .onDeleted(object_id -> null));

            var product = new Product();
            product.setName("ACME");
            var id = manager.save(product);

            product.setName("ACME2");
            manager.save(product);
            manager.delete(id);

            var events = response.getEvents();
            assertEquals(1, events.size());
            assertEquals("product", events.get(0).getName());
            assertEquals("<p>ACME: " + id + " <a href=\"http://localhost/events\">watch</a></p>", events.get(0).getData());
            assertEquals("ACME", events.get(0).getTemplate().getValue("symbol"));
        } finally {
            manager.remove();
        }
    }

    @Test
    void testJsonConversions() {
        var site = new EventsSite();
        var m = new MockConversation(site);
        var response = m.doRequest("/events");

        var manager = createManager("sse_gqm_json");
        manager.install();
        try {
            manager.addListener(SseGqmBridge.<Product>json(site.broadcaster));

            var product = new Product();
            product.setName("ACME");
            var id = manager.save(product);

            product.setName("ACME2");
            manager.save(product);
            manager.delete(id);

            var events = response.getEvents();
            assertEquals(3, events.size());
            assertEquals("inserted", events.get(0).getName());
            assertEquals(id, events.get(0).getDataAsJsonObject().getInt("id"));
            assertEquals("ACME", events.get(0).getDataAsJsonObject().getString("name"));
            assertEquals("ACME", events.get(0).getDataAsBean(Product.class).getName());
            assertEquals("updated", events.get(1).getName());
            assertEquals("ACME2", events.get(1).getDataAsJsonObject().getString("name"));
            assertEquals("deleted", events.get(2).getName());
            assertEquals(id, events.get(2).getDataAsJsonObject().getInt("id"));
        } finally {
            manager.remove();
        }
    }

    @Test
    void testInvalidArguments() {
        var broadcaster = new SseBroadcaster();
        assertThrows(IllegalArgumentException.class, () -> new SseGqmBridge<Product>(null));
        var bridge = new SseGqmBridge<Product>(broadcaster);
        assertThrows(IllegalArgumentException.class, () -> bridge.onInserted(null));
        assertThrows(IllegalArgumentException.class, () -> bridge.onUpdated(null));
        assertThrows(IllegalArgumentException.class, () -> bridge.onDeleted(null));
        assertThrows(IllegalArgumentException.class, () -> bridge.onError(null));
    }

    @Test
    void testDeliveryFailureDoesNotFailMutation() {
        var site = new EventsSite();
        var m = new MockConversation(site);
        m.doRequest("/events");

        var manager = createManager("sse_gqm_failure");
        manager.install();
        try {
            var errors = new java.util.ArrayList<Throwable>();
            manager.addListener(new SseGqmBridge<Product>(site.broadcaster)
                .onInserted(product -> {
                    throw new IllegalStateException("converter failure");
                })
                .onError(errors::add));

            // the database change has already happened when the bridge is
            // notified, so a delivery failure doesn't fail the mutation
            var product = new Product();
            product.setName("ACME");
            var id = manager.save(product);
            assertNotNull(manager.restore(id));

            assertEquals(1, errors.size());
            assertEquals("converter failure", errors.get(0).getMessage());
        } finally {
            manager.remove();
        }
    }
}
