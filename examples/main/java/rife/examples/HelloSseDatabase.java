/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.database.Datasource;
import rife.database.migrations.DbMigrations;
import rife.database.migrations.ReversibleDbMigration;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.database.querymanagers.generic.SseGqmBridge;
import rife.engine.*;
import rife.examples.models.Task;
import rife.resources.DatabaseResourcesFactory;
import rife.resources.exceptions.ResourceWriterErrorException;
import rife.template.Template;
import rife.template.TemplateFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class HelloSseDatabase extends Site {
    // each instance gets its own in-memory database, so that multiple
    // instances, like in HelloAll and in the tests, don't collide
    private static final AtomicInteger DB_SEQUENCE = new AtomicInteger();

    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:mem:hellossedb" + DB_SEQUENCE.incrementAndGet() + ";DB_CLOSE_DELAY=-1", "sa", "", 5);
    GenericQueryManager<Task> manager =
        GenericQueryManagerFactory.instance(datasource, Task.class);

    final SseBroadcaster broadcaster = new SseBroadcaster().history(100);

    // mutations and the page render synchronize on this lock, so that the
    // rendered rows and the embedded replay cursor are a consistent pair;
    // without it, a task that's inserted in between could both be rendered
    // and be replayed when the stream connects
    final Object mutationLock = new Object();

    Route events = get("/events", c -> c.sse(broadcaster));
    Route add = post("/add", c -> {
        synchronized (mutationLock) {
            manager.save(c.parametersBean(Task.class));
        }
    });
    Route toggle = post("/toggle", c -> {
        synchronized (mutationLock) {
            var task = manager.restore(c.parameterInt("id"));
            if (task != null) {
                task.setDone(!task.isDone());
                manager.save(task);
            }
        }
    });
    Route del = post("/delete", c -> {
        synchronized (mutationLock) {
            manager.delete(c.parameterInt("id"));
        }
    });
    Route page = get("/", c -> {
        var t = c.template("HelloSseDatabase");
        t.setValue("rows", "");
        synchronized (mutationLock) {
            // events between this render and the stream connection are replayed
            t.setValue("lastEventId", broadcaster.lastEventId());
            manager.restore(manager.getRestoreQuery().orderBy("id"), task -> {
                fillTaskRow(t, task, false);
                t.appendBlock("rows", "task_row");
            });
        }
        c.print(t);
    });

    static void fillTaskRow(Template t, Task task, boolean outOfBand) {
        t.setValue("id", task.getId());
        t.setValueEncoded("description", task.getDescription());
        t.setValue("checked", task.isDone() ? "checked" : "");
        t.setValue("style", task.isDone() ? "done" : "");
        t.setValue("oob", outOfBand ? " hx-swap-oob=\"true\"" : "");
    }

    // the initial schema is the structure that the generic query manager
    // expects for the Task bean
    static class CreateTaskSchema extends ReversibleDbMigration {
        public void up() {
            add(m -> GenericQueryManagerFactory.instance(m.getDatasource(), Task.class).install());
        }

        public void down() {
            add(m -> GenericQueryManagerFactory.instance(m.getDatasource(), Task.class).remove());
        }
    }

    static class IndexTaskDone extends ReversibleDbMigration {
        public void up() {
            add(createIndex("task_done_idx").table("Task").column("done"));
        }

        public void down() {
            add(dropIndex("task_done_idx").table("Task"));
        }
    }

    public void setup() {
        // the schema is versioned with declarative migrations, whose
        // current version is stored in the same database; this example
        // always starts with a fresh in-memory database, so the resource
        // store is installed unconditionally
        var migration_state = DatabaseResourcesFactory.instance(datasource);
        try {
            migration_state.install();
        } catch (ResourceWriterErrorException e) {
            throw new RuntimeException(e);
        }
        new DbMigrations(datasource)
            .state(migration_state)
            .add(1, new CreateTaskSchema())
            .add(2, new IndexTaskDone())
            .migrate();

        // new tasks are appended to the list, updated tasks replace their
        // own row out-of-band, and deleted tasks remove their row
        manager.addListener(new SseGqmBridge<Task>(broadcaster)
            .onInserted(task -> {
                var t = TemplateFactory.HTML.get("HelloSseDatabase");
                fillTaskRow(t, task, false);
                return new ServerSentEvent().name("inserted").templateBlock(t, "task_row");
            })
            .onUpdated(task -> {
                var t = TemplateFactory.HTML.get("HelloSseDatabase");
                fillTaskRow(t, task, true);
                return new ServerSentEvent().name("updated").templateBlock(t, "task_row");
            })
            .onDeleted(objectId -> new ServerSentEvent().name("deleted")
                .data("<li id=\"task-" + objectId + "\" hx-swap-oob=\"delete\"></li>")));
    }

    public void destroy() {
        broadcaster.close();
    }

    public static void main(String[] args) {
        new Server().start(new HelloSseDatabase());
    }
}
