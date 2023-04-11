/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import rife.database.exceptions.BeanException;
import rife.database.exceptions.DatabaseException;
import rife.tools.Convert;
import rife.tools.exceptions.ConversionException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * This class allows a {@link ResultSet} to be easily processed into bean
 * instance.
 * <p>Multiple instances can be collected into a list when processing an
 * entire {@link ResultSet}, or as a single bean instance can be retrieved for
 * one row of a {@link ResultSet}. The default behavior is to not collect
 * instances.
 *
 * @author JR Boyens (jboyens[remove] at uwyn dot com)
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class DbBeanFetcher<BeanType> extends DbRowProcessor {
    private Datasource datasource_ = null;
    private Class<BeanType> beanClass_ = null;
    private BeanType lastBeanInstance_ = null;
    private final HashMap<String, PropertyDescriptor> beanProperties_ = new HashMap<>();
    private ArrayList<BeanType> collectedInstances_ = null;

    /**
     * Create a new DbBeanFetcher
     *
     * @param datasource the datasource to be used
     * @param beanClass  the type of bean that will be handled
     * @throws BeanException thrown if there is an error getting
     *                       information about the bean via the beanClass
     * @since 1.0
     */
    public DbBeanFetcher(Datasource datasource, Class<BeanType> beanClass)
    throws BeanException {
        this(datasource, beanClass, false);
    }

    /**
     * Create a new DbBeanFetcher
     *
     * @param datasource       the datasource to be used
     * @param beanClass        the type of bean that will be handled
     * @param collectInstances {@code true} if the fetcher should
     *                         collect the bean instances; {@code false} if otherwise
     * @throws BeanException thrown if there is an error getting
     *                       information about the bean via the beanClass
     * @since 1.0
     */

    public DbBeanFetcher(Datasource datasource, Class<BeanType> beanClass, boolean collectInstances)
    throws BeanException {
        if (null == datasource) throw new IllegalArgumentException("datasource can't be null.");
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        BeanInfo bean_info = null;

        datasource_ = datasource;
        beanClass_ = beanClass;
        try {
            bean_info = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new BeanException("Couldn't introspect the bean with class '" + beanClass_.getName() + "'.", beanClass, e);
        }
        var bean_properties = bean_info.getPropertyDescriptors();
        for (var bean_property : bean_properties) {
            beanProperties_.put(bean_property.getName().toLowerCase(), bean_property);
        }

        if (collectInstances) {
            collectedInstances_ = new ArrayList<>();
        }

        assert datasource_ != null;
        assert beanClass_ != null;
        assert null == lastBeanInstance_;
    }

    /**
     * Process a ResultSet row into a bean. Call this method on a {@link
     * ResultSet} and the resulting bean will be stored and be accessible
     * via {@link #getBeanInstance()}
     *
     * @param resultSet the {@link ResultSet} from which to process the
     *                  row
     * @return {@code true} if a bean instance was retrieved; or
     * <p>{@code false} if otherwise
     * @throws SQLException thrown when there is a problem processing
     *                      the row
     */
    public boolean processRow(ResultSet resultSet)
    throws SQLException {
        if (null == resultSet) throw new IllegalArgumentException("resultSet can't be null.");

        BeanType instance = null;
        try {
            instance = beanClass_.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException e) {
            var e2 = new SQLException("Can't instantiate a bean with class '" + beanClass_.getName() + "' : " + e.getMessage());
            e2.initCause(e);
            throw e2;
        } catch (IllegalAccessException e) {
            var e2 = new SQLException("No permission to instantiate a bean with class '" + beanClass_.getName() + "' : " + e.getMessage());
            e2.initCause(e);
            throw e2;
        } catch (NoSuchMethodException e) {
            var e2 = new SQLException("No default constructor to instantiate a bean with class '" + beanClass_.getName() + "' : " + e.getMessage());
            e2.initCause(e);
            throw e2;
        }

        var meta = resultSet.getMetaData();

        String column_name;
        String column_label;

        // keep track of the columns that have been set as a bean property,
        // to prevent later results from overwriting earlier ones
        var processed_columns = new HashSet<String>();

        // go over all the columns and try to set them as bean properties
        for (var i = 1; i <= meta.getColumnCount(); i++) {
            column_name = meta.getColumnName(i).toLowerCase();
            column_label = meta.getColumnLabel(i).toLowerCase();
            if (beanProperties_.containsKey(column_name) && !processed_columns.contains(column_name)) {
                processed_columns.add(column_name);
                populateBeanProperty(instance, column_name, meta, resultSet, i);
            } else if (beanProperties_.containsKey(column_label) && !processed_columns.contains(column_label)) {
                processed_columns.add(column_label);
                populateBeanProperty(instance, column_label, meta, resultSet, i);
            }
        }

        lastBeanInstance_ = instance;

        if (collectedInstances_ != null) {
            collectedInstances_.add(instance);
        }

        return gotBeanInstance(instance);

    }

    private void populateBeanProperty(BeanType instance, String propertyName, ResultSetMetaData meta, ResultSet resultSet, int columnIndex)
    throws SQLException {
        var property = beanProperties_.get(propertyName);
        var write_method = property.getWriteMethod();
        if (write_method != null) {
            try {
                var column_type = meta.getColumnType(columnIndex);
                Object typed_object;
                try {
                    typed_object = datasource_.getSqlConversion().getTypedObject(resultSet, columnIndex, column_type, property.getPropertyType());
                } catch (DatabaseException e) {
                    var e2 = new SQLException("Data conversion error while obtaining the typed object.");
                    e2.initCause(e);
                    throw e2;
                }

                // the sql conversion couldn't create a typed value
                if (null == typed_object) {
                    // check if the object returned by the result set is of the same type hierarchy as the property type
                    var column_value = resultSet.getObject(columnIndex);
                    if (column_value != null &&
                        property.getPropertyType().isAssignableFrom(column_value.getClass())) {
                        typed_object = column_value;
                    }
                    // otherwise try to call the property type's constructor with a string argument
                    else {
                        var column_string_value = resultSet.getString(columnIndex);
                        if (column_string_value != null) {
                            Constructor<?> constructor;
                            try {
                                constructor = property.getPropertyType().getConstructor(String.class);
                            } catch (NoSuchMethodException ignored) {
                                // couldn't find a string argument constructor
                                constructor = null;
                            }

                            if (constructor != null) {
                                try {
                                    typed_object = constructor.newInstance((Object[]) new String[]{column_string_value});
                                } catch (SecurityException e) {
                                    throw new SQLException("No permission to obtain the String constructor of the property with name '" + property.getName() + "' and class '" + property.getPropertyType().getName() + "' of the bean with class '" + beanClass_.getName() + "'.", e);
                                } catch (InstantiationException e) {
                                    throw new SQLException("Can't instantiate a new instance of the property with name '" + property.getName() + "' and class '" + property.getPropertyType().getName() + "' of the bean with class '" + beanClass_.getName() + "'.", e);
                                }
                            } else {
                                try {
                                    typed_object = Convert.toType(column_string_value, property.getPropertyType());
                                } catch (ConversionException e) {
                                    throw new SQLException("Unable to convert '" + column_string_value + "' nor could find a String constructor for the property with name '" + property.getName() + "' and class '" + property.getPropertyType().getName() + "' of the bean with class '" + beanClass_.getName() + "'.", e);
                                }
                            }
                        }
                    }
                }

                // if the typed object isn't null, set the value
                if (typed_object != null) {
                    // stored the property type
                    write_method.invoke(instance, typed_object);
                }
            } catch (IllegalAccessException e) {
                instance = null;
                var e2 = new SQLException("No permission to invoke the '" + write_method.getName() + "' method on the bean with class '" + beanClass_.getName() + "'.");
                e2.initCause(e);
                throw e2;
            } catch (IllegalArgumentException e) {
                instance = null;
                var e2 = new SQLException("Invalid arguments while invoking the '" + write_method.getName() + "' method on the bean with class '" + beanClass_.getName() + "'.");
                e2.initCause(e);
                throw e2;
            } catch (InvocationTargetException e) {
                instance = null;
                var e2 = new SQLException("The '" + write_method.getName() + "' method of the bean with class '" + beanClass_.getName() + "' has thrown an exception");
                e2.initCause(e);
                throw e2;
            } catch (SQLException e) {
                instance = null;
                var e2 = new SQLException("SQLException while invoking the '" + write_method.getName() + "' method of the bean with class '" + beanClass_.getName() + "'");
                e2.initCause(e);
                throw e2;
            }
        }
    }

    /**
     * Hook method that can be overloaded to receive new bean instances as
     * they are retrieved, without relying on the internal collection into
     * a list.
     *
     * @param instance the received bean instance
     * @return {@code true} if the bean fetcher should continue to
     * retrieve the next bean; or
     * <p>{@code false} if the retrieval should stop after this bean
     * @since 1.0
     */
    public boolean gotBeanInstance(BeanType instance) {
        return true;
    }

    /**
     * Get the last processed bean instance
     *
     * @return the last processed bean instance
     * @since 1.0
     */
    public BeanType getBeanInstance() {
        return lastBeanInstance_;
    }

    /**
     * Get the collected bean instances
     *
     * @return the collected bean instances
     * @since 1.0
     */
    public List<BeanType> getCollectedInstances() {
        return collectedInstances_;
    }
}
