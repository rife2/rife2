/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.database.Datasource;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.*;
import rife.validation.ValidationBuilderHtml;

public class HelloValidation extends Site {
    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    GenericQueryManager<PersonWithEmail> manager =
        GenericQueryManagerFactory.instance(datasource, PersonWithEmail.class);

    Route install = get("/install", c -> {
        manager.install();
        c.print("Installed");
    });
    Route remove = get("/remove", c -> {
        manager.remove();
        c.print("Removed");
    });
    Route addForm = get("/add", c -> {
        var t = c.template("HelloValidation");
        c.print(t);
    });
    Route list = get("/list", c -> {
        var t = c.template("HelloValidation");
        manager.restore(person -> {
            t.setBean(person);
            t.appendBlock("entries", "entry");
        });
        t.setBlock("content", "list");
        c.print(t);
    });
    Route add = post("/add", c -> {
        var t = c.template("HelloValidation");
        var person = c.parametersBean(PersonWithEmail.class);
        if (!person.validate()) {
            var builder = new ValidationBuilderHtml();
            builder.generateValidationErrors(t, person.getValidationErrors());
            builder.generateErrorMarkings(t, person.getValidationErrors());
        } else {
            manager.save(person);
            t.setBean(person);
            t.setBlock("content", "added");
        }
        c.print(t);
    });

    public static void main(String[] args) {
        new Server().start(new HelloValidation());
    }
}