/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.workflow;

/**
 * This interface allows classes to register themselves to receive
 * notifications when events are triggered in a {@link Workflow}.
 * <p>Event listeners have to be registered through
 * {@link Workflow#addListener}.
 *
 * @apiNote The workflow engine is still in an ALPHA EXPERIMENTAL STAGE and might change.
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
