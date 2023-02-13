/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

import rife.continuations.*;
import rife.continuations.basic.*;
import rife.ioc.HierarchicalProperties;
import rife.workflow.config.ContinuationInstrument;

/**
 * Runs work and dispatches events to work that is paused.
 * <p>Note that this workflow executes the work, but doesn't create a new
 * thread for itself. When a workflow is used, you should take the
 * necessary steps to keep the application running for as long as you need the
 * work to be available.
 *
 * @apiNote The workflow engine is still in an ALPHA EXPERIMENTAL STAGE and might change.
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class Workflow {
    private static final ContinuationConfigInstrument CONFIG_INSTRUMENT = new ContinuationInstrument();

    private final HierarchicalProperties properties_;
    private final ExecutorService workExecutor_;
    private final BasicContinuableRunner runner_;
    private final ConcurrentMap<Object, Set<String>> eventsMapping_;
    private final ConcurrentMap<Object, Queue<Event>> pendingEvents_;
    private final Set<EventListener> listeners_;
    private final Lock workLock_ = new ReentrantLock();
    private final Condition workFinished_ = workLock_.newCondition();
    private final Condition workPaused_ = workLock_.newCondition();
    private final AtomicLong activeWorkAndPauseCount_ = new AtomicLong();
    private final AtomicLong activePauseCount_ = new AtomicLong();

    /**
     * Creates a new workflow instance with a cached thread pool.
     *
     * @since 1.0
     */
    public Workflow() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Creates a new workflow instance with a provided executor.
     *
     * @param executor the executor to use for running the work
     * @since 1.0
     */
    public Workflow(ExecutorService executor) {
        this(executor, new HierarchicalProperties().putAll(System.getProperties()));
    }

    /**
     * Creates a new workflow instance with a provided parent properties.
     *
     * @param properties the parent hierarchical properties
     * @since 1.0
     */
    public Workflow(HierarchicalProperties properties) {
        this(Executors.newCachedThreadPool(), properties);
    }

    /**
     * Creates a new workflow instance.
     *
     * @param executor   the executor to use for running the work
     * @param properties the parent hierarchical properties
     * @since 1.0
     */
    public Workflow(ExecutorService executor, HierarchicalProperties properties) {
        properties_ = new HierarchicalProperties().parent(properties);

        runner_ = new BasicContinuableRunner(CONFIG_INSTRUMENT, new Class[]{Workflow.class}) {
            public void executeContinuable(Object object)
            throws Throwable {
                var method = object.getClass().getMethod(
                    getConfigInstrumentation().getEntryMethodName(),
                    getEntryMethodArgumentTypes());
                method.setAccessible(true);
                method.invoke(object, Workflow.this);
            }
        };
        runner_.setCloneContinuations(false);
        runner_.setCallTargetRetriever(new EventTypeCallTargetRetriever());

        eventsMapping_ = new ConcurrentHashMap<>();
        pendingEvents_ = new ConcurrentHashMap<>();
        workExecutor_ = executor;
        listeners_ = new CopyOnWriteArraySet<>();
    }

    /**
     * Retrieves the hierarchical properties for this workflow instance.
     *
     * @return this workflow's collection of hierarchical properties
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Starts the execution of a new work instance.
     *
     * @param klass the work class whose instance that should be
     *              executed, the class should extend {@link Work}
     * @since 1.0
     */
    public Workflow start(final Class<? extends Work> klass) {
        activeWorkAndPauseCount_.incrementAndGet();
        workExecutor_.submit(() -> {
            try {
                runner_.start(klass);

                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        return this;
    }

    /**
     * Starts the execution of a new work instance.
     *
     * @param work the work that should be executed
     * @return this workflow instance
     * @since 1.0
     */
    public Workflow start(Work work) {
        activeWorkAndPauseCount_.incrementAndGet();
        workExecutor_.submit(() -> {
            try {
                runner_.start(work);

                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        return this;
    }

    /**
     * Convenience method that informs about an event in a workflow.
     *
     * @param type the type of the event
     * @see #inform(Object, Object)
     * @see #inform(Event)
     * @since 1.0
     */
    public void inform(Object type) {
        inform(new Event(type, null));
    }

    /**
     * Convenience method that informs about an event in a workflow with
     * associated data.
     *
     * @param type the type of the event
     * @param data the data that will be sent with the event
     * @see #inform(Object)
     * @see #inform(Event)
     * @since 1.0
     */
    public void inform(Object type, Object data) {
        inform(new Event(type, data));
    }

    /**
     * Informs about an event that wakes up work if it is paused for
     * the event type.
     * <p>If events are informed about and no work is ready to consume them,
     * they will be lost. This is different from events being triggered.
     *
     * @param event the event
     * @see #trigger(Object)
     * @see #trigger(Object, Object)
     * @since 1.0
     */
    public void inform(final Event event) {
        handleEvent(event, false);
    }

    /**
     * Convenience method that triggers an event in a workflow.
     *
     * @param type the type of the event
     * @see #trigger(Object, Object)
     * @see #trigger(Event)
     * @since 1.0
     */
    public void trigger(Object type) {
        trigger(new Event(type, null));
    }

    /**
     * Convenience method that triggers an event in a workflow with
     * associated data.
     *
     * @param type the type of the event
     * @param data the data that will be sent with the event
     * @see #trigger(Object)
     * @see #trigger(Event)
     * @since 1.0
     */
    public void trigger(Object type, Object data) {
        trigger(new Event(type, data));
    }

    /**
     * Triggers an event that wakes up work that is paused for the event
     * type.
     * <p>If events are triggered, and no work is ready to consume them,
     * they will be queued up until the first available work arrives.
     *
     * @param event the event
     * @see #trigger(Object)
     * @see #trigger(Object, Object)
     * @since 1.0
     */
    public void trigger(final Event event) {
        handleEvent(event, true);
    }

    private void handleEvent(final Event event, boolean schedulePending) {
        if (null == event) return;

        // retrieve the continuation IDs of the work that is paused for
        // the type of the event

        // first obtain the collection for this event's type
        final Set<String> ids_to_resume = new HashSet<>();
        eventsMapping_.compute(event.getType(), (eventType, ids) -> {
            if (ids != null) {
                synchronized (ids) {
                    ids_to_resume.addAll(ids);
                    int delta = -ids.size();
                    activeWorkAndPauseCount_.addAndGet(delta);
                    activePauseCount_.addAndGet(delta);
                    ids.clear();
                }
            }
            return ids;
        });

        if (ids_to_resume.isEmpty()) {
            if (schedulePending) {
                // couldn't find any continuations to resume, add the event as pending
                pendingEvents_.compute(event.getType(), (eventType, events) -> {
                    if (events == null) events = new ConcurrentLinkedQueue<>();
                    events.add(event);
                    return events;
                });
            }
        } else {
            // resume all the continuations that are paused for the event type
            for (var id : ids_to_resume) {
                answer(id, event);
            }
        }

        // notify all the event listeners that a new event has been triggered
        listeners_.forEach(listener -> listener.eventTriggered(event));

        signalWhenAllWorkFinished();
    }

    /**
     * Causes the calling thread to wait until work is paused for events.
     *
     * @throws InterruptedException when the current thread is interrupted
     * @since 1.0
     */
    public void waitForPausedWork()
    throws InterruptedException {
        workLock_.lock();
        try {
            if (activePauseCount_.get() > 0) {
                return;
            }

            workPaused_.await();
        } finally {
            workLock_.unlock();
        }
    }

    /**
     * Causes the calling thread to wait until no more work is running.
     *
     * @throws InterruptedException when the current thread is interrupted
     * @since 1.0
     */
    public void waitForNoWork()
    throws InterruptedException {
        workLock_.lock();
        try {
            if (activeWorkAndPauseCount_.get() == 0) {
                return;
            }

            workFinished_.await();
        } finally {
            workLock_.unlock();
        }
    }

    /**
     * Adds a new event listener.
     *
     * @param listener the event listener that will be added
     * @see #removeListener
     * @since 1.0
     */
    public void addListener(final EventListener listener) {
        if (null == listener) {
            return;
        }
        listeners_.add(listener);
    }

    /**
     * Removes an event listener.
     *
     * @param listener the event listener that will be removed
     * @see #addListener
     * @since 1.0
     */
    public void removeListener(final EventListener listener) {
        if (null == listener) {
            return;
        }

        listeners_.remove(listener);
    }

    private void signalWhenAllWorkFinished() {
        workLock_.lock();
        try {
            if (activeWorkAndPauseCount_.get() == 0) {
                workFinished_.signalAll();
            }
        } finally {
            workLock_.unlock();
        }
    }

    private void signalThatWorkIsPaused() {
        workLock_.lock();
        try {
            workPaused_.signalAll();
        } finally {
            workLock_.unlock();
        }
    }

    private void answer(final String id, final Object callAnswer) {
        if (null == id) return;
        activeWorkAndPauseCount_.incrementAndGet();
        workExecutor_.submit(() -> {
            try {
                runner_.answer(id, callAnswer);

                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private class EventTypeCallTargetRetriever implements CallTargetRetriever {
        public CloneableContinuable getCallTarget(Object type, CallState state) {
            // keeps track of the continuation ID for this event type
            eventsMapping_.compute(type, (eventType, ids) -> {
                if (ids == null) ids = new HashSet<>();
                synchronized (ids) {
                    ids.add(state.getContinuationId());
                    activeWorkAndPauseCount_.incrementAndGet();
                    activePauseCount_.incrementAndGet();
                }
                return ids;
            });

            signalThatWorkIsPaused();

            // get the next pending event of this call type and trigger it
            final var pending_event = new Event[1];
            pendingEvents_.computeIfPresent(type, (evenType, events) -> {
                pending_event[0] = events.poll();
                return events;
            });
            if (pending_event[0] != null) {
                trigger(pending_event[0]);
            }

            return null;
        }
    }
}