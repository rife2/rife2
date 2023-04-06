/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.tools.exceptions.SerializationUtilsErrorException;

import java.io.Serializable;
import java.text.*;

/**
 * Standard formatter to convert serializable objects to and from strings.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class SerializationFormatter extends Format {
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj == null) {
            return toAppendTo;
        }
        pos.setBeginIndex(0);
        pos.setEndIndex(0);
        if (obj instanceof Serializable serializable) {
            try {
                toAppendTo.append(SerializationUtils.serializeToString(serializable));
            } catch (SerializationUtilsErrorException e) {
                throw new IllegalArgumentException("Cannot format given Object as a serialized string", e);
            }
        }
        else {
            throw new IllegalArgumentException("Cannot format given Object as a serialized string since it doesn't implement Serializable");
        }
        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        try {
            Object result = SerializationUtils.deserializeFromString(source);
            pos.setIndex(source.length());
            return result;
        } catch (SerializationUtilsErrorException e) {
            return null;
        }
    }
}
