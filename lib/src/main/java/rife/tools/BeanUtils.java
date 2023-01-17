/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.sql.Time;
import java.time.*;
import java.util.*;

import rife.config.RifeConfig;
import rife.engine.UploadedFile;
import rife.tools.exceptions.BeanUtilsException;
import rife.tools.exceptions.ConversionException;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.Format;
import java.text.ParseException;

import static rife.tools.BeanUtils.Accessors.*;

public final class BeanUtils {
    public enum Accessors {
        GETTERS,
        SETTERS,
        GETTERS_SETTERS
    }

    private BeanUtils() {
        // no-op
    }

    public static BeanInfo getBeanInfo(Class beanClass)
    throws BeanUtilsException {
        try {
            return Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new BeanUtilsException("Couldn't introspect the bean.", beanClass, e);
        }
    }

    private static String validateProperty(Accessors accessor, PropertyDescriptor property, Collection<String> includedProperties, Collection<String> excludedProperties, String prefix) {
        String name = null;

        // only take properties into account that have both read and write accessors
        if ((GETTERS == accessor && property.getReadMethod() != null) ||
            (SETTERS == accessor && property.getWriteMethod() != null) ||
            (GETTERS_SETTERS == accessor && property.getReadMethod() != null && property.getWriteMethod() != null)) {
            name = property.getName();

            // don't take the class and the metaClass property into account
            if (name.equals("class") ||
                name.equals("metaClass")) {
                return null;
            }

            // apply the prefix if it was provided
            if (prefix != null) {
                name = prefix + name;
            }

            // check if the property isn't explicitly included or if it should be excluded before adding it
            if ((null == includedProperties || includedProperties.contains(name)) &&
                (null == excludedProperties || !excludedProperties.contains(name))) {
                return name;
            }
        }
        return null;
    }

    public static Set<String> getPropertyNames(Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        return getPropertyNames(GETTERS_SETTERS, beanClass, includedProperties, excludedProperties, prefix);
    }

    public static Set<String> getPropertyNames(Accessors accessors, Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        if (null == beanClass) return Collections.emptySet();

        final var property_names = new LinkedHashSet<String>();

        processProperties(accessors, beanClass, includedProperties, excludedProperties, prefix, (name, descriptor) -> {
            property_names.add(name);

            return true;
        });

        return property_names;
    }

    public static void processProperties(Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix, BeanPropertyProcessor processor)
    throws BeanUtilsException {
        processProperties(GETTERS_SETTERS, beanClass, includedProperties, excludedProperties, prefix, processor);
    }

    public static void processProperties(Accessors accessors, Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix, BeanPropertyProcessor processor)
    throws BeanUtilsException {
        if (null == beanClass) return;
        if (null == processor) return;

        // obtain the BeanInfo class
        var bean_info = getBeanInfo(beanClass);

        // process the properties of the bean
        var bean_properties = bean_info.getPropertyDescriptors();
        if (bean_properties.length > 0) {
            String property_name = null;
            Collection<String> included_properties = null;
            Collection<String> excluded_properties = null;
            if (null != includedProperties &&
                includedProperties.length > 0) {
                included_properties = new ArrayList<>(Arrays.asList(includedProperties));
            }
            if (null != excludedProperties &&
                excludedProperties.length > 0) {
                excluded_properties = new ArrayList<>(Arrays.asList(excludedProperties));
            }

            // iterate over the properties of the bean
            for (var bean_property : bean_properties) {
                property_name = validateProperty(accessors, bean_property, included_properties, excluded_properties, prefix);

                // process the property if it was valid
                if (property_name != null) {
                    try {
                        if (!processor.gotProperty(property_name, bean_property)) {
                            break;
                        }
                    } catch (IllegalAccessException e) {
                        throw new BeanUtilsException("No permission to invoke a method of the property '" + property_name + "' of the bean.", beanClass, e);
                    } catch (IllegalArgumentException e) {
                        throw new BeanUtilsException("Invalid arguments while invoking a method of the property '" + property_name + "' on the bean.", beanClass, e);
                    } catch (InvocationTargetException e) {
                        throw new BeanUtilsException("A method of the property '" + property_name + "' of the bean has thrown an exception.", beanClass, e.getTargetException());
                    }
                }
            }
        }
    }

    public static void processPropertyValues(final Object bean, String[] includedProperties, String[] excludedProperties, String prefix, final BeanPropertyValueProcessor processor)
    throws BeanUtilsException {
        processPropertyValues(GETTERS_SETTERS, bean, includedProperties, excludedProperties, prefix, processor);
    }

