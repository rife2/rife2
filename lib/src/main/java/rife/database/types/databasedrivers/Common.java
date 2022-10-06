/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types.databasedrivers;

import java.sql.*;

import rife.database.DbPreparedStatement;
import rife.database.exceptions.DatabaseException;
import rife.database.types.SqlConversion;
import rife.tools.FileUtils;
import rife.tools.JavaSpecificationUtils;
import rife.tools.StringUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public abstract class Common implements SqlConversion {
    protected abstract Object retrieveFieldObject(ResultSet resultSet, int columnNumber, int type)
    throws SQLException;

    /*
     *	UNSUPPORTED SQL TYPES :
     *	DATALINK
     *	DISTINCT
     *	NULL
     *	STRUCT
     */
    public Object getTypedObject(ResultSet resultSet, int columnNumber, int type, Class targetType)
    throws DatabaseException {
        if (null == resultSet) throw new IllegalArgumentException("resultSet can't be null.");
        if (columnNumber < 1) throw new IllegalArgumentException("columnNumber must be equal or bigger than 1.");
        if (null == targetType) throw new IllegalArgumentException("targetType can't be null.");

        try {
            Object field = retrieveFieldObject(resultSet, columnNumber, type);
            if (null == field) {
                return null;
            }

            Object typed = getTypedObject(field, targetType);
            if (null == typed) {
                throw new DatabaseException("Impossible to return a compatible typed object. The target type is '" + targetType.getName() + "' and the result type is '" + field.getClass().getName() + "'.");
            }

            return typed;
        } catch (SQLException e) {
            throw new DatabaseException("Unexpected exception while obtaining a typed object for column '" + columnNumber + "', type '" + type + "' and target type '" + targetType.getName() + "'", e);
        }
    }

    protected Object getTypedObject(Object result, Class targetType) {
        try {
            if (!targetType.isInstance(result)) {
                Class result_class = result.getClass();
                if (targetType == boolean.class && result_class == Boolean.class ||
                    targetType == byte.class && result_class == Byte.class ||
                    targetType == double.class && result_class == Double.class ||
                    targetType == float.class && result_class == Float.class ||
                    targetType == int.class && result_class == Integer.class ||
                    targetType == long.class && result_class == Long.class ||
                    targetType == short.class && result_class == Short.class ||
                    targetType == char.class && result_class == Character.class) {
                    return result;
                } else {
                    try {
                        if (targetType == boolean.class || targetType == Boolean.class) {
                            return StringUtils.convertToBoolean(result.toString());
                        } else if (targetType == byte.class || targetType == Byte.class) {
                            return Byte.valueOf(result.toString());
                        } else if (targetType == double.class || targetType == Double.class) {
                            return Double.valueOf(result.toString());
                        } else if (targetType == float.class || targetType == Float.class) {
                            return Float.valueOf(result.toString());
                        } else if (targetType == int.class || targetType == Integer.class) {
                            return Integer.valueOf(result.toString());
                        } else if (targetType == long.class || targetType == Long.class) {
                            return Long.valueOf(result.toString());
                        } else if (targetType == short.class || targetType == Short.class) {
                            return Short.valueOf(result.toString());
                        } else if (targetType == BigDecimal.class) {
                            return new BigDecimal(result.toString());
                        } else if (targetType == char.class || targetType == Character.class) {
                            return result.toString().charAt(0);
                        } else if (targetType == StringBuilder.class) {
                            return new StringBuilder(result.toString());
                        } else if (targetType == StringBuffer.class) {
                            return new StringBuffer(result.toString());
                        }
                        // convert the Timestamp type into other time / date types
                        else if (targetType == Calendar.class &&
                            (result_class == Timestamp.class ||
                                result instanceof Timestamp)) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime((Timestamp) result);
                            return cal;
                        } else if (targetType == Date.class &&
                            (result_class == Timestamp.class ||
                                result instanceof Timestamp)) {
                            return new Date(((Timestamp) result).getTime());
                        } else if (targetType == java.sql.Date.class &&
                            (result_class == Timestamp.class ||
                                result instanceof Timestamp)) {
                            return new java.sql.Date(((Timestamp) result).getTime());
                        } else if (targetType == Time.class &&
                            (result_class == Timestamp.class ||
                                result instanceof Timestamp)) {
                            return new Time(((Timestamp) result).getTime());
                        }
                        // convert the java.sql.Date type into other time / date types
                        else if (targetType == Calendar.class && result_class == java.sql.Date.class) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime((java.sql.Date) result);
                            return cal;
                        } else if (targetType == Date.class && result_class == java.sql.Date.class) {
                            return new Date(((java.sql.Date) result).getTime());
                        } else if (targetType == Time.class && result_class == java.sql.Date.class) {
                            return new Time(((java.sql.Date) result).getTime());
                        } else if (targetType == Timestamp.class && result_class == java.sql.Date.class) {
                            return new Timestamp(((java.sql.Date) result).getTime());
                        }
                        // convert the Time type into other time / date types
                        else if (targetType == Calendar.class && result_class == Time.class) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime((Time) result);
                            return cal;
                        } else if (targetType == Date.class && result_class == Time.class) {
                            return new Date(((Time) result).getTime());
                        } else if (targetType == java.sql.Date.class && result_class == Time.class) {
                            return new java.sql.Date(((Time) result).getTime());
                        } else if (targetType == Timestamp.class && result_class == Time.class) {
                            return new Timestamp(((Time) result).getTime());
                        } else if (targetType == byte[].class && Blob.class.isAssignableFrom(result_class)) {
                            Blob blob = (Blob) result;
                            return FileUtils.readBytes(blob.getBinaryStream());
                        } else if (targetType == byte[].class && InputStream.class.isAssignableFrom(result_class)) {
                            return FileUtils.readBytes((InputStream) result);
                        } else if (targetType.isEnum()) {
                            return Enum.valueOf(targetType, result.toString());
                        } else if (targetType == UUID.class) {
                            return result.toString();
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        throw new DatabaseException("Impossible to convert the result type '" + result_class.getName() + "' with value '" + result.toString() + "' to the target type '" + targetType.getName() + "'.", e);
                    }
                }
            } else {
                return result;
            }
        } catch (Throwable e) {
            throw new DatabaseException("Unexpected exception while obtaining a type object for object '" + result + "' for target type '" + targetType.getName() + "'", e);
        }
    }

    public void setTypedParameter(DbPreparedStatement statement, int parameterIndex, Class targetType, String name, Object value/*, Constrained constrained*/)
    throws DatabaseException {
        if (null == statement) throw new IllegalArgumentException("statement can't be null.");
        if (parameterIndex < 1) throw new IllegalArgumentException("parameterIndex must be at least 1.");
        if (null == targetType) throw new IllegalArgumentException("targetType can't be null.");

        if (targetType == String.class ||
            targetType == StringBuffer.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.VARCHAR);
            } else {
                // TODO
//				if (constrained != null)
//				{
//					ConstrainedProperty property = constrained.getConstrainedProperty(name);
//					if (property != null &&
//						!property.hasMaxLength())
//					{
//						String string_value = value.toString();
//						statement.setCharacterStream(parameterIndex, new StringReader(string_value), string_value.length());
//					}
//					else
//					{
//						statement.setString(parameterIndex, value.toString());
//					}
//				}
//				else
                {
                    statement.setString(parameterIndex, value.toString());
                }
            }
        } else if (targetType == Character.class ||
            targetType == char.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.CHAR);
            } else {
                statement.setString(parameterIndex, value.toString());
            }
        } else if (targetType == Time.class ||
            Time.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.TIME);
            } else {
                statement.setTime(parameterIndex, (Time) value);
            }
        } else if (targetType == java.sql.Date.class ||
            java.sql.Date.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.DATE);
            } else {
                statement.setDate(parameterIndex, (java.sql.Date) value);
            }
        } else if (targetType == Date.class ||
            Date.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(parameterIndex, new java.sql.Timestamp(((Date) value).getTime()));
            }
        } else if (targetType == Calendar.class ||
            Calendar.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(parameterIndex, new java.sql.Timestamp(((Calendar) value).getTime().getTime()));
            }
        } else if (targetType == Timestamp.class ||
            Timestamp.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(parameterIndex, (Timestamp) value);
            }
        }
        // make sure that the Boolean type is correctly caught
        else if (targetType == Boolean.class ||
            targetType == boolean.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.BOOLEAN);
            } else {
                statement.setBoolean(parameterIndex, (Boolean) value);
            }
        }
        // make sure that the Integer types are correctly caught
        else if (targetType == Byte.class ||
            targetType == byte.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.TINYINT);
            } else {
                statement.setByte(parameterIndex, (Byte) value);
            }
        } else if (targetType == Short.class ||
            targetType == short.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.SMALLINT);
            } else {
                statement.setShort(parameterIndex, (Short) value);
            }
        } else if (targetType == Integer.class ||
            targetType == int.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.INTEGER);
            } else {
                statement.setInt(parameterIndex, (Integer) value);
            }
        } else if (targetType == Long.class ||
            targetType == long.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.BIGINT);
            } else {
                statement.setLong(parameterIndex, (Long) value);
            }
        }
        // make sure that the Float types are correctly caught
        else if (targetType == Float.class ||
            targetType == float.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.FLOAT);
            } else {
                statement.setFloat(parameterIndex, (Float) value);
            }
        } else if (targetType == Double.class ||
            targetType == double.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.DOUBLE);
            } else {
                statement.setDouble(parameterIndex, (Double) value);
            }
        } else if (targetType == BigDecimal.class ||
            BigDecimal.class.isAssignableFrom(targetType)) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.NUMERIC);
            } else {
                statement.setBigDecimal(parameterIndex, (BigDecimal) value);
            }
        } else if (targetType == byte[].class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.ARRAY);
            } else {
                statement.setBytes(parameterIndex, (byte[]) value);
            }
        } else if (targetType.isEnum()) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.VARCHAR);
            } else {
                statement.setString(parameterIndex, value.toString());
            }
        } else if (targetType == UUID.class) {
            if (null == value) {
                statement.setNull(parameterIndex, Types.VARCHAR);
            } else {
                statement.setString(parameterIndex, value.toString());
            }
        } else {
            throw new DatabaseException("Setting a typed parameter is not supported for index '" + parameterIndex + "', target type '" + targetType.getName() + " and value '" + value + "'.");
        }
    }

    public String handleCommonSqlType(Class type, int precision, int scale) {
        if (type.isEnum()) {
            return "VARCHAR(255)";
        } else if (type == UUID.class) {
            return "VARCHAR(36)";
        }

        return null;
    }
}
