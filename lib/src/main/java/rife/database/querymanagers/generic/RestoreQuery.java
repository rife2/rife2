/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com> and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;


import rife.database.capabilities.Capabilities;
import rife.database.queries.AbstractWhereDelegateQuery;
import rife.database.queries.QueryParameters;
import rife.database.queries.ReadQuery;
import rife.database.queries.Select;

public class RestoreQuery extends AbstractWhereDelegateQuery<RestoreQuery, Select> implements ReadQuery, Cloneable {
    private Select delegatePristine_ = null;

    public String toString() {
        return getSql();
    }

    public RestoreQuery clone() {
        return new RestoreQuery(delegate_.clone());
    }

    public RestoreQuery(Select query) {
        super(query.clone());
        delegatePristine_ = query.clone();
    }

    public void clear() {
        delegate_ = delegatePristine_.clone();
    }

    public Capabilities getCapabilities() {
        return delegate_.getCapabilities();
    }

    public void setExcludeUnsupportedCapabilities(boolean flag) {
        delegate_.setExcludeUnsupportedCapabilities(flag);
    }

    public QueryParameters getParameters() {
        return delegate_.getParameters();
    }

    public String getSql() {
        return delegate_.getSql();
    }

    public RestoreQuery distinctOn(String column) {
        delegate_.distinctOn(column);

        return this;
    }

    public RestoreQuery distinctOn(String[] columns) {
        delegate_.distinctOn(columns);

        return this;
    }

    public String getFrom() {
        return delegate_.getFrom();
    }

    public RestoreQuery join(String table) {
        delegate_.join(table);

        return this;
    }

    public RestoreQuery joinCross(String table) {
        delegate_.joinCross(table);

        return this;
    }

    public RestoreQuery joinCustom(String customJoin) {
        delegate_.joinCustom(customJoin);

        return this;
    }

    public RestoreQuery joinInner(String table, Select.JoinCondition condition, String conditionExpression) {
        delegate_.joinInner(table, condition, conditionExpression);

        return this;
    }

    public RestoreQuery joinOuter(String table, Select.JoinType type, Select.JoinCondition condition, String conditionExpression) {
        delegate_.joinOuter(table, type, condition, conditionExpression);

        return this;
    }

    public RestoreQuery limit(int limit) {
        delegate_.limit(limit);

        return this;
    }

    public RestoreQuery offset(int offset) {
        delegate_.offset(offset);

        return this;
    }

    public RestoreQuery orderBy(String column) {
        delegate_.orderBy(column);

        return this;
    }

    public RestoreQuery orderBy(String column, Select.OrderByDirection direction) {
        delegate_.orderBy(column, direction);

        return this;
    }

    public RestoreQuery union(Select union) {
        delegate_.union(union);

        return this;
    }

    public RestoreQuery union(String union) {
        delegate_.union(union);

        return this;
    }

    public RestoreQuery field(String field) {
        delegate_.field(field);

        return this;
    }

    public RestoreQuery fields(Class bean) {
        delegate_.fields(bean);

        return this;
    }

    public RestoreQuery fieldsExcluded(Class bean, String... excluded) {
        delegate_.fieldsExcluded(bean, excluded);

        return this;
    }

    public RestoreQuery fields(String table, Class bean) {
        delegate_.fields(table, bean);

        return this;
    }

    public RestoreQuery fieldsExcluded(String table, Class bean, String... excluded) {
        delegate_.fieldsExcluded(table, bean, excluded);

        return this;
    }

    public RestoreQuery fields(String... fields) {
        delegate_.fields(fields);

        return this;
    }

    public RestoreQuery field(String alias, Select query) {
        delegate_.field(alias, query);

        return this;
    }

    public RestoreQuery fieldSubselect(Select query) {
        delegate_.fieldSubselect(query);

        return this;
    }
}
