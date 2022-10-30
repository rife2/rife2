/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import rife.database.DbPreparedStatement;
import rife.database.exceptions.DatabaseException;
import rife.validation.Constrained;

import java.sql.ResultSet;

public interface SqlConversion {
    public String getSqlValue(Object value);

    public String getSqlType(Class type, int precision, int scale);

    public Object getTypedObject(ResultSet resultSet, int columnNumber, int type, Class targetType)
    throws DatabaseException;

    public void setTypedParameter(DbPreparedStatement statement, int parameterIndex, Class targetType, String name, Object value, Constrained constrained)
    throws DatabaseException;
}
