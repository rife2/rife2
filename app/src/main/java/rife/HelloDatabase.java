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
    final Datasource datasource = new Datasource(
        "org.h2.Driver",
        "jdbc:h2:./embedded_dbs/h2/hello", "sa", "", 5);
    final CreateTable create = new CreateTable(datasource)
        .table("hello").column("name", String.class, 50);
    final DropTable drop = new DropTable(datasource)
        .table(create.getTable());
    final Select select = new Select(datasource)
        .from(create.getTable()).orderBy("name");
    final Insert insert = new Insert(datasource)
        .into(create.getTable()).fieldParameter("name");

    final Route add = get("/add", c -> {
        c.print("""
            <form action=''' method='post'>
            <input type='text' name='name'/><input type='submit'/>
            </form>""");
    });
    final Route list = get("/list", c -> {
        new DbQueryManager(datasource).executeFetchAll(select,
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
            new DbQueryManager(datasource).executeUpdate(create);
            c.print("Installed");
        });
        get("/remove", c -> {
            new DbQueryManager(datasource).executeUpdate(drop);
            c.print("Removed");
        });
        post("/add", c -> {
            var name = c.parameter("name");
            new DbQueryManager(datasource).executeUpdate(insert,
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