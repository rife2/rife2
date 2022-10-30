/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.datastructures.EnumClass;

public class QueryParameterType extends EnumClass<String> {
    public static final QueryParameterType FIELD = new QueryParameterType("FIELD", false);
    public static final QueryParameterType TABLE = new QueryParameterType("TABLE", false);
    public static final QueryParameterType WHERE = new QueryParameterType("WHERE", false);
    public static final QueryParameterType UNION = new QueryParameterType("UNION", false);
    public static final QueryParameterType LIMIT = new QueryParameterType("LIMIT", true);
    public static final QueryParameterType OFFSET = new QueryParameterType("OFFSET", true);

    private final boolean singular_;

    private QueryParameterType(String identifier, boolean singular) {
        super(identifier);

        singular_ = singular;
    }

    public boolean isSingular() {
        return singular_;
    }
}

