/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources.databasedrivers;

import rife.database.queries.*;

import rife.config.RifeConfig;
import rife.database.Datasource;
import rife.resources.DatabaseResources;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.resources.exceptions.ResourceWriterErrorException;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;

import java.net.URL;

public class generic extends DatabaseResources {
    protected CreateTable createTable_;
    protected DropTable dropTable_;
    protected Insert addResource_;
    protected Update updateResource_;
    protected Delete removeResource_;
    protected Select hasResource_;
    protected Select getResourceContent_;
    protected Select getResourceModified_;

    public generic(Datasource datasource) {
        super(datasource);

        String table = RifeConfig.resources().getTableResources();

        createTable_ = new CreateTable(getDatasource())
            .table(table)
            .column(COLUMN_NAME, String.class, 255, CreateTable.NOTNULL)
            .column(COLUMN_CONTENT, String.class)
            .column(COLUMN_MODIFIED, java.sql.Timestamp.class)
            .primaryKey(COLUMN_NAME);

        dropTable_ = new DropTable(getDatasource())
            .table(table);

        addResource_ = new Insert(getDatasource())
            .into(table)
            .fieldParameter(COLUMN_NAME)
            .fieldParameter(COLUMN_CONTENT)
            .fieldParameter(COLUMN_MODIFIED);

        updateResource_ = new Update(getDatasource())
            .table(table)
            .fieldParameter(COLUMN_CONTENT)
            .fieldParameter(COLUMN_MODIFIED)
            .whereParameter(COLUMN_NAME, "=");

        removeResource_ = new Delete(getDatasource())
            .from(table)
            .whereParameter(COLUMN_NAME, "=");

        hasResource_ = new Select(getDatasource())
            .from(table)
            .field(COLUMN_NAME)
            .whereParameter(COLUMN_NAME, "=");

        getResourceContent_ = new Select(getDatasource())
            .from(table)
            .field(COLUMN_CONTENT)
            .whereParameter(COLUMN_NAME, "=");

        getResourceModified_ = new Select(getDatasource())
            .from(table)
            .field(COLUMN_MODIFIED)
            .whereParameter(COLUMN_NAME, "=");
    }

    public boolean install()
    throws ResourceWriterErrorException {
        return _install(createTable_);
    }

    public boolean remove()
    throws ResourceWriterErrorException {
        return _remove(dropTable_);
    }

    public void addResource(String name, String content)
    throws ResourceWriterErrorException {
        _addResource(addResource_, name, content);
    }

    public boolean updateResource(String name, String content)
    throws ResourceWriterErrorException {
        return _updateResource(updateResource_, name, content);
    }

    public boolean removeResource(String name)
    throws ResourceWriterErrorException {
        return _removeResource(removeResource_, name);
    }

    public URL getResource(String name) {
        return _getResource(hasResource_, name);
    }

    public <ResultType> ResultType useStream(URL resource, InputStreamUser<ResultType, ?> user)
    throws ResourceFinderErrorException, InnerClassException {
        return (ResultType) _useStream(getResourceContent_, resource, user);
    }

    public String getContent(URL resource, String encoding)
    throws ResourceFinderErrorException {
        return _getContent(getResourceContent_, resource, encoding);
    }

    public long getModificationTime(URL resource)
    throws ResourceFinderErrorException {
        return _getModificationTime(getResourceModified_, resource);
    }
}
