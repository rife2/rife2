/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.engine.ServerSentEvent;
import rife.engine.SseBroadcaster;
import rife.engine.SseErrorListener;
import rife.tools.ExceptionUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Bridges the data changes of a {@link GenericQueryManager} to a server-sent
 * events (SSE) {@link SseBroadcaster}, so that every bean that is inserted,
 * updated or deleted through the query manager is pushed to all the
 * connected SSE clients.
 * <p>The bridge is registered like any other query manager listener:
 * <pre>manager.addListener(new SseGqmBridge&lt;&gt;(broadcaster));</pre>
 * <p>By default, the name of the operation ({@code inserted},
 * {@code updated} or {@code deleted}) becomes the SSE event name, and the
 * string representation of the bean becomes the SSE event data, or the
 * string representation of the object ID in case of deletions. The
 * conversion of each operation can be customized individually, which also
 * makes it possible to render template fragments with {@code setBean()}, or
 * to filter operations out by returning {@code null}:
 * <pre>manager.addListener(new SseGqmBridge&lt;Product&gt;(broadcaster)
 *     .onInserted(product -&gt; {
 *         var t = TemplateFactory.HTML.get("catalog");
 *         t.setBean(product);
 *         return new ServerSentEvent().name("product").templateBlock(t, "product_row");
 *     })
 *     .onDeleted(objectId -&gt; null));</pre>
 * <p>Restoring beans doesn't broadcast events, and neither do the
 * installation and the removal of the database structure. Note that this
 * bridge only observes the changes that are made through the query manager
 * that it is registered with, since modifications that reach the database
 * in other ways aren't broadcast.
 * <p>The converters run on the thread that performs the database
 * operation, after that operation has completed. Since the database change
 * has already happened at that point, exceptions from the conversion or
 * from the broadcast don't propagate to the database operation: they are
 * handed to the {@link #onError onError} listener, or logged to the
 * {@code rife.engine} logger when no listener was provided. You should
 * configure the converters before registering the bridge as a listener.
 *
 * @param <BeanType> the type of the bean that the query manager handles
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseBroadcaster
 * @see SseErrorListener
 * @see GenericQueryManager#addListener
 * @since 1.10
 */
public class SseGqmBridge<BeanType> implements GenericQueryManagerListener<BeanType> {
    private final SseBroadcaster broadcaster_;
    private Function<BeanType, ServerSentEvent> inserted_;
    private Function<BeanType, ServerSentEvent> updated_;
    private IntFunction<ServerSentEvent> deleted_;
    private SseErrorListener errorListener_ = null;

    /**
     * Creates a new bridge with the default conversions.
     *
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @see #json
     * @since 1.10
     */
    public SseGqmBridge(SseBroadcaster broadcaster) {
        if (null == broadcaster) throw new IllegalArgumentException("broadcaster can't be null");

        broadcaster_ = broadcaster;
        inserted_ = bean -> new ServerSentEvent().name("inserted").data(String.valueOf(bean));
        updated_ = bean -> new ServerSentEvent().name("updated").data(String.valueOf(bean));
        deleted_ = object_id -> new ServerSentEvent().name("deleted").data(String.valueOf(object_id));
    }

    /**
     * Creates a new bridge whose conversions transmit JSON event data,
     * which is suitable for clients that parse the events with
     * {@code JSON.parse(event.data)}.
     * <p>Inserted and updated beans are converted to a JSON object of their
     * properties with {@link ServerSentEvent#json}, while deletions
     * transmit a JSON object with the object ID as its {@code id} member.
     * The conversions can still be customized individually afterwards.
     *
     * @param <BeanType>  the type of the bean that the query manager
     *                    handles
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @return the new bridge instance
     * @see ServerSentEvent#json
     * @since 1.10
     */
    public static <BeanType> SseGqmBridge<BeanType> json(SseBroadcaster broadcaster) {
        return new SseGqmBridge<BeanType>(broadcaster)
            .onInserted(bean -> new ServerSentEvent().name("inserted").json(bean))
            .onUpdated(bean -> new ServerSentEvent().name("updated").json(bean))
            .onDeleted(object_id -> new ServerSentEvent().name("deleted").json(Map.of("id", object_id)));
    }

    /**
     * Customizes the conversion of inserted beans.
     * <p>The converter returns the server-sent event that will be
     * broadcast, or {@code null} when nothing should be broadcast for
     * insertions.
     *
     * @param converter the converter that will be used for inserted beans
     * @return this bridge instance
     * @see #onUpdated
     * @see #onDeleted
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onInserted(Function<BeanType, ServerSentEvent> converter) {
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        inserted_ = converter;
        return this;
    }

    /**
     * Customizes the conversion of updated beans.
     * <p>The converter returns the server-sent event that will be
     * broadcast, or {@code null} when nothing should be broadcast for
     * updates.
     *
     * @param converter the converter that will be used for updated beans
     * @return this bridge instance
     * @see #onInserted
     * @see #onDeleted
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onUpdated(Function<BeanType, ServerSentEvent> converter) {
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        updated_ = converter;
        return this;
    }

    /**
     * Customizes the conversion of deletions.
     * <p>The converter receives the object ID of the deleted bean and
     * returns the server-sent event that will be broadcast, or {@code null}
     * when nothing should be broadcast for deletions.
     *
     * @param converter the converter that will be used for deletions
     * @return this bridge instance
     * @see #onInserted
     * @see #onUpdated
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onDeleted(IntFunction<ServerSentEvent> converter) {
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        deleted_ = converter;
        return this;
    }

    /**
     * Provides a listener for conversion and broadcast failures.
     * <p>The bridge is notified after the database operation has completed,
     * so that a failure can't affect the result of a change that has already
     * happened. Failures are logged to the {@code rife.engine} logger by
     * default, and providing a listener will replace that behavior.
     * <p>A listener that fails itself can't affect the operation either,
     * since its failure is logged together with the failure that it was
     * given.
     *
     * @param listener the listener that will receive conversion and
     *                 broadcast failures
     * @return this bridge instance
     * @see SseErrorListener
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onError(SseErrorListener listener) {
        if (null == listener) throw new IllegalArgumentException("listener can't be null");

        errorListener_ = listener;
        return this;
    }

    public void installed() {
        // the installation of the database structure isn't broadcast
    }

    public void removed() {
        // the removal of the database structure isn't broadcast
    }

    public void inserted(BeanType bean) {
        deliver(() -> inserted_.apply(bean));
    }

    public void updated(BeanType bean) {
        deliver(() -> updated_.apply(bean));
    }

    public void restored(BeanType bean) {
        // restorations are reads and aren't broadcast
    }

    public void deleted(int objectId) {
        deliver(() -> deleted_.apply(objectId));
    }

    private void deliver(Supplier<ServerSentEvent> conversion) {
        try {
            var event = conversion.get();
            if (event != null) {
                broadcaster_.send(event);
            }
        } catch (Throwable e) {
            // the database change has already been committed, so a delivery
            // failure can't be allowed to fail the operation that caused it
            reportError(e);
        }
    }

    private void reportError(Throwable error) {
        if (errorListener_ != null) {
            try {
                errorListener_.errorOccurred(error);
                return;
            } catch (Throwable t) {
                // a listener that fails can't fail the operation either, and
                // the failure it was given would go unreported otherwise
                Logger.getLogger("rife.engine").severe("The SSE error listener failed to handle a delivery failure\n" + ExceptionUtils.getExceptionStackTrace(t));
            }
        }

        Logger.getLogger("rife.engine").severe("SSE delivery failed after a completed database change\n" + ExceptionUtils.getExceptionStackTrace(error));
    }
}
