/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;

abstract class AbstractQuery implements Query, Cloneable {
    protected Datasource datasource_ = null;
    protected String sql_ = null;
    protected boolean excludeUnsupportedCapabilities_ = false;

    private AbstractQuery() {
    }

    protected AbstractQuery(Datasource datasource) {
        assert datasource != null;

        datasource_ = datasource;
    }

    public Datasource getDatasource() {
        return datasource_;
    }

    public QueryParameters getParameters() {
        return null;
    }

    public void setExcludeUnsupportedCapabilities(boolean flag) {
        excludeUnsupportedCapabilities_ = flag;
    }

    public void clear() {
        sql_ = null;
    }

    protected void clearGenerated() {
        sql_ = null;
    }

    public String toString() {
        return getSql();
    }

    public AbstractQuery clone() {
        AbstractQuery new_instance = null;
        try {
            new_instance = (AbstractQuery) super.clone();
        } catch (CloneNotSupportedException e) {
            new_instance = null;
        }

        return new_instance;
    }
}

