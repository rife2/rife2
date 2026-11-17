/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

/**
 * Receives the response of a {@link SiteRunner} while it's being written.
 * <p>{@link #committed} is called once, when the status and the headers are
 * final, before any content. The content then follows in calls to
 * {@link #content}. Like a servlet container, the site runner collects what
 * the site writes until it flushes it or until there's 32 KB of it. The
 * response is however committed as soon as content is written to its output
 * stream, like with a container that has no buffer, so the status and the
 * headers have to be set before that. The response ends with
 * {@link #completed}, {@link #failed} or {@link #cancelled}. Exactly one of
 * them is called, and no other call comes after it, so that's where you
 * can let go of what you set up for the response. A failure can also come before the response was committed.
 * <p>The response of a detached event stream ends when its connection is
 * closed, which is after {@link SiteRunner#handle(String, SiteRequest, SiteResponseListener)}
 * returned, and its calls come from the threads that send the events.
 * <p>Once the request is {@linkplain SiteRequest#cancel() cancelled}, no new
 * call will start. A call that was already under way on another thread
 * still finishes, and {@link #cancelled} is called once it did, so if what
 * you pass the content to doesn't accept calls after it was stopped, you
 * have to guard against that yourself until then.
 * <p>When {@link #committed} or {@link #content} throws an exception, the
 * response ends like it does when the request is cancelled.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SiteRunner
 * @since 1.11.0
 */
public interface SiteResponseListener {
    /**
     * Called when the response is committed, which means that its status
     * and its headers are final.
     *
     * @param response the response, which provides its status through
     *                 {@link SiteResponse#getStatus()}, its headers through
     *                 {@link SiteResponse#getHttpHeaders()}, and whether it
     *                 was handled through {@link SiteResponse#isHandled()}
     * @since 1.11.0
     */
    void committed(SiteResponse response);

    /**
     * Called with a part of the content of the response. The bytes are only
     * valid during the call, so you have to copy them if you need them
     * afterwards.
     *
     * @param bytes  the buffer with the content
     * @param offset where the content starts in the buffer
     * @param length how many bytes of content there are
     * @since 1.11.0
     */
    void content(byte[] bytes, int offset, int length);

    /**
     * Called when the response has ended normally.
     *
     * @since 1.11.0
     */
    void completed();

    /**
     * Called when the response ended with a failure.
     *
     * @param exception the cause of the failure
     * @since 1.11.0
     */
    void failed(Throwable exception);

    /**
     * Called when the response ended because its request was cancelled,
     * once no other call of this listener is under way anymore. This can
     * happen on the thread that cancelled the request, or on the one of the
     * call that was still under way.
     * <p>This does nothing by default.
     *
     * @since 1.11.0
     */
    default void cancelled() {
    }
}