    public static void processPropertyValues(Accessors accessors, final Object bean, String[] includedProperties, String[] excludedProperties, String prefix, final BeanPropertyValueProcessor processor)
    throws BeanUtilsException {
        if (null == bean) return;
        if (bean instanceof Class)
            throw new IllegalArgumentException("bean should be a bean instance, not a bean class.");

        // check if the bean is constrained
        final var constrained = ConstrainedUtils.makeConstrainedInstance(bean);

        processProperties(accessors, bean.getClass(), includedProperties, excludedProperties, prefix, (name, descriptor) -> {
            // obtain the value of the property
            var property_read_method = descriptor.getReadMethod();
            if (property_read_method != null) {
                ConstrainedProperty constrained_property = null;

                // get the corresponding constrained property, if it exists
                if (constrained != null) {
                    constrained_property = constrained.getConstrainedProperty(descriptor.getName());
                }

                // handle the property value
                processor.gotProperty(name, descriptor, property_read_method.invoke(bean, (Object[]) null), constrained_property);
            }

            return true;
        });
    }

    public static int countProperties(Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        return countProperties(GETTERS_SETTERS, beanClass, includedProperties, excludedProperties, prefix);
    }

    public static int countProperties(Accessors accessors, Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        if (null == beanClass) return 0;

        final var result = new int[]{0};

        processProperties(accessors, beanClass, includedProperties, excludedProperties, prefix, (name, descriptor) -> {
            result[0]++;
            return true;
        });

        return result[0];
    }

    public static Object getPropertyValue(Object bean, String name)
    throws BeanUtilsException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");
        if (bean instanceof Class)
            throw new IllegalArgumentException("bean should be a bean instance, not a bean class.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        // obtain the BeanInfo class
        Class bean_class = bean.getClass();
        var bean_info = getBeanInfo(bean_class);

        // process the properties of the bean
        var bean_properties = bean_info.getPropertyDescriptors();
        if (bean_properties.length > 0) {
            String property_name = null;
            Method property_read_method = null;

            // iterate over the properties of the bean
            for (var bean_property : bean_properties) {
                property_name = bean_property.getName();

                // process the property if it was valid
                if (property_name.equals(name)) {
                    // obtain the value of the property
                    property_read_method = bean_property.getReadMethod();
                    if (null == property_read_method) {
                        throw new BeanUtilsException("The bean '" + bean_class + "' doesn't contain a getter for property '" + name + "'", bean_class);
                    }

                    try {
                        return property_read_method.invoke(bean, (Object[]) null);
                    } catch (IllegalAccessException e) {
                        throw new BeanUtilsException("No permission to invoke the '" + property_read_method.getName() + "' method on the bean.", bean_class, e);
                    } catch (IllegalArgumentException e) {
                        throw new BeanUtilsException("Invalid arguments while invoking the '" + property_read_method.getName() + "' method on the bean.", bean_class, e);
                    } catch (InvocationTargetException e) {
                        throw new BeanUtilsException("The '" + property_read_method.getName() + "' method of the bean has thrown an exception.", bean_class, e.getTargetException());
                    }
                }
            }
        }

        throw new BeanUtilsException("The bean '" + bean_class + "' doesn't contain property '" + name + "'", bean_class);
    }

    public static void setPropertyValue(Object bean, String name, Object value)
    throws BeanUtilsException {
        if (null == bean) throw new IllegalArgumentException("bean can't be null.");
        if (bean instanceof Class)
            throw new IllegalArgumentException("bean should be a bean instance, not a bean class.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        // obtain the BeanInfo class
        Class bean_class = bean.getClass();
        var bean_info = getBeanInfo(bean_class);

        // process the properties of the bean
        var bean_properties = bean_info.getPropertyDescriptors();
        if (bean_properties.length > 0) {
            String property_name = null;
            Method property_write_method = null;

            // iterate over the properties of the bean
            for (var bean_property : bean_properties) {
                property_name = bean_property.getName();

                // process the property if it was valid
                if (property_name.equals(name)) {
                    // obtain the value of the property
                    property_write_method = bean_property.getWriteMethod();
                    try {
                        property_write_method.invoke(bean, value);
                        return;
                    } catch (IllegalAccessException e) {
                        throw new BeanUtilsException("No permission to invoke the '" + property_write_method.getName() + "' method on the bean.", bean_class, e);
                    } catch (IllegalArgumentException e) {
                        throw new BeanUtilsException("Invalid arguments while invoking the '" + property_write_method.getName() + "' method on the bean.", bean_class, e);
                    } catch (InvocationTargetException e) {
                        throw new BeanUtilsException("The '" + property_write_method.getName() + "' method of the bean has thrown an exception.", bean_class, e.getTargetException());
                    }
                }
            }
        }

        throw new BeanUtilsException("The bean '" + bean_class + "' doesn't contain property '" + name + "'", bean_class);
    }

    public static Class getPropertyType(Class beanClass, String name)
    throws BeanUtilsException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (0 == name.length()) throw new IllegalArgumentException("name can't be empty.");

        // obtain the BeanInfo class
        var bean_info = getBeanInfo(beanClass);

        // process the properties of the bean
        var bean_properties = bean_info.getPropertyDescriptors();
        if (bean_properties.length > 0) {
            String property_name = null;
            Method property_read_method = null;

            // iterate over the properties of the bean
            for (var bean_property : bean_properties) {
                property_name = bean_property.getName();

                // process the property if it was valid
                if (property_name.equals(name)) {
                    // obtain the value of the property
                    property_read_method = bean_property.getReadMethod();
                    return property_read_method.getReturnType();
                }
            }
        }

        throw new BeanUtilsException("The bean '" + beanClass + "' doesn't contain property '" + name + "'", beanClass);
    }

