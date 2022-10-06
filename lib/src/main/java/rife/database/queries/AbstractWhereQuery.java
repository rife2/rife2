/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.exceptions.DbQueryException;
import rife.tools.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWhereQuery<QueryType extends AbstractWhereQuery> extends AbstractParametrizedQuery implements WhereQuery<QueryType>, Cloneable {
    protected StringBuilder where_ = null;

    AbstractWhereQuery(Datasource datasource) {
        super(datasource);

        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");

        clear();
    }

    public void clear() {
        super.clear();

        where_ = new StringBuilder();
    }

    public String getWhere() {
        return where_.toString();
    }

    public QueryType whereSubselect(Select query) {
        _whereSubselect(query);

        return (QueryType) this;
    }

    public QueryType where(String where) {
        if (null == where) throw new IllegalArgumentException("where can't be null.");
        if (0 == where.length()) throw new IllegalArgumentException("where can't be empty.");

        clearGenerated();
        clearWhereParameters();

        where_ = new StringBuilder(where);

        return (QueryType) this;
    }

    public WhereGroup<QueryType> startWhere() {
        return new WhereGroup<QueryType>(getDatasource(), this);
    }

    public QueryType whereAnd(String where) {
        if (null == where) throw new IllegalArgumentException("where can't be null.");
        if (0 == where.length()) throw new IllegalArgumentException("where can't be empty.");
        if (0 == where_.length())
            throw new IllegalArgumentException("can't perform whereAnd as initial where operation.");

        clearGenerated();

        where_.append(" AND ");
        where_.append(where);

        return (QueryType) this;
    }

    public WhereGroupAnd<QueryType> startWhereAnd() {
        return new WhereGroupAnd<QueryType>(getDatasource(), this);
    }

    public QueryType whereOr(String where) {
        if (null == where) throw new IllegalArgumentException("where can't be null.");
        if (0 == where.length()) throw new IllegalArgumentException("where can't be empty.");
        if (0 == where_.length())
            throw new IllegalArgumentException("can't perform whereOr as initial where operation.");

        clearGenerated();

        where_.append(" OR ");
        where_.append(where);

        return (QueryType) this;
    }

    public WhereGroupOr<QueryType> startWhereOr() {
        return new WhereGroupOr<QueryType>(getDatasource(), this);
    }

    public QueryType where(String field, String operator, boolean value) {
        return where(field, operator, Boolean.valueOf(value));
    }

    public QueryType where(String field, String operator, Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ");
        where_.append("(");
        where_.append(query.toString());
        where_.append(")");

        whereSubselect(query);

        return (QueryType) this;
    }

    public QueryType where(String field, String operator, Object value) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");

        clearGenerated();
        clearWhereParameters();

        where_ = new StringBuilder();
        _where(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, boolean value) {
        return whereAnd(field, operator, Boolean.valueOf(value));
    }

    public QueryType whereAnd(String field, String operator, Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        where_.append(" AND ");
        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ");
        where_.append("(");
        where_.append(query.toString());
        where_.append(")");

        whereSubselect(query);

        return (QueryType) this;
    }

    public QueryType whereAnd(String field, String operator, Object value) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");

        clearGenerated();

        where_.append(" AND ");
        _where(field, operator, value);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, boolean value) {
        return whereOr(field, operator, Boolean.valueOf(value));
    }

    public QueryType whereOr(String field, String operator, Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        where_.append(" OR ");
        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ");
        where_.append("(");
        where_.append(query.toString());
        where_.append(")");

        whereSubselect(query);

        return (QueryType) this;
    }

    public QueryType whereOr(String field, String operator, Object value) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");

        clearGenerated();

        where_.append(" OR ");
        _where(field, operator, value);

        return (QueryType) this;
    }

    private void _where(String field, String operator, Object value) {
        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ");
        where_.append(datasource_.getSqlConversion().getSqlValue(value));
    }

    public QueryType whereParameter(String field, String operator) {
        return whereParameter(field, field, operator);
    }

    public QueryType whereParameter(String field, String alias, String operator) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");

        clearGenerated();
        clearWhereParameters();

        where_ = new StringBuilder(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ?");

        addWhereParameter(alias);

        return (QueryType) this;
    }

    public QueryType whereParameterAnd(String field, String operator) {
        return whereParameterAnd(field, field, operator);
    }

    public QueryType whereParameterAnd(String field, String alias, String operator) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");
        if (0 == where_.length())
            throw new IllegalArgumentException("can't perform whereParameterAnd as initial where operation.");

        clearGenerated();

        where_.append(" AND ");
        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ?");

        addWhereParameter(alias);

        return (QueryType) this;
    }

    public QueryType whereParameterOr(String field, String operator) {
        return whereParameterOr(field, field, operator);
    }

    public QueryType whereParameterOr(String field, String alias, String operator) {
        if (null == field) throw new IllegalArgumentException("field can't be null.");
        if (0 == field.length()) throw new IllegalArgumentException("field can't be empty.");
        if (null == alias) throw new IllegalArgumentException("alias can't be null.");
        if (0 == alias.length()) throw new IllegalArgumentException("alias can't be empty.");
        if (null == operator) throw new IllegalArgumentException("operator can't be null.");
        if (0 == operator.length()) throw new IllegalArgumentException("operator can't be empty.");
        if (0 == where_.length())
            throw new IllegalArgumentException("can't perform whereParameterOr as initial where operation.");

        clearGenerated();

        where_.append(" OR ");
        where_.append(field);
        where_.append(" ");
        where_.append(operator);
        where_.append(" ?");

        addWhereParameter(alias);

        return (QueryType) this;
    }

    public QueryType where(Object bean)
    throws DbQueryException {
        return whereFiltered(bean, null, null);
    }

    public QueryType whereIncluded(Object bean, String[] includedFields)
    throws DbQueryException {
        return whereFiltered(bean, includedFields, null);
    }

    public QueryType whereExcluded(Object bean, String[] excludedFields)
    throws DbQueryException {
        return whereFiltered(bean, null, excludedFields);
    }

    public QueryType whereFiltered(Object bean, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");

        // TODO
//		Constrained constrained = ConstrainedUtils.makeConstrainedInstance(bean);

        ArrayList<String> where_parts = new ArrayList<String>();
        Map<String, String> property_values = QueryHelper.getBeanPropertyValues(bean, includedFields, excludedFields, getDatasource());

        for (String property_name : property_values.keySet()) {
//			if (!ConstrainedUtils.persistConstrainedProperty(constrained, property_name, null))
//			{
//				continue;
//			}

            where_parts.add(property_name + " = " + property_values.get(property_name));
        }

        where(StringUtils.join(where_parts, " AND "));

        return (QueryType) this;
    }

    public QueryType whereParameters(Class beanClass)
    throws DbQueryException {
        return whereParametersExcluded(beanClass, null);
    }

    public QueryType whereParametersExcluded(Class beanClass, String[] excludedFields)
    throws DbQueryException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        clearGenerated();

        // TODO
//		Constrained constrained = ConstrainedUtils.getConstrainedInstance(beanClass);

        Set<String> property_names = QueryHelper.getBeanPropertyNames(beanClass, excludedFields);

        for (String property_name : property_names) {
//			if (!ConstrainedUtils.persistConstrainedProperty(constrained, property_name, null))
//			{
//				continue;
//			}

            if (null == getWhereParameters()) {
                whereParameter(property_name, "=");
            } else {
                whereParameterAnd(property_name, "=");
            }
        }

        return (QueryType) this;
    }

    public QueryType clone() {
        AbstractWhereQuery new_instance = (AbstractWhereQuery) super.clone();
        if (new_instance != null) {
            if (where_ != null) {
                new_instance.where_ = new StringBuilder(where_.toString());
            }
        }

        return (QueryType) new_instance;
    }
}


