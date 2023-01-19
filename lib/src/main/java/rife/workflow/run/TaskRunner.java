/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow.run;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import rife.continuations.*;
import rife.continuations.basic.*;
import rife.ioc.HierarchicalProperties;
import rife.workflow.Event;
import rife.workflow.Task;
import rife.workflow.config.InstrumentWorkflowConfig;

/**
 * Runs tasks and dispatches events to tasks that are waiting for it.
 * <p>If events are triggered, and no tasks are ready to consume them, they
 * will be queued up until the first available task arrives.
 * <p>Note that this task runner executes the tasks, but doesn't create a new
 * thread for itself. When a task runner is used, you should take the
 * necessary steps to keep the application running for as long as you need the
 * tasks to be available.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class TaskRunner {
    private static final ContinuationConfigInstrument CONFIG_INSTRUMENT = new InstrumentWorkflowConfig();

    private final HierarchicalProperties properties_;
    private final ExecutorService taskExecutor_;
    private final BasicContinuableRunner runner_;
    private final ConcurrentHashMap<Object, Collection<String>> eventsMapping_;
    private final ConcurrentHashMap<Object, ConcurrentLinkedQueue<Event>> pendingEvents_;
    private final CopyOnWriteArraySet<EventListener> listeners_;

    /**
     * Creates a new task runner instance with a cached thread pool.
     *
     * @since 1.0
     */
    public TaskRunner() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Creates a new task runner instance with a provided executor.
     *
     * @param executor the executor to use for running the tasks
     * @since 1.0
     */
    public TaskRunner(ExecutorService executor) {
        var system_properties = new HierarchicalProperties().putAll(System.getProperties());
        properties_ = new HierarchicalProperties().parent(system_properties);

        runner_ = new BasicContinuableRunner(CONFIG_INSTRUMENT) {
            public void executeContinuable(Object object)
            throws Throwable {
                var method = object.getClass().getMethod(
                    getConfigInstrumentation().getEntryMethodName(),
                    getConfigInstrumentation().getEntryMethodArgumentTypes());
                method.invoke(object, TaskRunner.this);
            }
        };
        runner_.setCloneContinuations(false);
        runner_.setCallTargetRetriever(new EventTypeCallTargetRetriever());

        eventsMapping_ = new ConcurrentHashMap<>();
        pendingEvents_ = new ConcurrentHashMap<>();
        taskExecutor_ = executor;
        listeners_ = new CopyOnWriteArraySet<>();
    }

    /**
     * Retrieves the hierarchical properties for this task runner instance.
     *
     * @return this task runner's collection of hierarchical properties
     * @since 1.0
     */
    public HierarchicalProperties properties() {
        return properties_;
    }

    /**
     * Starts the execution of a new task instance.
     *
     * @param klass the task class whose instance that should be
     *              executed, the class should extend {@link rife.workflow.Task}
     * @since 1.0
     */
    public void start(final Class<? extends Task> klass) {
        taskExecutor_.submit(() -> {
            try {
                runner_.start(klass);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Triggers an event that wakes up tasks that are waiting for the event
     * type.
     * <p>If events are triggered, and no tasks are ready to consume them, they
     * will be queued up until the first available task arrives.
     *
     * @param event the event
     * @since 1.0
     */
    public void trigger(final Event event) {
        if (null == event) return;

        // retrieve the continuation IDs of the tasks that are waiting for
        // the type of the event

        // first obtain the collection for this event's type
        final Set<String> ids_to_resume = new HashSet<>();
        eventsMapping_.compute(event.getType(), (eventType, ids) -> {
            if (ids != null) {
                synchronized (ids) {
                    ids_to_resume.addAll(ids);
                    ids.clear();
                }
            }
            return ids;
        });

        if (ids_to_resume.isEmpty()) {
            // couldn't find any continuations to resume, add the event as pending
            pendingEvents_.compute(event.getType(), (eventType, events) -> {
                if (events == null) events = new ConcurrentLinkedQueue<>();
                events.add(event);
                return events;
            });
        } else {
            // resume all the continuations that are waiting for the event type
            for (var id : ids_to_resume) {
                answer(id, event);
            }
        }

        // notify all the event listeners that a new event has been triggered
        listeners_.forEach(listener -> listener.eventTriggered(event));
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

    private void answer(final String id, final Object callAnswer) {
        if (null == id) return;

        taskExecutor_.submit(() -> {
            try {
                runner_.answer(id, callAnswer);
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
                }
                return ids;
            });

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