    public static Map<String, Object> getPropertyValues(Object bean, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        return getPropertyValues(GETTERS_SETTERS, bean, includedProperties, excludedProperties, prefix);
    }

    public static Map<String, Object> getPropertyValues(Accessors accessors, Object bean, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        final var property_values = new LinkedHashMap<String, Object>();

        processPropertyValues(accessors, bean, includedProperties, excludedProperties, prefix, (name, descriptor, value, constrainedProperty) -> {
            // store the property value
            property_values.put(name, value);
        });

        return property_values;
    }

    public static String formatPropertyValue(Object propertyValue, ConstrainedProperty constrainedProperty) {
        if (propertyValue instanceof String) {
            return (String) propertyValue;
        }

        Format format = null;

        if (constrainedProperty != null &&
            constrainedProperty.isFormatted()) {
            format = constrainedProperty.getFormat();
        }

        if (propertyValue instanceof LocalTime) {
            if (format == null) {
                format = RifeConfig.tools().getConcisePreciseTimeFormat();
            }
            try {
                return format.format(Convert.toDate(propertyValue));
            } catch (ConversionException e) {
                throw new RuntimeException(e);
            }
        } else if (propertyValue instanceof Date ||
                   propertyValue instanceof Instant ||
                   propertyValue instanceof LocalDateTime ||
                   propertyValue instanceof LocalDate) {
            if (format == null) {
                format = RifeConfig.tools().getConcisePreciseDateFormat();
            }
            try {
                return format.format(Convert.toDate(propertyValue));
            } catch (ConversionException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (format != null) {
                return format.format(propertyValue);
            }
        }

        if (constrainedProperty != null &&
            constrainedProperty.isFormatted()) {
            format = constrainedProperty.getFormat();
            return format.format(propertyValue);
        }

        return Convert.toString(propertyValue);
    }

    public static Map<String, Class> getPropertyTypes(Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        return getPropertyTypes(GETTERS_SETTERS, beanClass, includedProperties, excludedProperties, prefix);
    }

    public static Map<String, Class> getPropertyTypes(Accessors accessors, Class beanClass, String[] includedProperties, String[] excludedProperties, String prefix)
    throws BeanUtilsException {
        if (null == beanClass) return Collections.emptyMap();

        final var property_types = new LinkedHashMap<String, Class>();

        processProperties(accessors, beanClass, includedProperties, excludedProperties, prefix, (name, descriptor) -> {
            Class property_class = null;

            // obtain and store the property type
            var property_read_method = descriptor.getReadMethod();
            if (property_read_method != null) {
                property_class = property_read_method.getReturnType();
            } else {
                var property_write_method = descriptor.getWriteMethod();
                property_class = property_write_method.getParameterTypes()[0];
            }

            property_types.put(name, property_class);

            return true;
        });

        return property_types;
    }

    /**
     * Retrieves a map of all the properties of a bean and their descriptors.
     * <p>The property names will be uppercased and an exception will be thrown
     * if two properties are equals case-insensitively.
     *
     * @param beanClass the class of the bean
     * @return the map of the bean properties
     * @throws rife.tools.exceptions.BeanUtilsException when an error
     *                                                  occurred while obtaining the bean properties
     * @see #setUppercasedBeanProperty(String, String[], String, Map, Object, Object)
     * @see #setUppercasedBeanProperty(String, UploadedFile, String, Map, Object)
     * @since 1.0
     */
    public static Map<String, PropertyDescriptor> getUppercasedBeanProperties(Class beanClass)
    throws BeanUtilsException {
        if (null == beanClass) throw new IllegalArgumentException("beanClass can't be null.");

        var bean_properties = new HashMap<String, PropertyDescriptor>();
        BeanInfo bean_info = null;
        PropertyDescriptor[] bean_properties_array = null;

        try {
            bean_info = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new BeanUtilsException("Couldn't introspect the bean with class '" + beanClass.getName() + "'.", beanClass, e);
        }
        bean_properties_array = bean_info.getPropertyDescriptors();
        String bean_property_name = null;
        for (var bean_property : bean_properties_array) {
            bean_property_name = bean_property.getName().toUpperCase();
            if (bean_properties.containsKey(bean_property_name)) {
                throw new BeanUtilsException("Duplicate case insensitive bean property '" + bean_property_name + "' in bean '" + beanClass.getName() + "'.", beanClass);
            }
            bean_properties.put(bean_property_name, bean_property);
        }

        return bean_properties;
    }

    /**
     * Parses the textual representation of the date using a custom format, or by
     * relying on the standard date formats.
     *
     * @param date   the textual representation of the date
     * @param format the custom format that should be used for parsing the string
     *               representation of the date; or {@code null} if the default formats should
     *               be used
     * @return the parsed date
     * @throws ParseException if an error occurred when the date was parsed
     * @since 1.0
     */
    public Object parseDate(String date, Format format)
    throws ParseException {
        if (null == date) {
            return null;
        }

        Object result = null;
        if (null == format) {
            try {
                result = RifeConfig.tools().getConcisePreciseDateFormat().parseObject(date);
            } catch (ParseException e) {
                try {
                    result = RifeConfig.tools().getDefaultInputDateFormat().parseObject(date);
                } catch (ParseException e2) {
                    throw e;
                }
            }
        } else {
            result = format.parseObject(date);
        }

        return result;
    }

    /**
     * Set the value of a bean property from an array of strings.
     *
     * @param propertyName       the name of the property
     * @param propertyValues     the values that will be set, can be {@code null}
     * @param propertyNamePrefix the prefix that the propertyName parameter
     *                           should have, can be {@code null}
     * @param beanProperties     the map of the uppercased bean property names and
     *                           their descriptors
     * @param beanInstance       the bean instance whose property should be updated
     * @param emptyBean          this bean instance will be used to set the value of the
     *                           property in case the propertyValues parameter is empty or null, can be
     *                           {@code null}
     * @throws rife.tools.exceptions.BeanUtilsException when an error
     *                                                  occurred while setting the bean property
     * @see #getUppercasedBeanProperties(Class)
     * @see #setUppercasedBeanProperty(String, UploadedFile, String, Map, Object)
     * @since 1.0
     */
    public static void setUppercasedBeanProperty(String propertyName, String[] propertyValues, String propertyNamePrefix, Map<String, PropertyDescriptor> beanProperties, Object beanInstance, Object emptyBean)
    throws BeanUtilsException {
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (null == beanProperties) throw new IllegalArgumentException("beanProperties can't be null.");
        if (null == beanInstance) throw new IllegalArgumentException("beanInstance can't be null.");

        Class bean_class = beanInstance.getClass();

        String name_upper = null;
        PropertyDescriptor property = null;
        Method write_method = null;
        Class property_type = null;

        if (propertyNamePrefix != null) {
            if (!propertyName.startsWith(propertyNamePrefix)) {
                return;
            }

            propertyName = propertyName.substring(propertyNamePrefix.length());
        }
        name_upper = propertyName.toUpperCase();

        if (beanProperties.containsKey(name_upper)) {
            if (null == emptyBean &&
                (null == propertyValues ||
                 0 == propertyValues.length)) {
                return;
            }

            property = beanProperties.get(name_upper);

            write_method = property.getWriteMethod();
            if (null == write_method) {
                return;
            }

            property_type = property.getPropertyType();
            if (null == property_type) {
                return;
            }

            Validated validated = null;
            if (beanInstance instanceof Validated) {
                validated = (Validated) beanInstance;
            }

            var constrained = ConstrainedUtils.makeConstrainedInstance(beanInstance);
            ConstrainedProperty constrained_property = null;
            if (constrained != null) {
                constrained_property = constrained.getConstrainedProperty(property.getName());
                if (constrained_property != null && !constrained_property.isEditable()) {
                    return;
                }
            }

            try {
                // handle the assignment of empty values to properties
                // in case an empty template bean has been provided
                if (emptyBean != null &&
                    (null == propertyValues ||
                     0 == propertyValues.length ||
                     null == propertyValues[0] ||
                     0 == propertyValues[0].length())) {
                    var read_method = property.getReadMethod();
                    var empty_value = read_method.invoke(emptyBean, (Object[]) null);
                    write_method.invoke(beanInstance, empty_value);
                }
                // assign the value normally
                else {
                    // process an array property
                    if (property_type.isArray()) {
                        var component_type = property_type.getComponentType();
                        if (component_type == String.class) {
                            write_method.invoke(beanInstance, new Object[]{propertyValues});
                        } else if (component_type == int.class) {
                            var parameter_values_typed = new int[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toInt(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toInt(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Integer.class) {
                            var parameter_values_typed = new Integer[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toInt(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toInt(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == char.class) {
                            var parameter_values_typed = new char[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = propertyValues[i].charAt(0);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Character.class) {
                            var parameter_values_typed = new Character[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = propertyValues[i].charAt(0);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == boolean.class) {
                            var parameter_values_typed = new boolean[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = StringUtils.convertToBoolean(propertyValues[i]);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Boolean.class) {
                            var parameter_values_typed = new Boolean[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = StringUtils.convertToBoolean(propertyValues[i]);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == byte.class) {
                            var parameter_values_typed = new byte[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toByte(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toByte(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Byte.class) {
                            var parameter_values_typed = new Byte[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toByte(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toByte(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == double.class) {
                            var parameter_values_typed = new double[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toDouble(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toDouble(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Double.class) {
                            var parameter_values_typed = new Double[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toDouble(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toDouble(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == float.class) {
                            var parameter_values_typed = new float[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toFloat(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toFloat(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Float.class) {
                            var parameter_values_typed = new Float[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toFloat(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toFloat(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == long.class) {
                            var parameter_values_typed = new long[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toLong(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toLong(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Long.class) {
                            var parameter_values_typed = new Long[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toLong(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toLong(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == short.class) {
                            var parameter_values_typed = new short[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toShort(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toShort(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == Short.class) {
                            var parameter_values_typed = new Short[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = Convert.toShort(constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } else {
                                        parameter_values_typed[i] = Convert.toShort(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == BigDecimal.class) {
                            var parameter_values_typed = new BigDecimal[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    if (constrained_property != null && constrained_property.isFormatted()) {
                                        parameter_values_typed[i] = new BigDecimal(Convert.toString(constrained_property.getFormat().parseObject(propertyValues[i])));
                                    } else {
                                        parameter_values_typed[i] = new BigDecimal(propertyValues[i]);
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == StringBuffer.class) {
                            var parameter_values_typed = new StringBuffer[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = new StringBuffer(propertyValues[i]);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (component_type == StringBuilder.class) {
                            var parameter_values_typed = new StringBuilder[propertyValues.length];
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    parameter_values_typed[i] = new StringBuilder(propertyValues[i]);
                                }
                            }
                            write_method.invoke(beanInstance, new Object[]{parameter_values_typed});
                        } else if (Date.class.isAssignableFrom(component_type) ||
                                   Instant.class.isAssignableFrom(component_type) ||
                                   LocalDateTime.class.isAssignableFrom(component_type) ||
                                   LocalDate.class.isAssignableFrom(component_type) ||
                                   Time.class.isAssignableFrom(component_type) ||
                                   LocalTime.class.isAssignableFrom(component_type)) {
                            Format custom_format = null;
                            if (constrained_property != null &&
                                constrained_property.isFormatted()) {
                                custom_format = constrained_property.getFormat();
                            }

                            try {
                                var parameter_values_typed = Array.newInstance(component_type, propertyValues.length);
                                for (var i = 0; i < propertyValues.length; i++) {
                                    if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                        Format used_format = null;

                                        Object parameter_value_typed = null;
                                        if (null == custom_format) {
                                            try {
                                                if (Time.class.isAssignableFrom(component_type) || LocalTime.class.isAssignableFrom(component_type)) {
                                                    used_format = RifeConfig.tools().getConcisePreciseTimeFormat();
                                                } else {
                                                    used_format = RifeConfig.tools().getConcisePreciseDateFormat();
                                                }
                                                parameter_value_typed = used_format.parseObject(propertyValues[i]);
                                            } catch (ParseException e) {
                                                try {
                                                    if (Time.class.isAssignableFrom(component_type) || LocalTime.class.isAssignableFrom(component_type)) {
                                                        used_format = RifeConfig.tools().getDefaultInputTimeFormat();
                                                    } else {
                                                        used_format = RifeConfig.tools().getDefaultInputDateFormat();
                                                    }
                                                    parameter_value_typed = used_format.parseObject(propertyValues[i]);
                                                } catch (ParseException e2) {
                                                    throw e;
                                                }
                                            }
                                        } else {
                                            used_format = custom_format;
                                            parameter_value_typed = used_format.parseObject(propertyValues[i]);
                                        }

                                        if (propertyValues[i].equals(used_format.format(parameter_value_typed))) {
                                            if (Date.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toDate(parameter_value_typed));
                                            } else if (Instant.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toInstant(parameter_value_typed));
                                            } else if (LocalDateTime.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toLocalDateTime(parameter_value_typed));
                                            } else if (LocalDate.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toLocalDate(parameter_value_typed));
                                            } else if (Time.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toSqlTime(parameter_value_typed));
                                            } else if (LocalTime.class.isAssignableFrom(component_type)) {
                                                Array.set(parameter_values_typed, i, Convert.toLocalTime(parameter_value_typed));
                                            }
                                        } else {
                                            if (validated != null) {
                                                validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[i]));
                                            }
                                        }
                                    }
                                }
                                write_method.invoke(beanInstance, parameter_values_typed);
                            } catch (ParseException e) {
                                if (validated != null) {
                                    validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                }
                            }
                        } else if (component_type.isEnum()) {
                            var parameter_values_typed = Array.newInstance(component_type, propertyValues.length);
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    try {
                                        Array.set(parameter_values_typed, i, Enum.valueOf(component_type, propertyValues[i]));
                                    } catch (IllegalArgumentException e) {
                                        // don't throw an exception for this since any invalid copy/paste of an URL
                                        // will give a general exception, just set the value to null and it will
                                        // not be set to the property
                                        if (validated != null) {
                                            validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[i]));
                                        }
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, parameter_values_typed);
                        } else if (constrained_property != null && constrained_property.isFormatted()) {
                            var parameter_values_typed = Array.newInstance(component_type, propertyValues.length);
                            for (var i = 0; i < propertyValues.length; i++) {
                                if (propertyValues[i] != null && propertyValues[i].length() > 0) {
                                    try {
                                        Array.set(parameter_values_typed, i, constrained_property.getFormat().parseObject(propertyValues[i]));
                                    } catch (ParseException e) {
                                        if (validated != null) {
                                            validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                        }
                                    }
                                }
                            }
                            write_method.invoke(beanInstance, parameter_values_typed);
                        }
                    }
                    // process an object or a primitive type
                    else if (propertyValues[0] != null && propertyValues[0].length() > 0) {
                        Object parameter_value_typed = null;
                        if (property_type == String.class) {
                            parameter_value_typed = propertyValues[0];
                        } else if (property_type == int.class ||
                                   property_type == Integer.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toInt(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toInt(propertyValues[0]);
                            }
                        } else if (property_type == char.class ||
                                   property_type == Character.class) {
                            parameter_value_typed = propertyValues[0].charAt(0);
                        } else if (property_type == boolean.class ||
                                   property_type == Boolean.class) {
                            parameter_value_typed = Convert.toBoolean(StringUtils.convertToBoolean(propertyValues[0]));
                        } else if (property_type == byte.class ||
                                   property_type == Byte.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toByte(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toByte(propertyValues[0]);
                            }
                        } else if (property_type == double.class ||
                                   property_type == Double.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toDouble(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toDouble(propertyValues[0]);
                            }
                        } else if (property_type == float.class ||
                                   property_type == Float.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toFloat(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toFloat(propertyValues[0]);
                            }
                        } else if (property_type == long.class ||
                                   property_type == Long.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toLong(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toLong(propertyValues[0]);
                            }
                        } else if (property_type == short.class ||
                                   property_type == Short.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                parameter_value_typed = Convert.toShort(constrained_property.getFormat().parseObject(propertyValues[0]));
                            } else {
                                parameter_value_typed = Convert.toShort(propertyValues[0]);
                            }
                        } else if (property_type == BigDecimal.class) {
                            if (constrained_property != null && constrained_property.isFormatted()) {
                                var n = (Number) constrained_property.getFormat().parseObject(propertyValues[0]);
                                parameter_value_typed = new BigDecimal(Convert.toString(n));
                            } else {
                                parameter_value_typed = new BigDecimal(propertyValues[0]);
                            }
                        } else if (property_type == StringBuffer.class) {
                            parameter_value_typed = new StringBuffer(propertyValues[0]);
                        } else if (property_type == StringBuilder.class) {
                            parameter_value_typed = new StringBuilder(propertyValues[0]);
                        } else if (Date.class.isAssignableFrom(property_type) ||
                                   Instant.class.isAssignableFrom(property_type) ||
                                   LocalDateTime.class.isAssignableFrom(property_type) ||
                                   LocalDate.class.isAssignableFrom(property_type) ||
                                   Time.class.isAssignableFrom(property_type) ||
                                   LocalTime.class.isAssignableFrom(property_type)) {
                            Format custom_format = null;
                            if (constrained_property != null &&
                                constrained_property.isFormatted()) {
                                custom_format = constrained_property.getFormat();
                            }

                            try {
                                Format used_format = null;

                                if (null == custom_format) {
                                    try {
                                        if (Time.class.isAssignableFrom(property_type) || LocalTime.class.isAssignableFrom(property_type)) {
                                            used_format = RifeConfig.tools().getConcisePreciseTimeFormat();
                                        } else {
                                            used_format = RifeConfig.tools().getConcisePreciseDateFormat();
                                        }
                                        parameter_value_typed = used_format.parseObject(propertyValues[0]);
                                    } catch (ParseException e) {
                                        try {
                                            if (Time.class.isAssignableFrom(property_type) || LocalTime.class.isAssignableFrom(property_type)) {
                                                used_format = RifeConfig.tools().getDefaultInputTimeFormat();
                                            } else {
                                                used_format = RifeConfig.tools().getDefaultInputDateFormat();
                                            }
                                            parameter_value_typed = used_format.parseObject(propertyValues[0]);
                                        } catch (ParseException e2) {
                                            throw e;
                                        }
                                    }
                                } else {
                                    used_format = custom_format;
                                    parameter_value_typed = used_format.parseObject(propertyValues[0]);
                                }

                                if (propertyValues[0].equals(used_format.format(parameter_value_typed))) {
                                    if (Date.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toDate(parameter_value_typed);
                                    } else if (Instant.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toInstant(parameter_value_typed);
                                    } else if (LocalDateTime.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toLocalDateTime(parameter_value_typed);
                                    } else if (LocalDate.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toLocalDate(parameter_value_typed);
                                    } else if (Time.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toSqlTime(parameter_value_typed);
                                    } else if (LocalTime.class.isAssignableFrom(property_type)) {
                                        parameter_value_typed = Convert.toLocalTime(parameter_value_typed);
                                    }
                                } else {
                                    parameter_value_typed = null;

                                    if (validated != null) {
                                        validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                    }
                                }
                            } catch (ParseException e) {
                                if (validated != null) {
                                    validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                }
                            }
                        } else if (property_type.isEnum()) {
                            try {
                                parameter_value_typed = Enum.valueOf(property_type, propertyValues[0]);
                            } catch (IllegalArgumentException e) {
                                parameter_value_typed = null;

                                if (validated != null) {
                                    validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                }
                            }
                        } else if (constrained_property != null && constrained_property.isFormatted()) {
                            try {
                                parameter_value_typed = constrained_property.getFormat().parseObject(propertyValues[0]);
                            } catch (ParseException e) {
                                // don't throw an exception for this since any invalid copy/paste of an URL
                                // will give a general exception, just set the value to null and it will
                                // not be set to the property
                                parameter_value_typed = null;

                                if (validated != null) {
                                    validated.addValidationError(new ValidationError.INVALID(propertyName).erroneousValue(propertyValues[0]));
                                }
                            }
                        }

                        if (parameter_value_typed != null) {
                            write_method.invoke(beanInstance, parameter_value_typed);
                        }
                    }
                }
            } catch (ParseException e) {
                if (validated != null) {
                    validated.addValidationError(new ValidationError.NOT_NUMERIC(propertyName).erroneousValue(propertyValues[0]));
                } else {
                    throw new BeanUtilsException("The '" + propertyName + "' property of the bean with class '" + bean_class.getName() + "' couldn't be populated due to a parsing error.", bean_class, e);
                }
            } catch (ConversionException e) {
                if (validated != null) {
                    validated.addValidationError(new ValidationError.NOT_NUMERIC(propertyName).erroneousValue(propertyValues[0]));
                } else {
                    throw new BeanUtilsException("The '" + propertyName + "' property of the bean with class '" + bean_class.getName() + "' couldn't be populated due to conversion error.", bean_class, e);
                }
            } catch (IllegalAccessException e) {
                throw new BeanUtilsException("No permission to invoke the '" + write_method.getName() + "' method on the bean with class '" + bean_class.getName() + "'.", bean_class, e);
            } catch (IllegalArgumentException e) {
                throw new BeanUtilsException("Invalid arguments while invoking the '" + write_method.getName() + "' method on the bean with class '" + bean_class.getName() + "'.", bean_class, e);
            } catch (InvocationTargetException e) {
                throw new BeanUtilsException("The '" + write_method.getName() + "' method of the bean with class '" + bean_class.getName() + "' has thrown an exception.", bean_class, e);
            }
        }
    }

    /**
     * Set the value of a bean property from an uploaded file.
     *
     * @param propertyName       the name of the property
     * @param propertyFile       the file that will be set, can be {@code null}
     * @param propertyNamePrefix the prefix that the propertyName parameter
     *                           should have, can be {@code null}
     * @param beanProperties     the map of the uppercased bean property names and
     *                           their descriptors
     * @param beanInstance       the bean instance whose property should be updated
     * @throws rife.tools.exceptions.BeanUtilsException when an error
     *                                                  occurred while setting the bean property
     * @see #getUppercasedBeanProperties(Class)
     * @see #setUppercasedBeanProperty(String, String[], String, Map, Object, Object)
     * @since 1.0
     */
    public static void setUppercasedBeanProperty(String propertyName, UploadedFile propertyFile, String propertyNamePrefix, Map<String, PropertyDescriptor> beanProperties, Object beanInstance)
    throws BeanUtilsException {
        if (null == propertyName) throw new IllegalArgumentException("propertyName can't be null.");
        if (null == beanProperties) throw new IllegalArgumentException("beanProperties can't be null.");
        if (null == beanInstance) throw new IllegalArgumentException("beanInstance can't be null.");

        Class bean_class = beanInstance.getClass();

        String name_upper = null;
        PropertyDescriptor property = null;
        Method write_method = null;
        Class property_type = null;

        if (propertyNamePrefix != null) {
            if (!propertyName.startsWith(propertyNamePrefix)) {
                return;
            }

            propertyName = propertyName.substring(propertyNamePrefix.length());
        }
        name_upper = propertyName.toUpperCase();

        if (beanProperties.containsKey(name_upper)) {
            if (null == propertyFile) {
                return;
            }

            property = beanProperties.get(name_upper);

            write_method = property.getWriteMethod();
            if (null == write_method) {
                return;
            }

            property_type = property.getPropertyType();
            if (null == property_type) {
                return;
            }

            Validated validated = null;
            if (beanInstance instanceof Validated) {
                validated = (Validated) beanInstance;
            }

            var constrained = ConstrainedUtils.makeConstrainedInstance(beanInstance);

            try {
                if (propertyFile.wasSizeExceeded()) {
                    if (validated != null) {
                        validated.addValidationError(new ValidationError.WRONG_LENGTH(propertyName));
                    }
                } else {
                    Object parameter_value_typed = null;
                    if (property_type == InputStream.class) {
                        parameter_value_typed = new FileInputStream(propertyFile.getFile());
                    } else if (property_type == String.class) {
                        parameter_value_typed = FileUtils.readString(propertyFile.getFile());
                    } else if (property_type == byte[].class) {
                        parameter_value_typed = FileUtils.readBytes(propertyFile.getFile());
                    }

                    if (parameter_value_typed != null) {
                        if (constrained != null) {
                            // fill in the property name automatically if none has been provided
                            // in the constraints
                            var constrained_property = constrained.getConstrainedProperty(propertyName);
                            if (constrained_property != null &&
                                propertyFile.getName() != null) {
                                if (null == constrained_property.getName()) {
                                    // if the filename exceeds the maximum length, reduce it intelligently
                                    var file_name = propertyFile.getName();
                                    if (file_name.length() > 100) {
                                        int reduction_index;
                                        var min_reduction_index = file_name.length() - 100;
                                        var slash_index = file_name.lastIndexOf('/');
                                        if (slash_index > min_reduction_index) {
                                            reduction_index = slash_index + 1;
                                        } else {
                                            var backslash_index = file_name.lastIndexOf('\\');
                                            if (backslash_index > min_reduction_index) {
                                                reduction_index = backslash_index + 1;
                                            } else {
                                                reduction_index = min_reduction_index;
                                            }
                                        }
                                        file_name = file_name.substring(reduction_index);
                                    }

                                    constrained_property.setName(file_name);
                                }
                            }
                        }

                        write_method.invoke(beanInstance, parameter_value_typed);
                    }
                }
            } catch (FileUtilsErrorException | FileNotFoundException e) {
                throw new BeanUtilsException("The '" + propertyName + "' property of the bean with class '" + bean_class.getName() + "' couldn't be populated due to an unexpected problem during file reading.", bean_class, e);
            } catch (IllegalAccessException e) {
                throw new BeanUtilsException("No permission to invoke the '" + write_method.getName() + "' method on the bean with class '" + bean_class.getName() + "'.", bean_class, e);
            } catch (IllegalArgumentException e) {
                throw new BeanUtilsException("Invalid arguments while invoking the '" + write_method.getName() + "' method on the bean with class '" + bean_class.getName() + "'.", bean_class, e);
            } catch (InvocationTargetException e) {
                throw new BeanUtilsException("The '" + write_method.getName() + "' method of the bean with class '" + bean_class.getName() + "' has thrown an exception.", bean_class, e);
            }
        }
    }
}
