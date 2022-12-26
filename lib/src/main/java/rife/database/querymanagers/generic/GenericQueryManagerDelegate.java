/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.*;
import rife.database.exceptions.DatabaseException;
import rife.database.queries.CreateTable;
import rife.validation.Validated;

import java.util.List;

public class GenericQueryManagerDelegate<T> implements GenericQueryManager<T> {
    private Datasource datasource_ = null;
    private GenericQueryManager<T> delegate_ = null;

    public GenericQueryManagerDelegate(Datasource datasource, Class<T> klass, String table) {
        datasource_ = datasource;
        delegate_ = GenericQueryManagerFactory.instance(datasource, klass, table);
    }

    public GenericQueryManagerDelegate(Datasource datasource, Class<T> klass) {
        datasource_ = datasource;
        delegate_ = GenericQueryManagerFactory.instance(datasource, klass);
    }

    public Datasource getDatasource() {
        return datasource_;
    }

    public GenericQueryManager<T> getDelegate() {
        return delegate_;
    }

    public Class getBaseClass() {
        return delegate_.getBaseClass();
    }

    public String getIdentifierName()
    throws DatabaseException {
        return delegate_.getIdentifierName();
    }

    public int getIdentifierValue(T bean)
    throws DatabaseException {
        return delegate_.getIdentifierValue(bean);
    }

	public void validate(Validated validated)
	{
		delegate_.validate(validated);
	}

    public String getTable() {
        return delegate_.getTable();
    }

    public void install()
    throws DatabaseException {
        delegate_.install();
    }

    public void install(CreateTable query)
    throws DatabaseException {
        delegate_.install(query);
    }

    public void remove()
    throws DatabaseException {
        delegate_.remove();
    }

    public int save(T bean)
    throws DatabaseException {
        return delegate_.save(bean);
    }

    public int insert(T bean)
    throws DatabaseException {
        return delegate_.insert(bean);
    }

    public int update(T bean)
    throws DatabaseException {
        return delegate_.update(bean);
    }

    public List<T> restore()
    throws DatabaseException {
        return delegate_.restore();
    }

    public T restore(int objectId)
    throws DatabaseException {
        return delegate_.restore(objectId);
    }

    public List<T> restore(RestoreQuery query)
    throws DatabaseException {
        return delegate_.restore(query);
    }

    public boolean restore(DbRowProcessor rowProcessor)
    throws DatabaseException {
        return delegate_.restore(rowProcessor);
    }

    public boolean restore(RowProcessor rowProcessor)
    throws DatabaseException {
        return delegate_.restore(rowProcessor);
    }

    public T restoreFirst(RestoreQuery query)
    throws DatabaseException {
        return delegate_.restoreFirst(query);
    }

    public boolean restore(RestoreQuery query, DbRowProcessor rowProcessor)
    throws DatabaseException {
        return delegate_.restore(query, rowProcessor);
    }

    public boolean restore(RestoreQuery query, RowProcessor rowProcessor)
    throws DatabaseException {
        return delegate_.restore(query, rowProcessor);
    }

    public CreateTable getInstallTableQuery()
    throws DatabaseException {
        return delegate_.getInstallTableQuery();
    }

    public RestoreQuery getRestoreQuery() {
        return delegate_.getRestoreQuery();
    }

    public RestoreQuery getRestoreQuery(int objectId) {
        return delegate_.getRestoreQuery(objectId);
    }

    public int count()
    throws DatabaseException {
        return delegate_.count();
    }

    public int count(CountQuery query)
    throws DatabaseException {
        return delegate_.count(query);
    }

    public CountQuery getCountQuery() {
        return delegate_.getCountQuery();
    }

    public boolean delete(int objectId)
    throws DatabaseException {
        return delegate_.delete(objectId);
    }

    public boolean delete(DeleteQuery query)
    throws DatabaseException {
        return delegate_.delete(query);
    }

    public DeleteQuery getDeleteQuery() {
        return delegate_.getDeleteQuery();
    }

    public DeleteQuery getDeleteQuery(int objectId) {
        return delegate_.getDeleteQuery(objectId);
    }

    public void addListener(GenericQueryManagerListener listener) {
        delegate_.addListener(listener);
    }

    public void removeListeners() {
        delegate_.removeListeners();
    }

    public <OtherBeanType> GenericQueryManager<OtherBeanType> createNewManager(Class<OtherBeanType> type) {
        return delegate_.createNewManager(type);
    }
}

