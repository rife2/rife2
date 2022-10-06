/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import java.util.HashMap;
import java.util.Map;

import rife.database.queries.Query;
import rife.database.queries.QueryParameters;

/**
 * Internal class to handle virtual parameters of a
 * <code>DbPreparedStatement</code>.
 *
 * @author Geert Bevin <gbevin[remove] at uwyn dot com>
 * @since 1.0
 */
public class VirtualParameters {
    private QueryParameters parameters_ = null;
    private Map<Integer, Integer> indexMapping_ = null;
    private Map<Integer, Object> values_ = null;
    private VirtualParametersHandler handler_ = null;

    /**
     * Creates a new <code>VirtualParameters</code> instance.
     *
     * @param parameters the actual parameters that are virtual.
     * @param handler    the <code>VirtualParametersHandler</code> that will
     *                   be used by the {@link #callHandler(DbPreparedStatement)} method.
     * @since 1.0
     */
    public VirtualParameters(QueryParameters parameters, VirtualParametersHandler handler) {
        if (null == parameters) throw new IllegalArgumentException("parameters can't be null.");
        if (null == handler) throw new IllegalArgumentException("handler can't be null.");

        parameters_ = parameters;
        handler_ = handler;
    }

    /**
     * Calls the registered <code>VirtualParametersHandler</code>. This is
     * typically called when all virtual parameters have been defined in a
     * prepared statement and the statement is ready to be executed.
     *
     * @param statement the prepared statement that has all the virtual
     *                  parameters defined.
     * @since 1.0
     */
    public void callHandler(DbPreparedStatement statement) {
        handler_.handleValues(statement);
    }

    void setup(Query query) {
        indexMapping_ = query.getParameters().getVirtualIndexMapping(parameters_);
    }

    Object getValue(int index) {
        if (null == values_) {
            return null;
        }

        return values_.get(index);
    }

    boolean hasValue(int index) {
        if (null == values_) {
            return false;
        }

        return values_.containsKey(index);
    }

    boolean hasParameter(int index) {
        if (null == indexMapping_) {
            return false;
        }

        return indexMapping_.containsKey(index);
    }

    int getRealIndex(int index) {
        if (null == indexMapping_) {
            return -1;
        }

        return indexMapping_.get(index);
    }

    void putValue(int index, Object value) {
        if (null == values_) {
            values_ = new HashMap<Integer, Object>();
        }

        values_.put(index, value);
    }
}
