/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.nativeapp;

import rife.engine.Site;
import rife.ioc.HierarchicalProperties;

// a site that's made with what the app told it
public class ProvidedSite implements SiteProvider {
    public Site site(HierarchicalProperties properties) {
        var data = properties.getValueString(NativeApp.DATA_PROPERTY);
        return new Site() {
            public void setup() {
                get("/hello", c -> c.print("provided with " + data));
            }
        };
    }
}
