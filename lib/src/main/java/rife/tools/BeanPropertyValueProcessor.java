/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.validation.ConstrainedProperty;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface BeanPropertyValueProcessor {
    void gotProperty(String name, PropertyDescriptor descriptor, Object value, ConstrainedProperty constrainedProperty)
    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;
}

