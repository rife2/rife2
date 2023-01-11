/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;

import java.util.List;

abstract class AbstractParametrizedQuery extends AbstractQuery implements Query, Cloneable {
    private QueryParameters parameters_ = null;

    protected AbstractParametrizedQuery(Datasource datasource) {
        super(datasource);
    }

    public void clear() {
        super.clear();

        if (parameters_ != null) {
            parameters_.clear();
        }
    }

    private void addTypedParameters(QueryParameterType type, QueryParameters parameters) {
        if (null == parameters) {
            return;
        }

        addTypedParameters(type, parameters.getOrderedNames());
    }

    private void addTypedParameters(QueryParameterType type, List<String> parameters) {
        if (null == parameters_) {
            parameters_ = new QueryParameters(this);
        }

        parameters_.addTypedParameters(type, parameters);
    }

    private void addTypedParameter(QueryParameterType type, String parameter) {
        if (null == parameters_) {
            parameters_ = new QueryParameters(this);
        }

        parameters_.addTypedParameter(type, parameter);
    }

    private <T> T getTypedParameters(QueryParameterType type) {
        if (null == parameters_) {
            return null;
        }

        return (T) parameters_.getTypedParameters(type);
    }

    private void clearTypedParameters(QueryParameterType type) {
        if (null == parameters_) {
            return;
        }

        parameters_.clearTypedParameters(type);
        if (0 == parameters_.getNumberOfTypes()) {
            parameters_ = null;
        }
    }

    protected void _fieldSubselect(Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        addTypedParameters(QueryParameterType.FIELD, query.getParameters());
    }

    protected void _tableSubselect(Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        addTypedParameters(QueryParameterType.TABLE, query.getParameters());
    }

    protected void _whereSubselect(Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        addTypedParameters(QueryParameterType.WHERE, query.getParameters());
    }

    protected void _unionSubselect(Select query) {
        if (null == query) throw new IllegalArgumentException("query can't be null.");

        addTypedParameters(QueryParameterType.UNION, query.getParameters());
    }

    public QueryParameters getParameters() {
        return parameters_;
    }

    protected void addFieldParameter(String field) {
        addTypedParameter(QueryParameterType.FIELD, field);
    }

    protected void clearWhereParameters() {
        clearTypedParameters(QueryParameterType.WHERE);
    }

    protected void addWhereParameter(String field) {
        addTypedParameter(QueryParameterType.WHERE, field);
    }

    protected List<String> getWhereParameters() {
        return getTypedParameters(QueryParameterType.WHERE);
    }

    public void addWhereParameters(List<String> parameters) {
        addTypedParameters(QueryParameterType.WHERE, parameters);
    }

    protected void setLimitParameter(String limitParameter) {
        addTypedParameter(QueryParameterType.LIMIT, limitParameter);
    }

    public String getLimitParameter() {
        return getTypedParameters(QueryParameterType.LIMIT);
    }

    protected void setOffsetParameter(String offsetParameter) {
        addTypedParameter(QueryParameterType.OFFSET, offsetParameter);
    }

    public String getOffsetParameter() {
        return getTypedParameters(QueryParameterType.OFFSET);
    }

    protected boolean isLimitBeforeOffset() {
        return true;
    }

    public AbstractParametrizedQuery clone() {
        AbstractParametrizedQuery new_instance = (AbstractParametrizedQuery) super.clone();

        if (new_instance != null &&
            parameters_ != null) {
            new_instance.parameters_ = parameters_.clone();
        }

        return new_instance;
    }
}
