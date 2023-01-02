/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import rife.ioc.exceptions.PropertyValueException;

import java.io.Serial;
import java.util.ArrayList;

/**
 * An ordered list of property values.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class PropertyValueList extends ArrayList<PropertyValue> {
    @Serial private static final long serialVersionUID = -7791482346118685259L;

    /**
     * Interprets the list of property values and make one new property value
     * out of it.
     *
     * @return the new <code>PropertyValue</code> instance
     * @since 1.0
     */
    public PropertyValue makePropertyValue()
    throws PropertyValueException {
        // evaluate the current property values series and check if this should be
        // interpreted as a text result or as a participant value
        PropertyValue result = null;

        PropertyValue non_negligible_prop_val = null;
        for (var prop_val : this) {
            if (!prop_val.isNegligible()) {
                if (non_negligible_prop_val != null) {
                    non_negligible_prop_val = null;
                    break;
                }

                non_negligible_prop_val = prop_val;
            }
        }

        if (non_negligible_prop_val != null) {
            if (non_negligible_prop_val instanceof PropertyValueObject ||
                !non_negligible_prop_val.isStatic()) {
                result = non_negligible_prop_val;
            } else {
                result = new PropertyValueObject(non_negligible_prop_val.getValueString().trim());
            }
        }

        if (null == result) {
            var key_text = new StringBuilder();
            for (var prop_val : this) {
                key_text.append(prop_val.getValueString());
            }
            result = new PropertyValueObject(key_text.toString().trim());
        }

        return result;
    }
}
