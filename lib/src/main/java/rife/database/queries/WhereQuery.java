/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.exceptions.DbQueryException;

import java.util.List;

interface WhereQuery<QueryType extends WhereQuery> {
    QueryType whereSubselect(Select query);

    QueryType where(String where);

    QueryType whereAnd(String where);

    QueryType whereOr(String where);

    WhereGroup<QueryType> startWhere();

    WhereGroupAnd<QueryType> startWhereAnd();

    WhereGroupOr<QueryType> startWhereOr();

    QueryType where(String field, String operator, boolean value);

    QueryType where(String field, String operator, Select query);

    QueryType where(String field, String operator, Object value);

    QueryType whereAnd(String field, String operator, boolean value);

    QueryType whereAnd(String field, String operator, Select query);

    QueryType whereAnd(String field, String operator, Object value);

    QueryType whereOr(String field, String operator, boolean value);

    QueryType whereOr(String field, String operator, Select query);

    QueryType whereOr(String field, String operator, Object value);

    QueryType where(Object bean)
    throws DbQueryException;

    QueryType whereIncluded(Object bean, String[] includedFields)
    throws DbQueryException;

    QueryType whereExcluded(Object bean, String[] excludedFields)
    throws DbQueryException;

    QueryType whereFiltered(Object bean, String[] includedFields, String[] excludedFields)
    throws DbQueryException;

    void addWhereParameters(List<String> parameters);
}


