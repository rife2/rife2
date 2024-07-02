/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.ioc.HierarchicalProperties;

public class TestServerRunner implements AutoCloseable {
    final Server server_ = new Server()
        .port(8181)
        .host("localhost")
        .minThreads(1)
        .maxThreads(4)
        .idleTimeout(4000);

    public TestServerRunner(Site site) {
        server_.start(site);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public HierarchicalProperties properties() {
        return server_.properties();
    }

    public void close() {
        server_.stop();
    }
}
