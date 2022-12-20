/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.database.*;
import rife.database.queries.*;
import rife.engine.*;
import java.sql.*;

public class HelloDatabase extends Site {
    Datasource datasource = new Datasource(
        "org.h2.Driver", "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    CreateTable createQuery = new CreateTable(datasource)
        .table("hello").column("name", String.class, 50);
    DropTable dropQuery = new DropTable(datasource)
        .table(createQuery.getTable());
    Select selectQuery = new Select(datasource)
        .from(createQuery.getTable()).orderBy("name");
    Insert insertQuery = new Insert(datasource)
        .into(createQuery.getTable()).fieldParameter("name");

    Route add = get("/add", c -> {
        c.print("""
            <form method='post'>
            <input name='name'/><input type='submit'/>
            </form>""");
    });
    Route list = get("/list", c -> {
        new DbQueryManager(datasource).executeFetchAll(selectQuery,
            new DbRowProcessor() {
                public boolean processRow(ResultSet resultSet)
                throws SQLException {
                    c.print(resultSet.getString("name") + "<br>");
                    return true;
                }
            });
        c.print("<br><a href='" + c.urlFor(add) + "'>add more</a><br>");
    });

    public void setup() {
        get("/install", c -> {
            new DbQueryManager(datasource).executeUpdate(createQuery);
            c.print("Installed");
        });
        get("/remove", c -> {
            new DbQueryManager(datasource).executeUpdate(dropQuery);
            c.print("Removed");
        });
        post("/add", c -> {
            var name = c.parameter("name");
            new DbQueryManager(datasource).executeUpdate(insertQuery,
                new DbPreparedStatementHandler<>() {
                    public void setParameters(DbPreparedStatement statement) {
                        statement.setString("name", name);
                    }
                });
            c.print("Added " + name + "<br><br>");
            c.print("<a href='" + c.urlFor(add) + "'>add more</a><br>");
            c.print("<a href='" + c.urlFor(list) + "'>list names</a><br>");
        });
    }

    public static void main(String[] args) {
        new Server().start(new HelloDatabase());
    }
}