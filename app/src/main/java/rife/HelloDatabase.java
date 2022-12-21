/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.database.*;
import rife.database.queries.*;
import rife.engine.*;

public class HelloDatabase extends Site {
    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    DbQueryManager manager = new DbQueryManager(datasource);
    CreateTable createQuery = new CreateTable(datasource)
        .table("hello").column("name", String.class, 50);
    DropTable dropQuery = new DropTable(datasource)
        .table(createQuery.getTable());
    Select selectQuery = new Select(datasource)
        .from(createQuery.getTable()).orderBy("name");
    Insert insertQuery = new Insert(datasource)
        .into(createQuery.getTable()).fieldParameter("name");

    Route addForm = get("/add", c -> c.print("""
        <form method='post'>
        <input name='name'/><input type='submit'/>
        </form>""")
    );
    Route list = get("/list", c -> {
        manager.executeFetchAll(selectQuery,
            resultSet -> c.print(resultSet.getString("name") + "<br>"));
        c.print("<br><a href='" + c.urlFor(addForm) + "'>Add more</a><br>");
    });
    Route install = get("/install", c -> {
        manager.executeUpdate(createQuery);
        c.print("Installed");
    });
    Route remove = get("/remove", c -> {
        manager.executeUpdate(dropQuery);
        c.print("Removed");
    });
    Route add = post("/add", c -> {
        var name = c.parameter("name");
        manager.executeUpdate(insertQuery,
            statement -> statement.setString("name", name));
        c.print("Added " + name + "<br><br>");
        c.print("<a href='" + c.urlFor(addForm) + "'>Add more</a><br>");
        c.print("<a href='" + c.urlFor(list) + "'>List names</a><br>");
    });

    public static void main(String[] args) {
        new Server().start(new HelloDatabase());
    }
}