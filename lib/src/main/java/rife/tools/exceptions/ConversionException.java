/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools.exceptions;

import java.io.Serial;

public class ConversionException extends Exception {
    @Serial
    private static final long serialVersionUID = 8951249584169075072L;

    private final Object from_;
    private final Class to_;

    public ConversionException(Object from, Class to, Throwable cause) {
        super("Impossible to convert " + from + " from " + (null == from ? "unknown" : from.getClass().getName()) + " to " + to.getName(), cause);
        from_ = from;
        to_ = to;
    }

    public Object getFrom() {
        return from_;
    }

    public Class getTo() {
        return to_;
    }
}
