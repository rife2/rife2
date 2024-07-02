/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import rife.config.RifeConfig;
import rife.engine.Context;

import java.util.Calendar;
import java.util.List;

public class SimpleEntryProvider implements EntryProvider {
    private final Calendar calendar_;

    public SimpleEntryProvider() {
        calendar_ = RifeConfig.tools().getCalendarInstance(2023, Calendar.NOVEMBER, 24, 0, 0, 0);
        calendar_.set(Calendar.AM_PM, Calendar.AM);
    }

    public Feed getFeedDescriptor(Context c) {
        Feed feed = new Feed();
        feed
            .title("feed_title")
            .author("feed_author")
            .copyright("feed_copyright")
            .description("feed_description")
            .language("feed_language")
            .link("feed_link")
            .categories(List.of("feed_category1", "feed_category2"))
            .publishedDate(calendar_.getTime());

        return feed;
    }

    public void provideEntries(Context c, EntryProcessor processor) {
        for (int i = 0; i < 2; i++) {
            calendar_.set(Calendar.HOUR, i + 1);
            Entry entry = new Entry();
            entry
                .author("entry_author" + (i + 1))
                .content("\"entry\"\n<content>" + (i + 1))
                .link("entry_link" + (i + 1))
                .categories(List.of("entry_category" + (i + 1) + "_1", "entry_category" + (i + 1) + "_2"))
                .publishedDate(calendar_.getTime())
                .title("entry_title" + (i + 1));

            processor.setEntry(entry);
        }
    }
}
