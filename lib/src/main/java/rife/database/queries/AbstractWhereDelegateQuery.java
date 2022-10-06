/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.exceptions.DbQueryException;

import java.util.List;

public abstract class AbstractWhereDelegateQuery<QueryType extends AbstractWhereDelegateQuery, DelegateType extends AbstractWhereQuery> implements WhereQuery<QueryType> {
    protected DelegateType delegate_ = null;

    protected AbstractWhereDelegateQuery(DelegateType delegate) {
        delegate_ = delegate;
    }

    public DelegateType getDelegate() {
        return delegate_;
    }

    public Datasource getDatasource() {
        return delegate_.getDatasource();
    }

    public WhereGroup<QueryType> startWhere() {
        return new WhereGroup<QueryType>(getDatasource(), this);
    }

    public WhereGroupAnd<QueryType> startWhereAnd() {
        return new WhereGroupAnd<QueryType>(getDatasource(), this);
    }

    public WhereGroupOr<QueryType> startWhereOr() {
        return new WhereGroupOr<QueryType>(getDatasource(), this);
    }

    public QueryType where(String where) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(where);
        } else {
            delegate_.where(where);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, boolean value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, byte value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, char value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, double value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, float value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, int value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, long value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, Select query) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, query);
        } else {
            delegate_.where(field, operator, query);
        }

        return (QueryType) this;
    }


    public QueryType where(String field, String operator, Object value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, short value) {
        if (delegate_.getWhere().length() > 0) {
            delegate_.whereAnd(field, operator, value);
        } else {
            delegate_.where(field, operator, value);
        }

        return (QueryType) this;
    }

    public QueryType whereAnd(String where) {
        delegate_.whereAnd(where);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, boolean value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, byte value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, char value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, double value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, float value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, int value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, long value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, Select query) {
        delegate_.whereAnd(field, operator, query);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, Object value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, short value) {
        delegate_.whereAnd(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String where) {
        delegate_.whereOr(where);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, boolean value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, byte value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, char value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, double value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, float value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, int value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, long value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, Select query) {
        delegate_.whereOr(field, operator, query);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, Object value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, short value) {
        delegate_.whereOr(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereSubselect(Select query) {
        delegate_.whereSubselect(query);

        return (QueryType) this;
    }

    public QueryType where(Object bean)
    throws DbQueryException {
        delegate_.where(bean);

        return (QueryType) this;
    }

    public QueryType whereIncluded(Object bean, String[] includedFields)
    throws DbQueryException {
        delegate_.whereIncluded(bean, includedFields);

        return (QueryType) this;
    }

    public QueryType whereExcluded(Object bean, String[] excludedFields)
    throws DbQueryException {
        delegate_.whereExcluded(bean, excludedFields);

        return (QueryType) this;
    }

    public QueryType whereFiltered(Object bean, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        delegate_.whereFiltered(bean, includedFields, excludedFields);

        return (QueryType) this;
    }

    public void addWhereParameters(List<String> parameters) {
        delegate_.addWhereParameters(parameters);
    }
}
