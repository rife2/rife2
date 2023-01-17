/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.database.Datasource;
import rife.engine.*;
import rife.scheduler.*;
import rife.scheduler.schedulermanagers.*;

public class HelloScheduler extends Site {
    Datasource datasource = new Datasource("org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    DatabaseScheduling scheduling = DatabaseSchedulingFactory.instance(datasource);

    public static class CounterExecutor extends Executor {
        private int counter_ = 0;

        public boolean executeTask(Task task) {
            counter_ += 1;
            return true;
        }

        public int getCount() {
            return counter_;
        }
    }

    public void setup() {
        final var scheduler = scheduling.getScheduler();

        final var executor = new CounterExecutor();
        scheduler.addExecutor(executor);

        get("/status", c -> {
            c.print("Counter: " + executor.getCount());
            printStatus(scheduler, c);
        });
        get("/start", c -> {
            scheduler.start();
            c.print("Started");
            printStatus(scheduler, c);
        });
        get("/stop", c -> {
            scheduler.stop();
            c.print("Stopped");
            printStatus(scheduler, c);
        });
        get("/add", c -> {
            scheduler.addTask(executor.createTask().frequency(Frequency.MINUTELY));
            c.print("Added");
            printStatus(scheduler, c);
        });
        get("/install", c -> {
            scheduling.install();
            c.print("Installed");
        });
        get("/remove", c -> {
            scheduler.stop();
            scheduling.remove();
            c.print("Removed");
        });
    }

    private static void printStatus(Scheduler scheduler, Context c) {
        c.print("<br>Status: " + (scheduler.isRunning() ? "running - " : "stopped - ") +
                scheduler.getTaskManager().getAllTasks().size() + " tasks");
    }

    public static void main(String[] args) {
        new Server().start(new HelloScheduler());
    }
}
