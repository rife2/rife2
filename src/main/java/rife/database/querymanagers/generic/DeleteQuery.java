/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.capabilities.Capabilities;
import rife.database.exceptions.DbQueryException;
import rife.database.queries.AbstractWhereDelegateQuery;
import rife.database.queries.Delete;
import rife.database.queries.Query;
import rife.database.queries.QueryParameters;

public class DeleteQuery extends AbstractWhereDelegateQuery<DeleteQuery, Delete> implements Query, Cloneable {
    private Delete delegatePristine_ = null;

    public String toString() {
        return getSql();
    }

    public DeleteQuery clone() {
        return new DeleteQuery(delegate_.clone());
    }

    public DeleteQuery(Delete query) {
        super(query.clone());
        delegatePristine_ = query.clone();
    }

    public String getSql()
    throws DbQueryException {
        return delegate_.getSql();
    }

    public void clear() {
        delegate_ = delegatePristine_.clone();
    }

    public QueryParameters getParameters() {
        return delegate_.getParameters();
    }

    public Capabilities getCapabilities() {
        return delegate_.getCapabilities();
    }

    public void setExcludeUnsupportedCapabilities(boolean flag) {
        delegate_.setExcludeUnsupportedCapabilities(flag);
    }

    public String getFrom() {
        return delegate_.getFrom();
    }
}

