/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.exceptions.DatabaseException;

public abstract class TestDropSequence extends TestQuery {
    public void execute(Datasource datasource, DropSequence query) {
        try {
            var connection = datasource.getConnection();
            connection.beginTransaction();
            var create_sequence = new CreateSequence(datasource);
            create_sequence.name(query.getName());
            connection.createStatement().executeUpdate(create_sequence);

            connection.createStatement().executeUpdate(query);
            connection.rollback();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
}
