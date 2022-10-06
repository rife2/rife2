/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.exceptions;

import java.io.Serial;

public class UnsupportedSqlFeatureException extends DbQueryException {
    @Serial private static final long serialVersionUID = 6597682956243876788L;

    private final String feature_;
    private final String driver_;

    public UnsupportedSqlFeatureException(String feature, String driver) {
        super("The '" + feature + "' feature isn't supported by the driver '" + driver + "'.");
        feature_ = feature;
        driver_ = driver;
    }

    public String getFeature() {
        return feature_;
    }

    public String getDriver() {
        return driver_;
    }
}
