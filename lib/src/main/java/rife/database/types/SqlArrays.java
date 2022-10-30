/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.types;

import rife.tools.StringUtils;

public abstract class SqlArrays {
    public static String convertArray(Object[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            StringBuilder result = new StringBuilder("{");

            for (Object array_field : array) {
                if (null == array_field) {
                    result.append(SqlNull.NULL.toString());
                } else if (array_field instanceof String) {
                    result.append("'").append(StringUtils.encodeSql((String) array_field)).append("'");
                } else if (array_field instanceof StringBuilder) {
                    result.append("'").append(StringUtils.encodeSql(array_field.toString())).append("'");
                } else if (array_field instanceof StringBuffer) {
                    result.append("'").append(StringUtils.encodeSql(array_field.toString())).append("'");
                } else if (array_field instanceof Object[]) {
                    result.append(convertArray((Object[]) array_field));
                } else {
                    result.append(array_field.toString());
                }
                result.append(",");
            }

            if (result.length() > 1) {
                result.setLength(result.length() - 1);
            }

            result.append("}");

            return result.toString();
        }
    }

    public static String convertArray(boolean[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(byte[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(double[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(float[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(int[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(long[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(short[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",") + "}";
        }
    }

    public static String convertArray(char[] array) {
        if (null == array) {
            return SqlNull.NULL.toString();
        } else {
            return "{" + StringUtils.join(array, ",", "'") + "}";
        }
    }
}
