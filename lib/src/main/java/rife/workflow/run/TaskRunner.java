/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rife.continuations.*;
import rife.continuations.basic.BasicContinuableClassLoader;
import rife.continuations.basic.BasicContinuableRunner;
import rife.continuations.basic.CallTargetRetriever;
import rife.workflow.Event;
import rife.workflow.EventType;
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

    private final BasicContinuableRunner runner_;
    private final Map<EventType, Collection<String>> eventsMapping_;
    private final ThreadGroup taskThreads_;
    private final List<Event> pendingEvents_;
    private final Collection<EventListener> listeners_;

    /**
     * Creates a new task runner instance.
     *
     * @since 1.0
     */
    public TaskRunner() {
        ClassLoader classloader = new BasicContinuableClassLoader(CONFIG_INSTRUMENT);
        runner_ = new BasicContinuableRunner(CONFIG_INSTRUMENT, classloader) {
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

        eventsMapping_ = new HashMap<>();
        taskThreads_ = new ThreadGroup("taskthreads");
        pendingEvents_ = new ArrayList<>();
        listeners_ = new LinkedHashSet<>();
    }

    /**
     * Starts the execution of a new task instance.
     *
     * @param className the class name of the task instance that should be
     *                  executed, the class should extend {@link rife.workflow.Task}
     * @since 1.0
     */
    public void start(final String className) {
        new Thread(taskThreads_, new Runnable() {
            public void run() {
                try {
                    runner_.start(className);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
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
        final Collection<String> ids;
        synchronized (eventsMapping_) {
            ids = eventsMapping_.getOrDefault(event.getType(), Collections.emptySet());
        }

        // then go over the continuation IDs for this event's type
        // and collect them into a seperate collection, while erasing them
        // from the events mapping
        Set<String> ids_to_resume = new HashSet<>();
        synchronized (ids) {
            if (ids.size() > 0) {
                var it = ids.iterator();
                while (it.hasNext()) {
                    ids_to_resume.add(it.next());
                    it.remove();
                }
            } else {
                synchronized (pendingEvents_) {
                    pendingEvents_.add(event);
                }
            }
        }

        // resume all the continuations that are waiting for the event type
        for (var id : ids_to_resume) {
            answer(id, event);
        }

        // notify all the event listeners that a new event has been triggered
        synchronized (listeners_) {
            for (var listener : listeners_) {
                listener.eventTriggered(event);
            }
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
        synchronized (listeners_) {
            listeners_.add(listener);
        }
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

        synchronized (listeners_) {
            listeners_.remove(listener);
        }
    }

    private void answer(final String id, final Object callAnswer) {
        if (null == id) return;

        new Thread(taskThreads_, () -> {
            try {
                runner_.answer(id, callAnswer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private class EventTypeCallTargetRetriever implements CallTargetRetriever {
        public CloneableContinuable getCallTarget(Object target, CallState state) {
            var type = (EventType) target;

            final Collection<String> ids;

            synchronized (eventsMapping_) {
                if (eventsMapping_.containsKey(type)) {
                    ids = eventsMapping_.get(type);
                } else {
                    ids = new HashSet<>();
                    eventsMapping_.put(type, ids);
                }
            }

            synchronized (ids) {
                ids.add(state.getContinuationId());
            }

            Event pending_event = null;
            synchronized (pendingEvents_) {
                var it = pendingEvents_.iterator();
                while (it.hasNext()) {
                    var candidate = it.next();
                    if (type.equals(candidate.getType())) {
                        pending_event = candidate;
                        it.remove();
                        break;
                    }
                }
            }
            if (pending_event != null) {
                trigger(pending_event);
            }

            return null;
        }
    }
}