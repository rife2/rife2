/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUrlBuilder {
    static class UrlSite extends Site {
        final Route item = get("/item", c -> {});
        final Route mapped = get("/mapped", PathInfoHandling.MAP(m -> m.p("id", "\\d+")), c -> {});
    }

    @Test
    void testWithoutContext() {
        var site = new UrlSite();
        assertEquals("https://example.com/item?id=3", new UrlBuilder("https://example.com/", site.item).param("id", "3").toString());
        assertEquals("https://example.com/mapped/42", new UrlBuilder("https://example.com/", site.mapped).param("id", "42").toString());
        assertEquals("https://example.com/item#top", new UrlBuilder("https://example.com/", site.item).fragment("top").toString());
    }
}
