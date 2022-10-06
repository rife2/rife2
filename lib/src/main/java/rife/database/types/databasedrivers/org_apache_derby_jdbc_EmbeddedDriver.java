/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types.databasedrivers;

import java.sql.*;

import rife.database.types.SqlArrays;
import rife.database.types.SqlConversion;
import rife.database.types.SqlNull;
import rife.tools.ClassUtils;
import rife.tools.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class org_apache_derby_jdbc_EmbeddedDriver extends Common implements SqlConversion {
    public String getSqlValue(Object value) {
        // handle the null value
        if (null == value ||
            SqlNull.NULL == value) {
            return SqlNull.NULL.toString();
        }
        // make sure that strings are escaped correctly
        else if (value instanceof CharSequence) {
            if (0 == ((CharSequence) value).length()) {
                return "''";
            } else {
                return "'" + StringUtils.encodeSql(value.toString()) + "'";
            }
        } else if (value instanceof Character) {
            if ((Character) value == 0) {
                return SqlNull.NULL.toString();
            } else {
                return "'" + StringUtils.encodeSql(value.toString()) + "'";
            }
        }
        // handle the numbers
        else if (ClassUtils.isNumeric(value.getClass())) {
            return value.toString();
        }
        // handle the time / date types
        else if (value instanceof Time) {
            return "'" + StringUtils.encodeSql(value.toString()) + "'";
        } else if (value instanceof Timestamp) {
            return "'" + StringUtils.encodeSql(value.toString()) + "'";
        } else if (value instanceof java.sql.Date) {
            return "'" + StringUtils.encodeSql(value.toString()) + "'";
        } else if (value instanceof Date) {
            return "'" + StringUtils.encodeSql(new Timestamp(((Date) value).getTime()).toString()) + "'";
        } else if (value instanceof Calendar) {
            return "'" + StringUtils.encodeSql(new Timestamp(((Calendar) value).getTime().getTime()).toString()) + "'";
        }
        // make sure that the Boolean type is correctly caught
        else if (value instanceof Boolean) {
            if ((Boolean) value) {
                return "1";
            } else {
                return "0";
            }
        }
        // make sure that the object arrays are correctly caught
        else if (value instanceof Object[]) {
            return SqlArrays.convertArray((Object[]) value);
        }
        // just return the other types through their toString() method
        else {
            return "'" + StringUtils.encodeSql(value.toString()) + "'";
        }
    }

    protected Object retrieveFieldObject(ResultSet resultSet, int columnNumber, int type)
    throws SQLException {
        assert resultSet != null;
        assert columnNumber > 0;

        Object result = null;

        if (type == Types.BIT || type == Types.BOOLEAN) {
            boolean value = resultSet.getBoolean(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.TINYINT) {
            byte value = resultSet.getByte(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.SMALLINT || type == Types.INTEGER) {
            int value = resultSet.getInt(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.BIGINT) {
            long value = resultSet.getLong(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR) {
            result = resultSet.getString(columnNumber);
        } else if (type == Types.DATE) {
            result = resultSet.getDate(columnNumber);
        } else if (type == Types.TIME) {
            result = resultSet.getTime(columnNumber);
        } else if (type == Types.TIMESTAMP) {
            result = resultSet.getTimestamp(columnNumber);
        } else if (type == Types.NUMERIC || type == Types.DECIMAL) {
            result = resultSet.getBigDecimal(columnNumber);
        } else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL) {
            double value = resultSet.getDouble(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.BLOB) {
            result = resultSet.getBlob(columnNumber);
        } else if (type == Types.CLOB) {
            result = resultSet.getClob(columnNumber);
        } else if (type == Types.REF) {
            result = resultSet.getRef(columnNumber);
        } else if (type == Types.JAVA_OBJECT || type == Types.OTHER) {
            result = resultSet.getObject(columnNumber);
        } else if (type == Types.ARRAY) {
            result = resultSet.getArray(columnNumber);
        } else if (type == Types.BINARY || type == Types.LONGVARBINARY || type == Types.VARBINARY) {
            result = resultSet.getBinaryStream(columnNumber);
        }

        return result;
    }

    public String getSqlType(Class type, int precision, int scale) {
        // handle character types
        if (type == String.class ||
            type == StringBuilder.class ||
            type == StringBuffer.class) {
            if (precision < 0) {
                return "VARCHAR(32672)";
            } else {
                return "VARCHAR(" + precision + ")";
            }
        } else if (type == Character.class ||
            type == char.class) {
            if (precision < 0) {
                return "CHAR";
            } else {
                return "CHAR(" + precision + ")";
            }
        }
        // handle the time / date types
        else if (type == Time.class) {
            return "TIME";
        } else if (type == java.sql.Date.class) {
            return "DATE";
        } else if (type == Timestamp.class ||
            type == Date.class ||
            type == Calendar.class) {
            return "TIMESTAMP";
        }
        // make sure that the Boolean type is correctly caught
        else if (type == Boolean.class ||
            type == boolean.class) {
            return "NUMERIC(1)";
        }
        // make sure that the Integer types are correctly caught
        else if (type == Byte.class ||
            type == byte.class ||
            type == Short.class ||
            type == short.class) {
            return "SMALLINT";
        } else if (type == Integer.class ||
            type == int.class) {
            return "INTEGER";
        } else if (type == Long.class ||
            type == long.class) {
            return "BIGINT";
        }
        // make sure that the Float types are correctly caught
        else if (type == Double.class ||
            type == double.class ||
            type == Float.class ||
            type == float.class) {
            return "FLOAT";
        }
        // make sure that the BigDecimal type is correctly caught
        else if (type == BigDecimal.class) {
            if (precision < 0) {
                return "NUMERIC";
            } else if (scale < 0) {
                return "NUMERIC(" + precision + ")";
            } else {
                return "NUMERIC(" + precision + "," + scale + ")";
            }
        }
        // make sure that the Blob type is correctly caught
        else if (type == Blob.class ||
            type == byte[].class) {
            return "BLOB";
        }
        // make sure that the Clob type is correctly caught
        else if (type == Clob.class) {
            return "CLOB";
        } else {
            String result = handleCommonSqlType(type, precision, scale);
            if (result != null) {
                return result;
            }

            // just return a TEXT type in which the object's toString value will be stored
            return "LONG VARCHAR";
        }
    }
}

