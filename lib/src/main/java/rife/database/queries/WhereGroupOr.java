/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.capabilities.Capabilities;

public class WhereGroupOr<ParentType extends WhereQuery> extends AbstractWhereGroup<ParentType> {
    public WhereGroupOr(Datasource datasource, WhereQuery parent) {
        super(datasource, parent);
    }

    public Capabilities getCapabilities() {
        return null;
    }

    public ParentType end() {
        var where = new StringBuilder();

        where.append("(");
        where.append(getSql());
        where.append(")");

        parent_.whereOr(where.toString());

        parent_.addWhereParameters(getWhereParameters());

        return (ParentType) parent_;
    }
}
