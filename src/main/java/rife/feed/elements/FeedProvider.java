/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed.elements;

import rife.config.RifeConfig;
import rife.engine.Context;
import rife.engine.Element;
import rife.feed.Entry;
import rife.feed.EntryProcessor;
import rife.feed.EntryProvider;
import rife.feed.Feed;
import rife.template.Template;
import rife.template.TemplateFactory;
import rife.tools.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Objects;

import static rife.tools.StringUtils.encodeXml;

/**
 * An <code>Element</code> that uses an <code>EntryProvider</code> to print
 * out a feed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.engine.Element
 * @see rife.feed.EntryProvider
 * @see rife.feed.Entry
 * @see rife.feed.Feed
 * @since 1.8.0
 */
public class FeedProvider implements Element, EntryProcessor {
    private final EntryProvider provider_;
    private final Template feedTemplate_;
    private final SimpleDateFormat dateFormat_;
    private boolean addedMediaNamespace_ = false;

    public enum FeedType {
        RSS_2_0,
        ATOM_0_3,
        ATOM_1_0,
        JSON_1_1
    }

    public FeedProvider(FeedType feedType, EntryProvider provider) {
        if (feedType == FeedType.JSON_1_1) {
            feedTemplate_ = TemplateFactory.JSON.get("feeds." + feedType.name().toLowerCase());
        } else {
            feedTemplate_ = TemplateFactory.XML.get("feeds." + feedType.name().toLowerCase());
        }

        switch (feedType) {
            case ATOM_0_3, ATOM_1_0 , JSON_1_1 ->
                // ISO8601
                dateFormat_ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            default ->
                // RFC822
                dateFormat_ = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");
        }
        dateFormat_.setTimeZone(RifeConfig.tools().getDefaultTimeZone());

        provider_ = provider;
    }

    public void process(Context c) {
        if (provider_ != null) {
            provider_.provideEntries(c, this);

            var feed = provider_.getFeedDescriptor(c);
            feedTemplate_.setBean(feed, "feed_");

            if (feedTemplate_.hasValueId("feed_publishedDate")) {
                feedTemplate_.setValue("feed_publishedDate", dateFormat_.format(feed.getPublishedDate()));
            }

            if (feed.getNamespaces() != null) {
                for (var entry : feed.getNamespaces().entrySet()) {
                    feedTemplate_.setValue("namespace_key", encodeXml(entry.getKey()));
                    feedTemplate_.setValue("namespace_url", encodeXml(entry.getValue()));

                    feedTemplate_.appendBlock("namespaces", "namespace");
                }
            }
        }

        c.print(feedTemplate_);
    }

    public void setEntry(Entry entry) {
        feedTemplate_.setBean(entry, "entry_");
        if (entry.isEscaped() &&
            feedTemplate_.hasValueId("entry_escaped_attribute")) {
            feedTemplate_.setBlock("entry_escaped_attribute");
        }
        if (!entry.isEscaped()) {
            feedTemplate_.setValue("entry_content", entry.getContent());
        }

        feedTemplate_.setValue("entry_publishedDate", dateFormat_.format(entry.getPublishedDate()));

        if (feedTemplate_.hasValueId("image")) {
            if (entry.getImage() != null) {
                if (!addedMediaNamespace_ && feedTemplate_.hasBlock("media_namespace")) {
                    feedTemplate_.appendBlock("namespaces", "media_namespace");
                    addedMediaNamespace_ = true;
                }
                feedTemplate_.setBlock("image");
            } else {
                feedTemplate_.blankValue("image");
            }
        }

        feedTemplate_.appendBlock("entries", "entry");
    }
}
