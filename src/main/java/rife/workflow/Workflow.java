/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

import rife.continuations.CallState;
import rife.continuations.CloneableContinuable;
import rife.continuations.ContinuationConfigInstrument;
import rife.continuations.basic.BasicContinuableRunner;
import rife.continuations.basic.CallTargetRetriever;
import rife.continuations.exceptions.ContinuationsNotActiveException;
import rife.ioc.HierarchicalProperties;
import rife.tools.ExceptionUtils;
import rife.workflow.config.ContinuationInstrument;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Runs work and dispatches events to work that is paused.
 * <p>Note that this workflow executes the work, but doesn't create a new
 * thread for itself. When a workflow is used, you should take the
 * necessary steps to keep the application running for as long as you need the
 * work to be available.
 * <p>Events are delivered to the registered listeners before any work that
 * is paused for them is resumed. Events that work triggers after being
 * woken up are therefore always delivered to listeners after the event
 * that woke the work up. No ordering is guaranteed between events that are
 * triggered concurrently from independent threads.
 *
 * @rife.apiNote The workflow engine is in a BETA STAGE and might still change.
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
    private final Set<ErrorListener> errorListeners_;
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
        this(executor, HierarchicalProperties.createSystemInstance());
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
        errorListeners_ = new CopyOnWriteArraySet<>();
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
            } catch (Throwable e) {
                reportWorkError("of class " + klass.getName(), e);
            } finally {
                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
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
            } catch (Throwable e) {
                reportWorkError("of class " + work.getClass().getName(), e);
            } finally {
                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
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
                    // the captured continuations are guaranteed to be
                    // resumed, so they keep counting as active work across
                    // the paused-to-resuming transition, they merely stop
                    // counting as paused; without this, waitForNoWork() and
                    // waitForPausedWork() could report completion while a
                    // listener runs before the resumptions
                    activePauseCount_.addAndGet(-ids.size());
                    ids.clear();
                }
            }
            if (ids_to_resume.isEmpty() &&
                schedulePending) {
                // couldn't find any continuations to resume, add the event as
                // pending; this has to happen inside this compute so that a
                // continuation that is concurrently pausing for this event
                // type either is captured above or finds the pending event,
                // otherwise both could miss each other and the event would be
                // stranded while the continuation stays paused forever
                pendingEvents_.compute(event.getType(), (pendingType, events) -> {
                    if (events == null) events = new ConcurrentLinkedQueue<>();
                    events.add(event);
                    return events;
                });
            }
            return ids;
        });

        try {
            // notify all the event listeners that a new event has been triggered,
            // before resuming any work, so that listeners are guaranteed to
            // receive this event before any events that resumed work triggers
            listeners_.forEach(listener -> listener.eventTriggered(event));
        } finally {
            // the captured continuations have already been removed from the
            // events mapping, they always have to be resumed, even when a
            // listener fails
            for (var id : ids_to_resume) {
                answer(id, event);
            }

            signalWhenAllWorkFinished();
        }
    }

    /**
     * Causes the calling thread to wait until work is paused for events.
     * <p>This method also returns when no active work remains that could
     * still pause, for instance when all work has finished or has failed
     * with an exception. The returned value indicates which of the two
     * situations occurred.
     *
     * @return {@code true} when work is paused for events; or
     * <p>{@code false} when no active work remains that could still pause
     * @throws InterruptedException when the current thread is interrupted
     * @since 1.0
     */
    public boolean waitForPausedWork()
    throws InterruptedException {
        workLock_.lock();
        try {
            while (activePauseCount_.get() == 0 &&
                   activeWorkAndPauseCount_.get() != 0) {
                workPaused_.await();
            }

            return activePauseCount_.get() > 0;
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

    /**
     * Adds a new error listener that will be notified when work fails with
     * an exception.
     * <p>When no error listeners are registered, work failures are logged
     * to the {@code rife.workflow} logger instead.
     *
     * @param listener the error listener that will be added
     * @see #removeErrorListener
     * @since 1.10
     */
    public void addErrorListener(final ErrorListener listener) {
        if (null == listener) {
            return;
        }
        errorListeners_.add(listener);
    }

    /**
     * Removes an error listener.
     *
     * @param listener the error listener that will be removed
     * @see #addErrorListener
     * @since 1.10
     */
    public void removeErrorListener(final ErrorListener listener) {
        if (null == listener) {
            return;
        }

        errorListeners_.remove(listener);
    }

    private void reportWorkError(String description, Throwable e) {
        var message = new StringBuilder("Error while executing work ");
        message.append(description);
        if (isCausedByContinuationsNotActive(e)) {
            message.append("""
                ; the work class hasn't been instrumented for continuations, \
                ensure that the RIFE2 agent is being used \
                (-javaagent:rife2-[version]-agent.jar) or that the class was \
                instrumented at build time, note that classes inside rife.* \
                packages are excluded from agent instrumentation""");
        }

        var error = new WorkErrorException(message.toString(), e);
        if (errorListeners_.isEmpty()) {
            Logger.getLogger("rife.workflow").severe(ExceptionUtils.getExceptionStackTrace(error));
        } else {
            for (var listener : errorListeners_) {
                try {
                    listener.errorOccurred(error);
                } catch (Throwable t) {
                    Logger.getLogger("rife.workflow").severe(ExceptionUtils.getExceptionStackTrace(t));
                }
            }
        }
    }

    private static boolean isCausedByContinuationsNotActive(Throwable e) {
        while (e != null) {
            if (e instanceof ContinuationsNotActiveException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    private void signalWhenAllWorkFinished() {
        workLock_.lock();
        try {
            if (activeWorkAndPauseCount_.get() == 0) {
                workFinished_.signalAll();
                // also wake up threads that are waiting for work to pause,
                // since no work remains that could still pause
                workPaused_.signalAll();
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
        // the active work count carries over from the paused continuation
        // that is being resumed, it was registered when the work paused
        workExecutor_.submit(() -> {
            try {
                runner_.answer(id, callAnswer);
            } catch (Throwable e) {
                reportWorkError("that was resumed for an event", e);
            } finally {
                activeWorkAndPauseCount_.decrementAndGet();
                signalWhenAllWorkFinished();
            }
        });
    }

    private class EventTypeCallTargetRetriever implements CallTargetRetriever {
        public CloneableContinuable getCallTarget(Object type, CallState state) {
            // keeps track of the continuation ID for this event type
            final var pending_event = new Event[1];
            eventsMapping_.compute(type, (eventType, ids) -> {
                if (ids == null) ids = new HashSet<>();
                synchronized (ids) {
                    ids.add(state.getContinuationId());
                    activeWorkAndPauseCount_.incrementAndGet();
                    activePauseCount_.incrementAndGet();
                }
                // get the next pending event of this call type; this has to
                // happen inside this compute so that a concurrent trigger of
                // this event type either already queued the event here or
                // captures the continuation ID that was just registered,
                // otherwise both could miss each other
                pendingEvents_.computeIfPresent(type, (pendingType, events) -> {
                    pending_event[0] = events.poll();
                    return events;
                });
                return ids;
            });

            signalThatWorkIsPaused();

            // the pending event has to be triggered outside of the compute
            // above, triggering re-enters the events mapping for the same
            // type and compute isn't reentrant
            if (pending_event[0] != null) {
                trigger(pending_event[0]);
            }

            return null;
        }
    }
}