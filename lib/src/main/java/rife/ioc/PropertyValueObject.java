/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

/**
 * Holds a single static object property value that doesn't change at runtime.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PropertyValueObject implements PropertyValue {
    private final Object value_;

    /**
     * The constructor that stores the static object instance.
     *
     * @param value the static object instance
     * @since 1.0
     */
    public PropertyValueObject(Object value) {
        value_ = value;
    }

    public Object getValue() {
        return value_;
    }

    public String getValueString() {
        return String.valueOf(value_);
    }

    public String toString() {
        return getValueString();
    }

    public boolean isNegligible() {
        if (null == value_) {
            return true;
        }

        return 0 == String.valueOf(value_).trim().length();
    }

    public boolean isStatic() {
        return true;
    }
}


