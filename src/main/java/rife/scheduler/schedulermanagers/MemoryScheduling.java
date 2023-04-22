/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.scheduler.schedulermanagers;

import rife.scheduler.Scheduler;
import rife.scheduler.SchedulerFactory;
import rife.scheduler.taskmanagers.MemoryTasks;
import rife.scheduler.taskoptionmanagers.MemoryTaskOptions;

public class MemoryScheduling implements SchedulerFactory {
    public MemoryScheduling() {
    }

    public Scheduler createScheduler() {
        return new Scheduler(new MemoryTasks(), new MemoryTaskOptions());
    }
}
