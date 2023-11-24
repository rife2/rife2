/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.feed;

/**
 * An <code>EntryProcessor</code> is a class that knows how to create a section of a feed
 * for output.
 * <p>This interface is generally implemented by an <code>Element</code> which then passes
 * itself into an <code>EntryProvider</code> to allow the <code>EntryProvider</fcode> to stream results
 * straight into the feed instead of dumping a large collection.</p>
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.feed.EntryProvider
 * @see rife.engine.Element
 * @since 1.8.0
 */
public interface EntryProcessor {
    /**
     * Sets an entry to the feed being constructed.
     * <p>Generally called by an <code>EntryProvider</code> while streaming results from the
     * DB to the feed provider/outputter.
     *
     * @param entry the Entry to be written to the feed
     * @see Entry
     * @see EntryProvider
     * @since 1.8.0
     */
    void setEntry(Entry entry);
}
