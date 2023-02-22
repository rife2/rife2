/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.Datasource;
import rife.database.DbQueryManagerCache;
import rife.database.exceptions.DatabaseException;
import rife.database.exceptions.UnsupportedJdbcDriverException;
import rife.database.querymanagers.generic.exceptions.MissingDefaultConstructorException;
import rife.tools.ClassUtils;
import rife.tools.StringUtils;
import rife.validation.Constrained;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class GenericQueryManagerFactory {
    private final static String GENERIC_DRIVER = "generic";

    private static DbQueryManagerCache cache_ = new DbQueryManagerCache();
    private static String packageName_ = GenericQueryManagerFactory.class.getPackage().getName() + ".databasedrivers.";

    public static <BeanType> GenericQueryManager<BeanType> instance(Datasource datasource, Class<BeanType> beanClass)
    throws DatabaseException {
        var short_name = ClassUtils.shortenClassName(beanClass);

        return instance(datasource, beanClass, short_name);
    }

    public static <BeanType> GenericQueryManager<BeanType> instance(Datasource datasource, Class<BeanType> beanClass, String tableName)
    throws DatabaseException {
        AbstractGenericQueryManager<BeanType> query_manager = null;

        var driver = datasource.getAliasedDriver();

        try {
            beanClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new MissingDefaultConstructorException(beanClass, e);
        }

        // get the identifier column
        String primary_key = null;

        var has_identifier = false;
        var constrained_bean = ConstrainedUtils.getConstrainedInstance(beanClass);
        if (constrained_bean != null) {
            for (var property : constrained_bean.getConstrainedProperties()) {
                if (property.isIdentifier()) {
                    primary_key = property.getPropertyName();
                    has_identifier = true;
                    break;
                }
            }
        }

        if (null == primary_key) {
            primary_key = "id";
        }

        // check if the query manager wasn't cached before
        var cache_name = "GENERIC." + beanClass.getName() + "." + primary_key;

        query_manager = (AbstractGenericQueryManager<BeanType>) cache_.get(datasource, cache_name);

        if (query_manager != null) {
            return query_manager;
        }

        // construct the specialized driver class name
        var specialized_name = new StringBuilder(packageName_);
        specialized_name.append(StringUtils.encodeClassname(driver));

        try {
            try {
                var specialized_class = (Class<AbstractGenericQueryManager<BeanType>>) Class.forName(specialized_name.toString());
                var specialized_constructor = specialized_class.getConstructor(new Class[]{Datasource.class, String.class, String.class, Class.class, boolean.class});

                query_manager = specialized_constructor.newInstance(datasource, tableName, primary_key, beanClass, has_identifier);
            } catch (ClassNotFoundException e) {
                // could not find a specialized class, try to get a generic driver
                try {
                    // construct the generic driver class name
                    var generic_name = new StringBuilder(packageName_);
                    generic_name.append(GENERIC_DRIVER);

                    var generic_class = (Class<AbstractGenericQueryManager<BeanType>>) Class.forName(generic_name.toString());
                    var generic_constructor = generic_class.getConstructor(new Class[]{Datasource.class, String.class, String.class, Class.class, boolean.class});

                    query_manager = generic_constructor.newInstance(datasource, tableName, primary_key, beanClass, has_identifier);
                } catch (ClassNotFoundException e2) {
                    throw new UnsupportedJdbcDriverException(driver, e);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException e) {
            throw new UnsupportedJdbcDriverException(driver, e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                throw new RuntimeException(e.getTargetException());
            } else {
                throw new UnsupportedJdbcDriverException(driver, e);
            }
        }

        return query_manager;
    }
}
