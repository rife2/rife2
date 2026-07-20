/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.engine.ServerSentEvent;
import rife.engine.SseBroadcaster;
import rife.tools.ExceptionUtils;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Bridges {@link GenericQueryManager} data changes to a server-sent events
 * (SSE) {@link SseBroadcaster}, so that every bean that is inserted, updated
 * or deleted through the query manager is pushed to all the connected SSE
 * clients.
 * <p>The bridge is registered like any other query manager listener:
 * <pre>manager.addListener(new SseGqmBridge&lt;&gt;(broadcaster));</pre>
 * <p>By default, the operation name ({@code inserted}, {@code updated} or
 * {@code deleted}) becomes the SSE event name, and the string representation
 * of the bean — or of the object ID for deletions — becomes the SSE event
 * data. Each operation's conversion can be customized individually, which
 * also makes it possible to render template fragments with
 * {@code setBean()}, or to filter operations by returning {@code null}:
 * <pre>manager.addListener(new SseGqmBridge&lt;Product&gt;(broadcaster)
 *     .onInserted(product -&gt; {
 *         var t = TemplateFactory.HTML.get("catalog");
 *         t.setBean(product);
 *         return new ServerSentEvent().name("product").templateBlock(t, "product_row");
 *     })
 *     .onDeleted(objectId -&gt; null));</pre>
 * <p>Restoring beans doesn't broadcast events, and neither do the
 * installation and removal of the database structure. Note that the bridge
 * only observes changes that are made through the query manager it is
 * registered with, modifications that reach the database in other ways are
 * not broadcast.
 * <p>The converters run on the thread that performs the database operation,
 * after that operation has completed. Since the database change has already
 * happened at that point, exceptions from the conversion or the broadcast
 * don't propagate to the database operation: they are passed to the
 * {@link #onError onError} handler, or logged to the {@code rife.engine}
 * logger when no handler was provided. Configure the converters before
 * registering the bridge as a listener.
 *
 * @param <BeanType> the type of the bean that the query manager handles
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see SseBroadcaster
 * @see GenericQueryManager#addListener
 * @since 1.10
 */
public class SseGqmBridge<BeanType> implements GenericQueryManagerListener<BeanType> {
    private final SseBroadcaster broadcaster_;
    private Function<BeanType, ServerSentEvent> inserted_;
    private Function<BeanType, ServerSentEvent> updated_;
    private IntFunction<ServerSentEvent> deleted_;
    private Consumer<Throwable> errorHandler_ = null;

    /**
     * Creates a new bridge with the default conversions.
     *
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
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
     * suitable for clients that parse the events with
     * {@code JSON.parse(event.data)}.
     * <p>Inserted and updated beans are converted to a JSON object of
     * their properties with {@link ServerSentEvent#json}, deletions
     * transmit a JSON object with the object ID as its {@code id} member.
     * The conversions can still be customized individually afterwards.
     *
     * @param <BeanType>  the type of the bean that the query manager
     *                    handles
     * @param broadcaster the broadcaster the converted events will be sent
     *                    to
     * @return the new bridge instance
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
     * <p>The converter returns the server-sent event to broadcast, or
     * {@code null} to not broadcast anything for insertions.
     *
     * @param converter the converter that will be used for inserted beans
     * @return this bridge instance
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onInserted(Function<BeanType, ServerSentEvent> converter) {
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        inserted_ = converter;
        return this;
    }

    /**
     * Customizes the conversion of updated beans.
     * <p>The converter returns the server-sent event to broadcast, or
     * {@code null} to not broadcast anything for updates.
     *
     * @param converter the converter that will be used for updated beans
     * @return this bridge instance
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
     * returns the server-sent event to broadcast, or {@code null} to not
     * broadcast anything for deletions.
     *
     * @param converter the converter that will be used for deletions
     * @return this bridge instance
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onDeleted(IntFunction<ServerSentEvent> converter) {
        if (null == converter) throw new IllegalArgumentException("converter can't be null");

        deleted_ = converter;
        return this;
    }

    /**
     * Provides a handler for conversion and broadcast failures.
     * <p>The bridge is notified after the database operation has completed,
     * so a failure can't affect the result of a change that has already
     * happened. By default failures are logged to the {@code rife.engine}
     * logger, providing a handler replaces that behavior.
     *
     * @param handler the handler that will receive conversion and broadcast
     *                failures
     * @return this bridge instance
     * @since 1.10
     */
    public SseGqmBridge<BeanType> onError(Consumer<Throwable> handler) {
        if (null == handler) throw new IllegalArgumentException("handler can't be null");

        errorHandler_ = handler;
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
            // the database change has already been committed, a delivery
            // failure can't be allowed to fail the operation that caused it
            if (errorHandler_ != null) {
                errorHandler_.accept(e);
            } else {
                Logger.getLogger("rife.engine").severe("SSE delivery failed after a completed database change\n" + ExceptionUtils.getExceptionStackTrace(e));
            }
        }
    }
}
