/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.Datasource;
import rife.database.exceptions.BeanException;
import rife.database.exceptions.DbQueryException;
import rife.database.types.SqlNull;
import rife.tools.BeanUtils;
import rife.tools.exceptions.BeanUtilsException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class QueryHelper {
    public static Set<String> getBeanPropertyNames(Class beanClass, String[] excludedFields)
    throws DbQueryException {
        try {
            return BeanUtils.getPropertyNames(beanClass, null, excludedFields, null);
        } catch (BeanUtilsException e) {
            throw new BeanException("Error while obtaining bean property names.", beanClass, e);
        }
    }

    public static Map<String, String> getBeanPropertyValues(Object bean, String[] includedFields, String[] excludedFields, Datasource datasource)
    throws DbQueryException {
        Map<String, String> property_values_sql = new LinkedHashMap<String, String>();

        if (bean != null) {
            Map<String, Object> property_values = null;
            Set<Map.Entry<String, Object>> property_values_entries = null;

            try {
                property_values = BeanUtils.getPropertyValues(bean, includedFields, excludedFields, null);
            } catch (BeanUtilsException e) {
                throw new BeanException("Error while obtaining bean property values.", bean.getClass(), e);
            }
            property_values_entries = property_values.entrySet();

            // convert the property values to strings that are usable in sql statements
            for (Map.Entry<String, Object> property_value_entry : property_values_entries) {
                // exclude null values
                if (property_value_entry.getValue() != null) {
                    String value = datasource.getSqlConversion().getSqlValue(property_value_entry.getValue());
                    // exclude some more null values
                    if (!value.equals(SqlNull.NULL.toString())) {
                        property_values_sql.put(property_value_entry.getKey(), value);
                    }
                }
            }
        }

        assert property_values_sql != null;

        return property_values_sql;
    }

    public static Map<String, Class> getBeanPropertyTypes(Class beanClass, String[] includedFields, String[] excludedFields)
    throws DbQueryException {
        try {
            return BeanUtils.getPropertyTypes(beanClass, includedFields, excludedFields, null);
        } catch (BeanUtilsException e) {
            throw new BeanException("Error while obtaining bean property types.", beanClass, e);
        }
    }
}
