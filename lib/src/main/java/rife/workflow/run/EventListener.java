/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow.run;

import rife.workflow.Event;

/**
 * This interface allows classes to register themselves to receive
 * notifications when events are triggered in a {@link TaskRunner}.
 * <p>Event listeners have to be registered through
 * {@link TaskRunner#addListener}.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public interface EventListener {
    /**
     * Called when an event was triggered.
     *
     * @param event the event that was triggered
     * @since 1.0
     */
    void eventTriggered(Event event);
}
