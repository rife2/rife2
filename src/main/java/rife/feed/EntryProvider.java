/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

import rife.engine.Context;

/**
 * An <code>EntryProvider</code> is a way to get entries for a feed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see Feed
 * @since 1.8.0
 */
public interface EntryProvider {
    /**
     * Get a bean describing the <code>Feed</code> being outputted.
     *
     * @param c the context for this EntryProvider
     * @return Feed a bean describing the feed currently being outputted
     * @see Feed
     * @since 1.8.0
     */
    Feed getFeedDescriptor(Context c);

    /**
     * Provide entries using {@link EntryProcessor#setEntry(Entry)} to set
     * each entry to the feed
     *
     * @param c         the context for this EntryProvider
     * @param processor the processor creating this feed
     * @see EntryProcessor
     * @since 1.8.0
     */
    void provideEntries(Context c, EntryProcessor processor);
}
