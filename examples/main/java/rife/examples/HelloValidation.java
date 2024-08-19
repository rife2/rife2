/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.examples;

import rife.database.Datasource;
import rife.database.querymanagers.generic.GenericQueryManager;
import rife.database.querymanagers.generic.GenericQueryManagerFactory;
import rife.engine.Route;
import rife.engine.Server;
import rife.engine.Site;
import rife.examples.models.Email;
import rife.examples.models.Friend;
import rife.template.Template;
import rife.validation.ValidationBuilderHtml;
import rife.validation.ValidationError;

import java.util.*;

public class HelloValidation extends Site {
    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    GenericQueryManager<Friend> friends = GenericQueryManagerFactory.instance(datasource, Friend.class);
    GenericQueryManager<Email> emails = GenericQueryManagerFactory.instance(datasource, Email.class);

    Route install = get("/install", c -> {
        friends.install();
        emails.install();
        c.print("Installed");
    });
    Route remove = get("/remove", c -> {
        emails.remove();
        friends.remove();
        c.print("Removed");
    });
    Route addForm = get("/add", c -> {
        var t = c.template("HelloValidation");
        c.print(t);
    });
    Route list = get("/list", c -> {
        var t = c.template("HelloValidation");
        friends.restore(friend -> {
            t.setBean(friend);
            for (var e : friend.getEmails()) {
                t.appendValueEncoded("email", " " + e.getEmail());
            }
            t.appendBlock("entries", "entry");
            t.removeValue("email");
        });
        t.setBlock("content", "list");
        c.print(t);
    });
    Route add = post("/add", c -> {
        var t = c.template("HelloValidation");
        var friend = c.parametersBean(Friend.class);
        var email1 = c.parametersBean(Email.class, "1-");
        var email2 = c.parametersBean(Email.class, "2-");
        var emails = new ArrayList<Email>();
        friend.setEmails(emails);

        Set<ValidationError> errors = new HashSet<>();
        validateEmail(email1, emails, t, errors, "1-");
        validateEmail(email2, emails, t, errors, "2-");
        validateFriend(friend, t, errors);

        if (!errors.isEmpty()) {
            new ValidationBuilderHtml().generateValidationErrors(t, errors);
        } else {
            friends.save(friend);
            for (var e : friend.getEmails()) {
                t.appendValueEncoded("email", " " + e.getEmail());
            }
            t.setBean(friend);
            t.setBlock("content", "added");
        }
        c.print(t);
    });

    private static void validateEmail(Email email, List<Email> emails, Template t,
                                      Set<ValidationError> errors, String prefix) {
        if (email.getEmail() != null) {
            if (!email.validate()) {
                new ValidationBuilderHtml().generateErrorMarkings(t,
                    email.getValidationErrors(), Collections.emptyList(), prefix);
                errors.addAll(email.getValidationErrors());
            } else {
                emails.add(email);
            }
        }
    }

    private static void validateFriend(Friend friend, Template t, Set<ValidationError> errors) {
        if (!friend.validate()) {
            errors.addAll(friend.getValidationErrors());
            new ValidationBuilderHtml().generateErrorMarkings(t, errors, Collections.emptyList());
        }
    }

    public static void main(String[] args) {
        new Server().start(new HelloValidation());
    }
}