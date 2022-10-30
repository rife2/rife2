/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template.exceptions;

import java.io.Serial;

public class InvalidValueFilterException extends TemplateException {
    @Serial private static final long serialVersionUID = 4060674518383871785L;

    private String valueFilter_ = null;

    public InvalidValueFilterException(String valueFilter) {
        super("The value filter " + valueFilter + " is an invalid regular expression.");
        valueFilter_ = valueFilter;
    }

    public String getValueFilter() {
        return valueFilter_;
    }
}
