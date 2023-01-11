/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;

public abstract class AbstractWhereGroup<ParentType extends WhereQuery>
    extends AbstractWhereQuery<AbstractWhereGroup<ParentType>>
    implements Cloneable {
    protected WhereQuery parent_ = null;

    protected AbstractWhereGroup(Datasource datasource, WhereQuery parent) {
        super(datasource);

        parent_ = parent;
    }

    public ParentType end() {
        parent_.whereAnd("(" + getSql() + ")");
        parent_.addWhereParameters(getWhereParameters());

        return (ParentType) parent_;
    }

    public String getSql() {
        return where_.toString();
    }

    public AbstractWhereGroup<ParentType> clone() {
        return (AbstractWhereGroup<ParentType>) super.clone();
    }
}
