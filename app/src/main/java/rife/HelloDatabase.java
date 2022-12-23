/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.database.*;
import rife.database.queries.*;
import rife.engine.*;
import rife.tools.InnerClassException;

public class HelloDatabase extends Site {
    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    DbQueryManager manager = new DbQueryManager(datasource);
    CreateTable createQuery = new CreateTable(datasource)
        .table("hello").column("name", String.class, 50);
    DropTable dropQuery = new DropTable(datasource)
        .table(createQuery.getTable());
    Insert insertQuery = new Insert(datasource)
        .into(createQuery.getTable()).fieldParameter("name");
    Select selectQuery = new Select(datasource)
        .from(createQuery.getTable()).orderBy("name");
    Select countQuery = new Select(datasource)
        .from(createQuery.getTable()).field("count(*)");

    Route install = get("/install", c -> {
        manager.executeUpdate(createQuery);
        c.print("Installed");
    });
    Route remove = get("/remove", c -> {
        manager.executeUpdate(dropQuery);
        c.print("Removed");
    });
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
    Route add = post("/add", c -> {
        var name = c.parameter("name");
        manager.inTransaction(new DbTransactionUserWithoutResult<>() {
            public void useTransactionWithoutResult()
            throws InnerClassException {
                manager.executeUpdate(insertQuery,
                    statement -> statement.setString("name", name));
                var count = manager.executeGetFirstInt(countQuery);
                if (count > 5) {
                    c.print("Maximum number of names reached<br><br>");
                    rollback();
                }
                c.print("Added " + name + " (#" + count+ ")<br><br>");
            }
        });
        c.print("<a href='" + c.urlFor(addForm) + "'>Add more</a><br>");
        c.print("<a href='" + c.urlFor(list) + "'>List names</a><br>");
    });

    public static void main(String[] args) {
        new Server().start(new HelloDatabase());
    }
}