/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.database.DbPreparedStatement;
import rife.database.DbResultSet;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.Select;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class RawContentStream extends InputStream {
    protected DbPreparedStatement statement_;
    protected DbResultSet resultSet_;
    protected byte[] buffer_ = null;
    protected int index_ = 0;
    protected boolean hasRow_;

    protected RawContentStream(DbPreparedStatement statement) {
        statement_ = statement;
        resultSet_ = statement_.getResultSet();
        hasRow_ = true;

        assert statement_ != null;
        assert resultSet_ != null;
    }

    @Override
    public int read()
    throws IOException {
        if (null == resultSet_) {
            throw new IOException("Trying to read from a closed raw content stream.");
        }

        var result = -1;
        try {
            if (null == buffer_) {
                if (!hasRow_) {
                    return -1;
                }

                buffer_ = resultSet_.getBytes("chunk");
                index_ = 0;
            }

            result = buffer_[index_++];

            if (index_ >= buffer_.length) {
                buffer_ = null;
                hasRow_ = resultSet_.next();
            }
        } catch (SQLException e) {
            throw new IOException("Unexpected error while reading the next bytes.", e);
        }

        return result;
    }

    @Override
    public void close()
    throws IOException {
        if (null == statement_) {
            return;
        }

        try {
            statement_.close();
        } catch (DatabaseException e) {
            throw new IOException("Unable to close prepared statement.", e);
        } finally {
            statement_ = null;
            resultSet_ = null;
            buffer_ = null;
        }
    }

    protected static DbPreparedStatement prepareStatement(DatabaseRawStore store, Select retrieveContentChunks, int id) {
        var statement = store.getStreamPreparedStatement(retrieveContentChunks, store.getConnection());

        statement
            .setInt("contentId", id);
        statement.executeQuery();
        var result_set = statement.getResultSet();
        try {
            if (!result_set.next()) {
                statement.close();
                return null;
            }
        } catch (SQLException e) {
            statement.close();
            return null;
        }

        return statement;
    }

    public static RawContentStream instance(DatabaseRawStore store, Select retrieveContentChunks, int id) {
        var statement = prepareStatement(store, retrieveContentChunks, id);
        if (null == statement) {
            return null;
        }

        return new RawContentStream(statement);
    }
}

