/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types.databasedrivers;

import java.sql.*;

import rife.database.types.SqlArrays;
import rife.database.types.SqlConversion;
import rife.database.types.SqlNull;
import rife.tools.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

public class oracle_jdbc_driver_OracleDriver extends Common implements SqlConversion {
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
            return "TO_DATE('" + StringUtils.encodeSql(value.toString()) + "', 'HH24:MI:SS')";
        } else if (value instanceof Timestamp) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(value)) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof java.sql.Date) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd 00:00:00");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(value)) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof Date date) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(Convert.toSqlTimestamp(date))) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof Calendar cal) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(Convert.toSqlTimestamp(cal))) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof Instant instant) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(Convert.toSqlTimestamp(instant))) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof LocalDateTime local) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(Convert.toSqlTimestamp(local))) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof LocalDate local) {
            var dateformat = new SimpleDateFormat("yyyy/MM/dd 00:00:00");
            return "TO_DATE('" + StringUtils.encodeSql(dateformat.format(Convert.toSqlDate(local))) + "', 'YYYY/MM/DD HH24:MI:SS')";
        } else if (value instanceof LocalTime local) {
            return "TO_DATE('" + StringUtils.encodeSql(Convert.toSqlTime(local).toString()) + "', 'HH24:MI:SS')";
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
            var value = resultSet.getBoolean(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.TINYINT) {
            var value = resultSet.getByte(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.SMALLINT || type == Types.INTEGER) {
            var value = resultSet.getInt(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.BIGINT) {
            var value = resultSet.getLong(columnNumber);
            if (!resultSet.wasNull()) {
                result = value;
            }
        } else if (type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGVARCHAR) {
            result = resultSet.getString(columnNumber);
        } else if (type == Types.DATE) {
            result = resultSet.getTimestamp(columnNumber);
        } else if (type == Types.TIME) {
            result = resultSet.getTime(columnNumber);
        } else if (type == Types.TIMESTAMP) {
            result = resultSet.getTimestamp(columnNumber);
        } else if (type == Types.NUMERIC || type == Types.DECIMAL) {
            result = resultSet.getBigDecimal(columnNumber);
        } else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL) {
            var value = resultSet.getDouble(columnNumber);
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
                return "VARCHAR2(4000)";
            } else {
                return "VARCHAR2(" + precision + ")";
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
        else if (type == Time.class ||
                 type == LocalTime.class) {
            return "DATE";
        } else if (type == java.sql.Date.class ||
                   type == LocalDate.class) {
            return "DATE";
        } else if (type == Timestamp.class ||
                   type == Date.class ||
                   type == Calendar.class ||
                   type == Instant.class ||
                   type == LocalDateTime.class) {
            return "DATE";
        }
        // make sure that the Boolean type is correctly caught
        else if (type == Boolean.class ||
                 type == boolean.class) {
            return "NUMBER(1)";
        }
        // make sure that the Integer types are correctly caught
        else if (type == Byte.class ||
                 type == byte.class) {
            return "NUMBER(3)";
        } else if (type == Short.class ||
                   type == short.class) {
            return "NUMBER(5)";
        } else if (type == Integer.class ||
                   type == int.class) {
            return "NUMBER(10)";
        } else if (type == Long.class ||
                   type == long.class) {
            return "NUMBER(19)";
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
            var result = handleCommonSqlType(type, precision, scale);
            if (result != null) {
                return result;
            }

            // just return a TEXT type in which the object's toString value will be stored
            return "VARCHAR2(4000)";
        }
    }
}

