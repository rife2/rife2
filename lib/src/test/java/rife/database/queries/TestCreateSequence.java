/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.DbConnection;
import rife.database.exceptions.DatabaseException;

public abstract class TestCreateSequence extends TestQuery {
    public void execute(Datasource datasource, CreateSequence query) {
        DbConnection connection = null;
        DropSequence drop_sequence = new DropSequence(datasource);

        try {
            connection = datasource.getConnection();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }

        try {
            connection.beginTransaction();

            // try to execute the table creation
            connection.createStatement().executeUpdate(query);

            // it was successful, remove the table again
            drop_sequence.name(query.getName());
            connection.createStatement().executeUpdate(drop_sequence);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        } finally {
            // clean up foreign key table
            try {
                connection.rollback();
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
