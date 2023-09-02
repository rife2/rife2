/*
 * Copyright 2023 Erik C. Thauvin (https://erik.thauvin.net/)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.ioc.HierarchicalProperties;

public class TestTomcatRunner implements AutoCloseable {
    final TomcatServer server_ = new TomcatServer().hostname("localhost").port(8282);

    public TestTomcatRunner(Site site) {
        server_.start(site);
    }

    public HierarchicalProperties properties() {
        return server_.properties();
    }

    @Override
    public void close() {
        server_.stop();
    }
}
