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

public class CountQuery extends AbstractWhereDelegateQuery<CountQuery, Select> implements ReadQuery, Cloneable {
    private Select delegatePristine_ = null;

    public String toString() {
        return getSql();
    }

    public CountQuery clone() {
        return new CountQuery(delegate_.clone());
    }

    public CountQuery(Select query) {
        super(query.clone());
        delegatePristine_ = query.clone();
    }

    public void clear() {
        delegate_ = delegatePristine_.clone();
    }

    public String getFrom() {
        return delegate_.getFrom();
    }

    public QueryParameters getParameters() {
        return delegate_.getParameters();
    }

    public String getSql() {
        return delegate_.getSql();
    }

    public Capabilities getCapabilities() {
        return delegate_.getCapabilities();
    }

    public void setExcludeUnsupportedCapabilities(boolean flag) {
        delegate_.setExcludeUnsupportedCapabilities(flag);
    }

    public CountQuery join(String table) {
        delegate_.join(table);

        return this;
    }

    public CountQuery joinCross(String table) {
        delegate_.joinCross(table);

        return this;
    }

    public CountQuery joinCustom(String customJoin) {
        delegate_.joinCustom(customJoin);

        return this;
    }

    public CountQuery joinInner(String table, Select.JoinCondition condition, String conditionExpression) {
        delegate_.joinInner(table, condition, conditionExpression);

        return this;
    }

    public CountQuery joinOuter(String table, Select.JoinType type, Select.JoinCondition condition, String conditionExpression) {
        delegate_.joinOuter(table, type, condition, conditionExpression);

        return this;
    }

    public CountQuery union(Select union) {
        delegate_.union(union);

        return this;
    }

    public CountQuery union(String union) {
        delegate_.union(union);

        return this;
    }

    public Select getDelegate() {
        return delegate_;
    }
}
