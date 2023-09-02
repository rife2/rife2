/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores;

import rife.cmf.MimeType;
import rife.cmf.dam.ContentStore;
import rife.cmf.dam.contentstores.exceptions.*;
import rife.cmf.dam.exceptions.ContentManagerException;
import rife.database.Datasource;
import rife.database.DbQueryManager;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.database.queries.Delete;
import rife.database.queries.DropTable;
import rife.database.queries.Select;
import rife.engine.Context;
import rife.tools.ExceptionUtils;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class DatabaseContentStore extends DbQueryManager implements ContentStore {
    private final List<MimeType> mimeTypes_ = new ArrayList<>();

    public DatabaseContentStore(Datasource datasource) {
        super(datasource);
    }

    protected void addMimeType(MimeType mimeType) {
        mimeTypes_.add(mimeType);
    }

    @Override
    public Collection<MimeType> getSupportedMimeTypes() {
        return mimeTypes_;
    }

    protected boolean _install(CreateTable createTableContentStore)
    throws ContentManagerException {
        assert createTableContentStore != null;

        try {
            executeUpdate(createTableContentStore);
        } catch (DatabaseException e) {
            throw new InstallContentStoreErrorException(e);
        }

        return true;
    }

    protected boolean _remove(DropTable dropTableContentStore)
    throws ContentManagerException {
        assert dropTableContentStore != null;

        try {
            executeUpdate(dropTableContentStore);
        } catch (DatabaseException e) {
            throw new RemoveContentStoreErrorException(e);
        }

        return true;
    }

    protected boolean _deleteContentData(final Delete deleteContentData, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert deleteContentData != null;

        Boolean result = null;

        try {
            result = inTransaction(() -> executeUpdate(deleteContentData, s -> s.setInt("contentId", id)) != 0);
        } catch (DatabaseException e) {
            throw new DeleteContentDataErrorException(id, e);
        }

        return result != null && result;
    }

    protected int _getSize(Select retrieveSize, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert retrieveSize != null;

        try {
            return executeGetFirstInt(retrieveSize, s -> s.setInt("contentId", id));
        } catch (DatabaseException e) {
            throw new RetrieveSizeErrorException(id, e);
        }
    }

    protected boolean _hasContentData(Select hasContentData, final int id)
    throws ContentManagerException {
        if (id < 0) throw new IllegalArgumentException("id must be positive");

        assert hasContentData != null;

        try {
            return executeHasResultRows(hasContentData, s -> s.setInt("contentId", id));
        } catch (DatabaseException e) {
            throw new HasContentDataErrorException(id, e);
        }
    }

    protected void _serveContentData(Select retrieveContent, final Context context, final int id)
    throws ContentManagerException {
        if (null == context) throw new IllegalArgumentException("context can't be null");

        if (id < 0) {
            context.defer();
            return;
        }

        assert retrieveContent != null;

        try {
            if (!executeFetchFirst(retrieveContent, resultSet -> {
                // set the content length header
                context.setContentLength(resultSet.getInt("contentSize"));

                // output the content
                OutputStream os = context.outputStream();
                outputContentColumn(resultSet, os);
            }, s -> s.setInt("contentId", id))) {
                context.defer();
            }
        } catch (DatabaseException e) {
            Logger.getLogger("rife.cmf").severe(ExceptionUtils.getExceptionStackTrace(e));
            context.setStatus(Context.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected abstract void outputContentColumn(ResultSet resultSet, OutputStream os)
    throws SQLException;
}
