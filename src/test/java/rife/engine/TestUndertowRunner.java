/*
 * Copyright 2026 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.ioc.HierarchicalProperties;

public class TestUndertowRunner implements AutoCloseable {
    final UndertowServer server_ = new UndertowServer().host("localhost").port(8888);

    public TestUndertowRunner(Site site) {
        server_.start(site);
    }

    public HierarchicalProperties properties() {
        return server_.properties();
    }

    public void close() {
        server_.stop();
    }
}
