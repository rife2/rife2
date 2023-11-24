/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import rife.config.RifeConfig;
import rife.engine.Context;

import java.util.Calendar;
import java.util.LinkedHashMap;

public class NamespacesEntryProvider implements EntryProvider {
    private final Calendar calendar_;

    public NamespacesEntryProvider() {
        calendar_ = Calendar.getInstance();
        calendar_.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
        calendar_.set(2023, Calendar.NOVEMBER, 24, 0, 0, 0);
        calendar_.set(Calendar.AM_PM, Calendar.AM);
    }

    public Feed getFeedDescriptor(Context c) {
        Feed feed = new Feed();
        feed
            .title("feed_title_namespace")
            .author("feed_author_namespace")
            .copyright("feed_copyright_namespace")
            .description("feed_description_namespace")
            .language("feed_language_namespace")
            .link("feed_link_namespace")
            .publishedDate(calendar_.getTime())
            .namespaces(new LinkedHashMap<>() {{
                put("doap", "http://usefulinc.com/ns/doap#");
                put("foaf", "http://xmlns.com/foaf/0.1/");
            }});

        return feed;
    }

    public void provideEntries(Context c, EntryProcessor processor) {
        for (int i = 0; i < 2; i++) {
            calendar_.set(Calendar.HOUR, i + 1);
            Entry entry = new Entry();
            entry
                .author("entry_author_namespace" + (i + 1))
                .content("<doap:Project>entry_content_namespace" + (i + 1) + "</doap:Project>")
                .link("entry_link_namespace" + (i + 1))
                .publishedDate(calendar_.getTime())
                .title("entry_title_namespace" + (i + 1))
                .type("application/rdf+xml")
                .escaped(false);

            processor.setEntry(entry);
        }
    }
}
